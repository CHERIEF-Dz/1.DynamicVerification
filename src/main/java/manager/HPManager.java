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
import events.hp.HPEnter;
import events.hp.HPExit;
import structure.HeavyProcessStructure;
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

public class HPManager implements Manager{

    private HashMap<String, HPEnter> enters; // Key = CodeLocation
    private HashMap<String, HPExit> exits; // Key = CodeLocation
    private HashMap<String, HeavyProcessStructure> structures;

    public HPManager() {
        this.enters = new HashMap<String, HPEnter>();
        this.exits = new HashMap<String, HPExit>();
        this.structures = new HashMap<String, HeavyProcessStructure>();
    }

    @Override
    public void checkStructures() {
        for (java.util.Map.Entry<String, HeavyProcessStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, HeavyProcessStructure> pair = (HashMap.Entry) stringStructureEntry;
            pair.getValue().checkStructure();
        }
    }

    @Override
    public void generateCSV(String outputPath, String apkName, String packageName, boolean returnAllInstances) throws IOException {
        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }

        //Print for coverage
        if (returnAllInstances) {
            int countHAS=0;
            int countHSS=0;
            int countHBR=0;
            int countHASexecuted=0;
            int countHSSexecuted=0;
            int countHBRexecuted=0;
            for (java.util.Map.Entry<String, HeavyProcessStructure> stringStructureEntry : this.structures.entrySet()) {
                HashMap.Entry<String, HeavyProcessStructure> pair = (HashMap.Entry) stringStructureEntry;
                System.out.println(pair.getValue().getLocation().getMethodName());
                String methodName = pair.getValue().getLocation().getMethodName();
                if (methodName.equals("onReceive")) {
                    countHBR++;
                }
                else if (methodName.equals("onStartCommand")) {
                    countHSS++;
                }
                else if (methodName.equals("onPreExecute") || methodName.equals("onProgressUpdate") || methodName.equals("onPostExecute")){
                    countHAS++;
                }
            }

            File coverageOutputfile = new File(outputPath + "coverage.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(coverageOutputfile, true))) {
                writer.write("Number of HBR methods," + countHBR + "\n");
                writer.write("Number of HAS methods," + countHAS + "\n");
                writer.write("Number of HSS methods," + countHSS + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }

            File executionOutputFile = new File(outputPath + "execution.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(executionOutputFile, true))) {
                for (Map.Entry<String, HPEnter> executioncountEntry : this.enters.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        String methodName = executioncountEntry.getValue().location.getMethodName();
                        if (methodName.equals("onReceive")) {
                            countHBRexecuted++;
                        } else if (methodName.equals("onStartCommand")) {
                            countHSSexecuted++;
                        } else if (methodName.equals("onPreExecute") || methodName.equals("onProgressUpdate") || methodName.equals("onPostExecute")) {
                            countHASexecuted++;
                        }
                    }
                }

                writer.write("Number of HBR methods," + countHBRexecuted + "\n");
                writer.write("Number of HAS methods," + countHASexecuted + "\n");
                writer.write("Number of HSS methods," + countHSSexecuted + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }
        }

        File csvOutputFile = new File(outputPath+"results_HP.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk,package,file,method,average executing time,worst executing time\n");
            for (java.util.Map.Entry<String, HeavyProcessStructure> stringStructureEntry : this.structures.entrySet()) {
                HashMap.Entry<String, HeavyProcessStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write(apkName+ ","+ packageName +","+fileName+","+methodName+ ","+pair.getValue().getAverageTime()+","+pair.getValue().getWorstTime()+"\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }

        if (returnAllInstances) {
            File csvOutputFileAll = new File(outputPath + "results_HP_all.csv");
            try (PrintWriter writer = new PrintWriter(csvOutputFileAll)) {
                writer.write("apk,package,file,method,average executing time,worst executing time,code smell\n");
                for (java.util.Map.Entry<String, HeavyProcessStructure> stringStructureEntry : this.structures.entrySet()) {
                    HashMap.Entry<String, HeavyProcessStructure> pair = (HashMap.Entry) stringStructureEntry;
                    if (pair.getValue().hasBeenExecuted()) {
                        String fileName = pair.getValue().getLocation().getFileName();
                        String methodName = pair.getValue().getLocation().getMethodName();
                        writer.write(apkName + "," + packageName + "," + fileName + "," + methodName + "," + pair.getValue().getAverageTime() + "," + pair.getValue().getWorstTime() + "," + pair.getValue().hasCodeSmell() + "\n");
                    }
                }
            } catch (FileNotFoundException e) {
                // Do something
            }
        }
    }

    @Override
    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        if ("hasenter".equals(code)) {
            //key = key.replace("$onPostExecute", "").replace("$onPreExecute","").replace("onProgressUpdate", "");
            fileName = fileName.replace("$onPostExecute", "").replace("$onPreExecute","").replace("$onProgressUpdate", "");
            executeEnter(key, fileName, Long.parseLong(id));
        } else if ("hasexit".equals(code)) {
            //key = key.replace("$onPostExecute", "").replace("$onPreExecute","").replace("onProgressUpdate", "");
            fileName = fileName.replace("$onPostExecute", "").replace("$onPreExecute","").replace("$onProgressUpdate", "");
            executeExit(key, fileName, Long.parseLong(id));
        } else if ("hbrenter".equals(code)) {
            executeEnter(key, fileName.replace("$onReceive",""), Long.parseLong(id));
        } else if ("hbrexit".equals(code)) {
            executeExit(key, fileName.replace("$onReceive",""), Long.parseLong(id));
        } else if ("hssenter".equals(code)) {
            executeEnter(key, fileName.replace("$onStartCommand",""), Long.parseLong(id));
        } else if ("hssexit".equals(code)) {
            executeExit(key, fileName.replace("$onStartCommand",""), Long.parseLong(id));
        }
    }

    public void addEnter(String key, HPEnter enter) {
        this.enters.put(key, enter);
    }

    public void addExit(String key, HPExit exit) {
        this.exits.put(key, exit);
    }

    public void addStructure(String key, HeavyProcessStructure structure) {this.structures.put(key, structure);}

    public void executeEnter(String key, String id, long date) {
        //this.structures.put(id, this.enters.get(key).execute(id, date));
/*
        System.out.println("Need : " + key + " and " + id);
        for (java.util.Map.Entry<String, HPEnter> stringStructureEntry : this.enters.entrySet()) {
            HashMap.Entry<String, HPEnter> pair = (HashMap.Entry) stringStructureEntry;
            System.out.println("Enter : " + pair.getKey());
        }

        for (java.util.Map.Entry<String, HeavyProcessStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, HeavyProcessStructure> pair = (HashMap.Entry) stringStructureEntry;
            System.out.println("Structure : " + pair.getKey() + " " + pair.getValue().getId());
        }
*/
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
        HPBranch("HSS", "hssenter", "hssexit", codesmellsFork, arity);
        HPBranch("HBR", "hbrenter", "hbrexit", codesmellsFork, arity+1);
        HPBranch("HAS", "hasenter", "hasexit", codesmellsFork, arity+2);
    }
    public void HPBranch(String codeSmellName, String enter, String exit, Fork codesmellsFork, int arity) {

        Filter filter = BeepBeepUtils.codesmellConditions(codesmellsFork, arity, new String[]{enter, exit});

        GroupProcessor HPDetector = new GroupProcessor(1, 1);
        {
            Fork forkLTL = new Fork(5);

            ApplyFunction equalEnter = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            new FunctionTree(new NthElement(2), StreamVariable.X), new Constant(enter)));
            connect(forkLTL, 0, equalEnter, 0);

            Filter filterEnter = new Filter();
            connect(forkLTL, 1, filterEnter, LEFT);
            connect(equalEnter, OUTPUT, filterEnter, RIGHT);

            ApplyFunction equalExit = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            new FunctionTree(new NthElement(2), StreamVariable.X),
                            new Constant(exit)));
            connect(forkLTL, 2, equalExit, 0);

            Fork forkExit = new Fork(2);
            connect(equalExit, OUTPUT, forkExit, INPUT);

            Filter filterExit = new Filter();
            connect(forkLTL, 3, filterExit, LEFT);
            connect(forkExit, 0, filterExit, RIGHT);

            ApplyFunction checkValues = new ApplyFunction(
                    new FunctionTree(Numbers.isGreaterOrEqual,
                            new FunctionTree(Numbers.subtraction,
                                    new FunctionTree(new NthElement(3), StreamVariable.X),
                                    new FunctionTree(new NthElement(3), StreamVariable.Y)),
                            new Constant(100000000)));
            connect(filterEnter, OUTPUT, checkValues, 1);
            connect(filterExit, OUTPUT, checkValues, 0);

            Eventually mediumF = new Eventually();
            connect(forkExit, 1, mediumF, INPUT);

            ApplyFunction mediumConjunction = new ApplyFunction(
                    new FunctionTree(Booleans.and,
                            new FunctionTree(Equals.instance,
                                    new FunctionTree(new NthElement(2), StreamVariable.X),
                                    new Constant(enter)),
                            StreamVariable.Y));
            connect(forkLTL, 4, mediumConjunction, 0);
            connect(mediumF, OUTPUT, mediumConjunction, 1);

            ApplyFunction bigConjunction = new ApplyFunction(
                    new FunctionTree(Booleans.and, StreamVariable.X, StreamVariable.Y));
            connect(mediumConjunction, OUTPUT, bigConjunction, 0);
            connect(checkValues, OUTPUT, bigConjunction, 1);

            Eventually bigF = new Eventually();
            connect(bigConjunction, OUTPUT, bigF, INPUT);

            HPDetector.addProcessor(forkLTL);
            HPDetector.addProcessor(equalEnter);
            HPDetector.addProcessor(filterEnter);
            HPDetector.addProcessor(equalExit);
            HPDetector.addProcessor(forkExit);
            HPDetector.addProcessor(filterExit);
            HPDetector.addProcessor(checkValues);
            HPDetector.addProcessor(mediumF);
            HPDetector.addProcessor(mediumConjunction);
            HPDetector.addProcessor(bigF);
            HPDetector.addProcessor(bigConjunction);
            HPDetector.associateInput(INPUT, forkLTL, INPUT);
            HPDetector.associateOutput(OUTPUT, bigF, OUTPUT);
        }

        ApplyFunction splitter = new ApplyFunction(new Strings.SplitString(":"));
        connect(filter, OUTPUT, splitter, INPUT);

        Slice slicer = new Slice(new NthElement(0), HPDetector);
        connect(splitter, OUTPUT, slicer, 0);

        KeepLast lastSlice = new KeepLast();
        connect(slicer, lastSlice);

        HashMap<String, Boolean> slicedHashMap = new HashMap<>();
        Pullable p3 = lastSlice.getPullableOutput();
        try  {
            slicedHashMap = (HashMap)p3.pull();
        } catch (NullPointerException e) {

        }

        System.out.println(codeSmellName + " : ");

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
                HeavyProcessStructure structure = new HeavyProcessStructure(location, ((String)pair.getKey()));
                structure.foundCodeSmell();
                structures.put(((String)pair.getKey()), structure);
                System.out.println(pair.getKey() + " is a code smell");
            }
        }
    }

    public void mergeManager(HPManager otherManager) {
        for (java.util.Map.Entry<String, HeavyProcessStructure> otherEntry : otherManager.structures.entrySet()) {
            if (this.structures.containsKey(otherEntry.getKey())) {
                HeavyProcessStructure thisStructure = this.structures.get(otherEntry.getKey());
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

        HPManager newManager = new HPManager();
        newManager.enters = (HashMap<String, HPEnter>) this.enters.clone();
        newManager.exits = (HashMap<String, HPExit>) this.exits.clone();
        newManager.structures = (HashMap<String, HeavyProcessStructure>) this.structures.clone();
        return newManager;
    }
}
