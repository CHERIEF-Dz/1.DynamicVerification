package manager;

public class ManagerGroup {
    public HMUManager managerHMU;
    public DWManager managerDW;
    public IODManager managerIOD;

    public ManagerGroup() {
        this.managerDW = new DWManager();
        this.managerHMU = new HMUManager();
        this.managerIOD = new IODManager();
    }

    public void checkStructures() {
        this.managerHMU.checkStructures();
        this.managerDW.checkStructures();
        this.managerIOD.checkStructures();
        this.managerDW.generateCSV();
        this.managerHMU.generateCSV();
        this.managerIOD.generateCSV();
    }
}
