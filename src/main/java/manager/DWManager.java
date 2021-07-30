package manager;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.functions.*;
import ca.uqac.lif.cep.ltl.Eventually;
import ca.uqac.lif.cep.ltl.Globally;
import ca.uqac.lif.cep.tmf.*;
import ca.uqac.lif.cep.util.*;
import events.dw.DWAcquire;
import events.dw.DWRelease;
import structure.MapStructure;
import structure.WakeLockStructure;
import utils.BeepBeepUtils;
import utils.CodeLocation;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.uqac.lif.cep.Connector.*;

public class DWManager implements Manager, Cloneable {
    private HashMap<String, DWAcquire> acquires;
    private HashMap<String, DWRelease> releases;
    private HashMap<String, WakeLockStructure> structures;

    public DWManager() {
        this.acquires = new HashMap<String, DWAcquire>();
        this.releases = new HashMap<String, DWRelease>();
        this.structures = new HashMap<String, WakeLockStructure>();
    }

    public void addAcquire(String key, DWAcquire acquire) {
        this.acquires.put(key, acquire);
    }

    public void addRelease(String key, DWRelease release) {
        this.releases.put(key, release);
    }

    public void executeAcquire(String key, String id) {
        this.structures.put(id, this.acquires.get(key).execute(id));
    }

    public void executeRelease(String key, String id) {
        // If there is a release without acquire, no need
        if (this.structures.get(id)!=null) {
            this.releases.get(key).execute(this.structures.get(id));
        }
    }

    public void checkStructures() {
        for (java.util.Map.Entry<String, WakeLockStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, WakeLockStructure> pair = (HashMap.Entry) stringStructureEntry;
            pair.getValue().checkStructure();
        }
    }

    public void generateCSV(String outputPath, String apkName, String packageName, boolean returnAllInstances) throws IOException {

        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }

        //Print for coverage
        if (returnAllInstances) {
            File coverageOutputfile = new File(outputPath + "coverage.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(coverageOutputfile, true))) {
                writer.write("Number of DW Acquires," + this.acquires.size() + "\n");
                writer.write("Number of DW Releases," + this.releases.size() + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }

            File executionOutputFile = new File(outputPath + "execution.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(executionOutputFile, true))) {
                int executionSumAcquire=0;
                for (Map.Entry<String, DWAcquire> executioncountEntry : this.acquires.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumAcquire++;
                    }
                }
                int executionSumRelease=0;
                for (Map.Entry<String, DWRelease> executioncountEntry : this.releases.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumRelease++;
                    }
                }
                writer.write("Number of DW Acquires," + executionSumAcquire + "\n");
                writer.write("Number of DW Releases," + executionSumRelease + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }
        }

        HashMap<String, WakeLockStructure> selectedMaps = new HashMap<String, WakeLockStructure>();

        for (java.util.Map.Entry<String, WakeLockStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, WakeLockStructure> pair = (HashMap.Entry) stringStructureEntry;
            String fileName = pair.getValue().getLocation().getFileName();
            String keyNumber = Integer.toString(pair.getValue().getLocation().getLine());
            String methodName = pair.getValue().getLocation().getMethodName();
            String selectionKey = fileName + "/" + packageName + "/" + methodName+"/"+keyNumber;
            if (!selectedMaps.containsKey(selectionKey)) {
                selectedMaps.put(selectionKey, pair.getValue());
            }
        }

        File csvOutputFile = new File(outputPath+"results_DW.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk, package, file, method\n");
            for (java.util.Map.Entry<String, WakeLockStructure> stringStructureEntry : selectedMaps.entrySet()) {
                HashMap.Entry<String, WakeLockStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write(apkName+ ","+ packageName +","+fileName+","+methodName+"\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }

        if (returnAllInstances) {
            File csvOutputFileAll = new File(outputPath + "results_DW_all.csv");
            try (PrintWriter writer = new PrintWriter(csvOutputFileAll)) {
                writer.write("apk, package, file, method,code smell\n");
                for (java.util.Map.Entry<String, WakeLockStructure> stringStructureEntry : selectedMaps.entrySet()) {
                    HashMap.Entry<String, WakeLockStructure> pair = (HashMap.Entry) stringStructureEntry;
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write(apkName + "," + packageName + "," + fileName + "," + methodName + "," + pair.getValue().hasCodeSmell() + "\n");
                }
            } catch (FileNotFoundException e) {
                // Do something
            }
        }
    }

    @Override
    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        if ("dwacq".equals(code)) {
            executeAcquire(key, id);
        } else if ("dwrel".equals(code)) {
            executeRelease(key, id);
        }
    }

