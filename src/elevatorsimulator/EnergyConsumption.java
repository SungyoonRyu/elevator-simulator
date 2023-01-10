package elevatorsimulator;

public class EnergeConsumption {
    private int consumption;
    private final Elevator elevator;

    EnergeConsumption(Elevator elevator) {
        this.elevator = elevator;
    }

    public int getConsumption() { return consumption; }

    public void update() {

    }
}
