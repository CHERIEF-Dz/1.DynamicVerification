package staticanalyzis;

import actions.hp.HPEnter;
import actions.hp.HPExit;
import actions.nlmr.NLMREnter;
import actions.nlmr.NLMRExit;
import manager.HPManager;
import manager.ManagerGroup;
import manager.NLMRManager;
import soot.Body;
import soot.UnitPatchingChain;
import utils.CodeLocation;

import java.util.regex.Matcher;

public class NLMRAnalyzer extends CodeSmellAnalyzer{

    public static void methodsToCheck(String name, String methodName, int lineNumber, ManagerGroup managerGroup, Body b, UnitPatchingChain units, boolean isInstrumenting) {
        checkNLMR(name, methodName, "onTrimMemory", 0, managerGroup.managerNLMR, b, b.getUnits(),"nlmrenter", "nlmrexit", isInstrumenting);
    }

    protected static void checkNLMR(String name, String methodName, String methodNameNeeded, int lineNumber, NLMRManager manager, Body b, UnitPatchingChain units, String prefix, String suffix, boolean isInstrumenting) {
        Matcher m = findPattern(methodName, methodNameNeeded);
        if (m.find()) {
            String key=generateKey(name);
            manager.addEnter(key, new NLMREnter(new CodeLocation(name, methodName, lineNumber)));
            manager.addExit(key, new NLMRExit(new CodeLocation(name, methodName, lineNumber)));
        }
        buildMethod(methodName, methodName, b, b.getUnits(), prefix, suffix, isInstrumenting);
    }
}
