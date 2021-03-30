import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import staticanalyzis.SootAnalyzer;
import manager.HMUManager;
import manager.ManagerGroup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Main {



    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newFor("ddcf").build().description("A dynamic analysis tool to detect Android Code smells");
        Subparsers subparsers = parser.addSubparsers().dest("sub_command");;
        Subparser analyseParser = subparsers.addParser("instrumentation").help("Instrumentalize an app");
        analyseParser.addArgument("apk").required(true).help("Path of the APK to analyze");
        analyseParser.addArgument("-a", "--androidJars").required(true).help("Path to android platforms jars");
        analyseParser.addArgument("-o", "--output").required(true).help("Path to the folder where the instrumented APK output is generated");
        analyseParser.addArgument("-p", "--package").required(true).help("Main package of the app");

        Subparser queryParser = subparsers.addParser("analyse").help("Analyse the execution trace of an app");
        queryParser.addArgument("apk").required(true).help("Path of the APK to analyze");
        queryParser.addArgument("-a", "--androidJars").required(true).help("Path to android platforms jars");
        queryParser.addArgument("-t", "--trace").required(true).help("Path to the execution trace");
        queryParser.addArgument("-o", "--output").required(true).help("Path to the folder for the .csv results of the detection");
        queryParser.addArgument("-p", "--package").required(true).help("Main package of the app");

        ManagerGroup managerGroup;
        try {
            Namespace res = parser.parseArgs(args);
            //System.out.println();
            if (res.getString("sub_command").equals("instrumentation")) {
                managerGroup = sootAnalyzer(res.getString("androidJars"), res.getString("apk"), res.getString("output"), res.getString("package"), true);
            }
            else if (res.getString("sub_command").equals("analyse")) {
                managerGroup = sootAnalyzer(res.getString("androidJars"), res.getString("apk"), "", res.getString("package"),false);
                String tracePath = res.getString("trace");
                sootanalyzeTrace(managerGroup, tracePath, res.getString("output"));
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }

    public static ManagerGroup sootAnalyzer(String platformPath, String apkPath, String outputPath, String pack, boolean isInstrumenting) {
        ManagerGroup managerGroup = new ManagerGroup();
        SootAnalyzer test = new SootAnalyzer(platformPath, apkPath, outputPath);
        test.analyze(managerGroup, isInstrumenting, pack);
        return managerGroup;
    }

    public static void sootanalyzeTrace(ManagerGroup managerGroup, String tracePath, String outputPath) {
        BufferedReader reader;
        int traceNumberLine=1;
        String timeStamp1 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        //System.out.println("Début analyse : " + timeStamp1);
        try {
            reader = new BufferedReader(new FileReader(
                    tracePath));
            String line = reader.readLine();
            while (line != null) {
                //System.out.println(line);
                if (line.contains("dynver")) {
                    String[] result = line.substring(line.indexOf("dynver")).split(":");
                    if (result.length == 5) {
                        //System.out.println("[TOCHECK]" + traceNumberLine + " " + line);
                        String fileName = result[1];
                        String lineNumber = result[2];
                        String code = result[3];
                        String id = result[4];
                        String key = fileName + ":" + lineNumber;
                        //System.out.println("Key : " + key);
                        //HMU
                        //HMUManager managerHMU = managerGroup.managerHMU;
                        managerGroup.execute(key, fileName, lineNumber, code, id);
                        //managerHMU.execute(key, fileName, lineNumber, code, id);
                        /*
                        if ("hmuimpl".equals(result[3])) {
                            managerHMU.executeImplementation(key, id);
                        } else if ("hmuadd".equals(result[3])) {
                            //System.out.println("Addition line !");
                            managerHMU.executeAddition(key, id);
                        } else if ("hmudel".equals(result[3])) {
                            managerHMU.executeDeletion(key, id);
                        } else if ("hmucln".equals(result[3])) {
                            managerHMU.executeClean(key, id);
                        } else if ("dwacq".equals(result[3])) {
                            managerGroup.managerDW.executeAcquire(key, id);
                        } else if ("dwrel".equals(result[3])) {
                            managerGroup.managerDW.executeRelease(key, id);
                        } else if ("iodenter".equals(result[3])) {
                            managerGroup.managerIOD.executeEnter(key, fileName, Long.valueOf(id));
                        } else if ("iodexit".equals(result[3])) {
                            managerGroup.managerIOD.executeExit(key, fileName, Long.valueOf(id));
                        } else if ("iodnew".equals(result[3])) {
                            managerGroup.managerIOD.executeNew(key, fileName);
                        }*/
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
        managerGroup.checkStructures();
        managerGroup.generateCSV(outputPath);
        //System.out.println(managerGroup.managerHMU.getBreakpoints());
        String timeStamp2 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        //System.out.println("Fin analyse : " + timeStamp2);
    }

}
