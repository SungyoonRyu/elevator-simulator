package elevatorsimulator.schedulers;

import elevatorsimulator.*;

public class CollectiveControl implements SchedulingAlgorithm {
    public CollectiveControl() {

    }

    @Override
    public void passengerArrived(Simulator simulator, Passenger passenger) {

    }

    @Override
    public void passengerBoarded(Simulator simulator, Elevator elevator, Passenger passenger) {

    }

    @Override
    public void passengerExited(Simulator simulator, Elevator elevator, Passenger passenger) {

    }

    @Override
    public void update(Simulator simulator) {
        for (Passenger passenger : simulator.getControlSystem().getHallQueue()) {
            for (Elevator elevator : simulator.getBuilding().getElevatorCars()) {
                if(!elevator.canPickupPassenger(passenger)) {
                    continue;
                }

                if (elevator.getState() == Elevator.State.IDLE)
                    elevator.moveTowards(simulator, passenger.getArrivalFloor());

                if (elevator.getState() == Elevator.State.MOVING) {
                    Direction dir = Direction.getDirection(passenger.getArrivalFloor(), passenger.getDestinationFloor());
                    if(elevator.getDirection() == dir) {
                        if (elevator.nextFloor() == passenger.getArrivalFloor()) {
                            elevator.stopElevatorAtNextFloor();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onIdle(Simulator simulator, Elevator elevator) {

    }

    @Override
    public void onTurned(Simulator simulator, Elevator elevator) {

    }

    public void changedTo(Simulator simulator) {

    }

    @Override
    public String toString() {
        return "Collective Control";
    }
}
