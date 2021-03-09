package utils;

public class ManagerGroup {
    public HMUManager managerHMU;
    public DWManager managerDW;

    public ManagerGroup() {
        this.managerDW = new DWManager();
        this.managerHMU = new HMUManager();
    }

    public void checkStructures() {
        this.managerHMU.checkStructures();
        this.managerDW.checkStructures();
        this.managerDW.generateCSV();
    }
}
