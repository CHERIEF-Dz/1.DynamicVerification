package staticanalyzis;

import actions.dw.DWAcquire;
import actions.dw.DWRelease;
import actions.iod.IODEnter;
import actions.iod.IODExit;
import manager.DWManager;
import manager.IODManager;
import manager.ManagerGroup;
import soot.*;
import soot.jimple.*;
import utils.CodeLocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public class IODAnalyzer extends CodeSmellAnalyzer {

    public static void checkMethod(String path, String name, String methodName, int lineNumber, ManagerGroup managerGroup, Body b, UnitPatchingChain units, boolean isInstrumenting) {
        checkIOD("test", name, methodName,0, managerGroup.managerIOD, b, b.getUnits(), isInstrumenting);
        ///checkIODExit(line, "test", name, methodName,0, managerGroup.managerIOD, b, u, b.getUnits(), isInstrumenting);
    }


    private static void checkIOD(String path, String name, String methodName, int lineNumber, IODManager manager, Body b, UnitPatchingChain units, boolean isInstrumenting) {
        Matcher m = findPattern(methodName, "onDraw");
        if (m.find()) {
            System.out.println("On Draw !!");
            String key=generateKey(name);
            String variableName=m.group(0).split("\\.")[0];
            manager.addEnter(key, new IODEnter(new CodeLocation(path, name, methodName, lineNumber)));
            manager.addExit(key, new IODExit(new CodeLocation(path, name, methodName, lineNumber)));

            if (isInstrumenting) {
                //System.out.println(b.toString());
                buildInstrumentationMethod(units, b, "iodenter:");
            }
        }
    }

    private static List<Unit> buildBeginningPrint(Body b, Local refPrint, Local refBuilder, Local tmpString, String suffix) {
        // insert "refPrint = java.lang.System.out;"


        List<Unit> generatedUnits = new ArrayList<>();
        AssignStmt printStmt = Jimple.v().newAssignStmt(
                refPrint, Jimple.v().newStaticFieldRef(
                        Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef()));
        generatedUnits.add(printStmt);

        // insert "tmpLong = 'HELLO';"
        AssignStmt stringStmt = Jimple.v().newAssignStmt(tmpString,
                StringConstant.v(CodeSmellAnalyzer.prefix+b.getMethod().getDeclaringClass().getName() + ".java:"+CodeSmellAnalyzer.keyCpt+":"+suffix));
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

        return generatedUnits;
    }

    private static List<Unit> buildEndingPrint(List<Unit> generatedUnits, Local refPrint, Local refBuilder, Local tmpString) {
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

    protected static void buildInstrumentationMethod(UnitPatchingChain units, Body b, String suffix) {
        //Add Print
        Local refIdentity = addTmpRef(b, "refIdentity", IntType.v());
        Local refBuilder = addTmpRef(b, "refBuilder", RefType.v("java.lang.StringBuilder"));
        Local refPrint = addTmpRef(b, "refPrint", RefType.v("java.io.PrintStream"));
        Local tmpString = addTmpRef(b, "tmpString", RefType.v("java.lang.String"));
        List<Unit> generatedUnits;

        generatedUnits = buildBeginningPrint(b, refPrint, refBuilder, tmpString, suffix);

        generatedUnits = buildEndingPrint(generatedUnits, refPrint, refBuilder, tmpString);

        //units.insertAfter(generatedUnits, u);
        units.insertBefore(generatedUnits, units.getFirst());

        System.out.println("Body : ");
        System.out.println(b.toString());
        //check that we did not mess up the Jimple
        b.validate();
    }

}
