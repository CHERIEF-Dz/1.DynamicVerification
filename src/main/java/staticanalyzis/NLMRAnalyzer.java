package staticanalyzis;

import actions.hp.HPEnter;
import actions.hp.HPExit;
import actions.nlmr.NLMREnter;
import actions.nlmr.NLMRExit;
import manager.HPManager;
import manager.ManagerGroup;
import manager.NLMRManager;
import soot.*;
import soot.jimple.*;
import utils.CodeLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class NLMRAnalyzer extends CodeSmellAnalyzer{

    private static SootClass runnerClass;

    public static void methodsToCheck(String name, String methodName, int lineNumber, ManagerGroup managerGroup, Body b, UnitPatchingChain units, boolean isInstrumenting) {
        checkNLMR(name, methodName, "onTrimMemory", 0, managerGroup.managerNLMR, b, b.getUnits(),"nlmrenter", "nlmrexit", isInstrumenting);
    }

    protected static void checkNLMR(String name, String methodName, String methodNameNeeded, int lineNumber, NLMRManager manager, Body b, UnitPatchingChain units, String prefix, String suffix, boolean isInstrumenting) {
        Matcher m = findPattern(methodName, methodNameNeeded);
        if (m.find()) {
            String key=generateKey(name);
            manager.addEnter(key, new NLMREnter(new CodeLocation(name, methodName, lineNumber)));
            manager.addExit(key, new NLMRExit(new CodeLocation(name, methodName, lineNumber)));
            if (isInstrumenting) {
                build(b, b.getMethod().getDeclaringClass());
            }
        }
    }

    private static  List<Unit> generatePrintMemory(Body newBody, String suffix) {

        Local refPrint = addTmpRef(newBody, "refPrint", RefType.v("java.io.PrintStream"));
        Local refBuilder = addTmpRef(newBody, "refBuilder", RefType.v("java.lang.StringBuilder"));
        Local tmpString = addTmpRef(newBody, "tmpString", RefType.v("java.lang.String"));

        Local refRuntime = addTmpRef(newBody, "refRuntime", RefType.v("java.lang.Runtime"));
        Local refLong1 = addTmpRef(newBody, "refLong1", LongType.v());
        Local refLong2 = addTmpRef(newBody, "refLong2", LongType.v());
        List<Unit> generatedUnits = new ArrayList<>();

        AssignStmt printStmt = Jimple.v().newAssignStmt(
                refPrint, Jimple.v().newStaticFieldRef(
                        Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef()));
        generatedUnits.add(printStmt);

        // insert "tmpLong = 'HELLO';"
        AssignStmt stringStmt = Jimple.v().newAssignStmt(tmpString,
                StringConstant.v(CodeSmellAnalyzer.prefix+newBody.getMethod().getDeclaringClass().getName() + ".java:"+CodeSmellAnalyzer.keyCpt+":"+suffix));
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

        //Virtual call Append
        SootMethod getRuntimeMethod = Scene.v().getSootClass("java.lang.Runtime").getMethod("java.lang.Runtime getRuntime()");
        AssignStmt runTimeAssignment = Jimple.v().newAssignStmt(refRuntime, Jimple.v().newStaticInvokeExpr(getRuntimeMethod.makeRef()));
        generatedUnits.add(runTimeAssignment);

        SootMethod totalMemoryMethod = Scene.v().getSootClass("java.lang.Runtime").getMethod("long totalMemory()");
        AssignStmt long1Assignment = Jimple.v().newAssignStmt(refLong1, Jimple.v().newVirtualInvokeExpr(refRuntime, totalMemoryMethod.makeRef()));
        generatedUnits.add(long1Assignment);

        SootMethod freeMemoryMethod = Scene.v().getSootClass("java.lang.Runtime").getMethod("long freeMemory()");
        AssignStmt long2Assignment = Jimple.v().newAssignStmt(refLong2, Jimple.v().newVirtualInvokeExpr(refRuntime, freeMemoryMethod.makeRef()));
        generatedUnits.add(long2Assignment);

        AssignStmt longMinusAssignment = Jimple.v().newAssignStmt(refLong1, Jimple.v().newSubExpr(refLong1, refLong2));;
        generatedUnits.add(longMinusAssignment);

        //Virtual call Append
        SootMethod appendMethod2 = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.StringBuilder append(long)");
        InvokeStmt invokeAppend2 = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(refBuilder, appendMethod2.makeRef(), refLong1));
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
        return generatedUnits;
    }

    private static void buildRun(JimpleBody newBody, SootClass sootClass) {

        Local refClass = addTmpRef(newBody, "refClass", RefType.v(sootClass.getName()));
        List<Unit> generatedUnits = new ArrayList<>();

        IdentityStmt classIdentity = Jimple.v().newIdentityStmt(refClass,
                Jimple.v().newThisRef(RefType.v(sootClass.getName())));
        generatedUnits.add(classIdentity);

        generatedUnits.addAll(generatePrintMemory(newBody, "nlmrexit:"));

        ReturnVoidStmt returnInit = Jimple.v().newReturnVoidStmt();
        generatedUnits.add(returnInit);

        newBody.getUnits().addAll(generatedUnits);
    }

    private static void buildInit(JimpleBody newBody, SootClass sootClass, SootClass activityClass) {
        Local refClass = addTmpRef(newBody, "refClass", RefType.v(sootClass.getName()));
        Local refActivityClass = addTmpRef(newBody, "refActivityClass", RefType.v(activityClass.getName()));

        List<Unit> generatedUnits = new ArrayList<>();


        IdentityStmt classIdentity = Jimple.v().newIdentityStmt(refClass,
                Jimple.v().newThisRef(RefType.v(sootClass.getName())));
        generatedUnits.add(classIdentity);

        IdentityStmt activityIdentity = Jimple.v().newIdentityStmt(refActivityClass,
                Jimple.v().newParameterRef(RefType.v(activityClass.getName()), 0)
        );
        generatedUnits.add(activityIdentity);

        AssignStmt assignStmt = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(refClass, sootClass.getFields().getFirst().makeRef()), refActivityClass);

        generatedUnits.add(assignStmt);

        InvokeStmt initObject = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(refClass,
                Scene.v().getSootClass("java.lang.Object").getMethod("void <init>()").makeRef()));
        generatedUnits.add(initObject);

        ReturnVoidStmt returnInit = Jimple.v().newReturnVoidStmt();
        generatedUnits.add(returnInit);

        newBody.getUnits().addAll(generatedUnits);
    }



    private static void buildTrim(Body newBody, SootClass sootClass, SootClass activityClass) {

        newBody.getUnits().insertBefore(generatePrintMemory(newBody, "nlmrenter:"), getFirstNonIdentityUnit(newBody.getUnits()));

        List<Unit> returnUnits = getReturns(newBody);

        for (Unit unit : returnUnits) {
            List<Unit> generatedUnits = new ArrayList<>();
            //Local refActivityClass = addTmpRef(newBody, "refActivityClass", RefType.v(activityClass.getName()));
            //Local refTrimParameter = addTmpRef(newBody, "refTrimParameter", RefType)
            Local refHandler = addTmpRef(newBody, "refHandler", RefType.v("android.os.Handler"));
            Local refClass = addTmpRef(newBody, "refClass", RefType.v(sootClass.getName()));

            AssignStmt handlerStmt = Jimple.v().newAssignStmt(refHandler,
                    Jimple.v().newNewExpr(RefType.v("android.os.Handler")));
            generatedUnits.add(handlerStmt);

            InvokeStmt initHandler = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(refHandler,
                    Scene.v().getSootClass("android.os.Handler").getMethod("void <init>()").makeRef()));
            generatedUnits.add(initHandler);

            AssignStmt runnerStmt = Jimple.v().newAssignStmt(refClass,
                    Jimple.v().newNewExpr(RefType.v(sootClass.getName())));
            generatedUnits.add(runnerStmt);

            InvokeStmt initRunner = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(refClass,
                    Scene.v().getSootClass(sootClass.getName()).getMethod("void <init>("+activityClass.getName()+")").makeRef(), newBody.getLocals().getFirst()));
            generatedUnits.add(initRunner);

            SootMethod runMethod = Scene.v().getSootClass("android.os.Handler").getMethod("boolean postDelayed(java.lang.Runnable,long)");
            InvokeStmt startHandler = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(refHandler, runMethod.makeRef(), refClass, LongConstant.v(5000)));
            generatedUnits.add(startHandler);

            newBody.getUnits().insertBefore(generatedUnits, unit);
        }
    }

    public static void buildRunner(SootClass activityClass) {
        //SootClass sootClass = body.getMethod().getDeclaringClass();
        SootClass sootClass = Scene.v().makeSootClass(activityClass.getName()+"$Runner", Modifier.PUBLIC);
        sootClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        sootClass.setApplicationClass();
        SootMethod runMethod = Scene.v().makeSootMethod("run", new ArrayList<Type>(), VoidType.v(), Modifier.PUBLIC);
        JimpleBody runBody = Jimple.v().newBody(runMethod);
        runMethod.setActiveBody(runBody);
        sootClass.addField(Scene.v().makeSootField("this$0", RefType.v(activityClass)));
        sootClass.addInterface(Scene.v().getSootClass("java.lang.Runnable"));
        //SootMethod sootMethod = new SootMethod("testRun", new ArrayList<Type>(), VoidType.v());
        //sootMethod.setSource(new AnonClassInitMethodSource());
        sootClass.addMethod(runMethod);

        buildRun(runBody, sootClass);

        SootMethod initMethod = Scene.v().makeSootMethod("<init>", Arrays.asList(new Type[] {RefType.v(activityClass.getName())}), VoidType.v());
        JimpleBody initBody = Jimple.v().newBody(initMethod);
        initMethod.setActiveBody(initBody);
        sootClass.addMethod(initMethod);

        buildInit(initBody, sootClass, activityClass);

        NLMRAnalyzer.runnerClass = sootClass;
    }

    public static void build(Body trimBody, SootClass activityClass) {


        buildRunner(activityClass);

        buildTrim(trimBody, NLMRAnalyzer.runnerClass, activityClass);

    }
}
