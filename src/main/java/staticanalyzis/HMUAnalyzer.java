package staticanalyzis;

import actions.hmu.HMUAddition;
import actions.hmu.HMUClean;
import actions.hmu.HMUDeletion;
import actions.hmu.HMUImplementation;
import manager.HMUManager;
import manager.ManagerGroup;
import soot.Body;
import soot.Unit;
import soot.UnitPatchingChain;
import utils.CodeLocation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HMUAnalyzer extends CodeSmellAnalyzer {

    public static void checkLine(String line, String path, String name, String methodName, int lineNumber, ManagerGroup managerGroup, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        checkHMUInst(line, "test", name, methodName,0, managerGroup.managerHMU, b, u, b.getUnits(), isInstrumenting);
        checkHMUAdd(line, "test", name, methodName,0, managerGroup.managerHMU, b, u, b.getUnits(), isInstrumenting);
        checkHMUDel(line, "test", name, methodName, 0, managerGroup.managerHMU, b, u, b.getUnits(), isInstrumenting);
        checkHMUClean(line, "test", name, methodName, 0, managerGroup.managerHMU, b, u, b.getUnits(), isInstrumenting);
    }

    private static void checkHMUInst(String line, String path, String name, String methodName, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        Matcher m = findPattern(line, "(?<=(HashMap))(.*)(?=void <init>\\()");
        if (m.find()) {
            //System.out.println("New HashMap !! : " + line);
            String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
            manager.addImplementation(generateKey(name), new HMUImplementation(new CodeLocation(path, name, methodName, lineNumber), "HashMap", variableName));
            if (isInstrumenting) {
                buildInstrumentation(getStructureInstanceLocalName(line), units, u, b, "hmuimpl:");
            }
        }
        else {
            m = findPattern(line, "(?<=(ArrayMap))(.*)(?=void <init>\\()");
            if (m.find()) {
                String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
                manager.addImplementation(generateKey(name), new HMUImplementation(new CodeLocation(path, name, methodName, lineNumber), "ArrayMap", variableName));
                if (isInstrumenting) {
                    buildInstrumentation(getStructureInstanceLocalName(line), units, u, b, "hmuimpl:");
                }
            }
            else {
                m = findPattern(line, "(?<=(SingleArrayMap))(.*)(?=void <init>\\()");
                if (m.find()) {
                    String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
                    manager.addImplementation(generateKey(name), new HMUImplementation(new CodeLocation(path, name, methodName, lineNumber), "SimpleArrayMap", variableName));
                    if (isInstrumenting) {
                        buildInstrumentation(getStructureInstanceLocalName(line), units, u, b, "hmuimpl:");
                    }
                }
            }
        }
    }


    public static void checkHMUAdd(String line, String path, String name, String methodName, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        Matcher m = findPattern(line, "(?<=(HashMap|ArrayMap|SingleArrayMap))(.*)(?=put\\()");
        if (m.find()) {
            String variableName=m.group(0).split("\\.")[0];
            manager.addAddition(generateKey(name), new HMUAddition(new CodeLocation(path, name, methodName, lineNumber), variableName));
            if (isInstrumenting) {
                buildInstrumentation(getStructureCallerLocalName(line), units, u, b, "hmuadd:");
            }
        }
    }

    public static void checkHMUDel(String line, String path, String name, String methodName, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        Matcher m = findPattern(line, "(?<=(HashMap|ArrayMap|SingleArrayMap))(.*)(?=remove\\()");
        if (m.find()) {
            String variableName=m.group(0).split("\\.")[0];
            manager.addDeletion(generateKey(name), new HMUDeletion(new CodeLocation(path, name, methodName, lineNumber), variableName));
            if (isInstrumenting) {
                buildInstrumentation(getStructureCallerLocalName(line), units, u, b, "hmudel:");
            }
        }
    }

    private static void checkHMUClean(String line, String path, String name, String methodName, int lineNumber, HMUManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        Matcher m = findPattern(line, "(?<=(HashMap|ArrayMap|SingleArrayMap))(.*)(?=clear\\()");
        if (m.find()) {
            String variableName=m.group(0).split("\\.")[0];
            manager.addClean(generateKey(name), new HMUClean(new CodeLocation(path, name, methodName, lineNumber), variableName));
            if (isInstrumenting) {
                buildInstrumentation(getStructureCallerLocalName(line), units, u, b, "hmucln:");
            }
        }
    }

}
