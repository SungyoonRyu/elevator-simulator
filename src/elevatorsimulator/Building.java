package elevatorsimulator;
/**
 * Represents a building
 * @author Anton Jansson and Kristoffer Uggla Lingvall
 *
 */
public class Building {
	private final Floor[] floors;
	private final Elevator[] elevators;
	
	/**
	 * The lobby floor
	 */
	public static final int LOBBY = 0;
	
	/**
	 * Creates a new building
	 * @param floors The floors
	 * @param numElevatorCars The number of elevator cars
	 * @param startFloor The start floor for the elevator cars
	 * @param elevatorConfiguration The configuration for the elevator cars
	 */
	public Building(Floor[] floors, int numElevatorCars, int startFloor, ElevatorConfiguration elevatorConfiguration) {
		if (floors.length < 2) {
			throw new IllegalArgumentException("The number of floors in the building must be >= 2.");
		}
		
		this.floors = floors;
		
		this.elevators = new Elevator[numElevatorCars];
		for (int i = 0; i < this.elevators.length; i++) {
			this.elevators[i] = new Elevator(i, startFloor, elevatorConfiguration);
		}
	}
	
	/**
	 * Returns the number of floors in the building
	 */
	public int numFloors() {
		return floors.length;
	}
	
	/**
	 * Returns the floors
	 * @return
	 */
	public Floor[] getFloors() {
		return floors;
	}

	/**
	 * Returns the elevator cars
	 */
	public Elevator[] getElevatorCars() {
		return elevators;
	}
	
	/**
	 * Returns the total number of residents
	 */
	public int getTotalNumberOfResidents() {
		int total = 0;
		
		for (Floor floor : this.floors) {
			total += floor.getNumResidents();
		}
		
		return total;
	}
	
	/**
	 * Updates the building
	 * @param simulator The simulator
	 * @param The elapsed time since the last time step
	 */
	public void update(Simulator simulator, long duration) {
		for (int i = 0; i < this.floors.length; i++) {
			this.floors[i].update(simulator, duration);
		}
		
		for (int i = 0; i < this.elevators.length; i++) {
			this.elevators[i].update(simulator);
		}
	}
	
	/**
	 * Resets the building
	 */
	public void reset() {
		for (Floor floor : this.floors) {
			floor.reset();
		}
		
		for (Elevator elevator : this.elevators) {
			elevator.reset();
		}
	}
}
