package manager;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.ltl.Eventually;
import ca.uqac.lif.cep.ltl.Globally;
import ca.uqac.lif.cep.tmf.*;
import ca.uqac.lif.cep.util.*;
import events.hmu.HMUAddition;
import events.hmu.HMUClean;
import events.hmu.HMUDeletion;
import events.hmu.HMUImplementation;
import structure.ArrayMapStructure;
import structure.MapStructure;
import utils.BeepBeepUtils;
import utils.CodeLocation;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.uqac.lif.cep.Connector.*;

public class HMUManager implements Manager{
    private HashMap<String, HMUImplementation> implementations; // Key = CodeLocation
    private HashMap<String, HMUAddition> additions; // Key = CodeLocation
    private HashMap<String, HMUDeletion> deletions; // Key = CodeLocation
    private HashMap<String, HMUClean> cleans; // Key = CodeLocation
    private HashMap<String, MapStructure> structures; // Key = unique id

    public HMUManager() {
        this.implementations = new HashMap<String, HMUImplementation>();
        this.additions = new HashMap<String, HMUAddition>();
        this.deletions = new HashMap<String, HMUDeletion>();
        this.cleans = new HashMap<String, HMUClean>();
        this.structures = new HashMap<String, MapStructure>();
    }

    public void addImplementation(String key, HMUImplementation implementation) {
        //System.out.println(implementation.generateBreakPoint());
        this.implementations.put(key, implementation);
    }

    public void addAddition(String key, HMUAddition addition) {
        //System.out.println(addition.generateBreakPoint());
        this.additions.put(key, addition);
    }

