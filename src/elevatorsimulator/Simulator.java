package elevatorsimulator;
import java.util.*;

import elevatorsimulator.schedulers.*;

/**
 * The main class for the simulator
 * @author Anton Jansson and Kristoffer Uggla Lingvall
 *
 */
public class Simulator {
	private final String scenarioName;
	private final SimulatorSettings settings;
	private final SimulatorClock clock;
	
	private Random random;
	
	private final SimulatorStats stats;
	private boolean exportStats = true;
	
	private final Building building;
	private final ControlSystem controlSystem;
	
	private long passengerId = 0;

	private final boolean usingPassengerList;
	private Deque<Passenger> pre_madeList = null;
	private final boolean enableLog = true;
	private final boolean debugMode = false;

//	/**
//	 * Creates a new simulator
//	 * @param scenario The scenario
//	 * @param settings The settings
//	 * @param schedulerCreator The scheduler
//	 */
//	public Simulator(Scenario scenario, SimulatorSettings settings, SchedulerCreator schedulerCreator) {
//		this(scenario, settings, schedulerCreator, false, -1);
//	}

	/**
	 * Creates a new simulator
	 * @param scenario The scenario
	 * @param settings The settings
	 * @param schedulerCreator The scheduler
	 * @param usingPassengerList The passengerList
	 */
	public Simulator(Scenario scenario, SimulatorSettings settings, SchedulerCreator schedulerCreator, boolean usingPassengerList) {
		this(scenario, settings, schedulerCreator, usingPassengerList, -1);
	}
	
	/**
	 * Creates a new simulator
	 * @param scenario The scenario
	 * @param settings The settings
	 * @param schedulerCreator The scheduler
	 * @param usingPassengerList The passengerList
	 * @param randSeed The random seed
	 */
	public Simulator(Scenario scenario, SimulatorSettings settings, SchedulerCreator schedulerCreator, boolean usingPassengerList, long randSeed) {
		if (randSeed == -1) {
			randSeed = System.currentTimeMillis();
		}
		
		this.random = new Random(randSeed);
		
		this.scenarioName = scenario.getName();
		this.settings = settings;
		this.clock = new SimulatorClock(settings.getTimeStep());
		this.building = scenario.createBuilding();
		this.controlSystem = new ControlSystem(this, schedulerCreator.createScheduler(this.building));
		this.stats = new SimulatorStats(this);
		this.usingPassengerList = usingPassengerList;

		if(this.usingPassengerList)
			pre_madeList = SimulatorInterface.createPre_madePassengerList(this);
	}
	
	/**
	 * Returns the name of the simulation
	 */
	public String getSimulationName() {
		return this.scenarioName + "-" + this.controlSystem.getSchedulerName();
	}
	
	/**
	 * Returns the simulator clock
	 */
	public SimulatorClock getClock() {
		return clock;
	}
	
	/**
	 * Returns the simulator statistics
	 */
	public SimulatorStats getStats() {
		return stats;
	}
	
	/**
	 * Returns the random generator
	 */
	public Random getRandom() {
		return random;
	}
	
	/**
	 * Returns the building
	 */
	public Building getBuilding() {
		return building;
	}
	
	/**
	 * Returns the control system
	 */
	public ControlSystem getControlSystem() {
		return controlSystem;
	}

	public boolean isUsingPassengerList() { return usingPassengerList; }
	public Deque<Passenger> getPre_madeList() { return pre_madeList; }

	/**
	 * Indicates if the stats are exported
	 */
	public boolean isExportStats() {
		return exportStats;
	}

	/**
	 * Sets if the stats are exported
	 * @param exportStats The export stats state
	 */
	public void setExportStats(boolean exportStats) {
		this.exportStats = exportStats;
	}

	/**
	 * Moves the simulation forward one time step
	 * @param duration The elapsed time since the last time step
	 */
	public void moveForward(long duration) {
		this.building.update(this, duration);
		this.controlSystem.update(duration);
		this.stats.update();
	}
	
	/**
	 * Logs the given line
	 * @param line The line
	 */
	public void log(String line) {
		if (enableLog) {
			double simulatedTime = (double)this.clock.elapsedSinceRealTime(0) / SimulatorClock.NANOSECONDS_PER_SECOND;
			System.out.println(this.clock.formattedTime(simulatedTime) + ": " + line);
		}
	}
	
