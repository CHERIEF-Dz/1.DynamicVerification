package staticanalyzis;

import actions.hp.HPEnter;
import actions.hp.HPExit;
import manager.HPManager;
import manager.ManagerGroup;
import soot.Body;
import soot.UnitPatchingChain;
import utils.CodeLocation;

import java.util.regex.Matcher;

public class HPAnalyzer extends CodeSmellAnalyzer {

    public static void methodsToCheck(String name, String methodName, int lineNumber, ManagerGroup managerGroup, Body b, UnitPatchingChain units, boolean isInstrumenting) {
        checkHP(name, methodName, "onStartCommand", 0, managerGroup.managerHP, b, b.getUnits(),"hssenter", "hssexit", isInstrumenting);
        checkHP(name, methodName, "onReceive", 0, managerGroup.managerHP, b, b.getUnits(),"hbrenter", "hbrexit", isInstrumenting);
        checkHP(name, methodName, "onPostExecute", 0, managerGroup.managerHP, b, b.getUnits(),"hasenter", "hasexit", isInstrumenting);
        checkHP(name, methodName, "onPreExecute", 0, managerGroup.managerHP, b, b.getUnits(),"hasenter", "hasexit", isInstrumenting);
        checkHP(name, methodName, "onProgressUpdate", 0, managerGroup.managerHP, b, b.getUnits(),"hasenter", "hasexit", isInstrumenting);

    }

    protected static void checkHP(String name, String methodName, String methodNameNeeded, int lineNumber, HPManager manager, Body b, UnitPatchingChain units, String prefix, String suffix, boolean isInstrumenting) {
        Matcher m = findPattern(methodName, methodNameNeeded);
        if (m.find()) {
            System.out.println("HP Method !!");
            String key=generateKey(name);
            manager.addEnter(key, new HPEnter(new CodeLocation(name, methodName, lineNumber)));
            manager.addExit(key, new HPExit(new CodeLocation(name, methodName, lineNumber)));
        }
        buildMethod(methodName, methodNameNeeded, b, b.getUnits(), prefix, suffix, isInstrumenting);
    }

}
