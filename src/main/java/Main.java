import staticanalyzis.Analyzer;
import staticanalyzis.SootAnalyzer;
import manager.HMUManager;
import manager.ManagerGroup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;


public class Main {



    public static void main(String[] args) throws IOException {

        Scanner S = new Scanner(System.in);
        System.out.println("Faites votre choix :");
        System.out.println("1 : Instrumentation");
        System.out.println("2 : Analyse");
        int choice = S.nextInt();
        System.out.println("Choix : " + choice);

        ManagerGroup managerGroup;
        //manager = classicAnalyzer();
        String platformPath = "android-platforms";
        String apkPath = "tests_apks/app-debug_iod.apk";

        if (choice == 1) {
            managerGroup = sootAnalyzer(platformPath, apkPath, true);
        }
        else if (choice == 2) {
            managerGroup = sootAnalyzer(platformPath, apkPath, false);
            String tracePath = "tests_ressources/lambda.txt";
            sootanalyzeTrace(managerGroup, tracePath);
        }
        /*
        String tracePath = "tests_ressources/trace_test_1.txt";
        analyzeTrace(manager, tracePath);
         */
    }

    public static HMUManager classicAnalyzer() throws IOException {
        HMUManager manager = new HMUManager();
        String path = "tests_folders/LambdaApp";
        Analyzer test = new Analyzer();

        test.analyze(manager, path);
        return manager;
    }

    public static ManagerGroup sootAnalyzer(String platformPath, String apkPath, boolean isInstrumenting) {
        ManagerGroup managerGroup = new ManagerGroup();
        SootAnalyzer test = new SootAnalyzer(platformPath, apkPath);
        test.analyze(managerGroup, isInstrumenting);
        return managerGroup;
    }

    public static void sootanalyzeTrace(ManagerGroup managerGroup, String tracePath) {
        BufferedReader reader;
        int traceNumberLine=1;
        String timeStamp1 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        System.out.println("Début analyse : " + timeStamp1);
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
                        String id = result[4];
                        String key = fileName + ":" + lineNumber;
                        //System.out.println("Key : " + key);
                        //HMU
                        HMUManager managerHMU = managerGroup.managerHMU;
                        if ("hmuimpl".equals(result[3])) {
                            System.out.println("Line : " + line);
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
                        }
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
        String timeStamp2 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        System.out.println("Fin analyse : " + timeStamp2);
    }

    public static void analyzeTrace(HMUManager manager, String tracePath) {
        BufferedReader reader;
        int traceNumberLine=1;
        try {
            reader = new BufferedReader(new FileReader(
                    tracePath));
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
