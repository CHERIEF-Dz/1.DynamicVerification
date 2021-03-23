package staticanalyzis;

import manager.ManagerGroup;
import soot.*;
import soot.options.Options;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class SootAnalyzer {

    private String apkPath, platformPath, outputPath;

    public SootAnalyzer(String platformPath, String apkPath, String outputPath) {
        this.platformPath = platformPath;
        this.apkPath = apkPath;
        this.outputPath = outputPath;
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
        Options.v().set_output_dir(this.outputPath);
        Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
        System.setOut(originalStream);
    }

    public void analyze(ManagerGroup managerGroup, boolean isInstrumenting, String pack) {
        setupSoot();

        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {
            @Override
            protected void internalTransform(Body body, String phaseName, Map<String, String> options) {

                if (body.getMethod().getDeclaringClass().getName().startsWith(pack)) {
                    //System.out.println("classe : " + body.getMethod().getDeclaringClass());
                    String name = body.getMethod().getDeclaringClass().getName()+".java";
                    String methodName = body.getMethod().getName();
                    //Check methods
                    IODAnalyzer.checkMethod(name, methodName, 0, managerGroup, body, body.getUnits(), isInstrumenting);

                    //Check lines

                    UnitPatchingChain chain = body.getUnits();
                    for (Iterator<Unit> iter = chain.snapshotIterator(); iter.hasNext(); ) {
                        final Unit u = iter.next();
                        String line = u.toString();
                        DWAnalyzer.checkLine(line, name, methodName, 0, managerGroup, body, u, body.getUnits(), isInstrumenting);
                        HMUAnalyzer.checkLine(line, name, methodName, 0, managerGroup, body, u, body.getUnits(), isInstrumenting);
                        IODAnalyzer.checkLine(line, name, methodName, 0, managerGroup, body, u, body.getUnits(), isInstrumenting);
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
