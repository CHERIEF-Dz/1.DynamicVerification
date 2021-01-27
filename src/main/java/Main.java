import actions.hmu.HMUAddition;
import actions.hmu.HMUImplementation;
import instrumentation.AndroidInstrument;
import staticanalyzis.Analyzer;
import staticanalyzis.SootAnalyzer;
import utils.CodeLocation;
import utils.HMUManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class Main {

    public static void main(String[] args) throws IOException {

        //-android-jars path/to/android-platforms -process-dir your.apk
        /*
        String[] newArgs = new String[4];
        newArgs[0] = "-android-jars";
        newArgs[1] = "android-platforms";
        newArgs[2] = "-process-dir";
        newArgs[3] = "tests_apks/app-debug.apk";
        */
        //AndroidInstrument.analyze(newArgs);

        //SootAnalyzer.analyze(args);

        HashMap<String, String> testHM = new HashMap<String, String>();
        testHM.put("1", "2");
        testHM.put("2", "3");

        String path = "tests_folders/materialistic-master";
        Analyzer test = new Analyzer();

        HMUManager manager = new HMUManager();



        test.analyze(manager, path);

        //System.out.print(manager.getBreakpoints());

        analyze(manager);




    }

    public static void analyze(HMUManager manager) {
        BufferedReader reader;
        int traceNumberLine=1;
        try {
            reader = new BufferedReader(new FileReader(
                    "tests_ressources/trace_test_materialistic_1.txt"));
            String line = reader.readLine();
            while (line != null) {
                //System.out.println(line);
                String[] result = line.split(":");
                if (result.length==4) {
                    //System.out.println("[TOCHECK]" + traceNumberLine + " " + line);
                    String fileName = result[0];
                    String lineNumber = result[1];
                    String id = result[3];
                    String key = fileName + ":" + lineNumber;
                    if ("impl".equals(result[2])) {
                        manager.executeImplementation(key, id);
                    } else if ("add".equals(result[2])) {
                        manager.executeAddition(key, id);
                    } else if ("del".equals(result[2])) {
                        manager.executeDeletion(key, id);
                    } else if ("cln".equals(result[2])) {
                        manager.executeDeletion(key, id);
                    }
                }
                else {
                    //System.out.println("[AVOID]" + traceNumberLine + " " + line);
                }
                line = reader.readLine();
                traceNumberLine++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        manager.checkStructures();
    }

}
