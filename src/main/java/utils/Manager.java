package utils;

public class Manager {
    public HMUManager managerHMU;
    public DWManager managerDW;

    public Manager() {
        this.managerDW = new DWManager();
        this.managerHMU = new HMUManager();
    }

    public void checkStructures() {
        this.managerHMU.checkStructures();
        this.managerDW.checkStructures();
    }
}
