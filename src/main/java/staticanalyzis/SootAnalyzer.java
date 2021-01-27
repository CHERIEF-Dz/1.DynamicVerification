package staticanalyzis;

import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JIfStmt;
import soot.options.Options;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SootAnalyzer {

    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "tests_folders" + File.separator + "LambdaApp";

    public static void setupSoot() {
        //Hack to prevent soot to print on System.out
        PrintStream originalStream = System.out;
        /*
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
                // NO-OP
            }
        }));
        */
        String apk = "tests_apks/app-debug.apk";
        G.reset();
        Options.v().set_verbose(false);
        //Path to android-sdk-platforms
        Options.v().set_android_jars("android-platforms");
        //Options.v().set_soot_classpath("/home/geoffrey/These/decompiler/android-platforms/android-14/android.jar");
        //prefer Android APK files
        Options.v().set_src_prec(Options.src_prec_apk);
        // Allow phantom references
        Options.v().set_allow_phantom_refs(true);
        //Set path to APK
        Options.v().set_process_dir(Collections.singletonList(apk));
        //Options.v().set_process_dir(Collections.singletonList("/home/geoffrey/These/LotOfAntiPatternsApplication/app/src/main/java"));
        Options.v().set_whole_program(true);
        Options.v().set_output_format(Options.output_format_grimple);
        //Options.v().set_output_dir("/home/geoffrey/These/decompiler/out");
        //Get directly the home directory and work on it
        //Options.v().set_output_dir(System.getProperty("user.home")+ File.separator + "/These/decompiler/out");
        //Options.v().set_soot_classpath();

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

    public static void analyze(String[] args) {
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
                        System.out.println("Methode : " + sm.getName() + " " + sm.getJavaSourceStartLineNumber());
                        /*
                        Body body = sm.getActiveBody();
                        UnitPatchingChain chain = body.getUnits();
                        for(Iterator<Unit> iter = chain.snapshotIterator(); iter.hasNext();) {
                            final Unit u = iter.next();
                            System.out.println(u.toString());
                        }
                         */
                    }
                }

            //System.out.println("")
        }

    }
}
