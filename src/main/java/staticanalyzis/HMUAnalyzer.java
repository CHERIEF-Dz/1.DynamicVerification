package staticanalyzis;

import events.hmu.HMUAddition;
import events.hmu.HMUClean;
import events.hmu.HMUDeletion;
import events.hmu.HMUImplementation;
import manager.HMUManager;
import manager.ManagerGroup;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.StringConstant;
import utils.CodeLocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public final class HMUAnalyzer extends CodeSmellAnalyzer {

    public static void checkLine(String line, String name, String methodName, int lineNumber, ManagerGroup managerGroup, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {

        String type="";
        Matcher m = findPattern(line, "(?<=(HashMap))");
        if (m.find()) {
            type="HashMap";
        }
        else {
            m = findPattern(line, "(?<=(SimpleArrayMap))");
            if (m.find()) {
                type="SimpleArrayMap";
            }
            else {
                m = findPattern(line, "(?<=(ArrayMap))");
                if (m.find()) {
                    type="ArrayMap";
                }
            }
        }

        checkHMUInst(line, name, methodName,0, managerGroup.managerHMU, b, u, b.getUnits(), isInstrumenting, type);
        checkHMUAdd(line, name, methodName,0, managerGroup.managerHMU, b, u, b.getUnits(), isInstrumenting, type);
        checkHMUDel(line, name, methodName, 0, managerGroup.managerHMU, b, u, b.getUnits(), isInstrumenting, type);
        checkHMUClean(line, name, methodName, 0, managerGroup.managerHMU, b, u, b.getUnits(), isInstrumenting, type);
    }

    private static void checkHMUInst(String line, String name, String methodName, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting, String type) {

        Matcher m = findPattern(line, "(?<=(HashMap))(.*)(?=void <init>\\()");
        if (m.find()) {
            String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
            manager.addImplementation(generateKey(name, methodName), new HMUImplementation(new CodeLocation(name, methodName, lineNumber), "HashMap", variableName));
            if (isInstrumenting) {
                buildInstrumentation(getStructureInstanceLocalName(line), units, u, b, "hmuimpl:", type);
            }
        }
        else {
            m = findPattern(line, "(?<=(SimpleArrayMap))(.*)(?=void <init>\\()");
            if (m.find()) {
                String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
                manager.addImplementation(generateKey(name, methodName), new HMUImplementation(new CodeLocation(name, methodName, lineNumber), "SimpleArrayMap", variableName));
                if (isInstrumenting) {
                    buildInstrumentation(getStructureInstanceLocalName(line), units, u, b, "hmuimpl:", type);
                }
            }
            else {
                m = findPattern(line, "(?<=(ArrayMap))(.*)(?=void <init>\\()");
                if (m.find()) {
                    String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
                    manager.addImplementation(generateKey(name, methodName), new HMUImplementation(new CodeLocation(name, methodName, lineNumber), "ArrayMap", variableName));
                    if (isInstrumenting) {
                        buildInstrumentation(getStructureInstanceLocalName(line), units, u, b, "hmuimpl:", type);
                    }
                }
            }
        }
    }

    protected static List<Unit> buildSize(List<Unit> generatedUnits, Body b, String structureLocalName, Local refSize, Local refBuilder, String type) {
        //Get Identity
        Local structureLocal = null;
        Iterator<Local> localIterator = b.getLocals().iterator();
        while (localIterator.hasNext()) {
            Local elt = localIterator.next();
            if (elt.getName().equals(structureLocalName)) {
                structureLocal = elt;
            }
        }

        SootMethod sizeMethod = null;
        System.out.println("Type : " + type);
        //if HashMap
        if (type.equals("HashMap")) {
            sizeMethod = Scene.v().getSootClass("java.util.HashMap").getMethod("int size()");
        }
        //if ArrayMap
        if (type.equals("ArrayMap") || type.equals("SimpleArrayMap")) {
            sizeMethod = Scene.v().getSootClass("androidx.collection.SimpleArrayMap").getMethod("int size()");
        }
        AssignStmt structureSize = Jimple.v().newAssignStmt(refSize,
                Jimple.v().newVirtualInvokeExpr(structureLocal, sizeMethod.makeRef()));
        generatedUnits.add(structureSize);

        //Virtual call Append
        SootMethod appendMethod1 = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.StringBuilder append(java.lang.String)");
        InvokeStmt invokeAppend1 = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(refBuilder, appendMethod1.makeRef(), StringConstant.v(":")));
        generatedUnits.add(invokeAppend1);

        //Append Size
        SootMethod appendMethod = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.StringBuilder append(int)");
        InvokeStmt invokeAppend = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(refBuilder, appendMethod.makeRef(), refSize));
        generatedUnits.add(invokeAppend);
        return generatedUnits;
    }

    protected static void buildInstrumentation(String structureLocalName, UnitPatchingChain units, Unit u, Body b, String suffix, String type) {
        //Add Print
        Local refPrint = addTmpRef(b, "refPrint", RefType.v("java.io.PrintStream"));
        Local refBuilder = addTmpRef(b, "refBuilder", RefType.v("java.lang.StringBuilder"));
        Local refIdentity = addTmpRef(b, "refIdentity", IntType.v());
        Local refSize = addTmpRef(b, "refSize", IntType.v());
        Local tmpString = addTmpRef(b, "tmpString", RefType.v("java.lang.String"));

        List<Unit> generatedUnits = new ArrayList<>();

        generatedUnits = buildBeginningPrint(b, refPrint, refBuilder, tmpString, suffix);

        generatedUnits = buildIdentity(generatedUnits, b, structureLocalName, refIdentity, refBuilder);

        generatedUnits = buildSize(generatedUnits, b, structureLocalName, refSize, refBuilder, type);

        generatedUnits = buildEndingPrint(generatedUnits, refPrint, refBuilder, tmpString);

        units.insertAfter(generatedUnits, u);

        //check that we did not mess up the Jimple
        b.validate();
    }


    public static void checkHMUAdd(String line, String name, String methodName, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting, String type) {
        Matcher m = findPattern(line, "(?<=(HashMap|ArrayMap|SimpleArrayMap))(.*)(?=put\\()");
        if (m.find()) {
            String variableName=m.group(0).split("\\.")[0];
            manager.addAddition(generateKey(name, methodName), new HMUAddition(new CodeLocation(name, methodName, lineNumber), variableName));
            if (isInstrumenting) {
                //System.out.println(b.toString());
                buildInstrumentation(getStructureCallerLocalName(line), units, u, b, "hmuadd:", type);
            }
        }
    }

    public static void checkHMUDel(String line, String name, String methodName, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting, String type) {
        Matcher m = findPattern(line, "(?<=(HashMap|ArrayMap|SimpleArrayMap))(.*)(?=remove\\()");
        if (m.find()) {
            String variableName=m.group(0).split("\\.")[0];
            manager.addDeletion(generateKey(name, methodName), new HMUDeletion(new CodeLocation(name, methodName, lineNumber), variableName));
            if (isInstrumenting) {
                buildInstrumentation(getStructureCallerLocalName(line), units, u, b, "hmudel:", type);
            }
        }
    }

    private static void checkHMUClean(String line, String name, String methodName, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting, String type) {
        Matcher m = findPattern(line, "(?<=(HashMap|ArrayMap|SimpleArrayMap))(.*)(?=clear\\()");
        if (m.find()) {
            String variableName=m.group(0).split("\\.")[0];
            manager.addClean(generateKey(name, methodName), new HMUClean(new CodeLocation(name, methodName, lineNumber), variableName));
            if (isInstrumenting) {
                buildInstrumentation(getStructureCallerLocalName(line), units, u, b, "hmucln:", type);
            }
        }
    }

}
