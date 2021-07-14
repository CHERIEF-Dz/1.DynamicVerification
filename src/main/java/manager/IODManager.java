package manager;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.functions.*;
import ca.uqac.lif.cep.ltl.Eventually;
import ca.uqac.lif.cep.ltl.Until;
import ca.uqac.lif.cep.tmf.Filter;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.KeepLast;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.util.*;
import events.hp.HPEnter;
import events.iod.IODEnter;
import events.iod.IODExit;
import events.iod.IODNew;
import structure.hp.HeavyProcessStructure;
import structure.iod.OnDrawStructure;
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

public class IODManager implements Manager{
    private HashMap<String, IODEnter> enters; // Key = CodeLocation
    private HashMap<String, IODExit> exits; // Key = CodeLocation
    private HashMap<String, IODNew> news;
    private HashMap<String, OnDrawStructure> structures;

    public IODManager() {
        this.enters = new HashMap<String, IODEnter>();
        this.exits = new HashMap<String, IODExit>();
        this.news = new HashMap<String, IODNew>();
        this.structures = new HashMap<String, OnDrawStructure>();
    }

    @Override
    public void checkStructures() {
        for (java.util.Map.Entry<String, OnDrawStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, OnDrawStructure> pair = (HashMap.Entry) stringStructureEntry;
            pair.getValue().checkStructure();
        }
    }

    public void addEnter(String key, IODEnter enter) {
        this.enters.put(key, enter);
    }

    public void addExit(String key, IODExit exit) {
        this.exits.put(key, exit);
    }

    public void addNew(String key, IODNew newElement) { this.news.put(key, newElement); }

    public void addStructure(String key, OnDrawStructure structure) {this.structures.put(key, structure);}

    public void executeEnter(String key, String id, long date) {
        //this.structures.put(id, this.enters.get(key).execute(id, date));
        /*
        System.out.println("Need : " + key + " and " + id);
        for (java.util.Map.Entry<String, OnDrawStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, OnDrawStructure> pair = (HashMap.Entry) stringStructureEntry;
            System.out.println("Structure : " + pair.getKey() + " " + pair.getValue().getId() + " has " + pair.getValue().getWorstInstantations());
        }
        */
        this.enters.get(key).execute(this.structures.get(id), date);
    }

    public void executeExit(String key, String id, long date) {
        this.exits.get(key).execute(this.structures.get(id), date);
    }

    public void executeNew(String key, String id) {
        this.news.get(key).execute(this.structures.get(id));
    }

    @Override
    public void generateCSV(String outputPath, String apkName, String packageName, boolean returnAllInstances) throws IOException {
        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }

