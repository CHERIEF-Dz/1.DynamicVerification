package staticanalyzis;

import actions.iod.IODEnter;
import actions.iod.IODExit;
import actions.iod.IODNew;
import manager.IODManager;
import manager.ManagerGroup;
import soot.*;
import utils.CodeLocation;

import java.util.regex.Matcher;

public class IODAnalyzer extends CodeSmellAnalyzer {

    public static void checkLine(String line, String name, String methodName, int lineNumber, ManagerGroup managerGroup, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        checkNew(line, name, methodName,0, managerGroup.managerIOD, b, u, b.getUnits(), isInstrumenting);
    }

    public static void checkNew(String line, String name, String methodName, int lineNumber, IODManager manager, Body b, Unit u, UnitPatchingChain units, boolean isInstrumenting) {
        Matcher m = findPattern(line, "<init>");
        Matcher m2 = findPattern(methodName, "onDraw");
        if (m2.find() && m.find()) {
            String variableName=m.group(0).split("\\.")[0];
            if (!getStructureInstanceLocalName(line).equals("refBuilder")) {
                manager.addNew(generateKey(name, methodName), new IODNew(new CodeLocation(name, methodName, lineNumber)));
                if (isInstrumenting) {
                    buildInstrumentation(getStructureInstanceLocalName(line), units, u, b, "iodnew:");
                }
            }
        }
    }

    public static void methodsToCheck(String name, String methodName, int lineNumber, ManagerGroup managerGroup, Body b, UnitPatchingChain units, boolean isInstrumenting) {
        checkIOD(name, methodName, "onDraw", 0, managerGroup.managerIOD, b, b.getUnits(),isInstrumenting);
    }

    protected static void checkIOD(String name, String methodName, String methodNameNeeded, int lineNumber, IODManager manager, Body b, UnitPatchingChain units, boolean isInstrumenting) {
        Matcher m = findPattern(methodName, methodNameNeeded);
        if (m.find()) {
            String key=generateKey(name, methodName);
            manager.addEnter(key, new IODEnter(new CodeLocation(name, methodName, lineNumber)));
            manager.addExit(key, new IODExit(new CodeLocation(name, methodName, lineNumber)));
        }
        buildMethod(methodName, methodNameNeeded, b, b.getUnits(), "iodenter:", "iodexit:", isInstrumenting);
    }
}
