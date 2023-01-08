package elevatorsimulator;

import elevatorsimulator.schedulers.LongestQueueFirst;
import elevatorsimulator.schedulers.RoundRobin;
import elevatorsimulator.schedulers.ThreePassageGroupElevator;
import elevatorsimulator.schedulers.Zoning;

import java.util.ArrayList;
import java.util.List;

public class SchedulerCreators {
    public static final List<SchedulerCreator> creators = new ArrayList<SchedulerCreator>();

    static {
        creators.add(new SchedulerCreator() {
            @Override

            public SchedulingAlgorithm createScheduler(Building building) {
                return new LongestQueueFirst();
            }
        });

        creators.add(new SchedulerCreator() {
            @Override
            public SchedulingAlgorithm createScheduler(Building building) {
                return new Zoning(building.getElevatorCars().length, building);
            }
        });

        creators.add(new SchedulerCreator() {
            @Override
            public SchedulingAlgorithm createScheduler(Building building) {
                return new RoundRobin(building, false);
            }
        });

        creators.add(new SchedulerCreator() {
            @Override
            public SchedulingAlgorithm createScheduler(Building building) {
                return new RoundRobin(building, true);
            }
        });

        creators.add(new SchedulerCreator() {
            @Override
            public SchedulingAlgorithm createScheduler(Building building) {
                return new ThreePassageGroupElevator(building);
            }
        });
    }
}
