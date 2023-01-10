package elevatorsimulator;

public class EnergyConsumption {
    private double consumption;
    private final Elevator elevator;

    private final double moveConsumption = 1.0;
    private final double idleConsumption = 0.01;
    private boolean diffFlag = true;

    EnergyConsumption(Elevator elevator) {
        this.elevator = elevator;
    }

    public double getConsumption() { return consumption; }

    public void update() {
        switch (elevator.getState()) {
            case MOVING:
                consumption += moveConsumption;
                diffFlag = true;
                break;
            case STOPPED, TURNING, IDLE:
                consumption += idleConsumption;
                diffFlag = true;
                break;
            case ACCELERATING, DECELERATING:
                if(diffFlag) {
                    consumption += (moveConsumption + idleConsumption) / 2 * elevator.getConfiguration().getStartTime();
                    diffFlag = false;
                }
                break;
            default:
                break;
        }
    }
}