    @Override
    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        if ("hmuimpl".equals(code)) {
            executeImplementation(key, id);
        } else if ("hmuadd".equals(code)) {
            executeAddition(key, id);
        } else if ("hmudel".equals(code)) {
            executeDeletion(key, id);
        } else if ("hmucln".equals(code)) {
            executeClean(key, id);
        }
    }

    public void addDeletion(String key, HMUDeletion deletion) {
        this.deletions.put(key, deletion);
    }

    public void addClean(String key, HMUClean clean) {
        this.cleans.put(key, clean);
    }

    public void executeImplementation(String key, String id) {
        this.structures.put(id, this.implementations.get(key).execute(id));
    }

    public void executeAddition(String key, String id) {
        this.additions.get(key).execute(this.structures.get(id));
    }

    public void executeDeletion(String key, String id) {
        this.deletions.get(key).execute(this.structures.get(id));
    }

    public void executeClean(String key, String id) {
        this.cleans.get(key).execute(this.structures.get(id));
    }

    public void checkStructures() {
        for (java.util.Map.Entry<String, MapStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, MapStructure> pair = (HashMap.Entry) stringStructureEntry;
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
            File coverageOutputfile = new File(outputPath + "coverage.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(coverageOutputfile, true))) {
                writer.write("Number of HMU Implementations," + this.implementations.size() + "\n");
                writer.write("Number of HMU Additions," + this.additions.size() + "\n");
                writer.write("Number of HMU Deletions," + this.deletions.size() + "\n");
                writer.write("Number of HMU Cleans," + this.cleans.size() + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }

            File executionOutputFile = new File(outputPath + "execution.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(executionOutputFile, true))) {
                int executionSumImplementation=0;
                for (Map.Entry<String, HMUImplementation> executioncountEntry : this.implementations.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumImplementation++;
                    }
                }
                int executionSumAddition=0;
                for (Map.Entry<String, HMUAddition> executioncountEntry : this.additions.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumAddition++;
                    }
                }
                int executionSumDeletion=0;
                for (Map.Entry<String, HMUDeletion> executioncountEntry : this.deletions.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumDeletion++;
                    }
                }
                int executionSumClean=0;
                for (Map.Entry<String, HMUClean> executioncountEntry : this.cleans.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumClean++;
                    }
                }
                writer.write("Number of HMU Implementations," + executionSumImplementation + "\n");
                writer.write("Number of HMU Additions," + executionSumAddition + "\n");
                writer.write("Number of HMU Deletions," + executionSumDeletion + "\n");
                writer.write("Number of HMU Cleans," + executionSumClean + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }
        }


        HashMap<String, MapStructure> selectedMaps = new HashMap<String, MapStructure>();
        HashMap<String, Integer> selectedMapsValues = new HashMap<String, Integer>();

        for (java.util.Map.Entry<String, MapStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, MapStructure> pair = (HashMap.Entry) stringStructureEntry;
                String fileName = pair.getValue().getLocation().getFileName();
                String keyNumber = Integer.toString(pair.getValue().getLocation().getLine());
                String methodName = pair.getValue().getLocation().getMethodName();
                String selectionKey = fileName + "/" + packageName + "/" + methodName+"/"+keyNumber;
                if (selectedMaps.containsKey(selectionKey)) {
                    if (selectedMapsValues.get(selectionKey) < pair.getValue().getMaximumSize()) {
                        selectedMaps.put(selectionKey, pair.getValue());
                        selectedMapsValues.put(selectionKey, pair.getValue().getMaximumSize());
                    }
                }
                else {
                    selectedMaps.put(selectionKey, pair.getValue());
                    selectedMapsValues.put(selectionKey, pair.getValue().getMaximumSize());
                }
        }

        File csvOutputFile = new File(outputPath+"results_HMU.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk,package,file,method,structure Type,maximumSize\n");
            for (java.util.Map.Entry<String, MapStructure> stringStructureEntry : selectedMaps.entrySet()) {
                HashMap.Entry<String, MapStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    String structureType = "HashMap";
                    if (pair.getValue() instanceof ArrayMapStructure) {
                        if (((ArrayMapStructure) pair.getValue()).isSimple) {
                            structureType = "SimpleArrayMap";
                        }
                        else {
                            structureType = "ArrayMap";
                        }
                    }
                    writer.write(apkName+ ","+ packageName +","+fileName+","+methodName+ "," + structureType + "," + pair.getValue().getMaximumSize()+ "\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }

        if (returnAllInstances) {
            File csvOutputFileAll = new File(outputPath + "results_HMU_all.csv");
            try (PrintWriter writer = new PrintWriter(csvOutputFileAll)) {
                writer.write("apk,package,file,method,structure Type,maximumSize,code smell\n");
                for (java.util.Map.Entry<String, MapStructure> stringStructureEntry : selectedMaps.entrySet()) {
                    HashMap.Entry<String, MapStructure> pair = (HashMap.Entry) stringStructureEntry;
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    String structureType = "HashMap";
                    if (pair.getValue() instanceof ArrayMapStructure) {
                        if (((ArrayMapStructure) pair.getValue()).isSimple) {
                            structureType = "SimpleArrayMap";
                        } else {
                            structureType = "ArrayMap";
                        }
                    }
                    writer.write(apkName + "," + packageName + "," + fileName + "," + methodName + "," + structureType + "," + pair.getValue().getMaximumSize() + "," + pair.getValue().hasCodeSmell() + "\n");
                }
            } catch (FileNotFoundException e) {
                // Do something
            }
        }
    }

    @Override
    public void beepBeepBranch(Fork codesmellsFork, int arity) {

        Filter filter = BeepBeepUtils.codesmellConditions(codesmellsFork, arity, new String[]{":hmuadd:", ":hmudel:", ":hmucln", ":hmuimpl:"});
        Fork forkDwDetection = new Fork(3);
        connect(filter, OUTPUT, forkDwDetection, INPUT);

        FindPattern getLocationPart = new FindPattern("([^:]*:[^:]*):[^:]*:[^:]*:[^:]*:[^:]*");
        connect(forkDwDetection, 1, getLocationPart, INPUT);


        FindPattern getIDPart = new FindPattern("[^:]*:[^:]*:[^:]*:([^:]*):[^:]*:[^:]*");
        connect(forkDwDetection, 2, getIDPart, INPUT);


        Maps.PutInto locationMap = new Maps.PutInto();
        connect(getIDPart, 0, locationMap, 0);
        connect(getLocationPart, OUTPUT, locationMap, 1);

        KeepLast lastLocation = new KeepLast();
        connect(locationMap, lastLocation);


        GroupProcessor hmuDetector = new GroupProcessor(1, 1);
        {
            Fork forkLTL = new Fork(6);


            ApplyFunction checkType = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            new FunctionTree(new NthElement(5), StreamVariable.X), new Constant("HashMap")));
            connect(forkLTL, 0, checkType, INPUT);

            ApplyFunction equalImpl = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            new FunctionTree(new NthElement(2), StreamVariable.X), new Constant("hmuimpl")));
            connect(forkLTL, 1, equalImpl, INPUT);

            Filter filterTruefier = new Filter();
            connect(checkType, OUTPUT, filterTruefier, LEFT);
            connect(equalImpl, OUTPUT, filterTruefier, RIGHT);





            ApplyFunction equalHashMap = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            new FunctionTree(new NthElement(5), StreamVariable.X), new Constant("HashMap")));
            connect(forkLTL, 2, equalHashMap, 0);

            Filter filterHashMap = new Filter();
            connect(forkLTL, 3, filterHashMap, LEFT);
            connect(equalHashMap, 0, filterHashMap, RIGHT);

            ApplyFunction checkHashMapSize = new ApplyFunction(
                    new FunctionTree(Numbers.isLessOrEqual,
                            new FunctionTree(new NthElement(4), StreamVariable.X),
                            new Constant(500)));
            connect(filterHashMap, OUTPUT, checkHashMapSize, 0);

            Globally bigG = new Globally();
            connect(checkHashMapSize, bigG);

            ApplyFunction equalArrayMap = new ApplyFunction(
                    new FunctionTree(Booleans.or,
                            new FunctionTree(Equals.instance,
                                    new FunctionTree(new NthElement(5), StreamVariable.X),
                                    new Constant("ArrayMap")),
                            new FunctionTree(Equals.instance,
                                    new FunctionTree(new NthElement(5), StreamVariable.X),
                                    new Constant("SimpleArrayMap"))));
            connect(forkLTL, 4, equalArrayMap, 0);

            Filter filterArrayMap = new Filter();
            connect(forkLTL, 5, filterArrayMap, LEFT);
            connect(equalArrayMap, 0, filterArrayMap, RIGHT);

            ApplyFunction checkArrayMapSize = new ApplyFunction(
                    new FunctionTree(Numbers.isGreaterOrEqual,
                            new FunctionTree(new NthElement(4), StreamVariable.X),
                            new Constant(500)));
            connect(filterArrayMap, OUTPUT, checkArrayMapSize, 0);

            Eventually bigF = new Eventually();
            connect(checkArrayMapSize, bigF);

            Multiplex outputMultiplex = new Multiplex(3);
            connect(filterTruefier, OUTPUT, outputMultiplex, 0);
            //connect(bigDisjunction, OUTPUT, outputMultiplex, 1);
            connect(bigF, OUTPUT, outputMultiplex, 1);
            connect(bigG, OUTPUT, outputMultiplex, 2);


            hmuDetector.addProcessor(forkLTL);

            hmuDetector.addProcessor(equalImpl);
            hmuDetector.addProcessor(checkType);
            hmuDetector.addProcessor(filterTruefier);


            hmuDetector.addProcessor(equalHashMap);
            hmuDetector.addProcessor(filterHashMap);
            hmuDetector.addProcessor(checkHashMapSize);
            hmuDetector.addProcessor(bigG);


            hmuDetector.addProcessor(equalArrayMap);
            hmuDetector.addProcessor(filterArrayMap);
            hmuDetector.addProcessor(checkArrayMapSize);
            hmuDetector.addProcessor(bigF);

            //hmuDetector.addProcessor(bigDisjunction);
            hmuDetector.addProcessor(outputMultiplex);

            hmuDetector.associateInput(INPUT, forkLTL, INPUT);
            hmuDetector.associateOutput(OUTPUT, outputMultiplex, OUTPUT);
        }

        ApplyFunction splitter = new ApplyFunction(new Strings.SplitString(":"));
        connect(forkDwDetection, 0, splitter, INPUT);

        Slice slicer = new Slice(new NthElement(3), hmuDetector);
        connect(splitter, OUTPUT, slicer, 0);

        KeepLast lastSlice = new KeepLast();
        connect(slicer, lastSlice);

        HashMap<String, String> locationHashMap = null;
        Pullable p1 = lastLocation.getPullableOutput();
        try {
            locationHashMap = (HashMap) p1.pull();
        } catch (NullPointerException e) {

        }

        HashMap<String, Boolean> slicedHashMap = new HashMap<>();
        Pullable p3 = lastSlice.getPullableOutput();
        try  {
            slicedHashMap = (HashMap)p3.pull();
        } catch (NullPointerException e) {

        }


        System.out.println("HMU : ");
        Iterator it2 = slicedHashMap.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pair = (Map.Entry)it2.next();
            if ((Boolean)pair.getValue()) {
                String[] locationSplit = locationHashMap.get(Integer.toString((Integer) pair.getKey())).split(":");
                Pattern pat = Pattern.compile("(.+\\.java)\\$(.*)");
                Matcher m = pat.matcher(locationSplit[0]);
                String fileName="";
                String methodName="";
                if (m.find()) {
                    fileName = m.group(1);
                    methodName = m.group(2);
                }
                int lineNumber = Integer.parseInt(locationSplit[1]);
                CodeLocation location = new CodeLocation(fileName, methodName, lineNumber);
                MapStructure structure = new ArrayMapStructure(location, Integer.toString((Integer) pair.getKey()), "", true);
                structure.foundCodeSmell();
                structures.put(Integer.toString((Integer) pair.getKey()), structure);
                System.out.println(locationHashMap.get(Integer.toString((Integer) pair.getKey())) + " is a code smell");
            }
        }

    }

    public void mergeManager(HMUManager otherManager) {
        for (Map.Entry<String, MapStructure> otherEntry : otherManager.structures.entrySet()) {
            if (this.structures.containsKey(otherEntry.getKey())) {
                MapStructure thisStructure = this.structures.get(otherEntry.getKey());
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
        HMUManager newManager = new HMUManager();
        newManager.additions = (HashMap<String, HMUAddition>) this.additions.clone();
        newManager.deletions = (HashMap<String, HMUDeletion>) this.deletions.clone();
        newManager.cleans = (HashMap<String, HMUClean>) this.cleans.clone();
        newManager.implementations = (HashMap<String, HMUImplementation>) this.implementations.clone();
        newManager.structures = (HashMap<String, MapStructure>) this.structures.clone();
        return newManager;
    }
}