        //Print for coverage
        if (returnAllInstances) {
            File coverageOutputfile = new File(outputPath + "coverage.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(coverageOutputfile, true))) {
                writer.write("Number of IOD methods," + this.enters.size() + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }

            File executionOutputFile = new File(outputPath + "execution.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(executionOutputFile, true))) {
                int executionSumMethod=0;
                for (Map.Entry<String, IODEnter> executioncountEntry : this.enters.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumMethod++;
                    }
                }

                writer.write("Number of IOD methods," + executionSumMethod + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }
        }

        File csvOutputFile = new File(outputPath+"results_IOD.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk,package,file,method,average executing time,worst executing time,average number of instantiations,worst number of instantiations \n");
            for (java.util.Map.Entry<String, OnDrawStructure> stringStructureEntry : this.structures.entrySet()) {
                HashMap.Entry<String, OnDrawStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    OnDrawStructure structure =  pair.getValue();
                    writer.write(apkName+ ","+ packageName +","+fileName+","+methodName+ ","+structure.getAverageTime()+","+structure.getWorstTime()+","+structure.getAverageInstantiations()+","+structure.getWorstInstantations()+"\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }

        if (returnAllInstances) {
            File csvOutputFileAll = new File(outputPath + "results_IOD_all.csv");
            try (PrintWriter writer = new PrintWriter(csvOutputFileAll)) {
                writer.write("apk,package,file,method,average executing time,worst executing time,average number of instantiations,worst number of instantiations \n");
                for (java.util.Map.Entry<String, OnDrawStructure> stringStructureEntry : this.structures.entrySet()) {
                    HashMap.Entry<String, OnDrawStructure> pair = (HashMap.Entry) stringStructureEntry;
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    OnDrawStructure structure = pair.getValue();
                    writer.write(apkName + "," + packageName + "," + fileName + "," + methodName + "," + structure.getAverageTime() + "," + structure.getWorstTime() + "," + structure.getAverageInstantiations() + "," + structure.getWorstInstantations() + "\n");
                }
            } catch (FileNotFoundException e) {
                // Do something
            }
        }
    }

    @Override
    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        if ("iodenter".equals(code)) {
            executeEnter(key, fileName, Long.parseLong(id));
        } else if ("iodexit".equals(code)) {
            executeExit(key, fileName, Long.parseLong(id));
        } else if ("iodnew".equals(code)) {
            executeNew(key, fileName);
        }
    }

    @Override
    public void beepBeepBranch(Fork codesmellsFork, int arity) {

        Filter filter = BeepBeepUtils.codesmellConditions(codesmellsFork, arity, new String[]{":iodenter:", ":iodexit:", ":iodnew:"});

        GroupProcessor IODDetector = new GroupProcessor(1, 1);
        {
            Fork forkLTL = new Fork(6);

            ApplyFunction equalEnter = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            new FunctionTree(new NthElement(2), StreamVariable.X), new Constant("iodenter")));
            connect(forkLTL, 0, equalEnter, 0);

            Fork forkEnter = new Fork(2);
            connect(equalEnter, OUTPUT, forkEnter, INPUT);

            Filter filterEnter = new Filter();
            connect(forkLTL, 1, filterEnter, LEFT);
            connect(forkEnter, 0, filterEnter, RIGHT);

            ApplyFunction equalExit = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            new FunctionTree(new NthElement(2), StreamVariable.X),
                            new Constant("iodexit")));
            connect(forkLTL, 2, equalExit, 0);

            Fork forkExit = new Fork(3);
            connect(equalExit, OUTPUT, forkExit, INPUT);

            Filter filterExit = new Filter();
            connect(forkLTL, 3, filterExit, LEFT);
            connect(forkExit, 0, filterExit, RIGHT);

            ApplyFunction checkValues = new ApplyFunction(
                    new FunctionTree(Numbers.isGreaterOrEqual,
                            new FunctionTree(Numbers.subtraction,
                                    new FunctionTree(new NthElement(3), StreamVariable.Y),
                                    new FunctionTree(new NthElement(3), StreamVariable.X)),
                            new Constant(16666666.6667)));
            connect(filterEnter, OUTPUT, checkValues, 0);
            connect(filterExit, OUTPUT, checkValues, 1);

            Eventually mediumF = new Eventually();
            connect(forkExit, 1, mediumF, INPUT);

            ApplyFunction mediumConjunction = new ApplyFunction(
                    new FunctionTree(Booleans.and,
                            new FunctionTree(Equals.instance,
                                    new FunctionTree(new NthElement(2), StreamVariable.X),
                                    new Constant("iodenter")),
                            StreamVariable.Y));
            connect(forkLTL, 4, mediumConjunction, 0);
            connect(mediumF, OUTPUT, mediumConjunction, 1);

            ApplyFunction bigConjunction = new ApplyFunction(
                    new FunctionTree(Booleans.and, StreamVariable.X, StreamVariable.Y));
            connect(mediumConjunction, OUTPUT, bigConjunction, 0);
            connect(checkValues, OUTPUT, bigConjunction, 1);

            Eventually bigF = new Eventually();
            connect(bigConjunction, OUTPUT, bigF, INPUT);

            ApplyFunction equalNew = new ApplyFunction(
                    new FunctionTree(Booleans.not, new FunctionTree(
                            Equals.instance, new FunctionTree(
                            new NthElement(2), StreamVariable.X),
                            new Constant("iodnew"))));
            connect(forkLTL, 5, equalNew, 0);

            Until mediumUntil = new Until();
            connect(equalNew, OUTPUT, mediumUntil, LEFT);
            connect(forkExit, 2, mediumUntil, RIGHT);

            ApplyFunction notUntil = new ApplyFunction(
                    new FunctionTree(Booleans.not,
                            StreamVariable.X));
            connect(mediumUntil, OUTPUT, notUntil, INPUT);

            ApplyFunction secondConjunction = new ApplyFunction(
                    new FunctionTree(Booleans.and,
                            StreamVariable.X,
                            StreamVariable.Y));
            connect(forkEnter, 1, secondConjunction, 0);
            connect(notUntil, OUTPUT, secondConjunction, 1);

            Eventually secondBigF = new Eventually();
            connect(secondConjunction, secondBigF);

            ApplyFunctionPartial bigDisjunction = new ApplyFunctionPartial(
                    new FunctionTree(Booleans.or,
                            StreamVariable.X,
                            StreamVariable.Y));
            connect(bigF, OUTPUT, bigDisjunction, 0);
            connect(secondBigF, OUTPUT, bigDisjunction, 1);

            IODDetector.addProcessor(forkLTL);
            IODDetector.addProcessor(equalEnter);
            IODDetector.addProcessor(forkEnter);
            IODDetector.addProcessor(filterEnter);
            IODDetector.addProcessor(equalExit);
            IODDetector.addProcessor(forkExit);
            IODDetector.addProcessor(filterExit);
            IODDetector.addProcessor(checkValues);
            IODDetector.addProcessor(mediumF);
            IODDetector.addProcessor(mediumConjunction);
            IODDetector.addProcessor(bigF);
            IODDetector.addProcessor(bigConjunction);
            IODDetector.addProcessor(equalNew);
            IODDetector.addProcessor(mediumUntil);
            IODDetector.addProcessor(notUntil);
            IODDetector.addProcessor(secondConjunction);
            IODDetector.addProcessor(secondBigF);
            IODDetector.addProcessor(bigDisjunction);
            IODDetector.associateInput(INPUT, forkLTL, INPUT);
            IODDetector.associateOutput(OUTPUT, bigDisjunction, OUTPUT);
        }

        ApplyFunction splitter = new ApplyFunction(new Strings.SplitString(":"));
        connect(filter, OUTPUT, splitter, INPUT);

        Slice slicer = new Slice(new NthElement(0), IODDetector);
        connect(splitter, OUTPUT, slicer, 0);

        KeepLast lastSlice = new KeepLast();
        connect(slicer, lastSlice);

        HashMap<String, Boolean> slicedHashMap = new HashMap<>();
        Pullable p3 = lastSlice.getPullableOutput();
        try  {
            slicedHashMap = (HashMap)p3.pull();
        } catch (NullPointerException e) {

        }

        System.out.println("IOD : ");

        Iterator it2 = slicedHashMap.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pair = (Map.Entry)it2.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            if ((Boolean)pair.getValue()) {
                String locationSplit = ((String)pair.getKey());
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
                OnDrawStructure structure = new OnDrawStructure(location, ((String)pair.getKey()));
                structure.foundCodeSmell();
                structures.put(((String)pair.getKey()), structure);
                System.out.println(pair.getKey() + " is a code smell");
            }
        }
    }
}
