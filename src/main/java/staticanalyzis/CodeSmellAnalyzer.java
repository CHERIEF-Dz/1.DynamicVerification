package staticanalyzis;

import soot.*;
import soot.jimple.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeSmellAnalyzer {

    static int keyCpt=0;
    static String prefix = "dynver:";

    protected static Local addTmpRef(Body body, String name, Type type)
    {
        Local tmpRef = Jimple.v().newLocal(name, type);
        body.getLocals().add(tmpRef);
        return tmpRef;
    }

    protected static Matcher findPattern(String line, String pattern) {
        Pattern pat = Pattern.compile(pattern);
        Matcher m = pat.matcher(line);
        return m;
    }

    protected static void buildInstrumentation(String structureLocalName, UnitPatchingChain units, Unit u, Body b, String suffix) {
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

    protected static String getStructureInstanceLocalName(String line) {
        return line.substring(line.indexOf("specialinvoke")).replace("specialinvoke ", "").trim().split("\\.")[0];
    }

    protected static String getStructureCallerLocalName(String line) {
        return line.substring(line.indexOf("virtualinvoke")).replace("virtualinvoke ", "").trim().split("\\.")[0];
    }

    protected static String generateKey(String name) {
        CodeSmellAnalyzer.keyCpt++;
        return name + ":" + CodeSmellAnalyzer.keyCpt;
    }
}
