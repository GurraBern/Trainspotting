import TSim.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Semaphore;
import static TSim.TSimInterface.SWITCH_LEFT;
import static TSim.TSimInterface.SWITCH_RIGHT;

public class Lab1 {
  private Map<Point,Semaphore> semaMap = new HashMap<>();
  private Map<Point,Boolean> underSemMap = new HashMap<>();
  private Map<Point,Point> switchMap = new HashMap<>();
  private HashMap<Point, Integer> switchDirectionsToA = new HashMap<>();
  private HashMap<Point, Integer> switchDirectionsToB = new HashMap<>();
  private HashMap<Point, Integer> switchDirectionsTaken = new HashMap<>();
  private HashMap<Point, Direction> activationDirection = new HashMap<>();
  private final HashSet<Point> stationAPositions = new HashSet<>();
  private final HashSet<Point> stationBPositions = new HashSet<>();
  private Semaphore[] semaphores = new Semaphore[6];

  public enum Direction {
    ToA,
    ToB
  }

  public Lab1(int speed1, int speed2) {
    loadSemaphores();
    loadSwitches();
    loadSwitchDirections();
    loadStations();
    loadActivationDirection();
    Train t1 = new Train(1, 15, Direction.ToB);
    Train t2 = new Train(2,  5, Direction.ToA);
    Thread thread1 = new Thread(t1);
    Thread thread2 = new Thread(t2);
    thread1.start();
    thread2.start();
  }

  private void loadActivationDirection(){
    activationDirection.put(new Point(1,9), Direction.ToA);
    activationDirection.put(new Point(18,9), Direction.ToB);
  }

  //Sensor vilken direction
  private void loadSwitchDirections() {
    switchDirectionsToA.put(new Point(13,9), SWITCH_RIGHT);
    switchDirectionsToB.put(new Point(15,7), SWITCH_LEFT);

    switchDirectionsToB.put(new Point(18,9), SWITCH_RIGHT);
    switchDirectionsToA.put(new Point(18,9), SWITCH_LEFT);

    switchDirectionsToB.put(new Point(15,7), SWITCH_RIGHT);
    switchDirectionsToB.put(new Point(15,8), SWITCH_LEFT);

    switchDirectionsToB.put(new Point(6,10), SWITCH_RIGHT);

    //switchDirectionsToA.put(new Point(1,11), SWITCH_RIGHT);       //gör en taken map istället!!! 1,11 triggas när man ska iväg så att tåget står på rälsen exception!!
    switchDirectionsToB.put(new Point(1,11), SWITCH_LEFT);

    //TAKEN
    switchDirectionsTaken.put(new Point(1,11), SWITCH_RIGHT);


    switchDirectionsToA.put(new Point(6,11), SWITCH_LEFT);
    switchDirectionsToB.put(new Point(6,11), SWITCH_RIGHT);

    switchDirectionsToA.put(new Point(1,9), SWITCH_LEFT);
    switchDirectionsToB.put(new Point(1,9), SWITCH_RIGHT);

    switchDirectionsToA.put(new Point(19,8), SWITCH_RIGHT);
    switchDirectionsToB.put(new Point(19,8), SWITCH_LEFT);

    switchDirectionsToA.put(new Point(6,9), SWITCH_RIGHT);
    switchDirectionsToB.put(new Point(6,9), SWITCH_LEFT);

     switchDirectionsToA.put(new Point(13,10), SWITCH_LEFT);
     switchDirectionsToB.put(new Point(13,10), SWITCH_RIGHT);

      switchDirectionsToA.put(new Point(4,13), SWITCH_RIGHT);
      switchDirectionsToB.put(new Point(4,13), SWITCH_LEFT);
  }
  private void loadSemaphores(){
    for(int i = 0; i < semaphores.length; i++){
      semaphores[i] =new Semaphore(1);
    }

    semaMap.put(new Point(14,11), semaphores[0]);//0 semBottomUpper
    semaMap.put(new Point(1,11), semaphores[0]);//0 semBottomUpper
    semaMap.put(new Point(1,9), semaphores[1]);//1 semMiddle
    semaMap.put(new Point(18,9), semaphores[1]);//1 semMiddle
    underSemMap.put(new Point(1,9), true);
    underSemMap.put(new Point(18,9), true);
    semaMap.put(new Point(6,11), semaphores[2]);//2 semMiddleLeft
    semaMap.put(new Point(6,9), semaphores[2]);//2 semMiddleLeft
    semaMap.put(new Point(6,10), semaphores[2]);//2 semMiddleLeft
    semaMap.put(new Point(4,13), semaphores[2]);//2 semMiddleLeft
    semaMap.put(new Point(13,10), semaphores[3]);//3 semUpperRight
    semaMap.put(new Point(13,9), semaphores[3]);//3 semUpperRight
    semaMap.put(new Point(15,8), semaphores[3]);//3 semUpperRight
    semaMap.put(new Point(15,7), semaphores[3]);//3 semUpperRight
    semaMap.put(new Point(10,8), semaphores[4]);//4 semUpperLeft
    semaMap.put(new Point(8,5), semaphores[4]);//4 semUpperLeft
    semaMap.put(new Point(10,7), semaphores[4]);//4 semUpperLeft
    semaMap.put(new Point(6,7), semaphores[4]);//4 semUpperLeft
    semaMap.put(new Point(19,8), semaphores[5]);//5 semUpperAbove
    semaMap.put(new Point(14,3), semaphores[5]);//5 semUpperAbove
    underSemMap.put(new Point(19,8), true);
    underSemMap.put(new Point(1,11), true);
  }

