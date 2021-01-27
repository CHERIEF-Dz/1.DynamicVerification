package aspectTest;

import fj.data.vector.V;
import staticanalyzis.Analyzer;
import utils.HMUManager;

import java.util.HashMap;

public aspect testAspect {

    /*
    pointcut publicMethodExecuted(): execution(public !static * *(..));

    after(): publicMethodExecuted() {
        System.out.printf("Enters on method: %s. \n", thisJoinPoint.getSignature());

        Object[] arguments = thisJoinPoint.getArgs();
        for (int i =0; i < arguments.length; i++){
            Object argument = arguments[i];
            if (argument != null){
                System.out.printf("With argument of type %s and value %s. \n", argument.getClass().toString(), argument);
            }
        }
        System.out.printf("Exits method: %s. \n", thisJoinPoint.getSignature());
    }
    */
    /*
    pointcut testAnalyze(HMUManager manager, String path, Analyzer analyzerClass) : execution(void Analyzer.analyze(HMUManager, String)) && args(manager, path) && target(analyzerClass);


    before(HMUManager manager, String path, Analyzer analyzerClass) : testAnalyze(manager, path, analyzerClass) {
        System.out.println("Début Analyze");
    }

    after(HMUManager manager, String path, Analyzer analyzerClass) : testAnalyze(manager, path, analyzerClass) {
        System.out.println("Fin Analyze!");
    }
    */


    pointcut addHashMap(HashMap structure) : call(* HashMap.put(..)) && target(structure);


    before(HashMap structure) : addHashMap(structure) {
        //System.out.println("StructuresManager.java:28:add:" + System.identityHashCode(structure));
    }

    after(HashMap structure) : addHashMap(structure) {
        //System.out.println(thisJoinPoint.getSourceLocation()+":add:" + System.identityHashCode(structure));
    }


    /*
    pointcut implTest() : initialization(*.new(..)) && !within(ObjectCreationAspect);

    before() : implTest() {
        System.out.println("Test !!");
    }
    */


    pointcut implHashMap(HashMap structure) : initialization(*.new()) && target(structure);

    after(HashMap structure) : implHashMap(structure) {
        //System.out.println(thisJoinPoint.getSourceLocation()+":impl:" + System.identityHashCode(structure));
    }

}
