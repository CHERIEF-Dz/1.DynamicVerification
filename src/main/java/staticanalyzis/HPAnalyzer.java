package staticanalyzis;

import events.hp.HPEnter;
import events.hp.HPExit;
import manager.HPManager;
import manager.ManagerGroup;
import soot.Body;
import soot.UnitPatchingChain;
import structure.HeavyProcessStructure;
import utils.CodeLocation;

public class HPAnalyzer extends CodeSmellAnalyzer {

    public static void methodsToCheck(String name, String methodName, int lineNumber, ManagerGroup managerGroup, Body b, UnitPatchingChain units, boolean isInstrumenting) {
        checkHP(name, methodName, "onStartCommand", lineNumber, managerGroup.managerHP, b, b.getUnits(),"hssenter:", "hssexit:", isInstrumenting);
        checkHP(name, methodName, "onReceive", lineNumber, managerGroup.managerHP, b, b.getUnits(),"hbrenter:", "hbrexit:", isInstrumenting);
        checkHP(name, methodName, "onPostExecute", lineNumber, managerGroup.managerHP, b, b.getUnits(),"hasenter:", "hasexit:", isInstrumenting);
        checkHP(name, methodName, "onPreExecute", lineNumber, managerGroup.managerHP, b, b.getUnits(),"hasenter:", "hasexit:", isInstrumenting);
        checkHP(name, methodName, "onProgressUpdate", lineNumber, managerGroup.managerHP, b, b.getUnits(),"hasenter:", "hasexit:", isInstrumenting);

    }

    protected static void checkHP(String name, String methodName, String methodNameNeeded, int lineNumber, HPManager manager, Body b, UnitPatchingChain units, String prefix, String suffix, boolean isInstrumenting) {
        if (checkMethodName(methodName, methodNameNeeded)) {
            String key=generateKey(name, methodName);
            manager.addStructure(name, new HeavyProcessStructure(new CodeLocation(name, methodName, lineNumber), name));
            manager.addEnter(key, new HPEnter(new CodeLocation(name, methodName, lineNumber)));
            manager.addExit(key, new HPExit(new CodeLocation(name, methodName, lineNumber)));
        }
        buildMethod(methodName, methodNameNeeded, b, b.getUnits(), prefix, suffix, isInstrumenting);
    }

}
