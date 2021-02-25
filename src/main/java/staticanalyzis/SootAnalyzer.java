package staticanalyzis;

import actions.hmu.HMUAddition;
import actions.hmu.HMUClean;
import actions.hmu.HMUDeletion;
import actions.hmu.HMUImplementation;
import soot.*;
import soot.jimple.*;
import soot.options.Options;
import utils.CodeLocation;
import utils.HMUManager;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SootAnalyzer {

    private String apkPath, platformPath;
    private int keyCpt;
    static String prefix = "dynver:";

    public SootAnalyzer(String platformPath, String apkPath) {
        this.platformPath = platformPath;
        this.apkPath = apkPath;
        this.keyCpt=0;
    }

    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "tests_folders" + File.separator + "LambdaApp";

    private void checkHMUInst(String line, String path, String name, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        //String regex = "(?<=new)(.*)(?=HashMap)";
        String regex = "(?<=(HashMap))(.*)(?=void <init>\\()";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            //System.out.println("New HashMap !! : " + line);
            System.out.println("Implementation  HM !! " + line);
            String key=generateKey(name);
            String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
            manager.addImplementation(key, new HMUImplementation(new CodeLocation(path, name, lineNumber), "HashMap", variableName));
            if (isInstrumenting) {
                buildInstrumentation(this.getStructureInstanceLocalName(line), units, u, b, "impl:");
            }
        }
        else {
            //regex = "(?<=new)(.*)(?=ArrayMap)";
            regex = "(?<=(ArrayMap))(.*)(?=void <init>\\()";
            pat = Pattern.compile(regex);
            m = pat.matcher(line);
            if (m.find()) {
                //System.out.println("New ArrayMap !! : " + line);
                System.out.println("Implementation AM !!");
                String key=generateKey(name);
                String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
                manager.addImplementation(key, new HMUImplementation(new CodeLocation(path, name, lineNumber), "ArrayMap", variableName));
                if (isInstrumenting) {
                    buildInstrumentation(this.getStructureInstanceLocalName(line), units, u, b, "impl:");
                }
            }
            else {
                //regex = "(?<=new)(.*)(?=SimpleArrayMap)";
                regex = "(?<=(SingleArrayMap))(.*)(?=void <init>\\()";
                pat = Pattern.compile(regex);
                m = pat.matcher(line);
                if (m.find()) {
                    System.out.println("Implementation SAM !!");
                    String key=generateKey(name);
                    String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
                    manager.addImplementation(key, new HMUImplementation(new CodeLocation(path, name, lineNumber), "SimpleArrayMap", variableName));
                    if (isInstrumenting) {
                        buildInstrumentation(this.getStructureInstanceLocalName(line), units, u, b, "impl:");
                    }
                }
            }

        }
    }

    private static Local addTmpRef(Body body, String name, Type type)
    {
        Local tmpRef = Jimple.v().newLocal(name, type);
        body.getLocals().add(tmpRef);
        return tmpRef;
    }

    //Limité par les expressions régulières et analyse statique

    private void buildInstrumentation(String structureLocalName, UnitPatchingChain units, Unit u, Body b, String suffix) {
        //Add Print
        Local refPrint = addTmpRef(b, "refPrint", RefType.v("java.io.PrintStream"));
        Local refBuilder = addTmpRef(b, "refBuilder", RefType.v("java.lang.StringBuilder"));
        Local refIdentity = addTmpRef(b, "refIdentity", IntType.v());
        Local tmpString = addTmpRef(b, "tmpString", RefType.v("java.lang.String"));

        List<Unit> generatedUnits = new ArrayList<>();

        // insert "refPrint = java.lang.System.out;"
        AssignStmt printStmt = Jimple.v().newAssignStmt(
                refPrint, Jimple.v().newStaticFieldRef(
                        Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef()));
        generatedUnits.add(printStmt);

        // insert "tmpLong = 'HELLO';"
        AssignStmt stringStmt = Jimple.v().newAssignStmt(tmpString,
                StringConstant.v(this.prefix+b.getMethod().getDeclaringClass().getName() + ".java:"+this.keyCpt+":"+suffix));
        generatedUnits.add(stringStmt);

        // insert tmpRef = new java.lang.StringBuilder;
        NewExpr newString = Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder"));
        AssignStmt builderStmt = Jimple.v().newAssignStmt(refBuilder, newString);
        generatedUnits.add(builderStmt);

        // special invoke init
        InvokeStmt initBuilder = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(refBuilder,
                Scene.v().getSootClass("java.lang.StringBuilder").getMethod("void <init>()").makeRef()));
        generatedUnits.add(initBuilder);

        //Virtual call Append
        SootMethod appendMethod1 = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.StringBuilder append(java.lang.String)");
        InvokeStmt invokeAppend1 = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(refBuilder, appendMethod1.makeRef(), tmpString));
        generatedUnits.add(invokeAppend1);

        //Get Identity
        Local structureLocal = null;
        Iterator<Local> localIterator = b.getLocals().iterator();
        while (localIterator.hasNext()) {
            Local elt = localIterator.next();
            if (elt.getName().equals(structureLocalName)) {
                structureLocal = elt;
            }
        }

        SootMethod identifyMethod = Scene.v().getSootClass("java.lang.System").getMethod("int identityHashCode(java.lang.Object)");
        AssignStmt structureIdentity = Jimple.v().newAssignStmt(refIdentity,
                Jimple.v().newStaticInvokeExpr(identifyMethod.makeRef(), structureLocal)
        );
        generatedUnits.add(structureIdentity);

        //Append identity
        SootMethod appendMethod2 = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.StringBuilder append(int)");
        InvokeStmt invokeAppend2 = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(refBuilder, appendMethod2.makeRef(), refIdentity));
        generatedUnits.add(invokeAppend2);

        //Builder to String
        SootMethod toStringMethod = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.String toString()");
        AssignStmt builderString = Jimple.v().newAssignStmt(tmpString,
                Jimple.v().newVirtualInvokeExpr(refBuilder, toStringMethod.makeRef())
        );
        generatedUnits.add(builderString);

        // insert "tmpRef.println(tmpString);"
        SootMethod printMethod = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");
        InvokeStmt invokePrint = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(refPrint, printMethod.makeRef(), tmpString));
        generatedUnits.add(invokePrint);

        units.insertAfter(generatedUnits, u);

        //check that we did not mess up the Jimple
        b.validate();
    }

    private String getStructureInstanceLocalName(String line) {
        return line.substring(line.indexOf("specialinvoke")).replace("specialinvoke ", "").trim().split("\\.")[0];
    }

    private String getStructureCallerLocalName(String line) {
        return line.substring(line.indexOf("virtualinvoke")).replace("virtualinvoke ", "").trim().split("\\.")[0];
    }

    public void checkHMUAdd(String line, String path, String name, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        String regex = "(?<=(HashMap|ArrayMap|SingleArrayMap))(.*)(?=put\\()";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            String key=generateKey(name);
            String variableName=m.group(0).split("\\.")[0];
            manager.addAddition(key, new HMUAddition(new CodeLocation(path, name, lineNumber), variableName));
            if (isInstrumenting) {
                buildInstrumentation(this.getStructureCallerLocalName(line), units, u, b, "add:");
            }
        }
    }

    private String generateKey(String name) {
        this.keyCpt++;
        return name + ":" + this.keyCpt;
    }

    public void checkHMUDel(String line, String path, String name, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        String regex = "(?<=(HashMap|ArrayMap|SingleArrayMap))(.*)(?=remove\\()";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {

            String key=generateKey(name);
            String variableName=m.group(0).split("\\.")[0];

            manager.addDeletion(key, new HMUDeletion(new CodeLocation(path, name, lineNumber), variableName));

            if (isInstrumenting) {
                buildInstrumentation(this.getStructureCallerLocalName(line), units, u, b, "del:");
            }
        }
    }

    private void checkHMUClean(String line, String path, String name, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        String regex = "(?<=(HashMap|ArrayMap|SingleArrayMap))(.*)(?=clear\\()";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            String key=generateKey(name);
            String variableName=m.group(0).split("\\.")[0];
            manager.addClean(key, new HMUClean(new CodeLocation(path, name, lineNumber), variableName));

            if (isInstrumenting) {
                buildInstrumentation(this.getStructureCallerLocalName(line), units, u, b, "clean:");
            }
            System.out.println(b.toString());
        }
    }


    public void setupSoot() {
        //Hack to prevent soot to print on System.out
        PrintStream originalStream = System.out;
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_dex);
        Options.v().set_android_jars(this.platformPath);
        Options.v().set_process_dir(Collections.singletonList(this.apkPath));
        Options.v().set_include_all(true);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_output_dir("tests");
        Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);

        List<String> excludeList = new LinkedList<String>();
        /*
        excludeList.add("com.google.android.");
        excludeList.add("java.");
        excludeList.add("sun.misc.");
        excludeList.add("android.");
        excludeList.add("org.apache.");
        excludeList.add("soot.");
        excludeList.add("javax.servlet.");
        excludeList.add("androidx.");
        Options.v().set_exclude(excludeList);
         */
        Scene.v().loadNecessaryClasses();
        System.setOut(originalStream);
    }

    public void analyze(HMUManager manager, boolean isInstrumenting) {
        setupSoot();
        String pack = "com.core.lambdaapp";

        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {
            @Override
            protected void internalTransform(Body body, String phaseName, Map<String, String> options) {

                if (body.getMethod().getDeclaringClass().getName().startsWith(pack)) {
                    //System.out.println("classe : " + body.getMethod().getDeclaringClass());
                    UnitPatchingChain chain = body.getUnits();
                    for (Iterator<Unit> iter = chain.snapshotIterator(); iter.hasNext(); ) {
                        final Unit u = iter.next();
                        String line = u.toString();
                        String name = body.getMethod().getDeclaringClass().getName()+".java";
                        checkHMUInst(line, "test", name, 0, manager, body, u, body.getUnits(), isInstrumenting);
                        checkHMUAdd(line, "test", name, 0, manager, body, u, body.getUnits(), isInstrumenting);
                        checkHMUDel(line, "test", name, 0, manager, body, u, body.getUnits(), isInstrumenting);
                        checkHMUClean(line, "test", name, 0, manager, body, u, body.getUnits(), isInstrumenting);
                    }
                }
            }
        }));


            // Run Soot packs (note that our transformer pack is added to the phase "jtp")
            PackManager.v().runPacks();
        if (isInstrumenting) {
            // Write the result of packs in outputPath
            PackManager.v().writeOutput();
        }
    }
}
