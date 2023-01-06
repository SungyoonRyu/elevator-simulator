package elevatorsimulator.schedulers;

import java.util.ArrayList;
import java.util.List;

import elevatorsimulator.Elevator;
import elevatorsimulator.Passenger;
import elevatorsimulator.SchedulingAlgorithm;
import elevatorsimulator.Simulator;

/**
 * Represents a scheduler that uses Reinforcement learning
 * @author Anton Jansson and Kristoffer Uggla Lingvall
 *
 */
public class ReinforcementLearning implements SchedulingAlgorithm {
	private final List<SchedulingAlgorithm> schedulers = new ArrayList<SchedulingAlgorithm>();
	private int activeScheduler;
	
	/**
	 * Creates a new Reinforcement learning scheduler
	 * @param schedulers The schedulers
	 */
	public ReinforcementLearning(List<SchedulingAlgorithm> schedulers) {
		this.schedulers.addAll(schedulers);
	}
	
	private SchedulingAlgorithm activeScheduler() {
		return this.schedulers.get(this.activeScheduler);
	}
	
	/**
	 * Switches the active scheduler to the given
	 * @param simulator The simulator
	 * @param scheduler The scheduler
	 */
	public void switchTo(Simulator simulator, int scheduler) {
		boolean hasSwitched = false;
		
		if (this.activeScheduler != scheduler) {
			//The strategy has switched
			hasSwitched = true;
		}
		
		this.activeScheduler = scheduler;
		
		if (hasSwitched) {
			this.activeScheduler().changedTo(simulator);
		}
	}
	
	@Override
	public String toString() {
		return "Reinforcement Learning";
	}
	
	@Override
	public void passengerArrived(Simulator simulator, Passenger passenger) {
		this.schedulers.get(this.activeScheduler).passengerArrived(simulator, passenger);
	}
	
	@Override
	public void passengerBoarded(Simulator simulator, Elevator elevator, Passenger passenger) {
		this.schedulers.get(this.activeScheduler).passengerBoarded(simulator, elevator, passenger);
	}
	
	@Override
	public void passengerExited(Simulator simulator, Elevator elevator, Passenger passenger) {
		this.schedulers.get(this.activeScheduler).passengerExited(simulator, elevator, passenger);
	}

	@Override
	public void update(Simulator simulator) {
		this.schedulers.get(this.activeScheduler).update(simulator);
	}

	@Override
	public void onIdle(Simulator simulator, Elevator elevator) {
		this.schedulers.get(this.activeScheduler).onIdle(simulator, elevator);
	}
	
	@Override
	public void onTurned(Simulator simulator, Elevator elevator) {
		this.schedulers.get(this.activeScheduler).onTurned(simulator, elevator);
	}
	
	@Override
	public void changedTo(Simulator simulator) {

	}
}