  private void loadSwitches(){
    switchMap.put(new Point(13,9), new Point(15,9));
    switchMap.put(new Point(18,9), new Point(15,9));
    switchMap.put(new Point(15,7), new Point(17,7));
    switchMap.put(new Point(15,8), new Point(17,7));
    switchMap.put(new Point(19,8), new Point(17,7));
    switchMap.put(new Point(6,10), new Point(4,9));
    switchMap.put(new Point(1,9), new Point(4,9));
    switchMap.put(new Point(1,11), new Point(3,11));
    switchMap.put(new Point(6,11), new Point(3,11));
    switchMap.put(new Point(4,13), new Point(3,11));
    switchMap.put(new Point(6,9), new Point(4,9));
    switchMap.put(new Point(13,10), new Point(15,9));
  }

  private void loadStations(){
    stationAPositions.add(new Point(14,5));
    stationAPositions.add(new Point(14,3));
    stationBPositions.add(new Point(14,11));
    stationBPositions.add(new Point(14,13));
  }

  public class Train implements Runnable {
    private int id;
    private int speed;
    private final int maxSpeed = 20;
    private TSimInterface tsi;
    public ArrayList<Semaphore> holding = new ArrayList();
    private Direction currentDir;

    public Train(int id, int speed, Direction direction) {
      this.id = id;
      this.speed = speed;
      this.currentDir = direction;
      this.tsi = TSimInterface.getInstance();
      try{
        tsi.setSpeed(id, Math.min(speed, maxSpeed));
      } catch (CommandException e) {
        throw new RuntimeException(e);
      }
    }

    private void flipDirection(){
      if(currentDir == Direction.ToA)
        currentDir = Direction.ToB;
      else
        currentDir = Direction.ToA;
    }

    private void changeTrack(Point point, boolean taken){
      try{
        Integer switchDirection = null;
        var switchPosition = switchMap.get(point);

        if(currentDir.compareTo(Direction.ToA) == 0){
          switchDirection = switchDirectionsToA.get(point);

          if(taken){
            switchDirection = switchDirectionsToB.get(point);
          }
        } else if(currentDir.compareTo(Direction.ToB) == 0){
          switchDirection = switchDirectionsToB.get(point);

          if(taken){
            switchDirection = switchDirectionsToA.get(point);
          }
        }

        if(switchPosition == null || switchDirection == null){
          return;
        }

        tsi.setSwitch(switchPosition.x,switchPosition.y, switchDirection);
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
        Thread.sleep(Math.abs(1000 + 20*Math.abs(speed)));
        reverseSpeed();
      } catch (CommandException | InterruptedException e) {
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
            acquireSection(sensorPoint);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private void acquireSection(Point point) throws InterruptedException, CommandException {
      Semaphore tempSem = semaMap.get(point);
      if(tempSem == null){
        return;
      }

      if(holding.contains(tempSem)){
        if(!stationAPositions.contains(point) && !stationBPositions.contains(point)){
          holding.remove(holding.indexOf(tempSem));
          tempSem.release();
          System.out.println("train"+id+ ": removed index: "+ removeMethod(tempSem));
        }
      } else {
        //den acquirear middle[1] åt båda hållen
        //vi behöver separarera så att det bara sker från ett håll
        if(!activationDirection.containsKey(point) || activationDirection.containsKey(point) && activationDirection.get(point).equals(currentDir)){
           stopTrain();
          if(tempSem.tryAcquire()){
            changeTrack(point, false);
            holding.add(tempSem);

            System.out.println( "train" + id + ": acquired index: " + removeMethod(tempSem));
          } else{
            if(underSemMap.containsKey(point)){
              changeTrack(point, true);
            } else {
              tempSem.acquire();
              changeTrack(point,false);
              holding.add(tempSem);
              System.out.println( "train"+id + ": acquired index: "+ removeMethod(tempSem));
            }
          }
        }
        tsi.setSpeed(id, Math.min(speed, maxSpeed));
      }
    }

    private int removeMethod(Semaphore sem){
      for(int i = 0; i < semaphores.length; i++){
        if(sem.equals(semaphores[i])){
          return i;
        }
      }
      return -1;
    }
  }
}
