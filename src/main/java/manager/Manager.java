package manager;

import ca.uqac.lif.cep.tmf.Fork;

import java.io.IOException;

public interface Manager {
    void checkStructures();
    void generateCSV(String outputPath, String apkName, String packageName, boolean returnAllInstances) throws IOException;
    void execute(String key, String fileName, String lineNumber, String code, String id);
    void beepBeepBranch(Fork codesmellsFork, int arity);
}
