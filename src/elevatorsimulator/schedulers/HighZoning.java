package elevatorsimulator.schedulers;

import elevatorsimulator.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HighZoning implements SchedulingAlgorithm {
    private final int numZones;
    private final List<HighZoning.Zone> zones;
    private final HighZoning.Zone[] floorToZone;
    private final HighZoning.Zone[] elevatorToZone;

    private final List<Passenger> renewal = new ArrayList<Passenger>();

    /**
     * Represents a zone
     * @author Anton Jansson and Kristoffer Uggla Lingvall
     *
     */
    private static class Zone {
        public final int minFloorNum;
        public final int maxFloorNum;
        public final List<Floor> floors;
        public final List<Elevator> elevators;

        public Zone(List<Floor> floors, List<Elevator> elevators, int minFloorNum, int maxFloorNum) {
            this.floors = floors;
            this.elevators = elevators;
            this.minFloorNum = minFloorNum;
            this.maxFloorNum = maxFloorNum;
        }

        public int bottomFloor() {
            return this.floors.get(0).getFloorNumber();
        }

        public int middleFloor() {
            return this.floors.get(this.floors.size() / 2).getFloorNumber();
        }

        public int topFloor() {
            return this.floors.get(this.floors.size() - 1).getFloorNumber();
        }
    }

    /**
     * Creates a new instance of the Zoning class
     * @param numZones The number of zones
     * @param building The building
     */
    public HighZoning(int numZones, Building building) {
        this.numZones = numZones;
        this.zones = new ArrayList<HighZoning.Zone>();
        this.floorToZone = new HighZoning.Zone[building.getFloors().length];
        this.elevatorToZone = new HighZoning.Zone[building.getElevatorCars().length];

        int floorsPerZone = (building.getFloors().length + this.numZones - 1) / this.numZones;
        double spillPerFloor = (building.getFloors().length / (double)this.numZones) - floorsPerZone;

        double totalSpill = 0;
        int handledFloors = 0;

        for (int zone = 0; zone < numZones; zone++) {
            List<Elevator> zoneElevators = new ArrayList<Elevator>();
            List<Floor> zoneFloors = new ArrayList<Floor>();

            int elevatorsPerZone = building.getElevatorCars().length / this.numZones;

            totalSpill += spillPerFloor;
            int minFloor = handledFloors;
            int maxFloor = handledFloors + floorsPerZone - 1;

            if (totalSpill >= 1.0 - 0.00001) {
                totalSpill = Math.max(0, totalSpill - 1.0);
                maxFloor++;
            }

            if(zone == numZones - 1) {
                while (maxFloor < building.numFloors() - 1) {
                    maxFloor++;
                }
            }

            for (Elevator elevator : building.getElevatorCars()) {
                if (elevator.getId() >= zone * elevatorsPerZone && elevator.getId() < (zone + 1) * elevatorsPerZone) {
                    zoneElevators.add(elevator);
                }
            }

            for (int floor = minFloor; floor <= maxFloor; floor++) {
                zoneFloors.add(building.getFloors()[floor]);
            }

            handledFloors += maxFloor - minFloor;
            this.zones.add(new HighZoning.Zone(zoneFloors, zoneElevators, minFloor, maxFloor));

            for (int i = minFloor; i <= maxFloor; i++) {
                this.floorToZone[i] = this.zones.get(this.zones.size() - 1);
            }


//            for (Floor floor : zoneFloors) {
//                this.floorToZone[floor.getFloorNumber()] = this.zones.get(this.zones.size() - 1);
//            }
//
            for (Elevator elevator : zoneElevators) {
                this.elevatorToZone[elevator.getId()] = this.zones.get(this.zones.size() - 1);
            }
        }
    }

    /**
     * Returns the zone for the given elevator car
     * @param elevator The elevator car
     */
    private HighZoning.Zone getZone(Elevator elevator) {
        return this.elevatorToZone[elevator.getId()];
    }

    /**
     * Returns the floor for the given floor
     * @param floor The floor
     */
    private HighZoning.Zone getZone(int floor) {
        return this.floorToZone[floor];
    }

    @Override
    public void passengerArrived(Simulator simulator, Passenger passenger) {

    }

    @Override
    public void passengerBoarded(Simulator simulator, Elevator elevator, Passenger passenger) {
        if (passenger.getDestinationFloor() <= this.getZone(elevator).maxFloorNum &&
                passenger.getDestinationFloor() >= this.getZone(elevator).minFloorNum) {

        } else if (passenger.getDestinationFloor() > this.getZone(elevator).maxFloorNum) {
            renewal.add(new Passenger(passenger.getId(), passenger.getArrivalFloor(),
                    passenger.getDestinationFloor(), passenger.getCapacity(), simulator.getClock()));
            passenger.setDestinationFloor(this.getZone(elevator).maxFloorNum);
        } else {
            renewal.add(new Passenger(passenger.getId(), passenger.getArrivalFloor(),
                    passenger.getDestinationFloor(), passenger.getCapacity(), simulator.getClock()));
            passenger.setDestinationFloor(this.getZone(elevator).minFloorNum);
        }
    }

    @Override
    public void passengerExited(Simulator simulator, Elevator elevator, Passenger passenger) {
        Iterator<Passenger> iterator = renewal.iterator();
        while(iterator.hasNext()) {
            Passenger renew = iterator.next();
            if(renew.getId() == passenger.getId()) {
                simulator.getPre_madeList().addFirst(new Passenger(passenger.getId(), elevator.getFloor(),
                        renew.getDestinationFloor(), passenger.getCapacity(),
                        simulator.getClock().timeNow() + 100));
                iterator.remove();
            }
        }
    }

    @Override
    public void update(Simulator simulator) {
        for (Passenger passenger : simulator.getControlSystem().getHallQueue()) {
            if (passenger.getArrivalFloor() == this.getZone(passenger.getArrivalFloor()).minFloorNum &&
                    passenger.getDestinationFloor() < this.getZone(passenger.getArrivalFloor()).minFloorNum) {
                Elevator elevator = simulator.getBuilding().getElevatorCars()[this.getZone(passenger.getArrivalFloor()).elevators.get(0).getId()-1];

                if (elevator.getState() == Elevator.State.IDLE && elevator.canPickupPassenger(passenger)) {
                    if (passenger.getArrivalFloor() == this.getZone(elevator).minFloorNum)
                        if (passenger.getDestinationFloor() < this.getZone(elevator).minFloorNum)
                            continue;
                    if(passenger.getArrivalFloor() == this.getZone(elevator).maxFloorNum)
                        if(passenger.getDestinationFloor() > this.getZone(elevator).maxFloorNum)
                            continue;

                    elevator.moveTowards(simulator, passenger.getArrivalFloor());
                    break;
                }
            }
            for (Elevator elevator : this.getZone(passenger.getArrivalFloor()).elevators) {
                //Check if to dispatch the elevator
                if (elevator.getState() == Elevator.State.IDLE && elevator.canPickupPassenger(passenger)) {
                    if (passenger.getArrivalFloor() == this.getZone(elevator).minFloorNum)
                        if (passenger.getDestinationFloor() < this.getZone(elevator).minFloorNum)
                            continue;
                    if(passenger.getArrivalFloor() == this.getZone(elevator).maxFloorNum)
                        if(passenger.getDestinationFloor() > this.getZone(elevator).maxFloorNum)
                            continue;

                    elevator.moveTowards(simulator, passenger.getArrivalFloor());
                    break;
                }

                //Check if to stop at the next floor
                if (elevator.getState() == Elevator.State.MOVING) {
                    Direction dir = Direction.getDirection(passenger.getArrivalFloor(), passenger.getDestinationFloor());

                    if (elevator.getDirection() == dir) {
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
        HighZoning.Zone zone = this.getZone(elevator);

        int targetFloor = -1;

        for (Floor floor : zone.floors) {
            if (!floor.getWaitingQueue().isEmpty()) {
                if (targetFloor == -1) {
                    targetFloor = floor.getFloorNumber();
                    continue;
                }

                int delta = Math.abs(floor.getFloorNumber() - elevator.getFloor());
                int bestDelta = Math.abs(targetFloor - elevator.getFloor());

                if (elevator.getFloor() < zone.bottomFloor()) {
                    //Below the zone
                    if (delta > bestDelta) {
                        bestDelta = delta;
                        targetFloor = floor.getFloorNumber();
                    }
                } else if (elevator.getFloor() > zone.topFloor()) {
                    //Over the zone
                    if (delta < bestDelta) {
                        bestDelta = delta;
                        targetFloor = floor.getFloorNumber();
                    }
                } else {
                    //Inside the zone
                    if (floor.getFloorNumber() > targetFloor) {
                        targetFloor = floor.getFloorNumber();
                    }
                }
            }
        }

        if (targetFloor == -1) {
            targetFloor = zone.middleFloor();
        }

        elevator.moveTowards(simulator, targetFloor);
    }

    @Override
    public void onTurned(Simulator simulator, Elevator elevator) {

    }

    @Override
    public void changedTo(Simulator simulator) {

    }

    @Override
    public String toString() {
        return "High building Zoning";
    }
}
