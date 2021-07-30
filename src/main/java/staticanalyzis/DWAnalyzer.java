package staticanalyzis;

import events.dw.DWAcquire;
import events.dw.DWRelease;
import manager.DWManager;
import manager.ManagerGroup;
import soot.Body;
import soot.Unit;
import soot.UnitPatchingChain;
import utils.CodeLocation;

import java.util.regex.Matcher;

public final class DWAnalyzer extends CodeSmellAnalyzer{

    public static void checkLine(String line, String name, String methodName, int lineNumber, ManagerGroup managerGroup, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        checkDWAcquire(line, name, methodName, lineNumber, managerGroup.managerDW, b, u, b.getUnits(), isInstrumenting);
        checkDWRelease(line, name, methodName, lineNumber, managerGroup.managerDW, b, u, b.getUnits(), isInstrumenting);
    }

    private static void checkDWAcquire(String line, String name, String methodName, int lineNumber, DWManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        Matcher m = findPattern(line, "(?<=(WakeLock))(.*)(?=acquire\\()");
        if (m.find()) {
            String key=generateKey(name, methodName);
            String variableName=m.group(0).split("\\.")[0];
            manager.addAcquire(key, new DWAcquire(new CodeLocation(name, methodName, getKey(name, methodName)), variableName));

            if (isInstrumenting) {
                //System.out.println(b.toString());
                buildInstrumentation(getStructureCallerLocalName(line), units, u, b, "dwacq:");
            }
        }
    }

    private static void checkDWRelease(String line, String name, String methodName, int lineNumber, DWManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        Matcher m = findPattern(line, "(?<=(WakeLock))(.*)(?=release\\()");
        if (m.find()) {
            String key=generateKey(name, methodName);
            String variableName=m.group(0).split("\\.")[0];
            manager.addRelease(key, new DWRelease(new CodeLocation(name, methodName, getKey(name, methodName)), variableName));

            if (isInstrumenting) {
                buildInstrumentation(getStructureCallerLocalName(line), units, u, b, "dwrel:");
            }
        }
    }
}