    @Override
    public void beepBeepBranch(Fork codesmellsFork, int arity) {

        Filter filter = BeepBeepUtils.codesmellConditions(codesmellsFork, arity, new String[]{":dwacq:", ":dwrel:"});
        Fork forkDwDetection = new Fork(3);
        connect(filter, OUTPUT, forkDwDetection, INPUT);

        FindPattern getLocationPart = new FindPattern("([^:]*:[^:]*):[^:]*:[^:]*");
        connect(forkDwDetection, 1, getLocationPart, INPUT);


        FindPattern getIDPart = new FindPattern("[^:]*:[^:]*:[^:]*:([^:]*)");
        connect(forkDwDetection, 2, getIDPart, INPUT);


        Maps.PutInto locationMap = new Maps.PutInto();
        connect(getIDPart, 0, locationMap, 0);
        connect(getLocationPart, OUTPUT, locationMap, 1);

        KeepLast lastLocation = new KeepLast();
        connect(locationMap, lastLocation);


        GroupProcessor dwDetector = new GroupProcessor(1, 1);
        {
            ApplyFunction getEvent = new ApplyFunction(new NthElement(2));


            Fork forkLTL = new Fork(3);
            connect(getEvent, OUTPUT, forkLTL, INPUT);

            ApplyFunction neg = new ApplyFunction(
                    new FunctionTree(Booleans.not,
                            new FunctionTree(Equals.instance,
                                    StreamVariable.X, new Constant("dwrel"))));
            connect(forkLTL, 0, neg, 0);

            Globally mediumG = new Globally();
            connect(neg, mediumG);

            ApplyFunctionPartial andFunction = new ApplyFunctionPartial(
                    new FunctionTree(Booleans.and,
                            new FunctionTree(Equals.instance,
                                    StreamVariable.X, new Constant("dwacq")),
                            StreamVariable.Y));
            connect(forkLTL, 1, andFunction, 0);
            connect(mediumG, OUTPUT, andFunction, 1);

            ApplyFunction negAnd = new ApplyFunction(
                    new FunctionTree(Booleans.not, StreamVariable.X)
            );
            connect(andFunction, negAnd);

            Eventually finalEventually = new Eventually();
            connect(negAnd,finalEventually);

            ApplyFunction negEventually = new ApplyFunction(
                    new FunctionTree(Booleans.not, StreamVariable.X)
            );
            connect(finalEventually, negEventually);

            ApplyFunction acquireTruefier = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            StreamVariable.X, new Constant("dwacq")));
            connect(forkLTL, 2, acquireTruefier, 0);

            Fork forkTruefier = new Fork(2);
            connect(acquireTruefier, forkTruefier);
            Filter filterTruefier = new Filter();
            connect(forkTruefier, 0, filterTruefier, LEFT);
            connect(forkTruefier, 1, filterTruefier, RIGHT);

            Multiplex outputMultiplex = new Multiplex(2);
            connect(filterTruefier, OUTPUT, outputMultiplex, 0);
            connect(negEventually, OUTPUT, outputMultiplex, 1);


            dwDetector.addProcessor(getEvent);
            dwDetector.addProcessor(forkLTL);
            dwDetector.addProcessor(neg);
            dwDetector.addProcessor(mediumG);
            dwDetector.addProcessor(andFunction);
            dwDetector.addProcessor(negAnd);
            dwDetector.addProcessor(finalEventually);
            dwDetector.addProcessor(negEventually);

            dwDetector.addProcessor(acquireTruefier);
            dwDetector.addProcessor(forkTruefier);
            dwDetector.addProcessor(filterTruefier);
            dwDetector.addProcessor(outputMultiplex);

            dwDetector.associateInput(INPUT, getEvent, INPUT);
            dwDetector.associateOutput(OUTPUT, outputMultiplex, OUTPUT);
        }

        ApplyFunction splitter = new ApplyFunction(new Strings.SplitString(":"));
        connect(forkDwDetection, 0, splitter, INPUT);

        Slice slicer = new Slice(new NthElement(3), dwDetector);
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


        System.out.println("DW : ");
        Iterator it2 = slicedHashMap.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pair = (Map.Entry)it2.next();
            //System.out.println(pair.getKey().getClass() + " " + pair.getKey() + " = " + pair.getValue());
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
                WakeLockStructure structure = new WakeLockStructure(location, Integer.toString((Integer) pair.getKey()));
                structure.foundCodeSmell();
                structures.put(Integer.toString((Integer) pair.getKey()), structure);
                System.out.println(locationHashMap.get(Integer.toString((Integer) pair.getKey())) + " is a code smell");
            }
        }

    }

    public void mergeManager(DWManager otherManager) {
        for (java.util.Map.Entry<String, WakeLockStructure> otherEntry : otherManager.structures.entrySet()) {
            if (this.structures.containsKey(otherEntry.getKey())) {
                WakeLockStructure thisStructure = this.structures.get(otherEntry.getKey());
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
        DWManager newManager = new DWManager();
        newManager.acquires = (HashMap<String, DWAcquire>) this.acquires.clone();
        newManager.releases = (HashMap<String, DWRelease>) this.releases.clone();
        newManager.structures = (HashMap<String, WakeLockStructure>) this.structures.clone();
        return newManager;
    }
}