	/**
	 * Logs the given line for an elevator
	 * @param elevatorId The id of the elevator
	 * @param line The line
	 */
	public void elevatorLog(int elevatorId, String line) {
		this.log("Elevator " + elevatorId + ": " + line);
	}
	
	/**
	 * Logs the given debug line for an elevator
	 * @param elevatorId The id of the elevator
	 * @param line The line
	 */
	public void elevatorDebugLog(int elevatorId, String line) {
		if (this.debugMode) {
			this.elevatorLog(elevatorId, line);
		}
	}
	
	/**
	 * Marks that an arrival has been generated
	 * @param passenger The passenger
	 */
	public void arrivalGenerated(Passenger passenger) {
		this.stats.generatedPassenger(passenger);
	}
	
	/**
	 * Marks that the given passenger has exited the given elevator car
	 * @param elevator The elevator car
	 * @param passenger The passenger
	 */
	public void passengerExited(Elevator elevator, Passenger passenger) {
		this.stats.passengerExited(elevator, passenger);
		this.controlSystem.passengerExited(elevator, passenger);
	}
	
	/**
	 * Indicates if new arrivals can be generated
	 */
	public boolean canGenerateArrivals() {
		return this.clock.simulatedTime() < this.settings.getSimulationTimeInSec() * SimulatorClock.NANOSECONDS_PER_SECOND;
	}
			
	/**
	 * Indicates if all floors are empty
	 */
	private boolean floorsEmpty() {
		for (Floor floor : this.building.getFloors()) {
			if (!floor.getWaitingQueue().isEmpty()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Indicates if all the elevator cars are empty
	 */
	private boolean elevatorsEmpty() {
		for (Elevator elevator : this.building.getElevatorCars()) {
			if (!elevator.getPassengers().isEmpty()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Runs the simulation
	 */
	public void run() {
		System.out.println(new Date() + ": Simulation started.");
		
		while (true) {
			moveForward((long)(this.settings.getTimeStep() * SimulatorClock.NANOSECONDS_PER_SECOND));
			clock.step();
			
			if (!this.canGenerateArrivals()) {
				if (this.floorsEmpty() && this.elevatorsEmpty()) {
					break;
				}
			}
		}	
		
		this.stats.done();
		
		System.out.println(new Date() + ": Simulation finished.");		
		System.out.println("--------------------" + this.controlSystem.getSchedulerName() + "--------------------");
		this.printStats();
	}
	
	private boolean run = false;
		
	/**
	 * Starts the simulator
	 */
	public void start() {
		this.run = true;
	}
	
	/**
	 * Resets the simulator
	 */
	public void reset() {
		this.reset(-1);
	}
	
	/**
	 * Resets the simulator using the given seed
	 * @param seed The seed
	 */
	public void reset(long seed) {
		if (seed == -1) {
			this.random = new Random();
		} else {
			this.random = new Random(seed);
		}
		
		this.controlSystem.reset();
		this.building.reset();
		this.clock.reset();
		this.stats.reset();
		this.run = false;
	}
	
	/**
	 * Advances the simulator one step
	 * @return True if there are any more steps
	 */
	public boolean advance() {
		if (this.run) {
			moveForward((long)(this.settings.getTimeStep() * SimulatorClock.NANOSECONDS_PER_SECOND));
			this.clock.step();
			
			if (!this.canGenerateArrivals()) {				
				if (this.floorsEmpty() && this.elevatorsEmpty()) {
					this.stats.done();
					this.run = false;				
					return false;
				}
			}
		} else {
			return false;
		}

		return true;
	}
	
	/**
	 * Prints the statistics
	 */
	public void printStats() {
		this.stats.printStats();
		
		if (this.isExportStats()) {
			this.stats.exportStats(this.getSimulationName());
		}
	}
	
	public static void main(String[] args) {		
		SimulatorInterface inputInterface = new SimulatorInterface();
				
		Simulator simulator = inputInterface.createSimulator();

		simulator.run();
	}
	
	/**
	 * Returns the next passenger id
	 */
	public long nextPassengerId() {
		return this.passengerId++;
	}
}
