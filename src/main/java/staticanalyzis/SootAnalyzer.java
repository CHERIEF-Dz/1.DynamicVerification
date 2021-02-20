package staticanalyzis;

import actions.hmu.HMUAddition;
import actions.hmu.HMUDeletion;
import actions.hmu.HMUImplementation;
import soot.*;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.StringConstant;
import soot.jimple.internal.JIfStmt;
import soot.options.Options;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import utils.CodeLocation;
import utils.HMUManager;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SootAnalyzer {

    private String apkPath;

    public SootAnalyzer(String apkPath) {
        this.apkPath = apkPath;
    }

    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "tests_folders" + File.separator + "LambdaApp";

    private void checkHMUInst(String line, String path, String name, int lineNumber, HMUManager manager) {
        String regex = "(?<=new)(.*)(?=HashMap)";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            System.out.println("New HashMap !! : " + line);
            String key=name+":"+lineNumber;
            String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
            manager.addImplementation(key, new HMUImplementation(new CodeLocation(path, name, lineNumber), "HashMap", variableName));
        }
        else {
            regex = "(?<=new)(.*)(?=ArrayMap)";
            pat = Pattern.compile(regex);
            m = pat.matcher(line);
            if (m.find()) {
                System.out.println("New ArrayMap !! : " + line);
                String key=name+":"+lineNumber;
                String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
                manager.addImplementation(key, new HMUImplementation(new CodeLocation(path, name, lineNumber), "ArrayMap", variableName));
            }
            else {
                regex = "(?<=new)(.*)(?=SimpleArrayMap)";
                pat = Pattern.compile(regex);
                m = pat.matcher(line);
                if (m.find()) {
                    String key=name+":"+lineNumber;
                    String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
                    manager.addImplementation(key, new HMUImplementation(new CodeLocation(path, name, lineNumber), "SimpleArrayMap", variableName));
                }
            }
        }
    }

    private static Local addTmpRef(Body body)
    {
        Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
        body.getLocals().add(tmpRef);
        return tmpRef;
    }

    private static Local addTmpString(Body body)
    {
        Local tmpString = Jimple.v().newLocal("tmpString", RefType.v("java.lang.String"));
        body.getLocals().add(tmpString);
        return tmpString;
    }

    //Limité par les expressions régulières et analyse statique

    public void checkHMUAdd(String line, String path, String name, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units) {
        String regex = "(?<=(HashMap|ArrayMap|SingleArrayMap))(.*)(?=put\\()";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            System.out.println("Addition !! : " + line);
            String key=name+":"+lineNumber;
            String variableName=m.group(0).split("\\.")[0];
            manager.addAddition(key, new HMUAddition(new CodeLocation(path, name, lineNumber), variableName));
            //Add Print
            Local refPrint = addTmpRef(b);
            Local refStringBuilder = addTmpRef(b);
            Local tmpString = addTmpString(b);




            // insert "tmpRef.println(tmpString);"
            SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");
            units.insertAfter(Jimple.v().newInvokeStmt(
                    Jimple.v().newVirtualInvokeExpr(refPrint, toCall.makeRef(), tmpString)), u);

            // insert "tmpLong = 'HELLO';"
            units.insertAfter(Jimple.v().newAssignStmt(tmpString,
                    StringConstant.v("ADDITION")), u);

            // insert "refPrint = java.lang.System.out;"
            units.insertAfter(Jimple.v().newAssignStmt(
                    refPrint, Jimple.v().newStaticFieldRef(
                            Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), u);
            // insert "refStringBuilder =
            //units.insertAfter(Jimple.v().newAssignStmt(
            //        refStringBuilder, Jimple.v().newStaticFieldRef(
            //                Scene.v().getField("new java.lang.StringBuilder").makeRef())), u);


            //check that we did not mess up the Jimple
            b.validate();
        }
    }

    public void checkHMUDel(String line, String path, String name, int lineNumber, HMUManager manager) {
        String regex = "(?<=(HashMap|ArrayMap|SingleArrayMap))(.*)(?=remove\\()";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            String key=name+":"+lineNumber;
            String variableName=m.group(0).split("\\.")[0];
            manager.addDeletion(key, new HMUDeletion(new CodeLocation(path, name, lineNumber), variableName));
        }
    }

    private void checkHMUClean(String line, String path, String name, int lineNumber, HMUManager manager) {
        String regex = "(?<=(HashMap|ArrayMap|SingleArrayMap))(.*)(?=clear\\()";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            String key=name+":"+lineNumber;
            String variableName=m.group(0).split("\\.")[0];
            manager.addDeletion(key, new HMUDeletion(new CodeLocation(path, name, lineNumber), variableName));
        }
    }


    public void setupSoot() {
        //Hack to prevent soot to print on System.out
        PrintStream originalStream = System.out;

        String apk = this.apkPath;
        G.reset();
        Options.v().set_verbose(false);
        //Path to android-sdk-platforms
        Options.v().set_android_jars("android-platforms");
        //prefer Android APK files
        Options.v().set_src_prec(Options.src_prec_apk);
        // Allow phantom references
        Options.v().set_allow_phantom_refs(true);
        //Set path to APK
        Options.v().set_process_dir(Collections.singletonList(apk));
        Options.v().set_whole_program(true);
        Options.v().set_output_format(Options.output_format_grimple);

        PhaseOptions.v().setPhaseOption("gop", "enabled:true");
        System.setOut(originalStream);

        List<String> excludeList = new LinkedList<String>();
        excludeList.add("java.");
        excludeList.add("sun.misc.");
        excludeList.add("android.");
        excludeList.add("org.apache.");
        excludeList.add("soot.");
        excludeList.add("javax.servlet.");
        Options.v().set_exclude(excludeList);
        Scene.v().loadNecessaryClasses();
    }

    public void analyze(HMUManager manager) {
        setupSoot();
        String pack = "com.core.lambdaapp";
        String buildConfigClass = pack.concat(".BuildConfig");
        String rClass = pack.concat(".R");
        //Get all classes
        Chain classes = Scene.v().getClasses();
        for(Iterator<SootClass> iterClass = classes.iterator(); iterClass.hasNext();) {


            final SootClass sc = iterClass.next();

                String rsubClassStart = rClass + "$";
                String name = sc.getName();
                String packs =  pack.concat(".");
                if(name.equals(rClass) || name.startsWith(rsubClassStart) || name.equals(buildConfigClass) || name.matches(".*[$][123456789]+$")) {
                    //sootClass.setLibraryClass();
                }else if(name.startsWith(packs)){
                    System.out.println("Classe : " + sc.getName());
                    List methods = sc.getMethods();

                    for (Iterator<SootMethod> iterMethod = methods.iterator(); iterMethod.hasNext(); ) {
                        final SootMethod sm = iterMethod.next();
                        System.out.println("Methode : " + sm.getName());
                        Body body = sm.retrieveActiveBody();
                        UnitPatchingChain chain = body.getUnits();
                        for (Iterator<Unit> iter = chain.snapshotIterator(); iter.hasNext(); ) {
                            final Unit u = iter.next();
                            //System.out.println(u.toString());
                            String line = u.toString();
                            checkHMUInst(line, "test", "test", 0, manager);
                            checkHMUAdd(line, "test", "test", 0, manager, body, u, body.getUnits());
                            checkHMUDel(line, "test", "test", 0, manager);
                            checkHMUClean(line, "test", "test", 0, manager);
                        }
                        System.out.println(body.toString());
                    }
                }
        }

    }
}
