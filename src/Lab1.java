import TSim.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import static TSim.TSimInterface.SWITCH_LEFT;
import static TSim.TSimInterface.SWITCH_RIGHT;

public class Lab1 {
  private HashMap<Point,Semaphore> semaMap = new HashMap<>();
  private HashMap<Point,Boolean> underSemMap = new HashMap<>();
  private HashMap<Point,Point> switchMap = new HashMap<>();
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
    Train t1 = new Train(1, speed1, Direction.ToB, semaphores[5]);
    Train t2 = new Train(2,  speed2, Direction.ToA, semaphores[0]);
    Thread thread1 = new Thread(t1);
    Thread thread2 = new Thread(t2);
    thread1.start();
    thread2.start();
  }

  //This method maps a Point (sensor position) to a Direction, the sensors in this map only try to acquire the semaphore if
  //the train is moving in the specified direction.
  private void loadActivationDirection(){
    activationDirection.put(new Point(1,9), Direction.ToA);
    activationDirection.put(new Point(18,9), Direction.ToB);
    activationDirection.put(new Point(19,8), Direction.ToA);
    activationDirection.put(new Point(14,13), Direction.ToB);
    activationDirection.put(new Point(1,11), Direction.ToB);
  }

  //This method maps a Point (sensor position) to a SwitchDirection depending on which direction the
  //train is traveling and activating the sensor. This is used to tell the switch which direction it should turn
  // depending on the sensor.
  private void loadSwitchDirections() {
    switchDirectionsToA.put(new Point(12,9), SWITCH_RIGHT);
    switchDirectionsToA.put(new Point(6,11), SWITCH_LEFT);
    switchDirectionsToA.put(new Point(1,9), SWITCH_LEFT);
    switchDirectionsToA.put(new Point(19,8), SWITCH_RIGHT);
    switchDirectionsToA.put(new Point(12,10), SWITCH_LEFT);
    switchDirectionsToA.put(new Point(4,13), SWITCH_RIGHT);
    switchDirectionsToB.put(new Point(18,9), SWITCH_RIGHT);
    switchDirectionsToB.put(new Point(14,7), SWITCH_RIGHT);
    switchDirectionsToB.put(new Point(14,8), SWITCH_LEFT);
    switchDirectionsToB.put(new Point(7,10), SWITCH_RIGHT);
    switchDirectionsToB.put(new Point(1,11), SWITCH_LEFT);
    switchDirectionsToB.put(new Point(7,9), SWITCH_LEFT);
    switchDirectionsTaken.put(new Point(1,11), SWITCH_RIGHT);
    switchDirectionsTaken.put(new Point(1,9), SWITCH_RIGHT);
    switchDirectionsTaken.put(new Point(19,8), SWITCH_LEFT);
    switchDirectionsTaken.put(new Point(18,9), SWITCH_LEFT);
  }

  //This method creates all the semaphores then maps a Point (sensor position) to a specific semaphore. This makes it
  //possible to acquire or release semaphores by activating the specified sensor position.
  private void loadSemaphores(){
    for(int i = 0; i < semaphores.length; i++){
      semaphores[i] =new Semaphore(1);
    }

    semaMap.put(new Point(1,11), semaphores[0]);//0 semBottomUpper
    semaMap.put(new Point(1,9), semaphores[1]);//1 semMiddle
    semaMap.put(new Point(18,9), semaphores[1]);//1 semMiddle
    semaMap.put(new Point(6,11), semaphores[2]);//2 semMiddleLeft
    semaMap.put(new Point(7,9), semaphores[2]);//2 semMiddleLeft
    semaMap.put(new Point(7,10), semaphores[2]);//2 semMiddleLeft
    semaMap.put(new Point(4,13), semaphores[2]);//2 semMiddleLeft
    semaMap.put(new Point(12,10), semaphores[3]);//3 semUpperRight
    semaMap.put(new Point(12,9), semaphores[3]);//3 semUpperRight
    semaMap.put(new Point(14,8), semaphores[3]);//3 semUpperRight
    semaMap.put(new Point(14,7), semaphores[3]);//3 semUpperRight
    semaMap.put(new Point(11,8), semaphores[4]);//4 semUpperLeft
    semaMap.put(new Point(9,5), semaphores[4]);//4 semUpperLeft
    semaMap.put(new Point(11,7), semaphores[4]);//4 semUpperLeft
    semaMap.put(new Point(6,6), semaphores[4]);//4 semUpperLeft
    semaMap.put(new Point(19,8), semaphores[5]);//5 semUpperAbove
    underSemMap.put(new Point(1,9), true); //Under middle track
    underSemMap.put(new Point(18,9), true); //Under middle track
    underSemMap.put(new Point(19,8), true); //Under upper track
    underSemMap.put(new Point(1,11), true); //Under upper track
  }

  //This method maps a Point(sensor position) to a Point(switch position), this allows the sensors which switch to change direciton of.
  private void loadSwitches(){
    switchMap.put(new Point(12,9), new Point(15,9));
    switchMap.put(new Point(18,9), new Point(15,9));
    switchMap.put(new Point(12,10), new Point(15,9));
    switchMap.put(new Point(14,7), new Point(17,7));
    switchMap.put(new Point(14,8), new Point(17,7));
    switchMap.put(new Point(19,8), new Point(17,7));
    switchMap.put(new Point(7,10), new Point(4,9));
    switchMap.put(new Point(1,9), new Point(4,9));
    switchMap.put(new Point(7,9), new Point(4,9));
    switchMap.put(new Point(1,11), new Point(3,11));
    switchMap.put(new Point(6,11), new Point(3,11));
    switchMap.put(new Point(4,13), new Point(3,11));
  }

  //This method adds all sensors which are supposed to act as stations.
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
    private Direction currentDirection;

    //The train class takes the parameters id and speed of the train, which direction the train is travelling(to station A or to station B),
    //startingSemaphore needs to be specified to acquire the first semaphore at the station section.
    public Train(int id, int speed, Direction direction, Semaphore startingSemaphore) {
      this.id = id;
      this.speed = speed;
      this.currentDirection = direction;
      this.tsi = TSimInterface.getInstance();
      try{
        startingSemaphore.acquire();
        holding.add(startingSemaphore);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      try{
        tsi.setSpeed(id, Math.min(speed, maxSpeed));
      } catch (CommandException e) {
        throw new RuntimeException(e);
      }
    }

    private void flipDirection(){
      if(currentDirection == Direction.ToA)
        currentDirection = Direction.ToB;
      else
        currentDirection = Direction.ToA;
    }

    //This method changes the direction of a switch depending on the direction of the train. This method also takes a
    //boolean taken which is used to change the switch to cause the train to take a railway underneath the track.
    private void changeTrack(Point point, boolean taken) throws CommandException {
      Integer switchDirection;
      var switchPosition = switchMap.get(point);
      if(switchPosition == null)
        return;

      if(taken){
        switchDirection = switchDirectionsTaken.get(point);
      } else if(currentDirection == Direction.ToA) {
        switchDirection = switchDirectionsToA.get(point);
      } else if(currentDirection == Direction.ToB){
        switchDirection = switchDirectionsToB.get(point);
      } else {
        return;
      }
      tsi.setSwitch(switchPosition.x, switchPosition.y, switchDirection);
    }

    private void reverseSpeed() throws CommandException {
      speed = -speed;
      tsi.setSpeed(id, speed);
    }

    public void stopTrain() throws CommandException {
      tsi.setSpeed(id, 0);
    }

    //This method checks if the sensor we hit is a sensor at one of the stations and if so causing the train to turn around.
    private void reachedStation(Point point) throws CommandException, InterruptedException {
      if(currentDirection == Direction.ToA & stationAPositions.contains(point) || currentDirection == Direction.ToB & stationBPositions.contains(point)){
        turnAround();
      }
    }

    public void turnAround() throws CommandException, InterruptedException {
      tsi.setSpeed(id, 0);
      flipDirection();
      Thread.sleep(Math.abs(1000 + 20*Math.abs(speed)));
      reverseSpeed();
    }

    //This method checks whether the sensor we activated reached a station and if the train should try to acquire a section of the track.
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

    //This method checks if the sensor the train activated has a semaphore connected to it, if so it will check if it should
    //release the semaphore if we are already holding the same semaphore. Otherwise, it should try to acquire the semaphore resulting in
    //acquiring the semaphore proceeding on the track as normal or to go underneath the track if such a section exists.
    private void acquireSection(Point point) throws InterruptedException, CommandException {
      Semaphore tempSem = semaMap.get(point);
      if(tempSem == null){
        return;
      }

      if(holding.contains(tempSem)){
        if(!stationAPositions.contains(point) && !stationBPositions.contains(point)){
          holding.remove(holding.indexOf(tempSem));
          tempSem.release();
        }
      } else {
        if(!activationDirection.containsKey(point) || activationDirection.containsKey(point) && activationDirection.get(point).equals(currentDirection)){
           stopTrain();
          if(tempSem.tryAcquire()){
            changeTrack(point, false);
            holding.add(tempSem);
          } else{
            if(underSemMap.containsKey(point)){
              changeTrack(point, true);
            } else {
              tempSem.acquire();
              changeTrack(point,false);
              holding.add(tempSem);
            }
          }
        }
        tsi.setSpeed(id, Math.min(speed, maxSpeed));
      }
    }
  }
}
