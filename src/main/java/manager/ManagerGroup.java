package manager;

public class ManagerGroup {
    public HMUManager managerHMU;
    public DWManager managerDW;
    public IODManager managerIOD;
    public HPManager managerHP;
    public NLMRManager managerNLMR;

    public ManagerGroup() {
        this.managerDW = new DWManager();
        this.managerHMU = new HMUManager();
        this.managerIOD = new IODManager();
        this.managerHP = new HPManager();
        this.managerNLMR = new NLMRManager();
    }

    public void checkStructures() {
        this.managerHMU.checkStructures();
        this.managerDW.checkStructures();
        this.managerIOD.checkStructures();
        this.managerHP.checkStructures();
        this.managerNLMR.checkStructures();
    }

    public void generateCSV(String outputPath) {
        this.managerDW.generateCSV(outputPath);
        this.managerHMU.generateCSV(outputPath);
        this.managerIOD.generateCSV(outputPath);
        this.managerHP.generateCSV(outputPath);
        this.managerNLMR.generateCSV(outputPath);
    }

    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        managerDW.execute(key, fileName, lineNumber, code, id);
        managerHMU.execute(key, fileName, lineNumber, code, id);
        managerIOD.execute(key, fileName, lineNumber, code, id);
        managerHP.execute(key, fileName, lineNumber, code, id);
        managerNLMR.execute(key, fileName, lineNumber, code, id);
    }
}
