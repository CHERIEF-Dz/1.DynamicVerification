package manager;

import ca.uqac.lif.cep.tmf.Fork;

import java.io.*;

public class ManagerGroup implements Cloneable{
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

    public void generateCSV(String outputPath, String apkName, String packageName, boolean returnAllInstances) throws IOException {
        //Print for Coverage
        if (returnAllInstances) {
            File coverageOutputfile = new File(outputPath + "coverage.csv");
            try (PrintWriter writer = new PrintWriter(coverageOutputfile)) {
                writer.write("");
            } catch (FileNotFoundException e) {
                // Do something
            }
            File executionOutputfile = new File(outputPath + "execution.csv");
            try (PrintWriter writer = new PrintWriter(executionOutputfile)) {
                writer.write("");
            } catch (FileNotFoundException e) {
                // Do something
            }
        }

        this.managerDW.generateCSV(outputPath, apkName, packageName, returnAllInstances);
        this.managerHMU.generateCSV(outputPath, apkName, packageName, returnAllInstances);
        this.managerIOD.generateCSV(outputPath, apkName, packageName, returnAllInstances);
        this.managerHP.generateCSV(outputPath, apkName, packageName, returnAllInstances);
        this.managerNLMR.generateCSV(outputPath, apkName, packageName, returnAllInstances);
    }

    public void execute(String key, String fileName, String lineNumber, String code, String id, String size) {
        managerDW.execute(key, fileName, lineNumber, code, id);
        managerHMU.execute(key, fileName, lineNumber, code, id, size);
        managerIOD.execute(key, fileName, lineNumber, code, id);
        managerHP.execute(key, fileName, lineNumber, code, id);
        managerNLMR.execute(key, fileName, lineNumber, code, id);
    }

    public void beepBeep(Fork codesmellsFork) {
        managerDW.beepBeepBranch(codesmellsFork, 0);
        managerNLMR.beepBeepBranch(codesmellsFork, 1);
        managerHP.beepBeepBranch(codesmellsFork, 2);
        managerIOD.beepBeepBranch(codesmellsFork, 5);
        managerHMU.beepBeepBranch(codesmellsFork, 6);
    }

    public void mergeManager(ManagerGroup otherManagerGroup) {
        managerDW.mergeManager(otherManagerGroup.managerDW);
        managerNLMR.mergeManager(otherManagerGroup.managerNLMR);
        managerHP.mergeManager(otherManagerGroup.managerHP);
        managerIOD.mergeManager(otherManagerGroup.managerIOD);
        managerHMU.mergeManager(otherManagerGroup.managerHMU);
    }

    public ManagerGroup clone() throws CloneNotSupportedException {
        ManagerGroup newManagerGroup = new ManagerGroup();
        newManagerGroup.managerDW = (DWManager) this.managerDW.clone();
        newManagerGroup.managerNLMR = (NLMRManager) this.managerNLMR.clone();
        newManagerGroup.managerHP = (HPManager) this.managerHP.clone();
        newManagerGroup.managerIOD = (IODManager) this.managerIOD.clone();
        newManagerGroup.managerHMU = (HMUManager) this.managerHMU.clone();
        return newManagerGroup;
    }

}
