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

    }

    public void generateCSV(String outputPath) {
        this.managerDW.generateCSV(outputPath);
        this.managerHMU.generateCSV(outputPath);
        this.managerIOD.generateCSV(outputPath);
    }
}
