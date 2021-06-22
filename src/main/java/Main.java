import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
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
        Subparser instrumentationParser = subparsers.addParser("instrumentation").help("Instrumentalize an app");
        instrumentationParser.addArgument("apk").required(true).help("Path of the APK to analyze");
        instrumentationParser.addArgument("-a", "--androidJars").required(true).help("Path to android platforms jars");
        instrumentationParser.addArgument("-o", "--output").required(true).help("Path to the folder where the instrumented APK output is generated");
        instrumentationParser.addArgument("-p", "--package").required(true).help("Main package of the app");

        Subparser analyseParser = subparsers.addParser("analyse").help("Analyse the execution trace of an app");
        analyseParser.addArgument("apk").required(true).help("Path of the APK to analyze");
        analyseParser.addArgument("-a", "--androidJars").required(true).help("Path to android platforms jars");
        analyseParser.addArgument("-t", "--trace").required(true).help("Path to the execution trace");
        analyseParser.addArgument("-o", "--output").required(true).help("Path to the folder for the .csv results of the detection");
        analyseParser.addArgument("-p", "--package").required(false).help("Main package of the app");
        analyseParser.addArgument("-ai", "--allInstances").type(Boolean.class).setDefault(false).help("Returning the instances associated to code smells, even if it is not one");

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
                sootanalyzeTrace(managerGroup, tracePath, res.getString("output"), res.getString("apk"), res.getBoolean("allInstances"));
            }
        } catch (ArgumentParserException | XmlPullParserException e) {
            parser.handleError((ArgumentParserException) e);
        }
    }

    public static ManagerGroup sootAnalyzer(String platformPath, String apkPath, String outputPath, String pack, boolean isInstrumenting) throws IOException, XmlPullParserException {
        ManagerGroup managerGroup = new ManagerGroup();
        SootAnalyzer test = new SootAnalyzer(platformPath, apkPath, outputPath);
        test.analyze(managerGroup, isInstrumenting);
        return managerGroup;
    }

    public static void sootanalyzeTrace(ManagerGroup managerGroup, String tracePath, String outputPath, String apkName, boolean returnAllInstances) throws IOException, XmlPullParserException {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    tracePath));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("dynver")) {
                    String[] result = line.substring(line.indexOf("dynver")).split(":");
                    if (result.length >= 5) {
                        String fileName = result[1];
                        String lineNumber = result[2];
                        String code = result[3];
                        String id = result[4];
                        String key = fileName + ":" + lineNumber;
                        managerGroup.execute(key, fileName, lineNumber, code, id);
                    }
                }
                else {
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        managerGroup.checkStructures();
        final ProcessManifest processManifest = new ProcessManifest(apkName);
        String packageName = processManifest.getPackageName();
        managerGroup.generateCSV(outputPath, apkName, packageName, returnAllInstances);
    }

}
