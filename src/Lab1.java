import TSim.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Semaphore;
import static TSim.TSimInterface.SWITCH_LEFT;
import static TSim.TSimInterface.SWITCH_RIGHT;

public class Lab1 {
  private Map<Point,Semaphore> semaMap = new HashMap<>();
  private Map<Point,Point> switchMap = new HashMap<>();
  private HashMap<Point, Integer> switchDirectionsToA = new HashMap<>();
  private HashMap<Point, Integer> switchDirectionsToB = new HashMap<>();
  private final HashSet<Point> stationAPositions = new HashSet<>();
  private final HashSet<Point> stationBPositions = new HashSet<>();
  private Semaphore semUpperLeft = new Semaphore(1);
  private Semaphore semUpperRight = new Semaphore(1);
  private Semaphore semLower = new Semaphore(1);


  public enum Direction {
    ToA,
    ToB
  }

  public Lab1(int speed1, int speed2) {
    loadSemaphores();
    loadSwitches();
    loadSwitchDirections();
    loadStations();
    Train t1 = new Train(1, speed1, Direction.ToB);
    Train t2 = new Train(2, speed2, Direction.ToA);
    Thread thread1 = new Thread(t1);
    Thread thread2 = new Thread(t2);
    thread1.start();
    thread2.start();
  }

  private void loadSwitchDirections() {
    switchDirectionsToA.put(new Point(15,8), SWITCH_LEFT);
    switchDirectionsToA.put(new Point(15,7), SWITCH_RIGHT);
    switchDirectionsToA.put(new Point(13,9), SWITCH_RIGHT);

    switchDirectionsToB.put(new Point(15,7), SWITCH_RIGHT);
    switchDirectionsToB.put(new Point(13,9), SWITCH_LEFT);
    switchDirectionsToB.put(new Point(15,8), SWITCH_LEFT);

    switchDirectionsToB.put(new Point(4,9), SWITCH_LEFT);
    switchDirectionsToB.put(new Point(5,11), SWITCH_RIGHT);

  }
  private void loadSemaphores(){
    semaMap.put(new Point(6,7), semUpperLeft);
    semaMap.put(new Point(10,7), semUpperLeft);
    semaMap.put(new Point(10,8), semUpperLeft);
    semaMap.put(new Point(8,5), semUpperLeft);

    semaMap.put(new Point(15,7), semUpperRight);
    semaMap.put(new Point(15,8), semUpperRight);
    semaMap.put(new Point(13,9), semUpperRight);
    semaMap.put(new Point(15,12), semUpperRight);

    semaMap.put(new Point(6,9), semLower);
    semaMap.put(new Point(6,10), semLower);
    semaMap.put(new Point(5,11), semLower);
    semaMap.put(new Point(3,13), semLower);


  }
  private void loadSwitches(){
    switchMap.put(new Point(15,7), new Point(17,7));
    switchMap.put(new Point(15,8), new Point(17,7));
    switchMap.put(new Point(13,9), new Point(15,9));

    switchMap.put(new Point(6,10), new Point(4,9));
    switchMap.put(new Point(5,11), new Point(4,9));
    switchMap.put(new Point(3,11), new Point(4,9));


  }

  private void loadStations(){
    stationAPositions.add(new Point(15, 5));
    stationAPositions.add(new Point(15, 3));
    stationBPositions.add(new Point(15, 11));
    stationBPositions.add(new Point(15, 13));
  }

  public class Train implements Runnable {
    private int id;
    private int speed;
    private final int maxSpeed = 20;
    private TSimInterface tsi;
    public Semaphore holding;//todo may have to hold multiple on upper right critical point
    private Direction currentDir;
    public Train(int id, int speed, Direction direction) {
      this.id = id;
      this.speed = speed;
      this.currentDir = direction;
      this.tsi = TSimInterface.getInstance();
      incSpeed();
    }

    private void flipDirection(){
      if(currentDir == Direction.ToA){
        currentDir = Direction.ToB;
      } else {
        currentDir = Direction.ToA;
      }
    }

    private void changeTrack(Point point){
      try{
        Integer switchDirection = null;
        var switchPosition = switchMap.get(point);

        if(currentDir.compareTo(Direction.ToA) == 0){
          switchDirection = switchDirectionsToA.get(point);
        } else if(currentDir.compareTo(Direction.ToB) == 0){
          switchDirection = switchDirectionsToB.get(point);
        }

        if(switchPosition == null || switchDirection == null){
          return;
        }

        tsi.setSwitch(switchPosition.x,switchPosition.y, switchDirection);
      } catch (CommandException e) {
        throw new RuntimeException(e);
      }
    }

    public void incSpeed() {
      try {
        tsi.setSpeed(id, speed);
      } catch (CommandException e) {
        throw new RuntimeException(e);
      }
    }

    private void reverseSpeed() {
      try {
        speed = -speed;
        tsi.setSpeed(id, speed);
      } catch (CommandException e) {
        throw new RuntimeException(e);
      }
    }

    public void stopTrain() {
      try {
        tsi.setSpeed(id, 0);
      } catch (CommandException e) {
        throw new RuntimeException(e);
      }
    }

    private void reachedStation(Point point){
      if(currentDir == Direction.ToA & stationAPositions.contains(point) || currentDir == Direction.ToB & stationBPositions.contains(point)){
        turnAround();
      }
    }

    public void turnAround() {
      try {
        tsi.setSpeed(id, 0);
        flipDirection();
        Thread.sleep(Math.abs(1000 + 20 * speed));
        reverseSpeed();
      } catch (CommandException e) {
        throw new RuntimeException(e);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void run() {
      try {
        while (true) {
          SensorEvent sensorEvent = tsi.getSensor(id);
          Point sensorPoint = new Point(sensorEvent.getXpos(), sensorEvent.getYpos());
          if (sensorEvent.getStatus() == SensorEvent.ACTIVE){
            reachedStation(sensorPoint);
            changeTrack(sensorPoint);
            acquireSection(sensorPoint);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private void acquireSection(Point point) {
      Semaphore tempSem = semaMap.get(point);
      if(tempSem == null){
        return;
      }

      if(tempSem.tryAcquire()){
        changeTrack(point);
        holding = tempSem;
        incSpeed();
        return;
      } else {
        stopTrain();
      }

      if(tempSem == holding){
        holding = null;
        tempSem.release();
        incSpeed();
      } else {
        while (tempSem.availablePermits() == 0){
          stopTrain();
        }
        incSpeed();
      }
    }
  }
}
