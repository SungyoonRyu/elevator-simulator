package elevatorsimulator.schedulers;

import java.util.ArrayList;
import java.util.List;

import elevatorsimulator.Building;
import elevatorsimulator.Elevator;
import elevatorsimulator.Elevator.State;
import elevatorsimulator.Direction;
import elevatorsimulator.Floor;
import elevatorsimulator.Passenger;
import elevatorsimulator.SchedulingAlgorithm;
import elevatorsimulator.Simulator;

/**
 * Implements the 'Zoning' scheduling algorithm
 * @author Anton Jansson and Kristoffer Uggla Lingvall
 *
 */
public class Zoning implements SchedulingAlgorithm {
	private final int numZones;
	private final List<Zone> zones;
	private final Zone[] floorToZone;
	private final Zone[] elevatorToZone;
	
	/**
	 * Represents a zone
	 * @author Anton Jansson and Kristoffer Uggla Lingvall
	 *
	 */
	private static class Zone {
		public final List<Floor> floors;
		public final List<Elevator> elevators;
		
		public Zone(List<Floor> floors, List<Elevator> elevators) {
			this.floors = floors;
			this.elevators = elevators;
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
	public Zoning(int numZones, Building building) {
		this.numZones = numZones;
		this.zones = new ArrayList<Zoning.Zone>();
		this.floorToZone = new Zone[building.getFloors().length];
		this.elevatorToZone = new Zone[building.getElevatorCars().length];
		
		int floorsPerZone = building.getFloors().length / this.numZones;
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
								
			for (Elevator elevator : building.getElevatorCars()) {
				if (elevator.getId() >= zone * elevatorsPerZone && elevator.getId() < (zone + 1) * elevatorsPerZone) {
					zoneElevators.add(elevator);
				}
			}
								
			for (int floor = minFloor; floor <= maxFloor; floor++) {
				zoneFloors.add(building.getFloors()[floor]);
			}		
					
			handledFloors += maxFloor - minFloor + 1;
			this.zones.add(new Zone(zoneFloors, zoneElevators));
			
			for (Floor floor : zoneFloors) {
				this.floorToZone[floor.getFloorNumber()] = this.zones.get(this.zones.size() - 1);
			}	
			
			for (Elevator elevator : zoneElevators) {
				this.elevatorToZone[elevator.getId()] = this.zones.get(this.zones.size() - 1);
			}
		} 
	}
	
	/**
	 * Returns the zone for the given elevator car
	 * @param elevator The elevator car
	 */
	private Zone getZone(Elevator elevator) {
		return this.elevatorToZone[elevator.getId()];
	}
	
	/**
	 * Returns the floor for the given floor
	 * @param floor The floor
	 */
	private Zone getZone(int floor) {
		return this.floorToZone[floor];
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
			for (Elevator elevator : this.getZone(passenger.getArrivalFloor()).elevators) {
				//Check if to dispatch the elevator
				if (elevator.getState() == State.IDLE && elevator.canPickupPassenger(passenger)) {
					elevator.moveTowards(simulator, passenger.getArrivalFloor());
					break;
				}
				
				//Check if to stop at the next floor
				if (elevator.getState() == State.MOVING) {
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
		Zone zone = this.getZone(elevator);
		
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
		return "Zoning";
	}
}
