package manager;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.ltl.Eventually;
import ca.uqac.lif.cep.tmf.Filter;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.KeepLast;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.util.*;
import events.nlmr.NLMREnter;
import events.nlmr.NLMRExit;
import staticanalyzis.NLMRAnalyzer;
import structure.NLMRStructure;
import utils.BeepBeepUtils;
import utils.CodeLocation;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.uqac.lif.cep.Connector.*;
import static ca.uqac.lif.cep.Connector.connect;

public class NLMRManager implements Manager{
    private HashMap<String, NLMREnter> enters; // Key = CodeLocation
    private HashMap<String, NLMRExit> exits; // Key = CodeLocation
    private HashMap<String, NLMRStructure> structures;

    public NLMRManager() {
        this.enters = new HashMap<String, NLMREnter>();
        this.exits = new HashMap<String, NLMRExit>();
        this.structures = new HashMap<String, NLMRStructure>();
    }

    @Override
    public void checkStructures() {
        for (java.util.Map.Entry<String, NLMRStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, NLMRStructure> pair = (HashMap.Entry) stringStructureEntry;
            pair.getValue().checkStructure();
        }
    }

    @Override
    public void generateCSV(String outputPath, String apkName, String packageName, boolean returnAllInstances) throws IOException {
        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }

        if (returnAllInstances) {
            //Print for coverage
            File coverageOutputfile = new File(outputPath + "coverage.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(coverageOutputfile, true))) {
                writer.write("Number of NLMR methods," + this.enters.size() + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }

            File executionOutputFile = new File(outputPath + "execution.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(executionOutputFile, true))) {
                int executionSumMethod=0;
                for (Map.Entry<String, NLMREnter> executioncountEntry : this.enters.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumMethod++;
                    }
                }

                writer.write("Number of NLMR methods," + executionSumMethod + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }
        }


        File csvOutputFile = new File(outputPath+"results_NLMR.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk,package,file,method,average Memory released,maximum memory released\n");
            for (java.util.Map.Entry<String, NLMRStructure> stringStructureEntry : this.structures.entrySet()) {
                HashMap.Entry<String, NLMRStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write(apkName+ ","+ packageName +","+fileName+","+methodName+ ","+pair.getValue().getBetterMemory()+","+pair.getValue().getAverageMemory()+"\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }

        if (returnAllInstances) {
            File csvOutputFileAll = new File(outputPath + "results_NLMR_all.csv");
            try (PrintWriter writer = new PrintWriter(csvOutputFileAll)) {
                writer.write("apk,package,file,method,average Memory released,maximum memory released,code smell\n");
                for (java.util.Map.Entry<String, NLMRStructure> stringStructureEntry : this.structures.entrySet()) {
                    HashMap.Entry<String, NLMRStructure> pair = (HashMap.Entry) stringStructureEntry;
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write(apkName + "," + packageName + "," + fileName + "," + methodName + "," + pair.getValue().getBetterMemory() + "," + pair.getValue().getAverageMemory() + "," + pair.getValue().hasCodeSmell() + "\n");
                }
            } catch (FileNotFoundException e) {
                // Do something
            }
        }
    }

    @Override
    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        key = key.replace(NLMRAnalyzer.runnerSuffix, "");
        fileName = fileName.replace(NLMRAnalyzer.runnerSuffix, "");
        if ("nlmrenter".equals(code)) {
            executeEnter(key.replace("$onTrimMemory",""), fileName.replace("$onTrimMemory",""), Long.parseLong(id));
        } else if ("nlmrexit".equals(code)) {
            executeExit(key.replace("$run",""), fileName.replace("$run",""), Long.parseLong(id));
        }
    }

    public void addEnter(String key, NLMREnter enter) {
        this.enters.put(key, enter);
    }

    public void addExit(String key, NLMRExit exit) {
        this.exits.put(key, exit);
    }

    public void addStructure(String key, NLMRStructure structure) {this.structures.put(key, structure);}

    public void executeEnter(String key, String id, long date) {
        if (this.structures.containsKey(id)) {
            this.enters.get(key).execute(this.structures.get(id), date);
        }
    }

    public void executeExit(String key, String id, long date) {
        if (this.structures.containsKey(id)) {
            this.exits.get(key).execute(this.structures.get(id), date);
        }
    }

    @Override
    public void beepBeepBranch(Fork codesmellsFork, int arity) {

        Filter filter = BeepBeepUtils.codesmellConditions(codesmellsFork, arity, new String[]{":nlmrenter:", ":nlmrexit:"});

        GroupProcessor NLMRDetector = new GroupProcessor(1, 1);
        {
            Fork forkLTL = new Fork(5);

            ApplyFunction equalEnter = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            new FunctionTree(new NthElement(2), StreamVariable.X), new Constant("nlmrenter")));
            connect(forkLTL, 0, equalEnter, 0);

            Filter filterEnter = new Filter();
            connect(forkLTL, 1, filterEnter, LEFT);
            connect(equalEnter, OUTPUT, filterEnter, RIGHT);

            ApplyFunction equalExit = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            new FunctionTree(new NthElement(2), StreamVariable.X),
                            new Constant("nlmrexit")));
            connect(forkLTL, 2, equalExit, 0);

            Fork forkExit = new Fork(2);
            connect(equalExit, OUTPUT, forkExit, INPUT);

            Filter filterExit = new Filter();
            connect(forkLTL, 3, filterExit, LEFT);
            connect(forkExit, 0, filterExit, RIGHT);

            ApplyFunction checkValues = new ApplyFunction(
                    new FunctionTree(Numbers.isLessThan,
                            new FunctionTree(Numbers.subtraction,
                                    new FunctionTree(new NthElement(3), StreamVariable.X),
                                    new FunctionTree(new NthElement(3), StreamVariable.Y)),
                            new Constant(1024)));
            connect(filterEnter, OUTPUT, checkValues, 0);
            connect(filterExit, OUTPUT, checkValues, 1);

            Eventually mediumF = new Eventually();
            connect(forkExit, 1, mediumF, INPUT);

            ApplyFunction mediumConjunction = new ApplyFunction(
                    new FunctionTree(Booleans.and,
                            new FunctionTree(Equals.instance,
                                    new FunctionTree(new NthElement(2), StreamVariable.X),
                                    new Constant("nlmrenter")),
                            StreamVariable.Y));
            connect(forkLTL, 4, mediumConjunction, 0);
            connect(mediumF, OUTPUT, mediumConjunction, 1);

            ApplyFunction bigConjunction = new ApplyFunction(
                    new FunctionTree(Booleans.and, StreamVariable.X, StreamVariable.Y));
            connect(mediumConjunction, OUTPUT, bigConjunction, 0);
            connect(checkValues, OUTPUT, bigConjunction, 1);

            Eventually bigF = new Eventually();
            connect(bigConjunction, OUTPUT, bigF, INPUT);

            NLMRDetector.addProcessor(forkLTL);
            NLMRDetector.addProcessor(equalEnter);
            NLMRDetector.addProcessor(filterEnter);
            NLMRDetector.addProcessor(equalExit);
            NLMRDetector.addProcessor(forkExit);
            NLMRDetector.addProcessor(filterExit);
            NLMRDetector.addProcessor(checkValues);
            NLMRDetector.addProcessor(mediumF);
            NLMRDetector.addProcessor(mediumConjunction);
            NLMRDetector.addProcessor(bigF);
            NLMRDetector.addProcessor(bigConjunction);
            NLMRDetector.associateInput(INPUT, forkLTL, INPUT);
            NLMRDetector.associateOutput(OUTPUT, bigF, OUTPUT);
        }

        ApplyFunction splitter = new ApplyFunction(new Strings.SplitString(":"));
        connect(filter, OUTPUT, splitter, INPUT);

        Slice slicer = new Slice(new NthElement(0), NLMRDetector);
        connect(splitter, OUTPUT, slicer, 0);

        KeepLast lastSlice = new KeepLast();
        connect(slicer, lastSlice);

        HashMap<String, Boolean> slicedHashMap = new HashMap<>();
        Pullable p3 = lastSlice.getPullableOutput();
        try  {
            slicedHashMap = (HashMap)p3.pull();
        } catch (NullPointerException e) {

        }

        System.out.println("NLMR : ");

        Iterator it2 = slicedHashMap.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pair = (Map.Entry)it2.next();
            if ((Boolean)pair.getValue()) {
                String locationSplit = (String)pair.getKey();
                Pattern pat = Pattern.compile("(.+\\.java)\\$(.*)");
                Matcher m = pat.matcher(locationSplit);
                String fileName="";
                String methodName="";
                if (m.find()) {
                    fileName = m.group(1);
                    methodName = m.group(2);
                }
                int lineNumber = 0;
                CodeLocation location = new CodeLocation(fileName, methodName, lineNumber);
                NLMRStructure structure = new NLMRStructure(location, ((String)pair.getKey()));
                structure.foundCodeSmell();
                structures.put(((String)pair.getKey()), structure);
                System.out.println(pair.getKey() + " is a code smell");
            }
        }
    }

    public void mergeManager(NLMRManager otherManager) {
        for (java.util.Map.Entry<String, NLMRStructure> otherEntry : otherManager.structures.entrySet()) {
            if (this.structures.containsKey(otherEntry.getKey())) {
                NLMRStructure thisStructure = this.structures.get(otherEntry.getKey());
                if (!thisStructure.hasCodeSmell() && otherEntry.getValue().hasCodeSmell()) {
                    this.structures.put(otherEntry.getKey(), otherEntry.getValue());
                }
            }
            else {
                this.structures.put(otherEntry.getKey(), otherEntry.getValue());
            }
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //Object result = super.clone();
        //return result;

        NLMRManager newManager = new NLMRManager();
        newManager.enters = (HashMap<String, NLMREnter>) this.enters.clone();
        newManager.exits = (HashMap<String, NLMRExit>) this.exits.clone();
        newManager.structures = (HashMap<String, NLMRStructure>) this.structures.clone();
        return newManager;

    }
}
