package manager;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.functions.*;
import ca.uqac.lif.cep.ltl.Eventually;
import ca.uqac.lif.cep.ltl.Globally;
import ca.uqac.lif.cep.ltl.Next;
import ca.uqac.lif.cep.ltl.Until;
import ca.uqac.lif.cep.tmf.*;
import ca.uqac.lif.cep.util.*;
import events.dw.DWAcquire;
import events.dw.DWRelease;
import soot.jimple.infoflow.android.iccta.App;
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
        if (this.structures.containsKey(id)) {
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
        Fork forkDwDetection = new Fork(4);
        connect(filter, OUTPUT, forkDwDetection, INPUT);

        FindPattern getLocationPart = new FindPattern("([^:]*:[^:]*):[^:]*:[^:]*");
        connect(forkDwDetection, 1, getLocationPart, INPUT);

        Fork locationFork = new Fork(2);
        connect(getLocationPart, locationFork);


        FindPattern getIDPart = new FindPattern("[^:]*:[^:]*:[^:]*:([^:]*)");
        connect(forkDwDetection, 2, getIDPart, INPUT);

        Fork idFork = new Fork(2);
        connect(getIDPart, idFork);

        Maps.PutInto locationMap = new Maps.PutInto();
        connect(idFork, 0, locationMap, 0);
        connect(locationFork, 0, locationMap, 1);

        KeepLast lastLocation = new KeepLast();
        connect(locationMap, lastLocation);

        QueueSource dummyEvent = new QueueSource();
        dummyEvent.setEvents(":dwend:");

        ApplyFunction concat1 = new ApplyFunction(
                new FunctionTree(Strings.concat, StreamVariable.X, StreamVariable.Y));
        connect(locationFork, 1, concat1, 0);
        connect(dummyEvent, OUTPUT, concat1, 1);

        ApplyFunction concat2 = new ApplyFunction(
                new FunctionTree(Strings.concat, StreamVariable.X, StreamVariable.Y));
        connect(concat1, OUTPUT, concat2, 0);
        connect(idFork, 1, concat2, 1);


        GroupProcessor dwDetector = new GroupProcessor(1, 1);
        {
            ApplyFunction getEvent = new ApplyFunction(new NthElement(2));

            Fork forkLTL = new Fork(2);
            connect(getEvent, forkLTL);


            ApplyFunction equalAcquire = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                                    StreamVariable.X, new Constant("dwacq")));
            connect(forkLTL, 0, equalAcquire, INPUT);

            Fork forkAcquire = new Fork(3);
            connect(equalAcquire, forkAcquire);

            ApplyFunction equalRelease = new ApplyFunction(
                    new FunctionTree(Equals.instance,
                            StreamVariable.X, new Constant("dwrel")));
            connect(forkLTL, 1, equalRelease, INPUT);

            Fork forkRelease = new Fork(1);
            connect(equalRelease, forkRelease);

            ApplyFunction neg = new ApplyFunction(new FunctionTree(Booleans.not, StreamVariable.X));
            connect(forkAcquire, 0, neg, INPUT);

            Until midUntil = new Until();
            connect(neg, OUTPUT, midUntil, 0);
            connect(forkRelease, 0, midUntil, 1);

            Next midNext = new Next();
            connect(midUntil, midNext);

            ApplyFunctionPartial implies = new ApplyFunctionPartial(
                    new FunctionTree(Booleans.implies, StreamVariable.X, StreamVariable.Y));
            connect(forkAcquire, 1, implies, 0);
            connect(midNext, OUTPUT, implies, 1);


            ApplyFunction negImplies = new ApplyFunction(
                    new FunctionTree(Booleans.not, StreamVariable.X)
            );
            connect(implies, negImplies);



            Globally bigG = new Globally();
            connect(negImplies, bigG);

            /*
            ApplyFunction bigNeg = new ApplyFunction(
                    new FunctionTree(Booleans.not, StreamVariable.X)
            );
            connect(bigG, bigNeg);
            */


            Fork forkTrue = new Fork(2);
            connect(forkAcquire, 2, forkTrue, INPUT);
            Filter filterTrue = new Filter();
            connect(forkTrue, 0, filterTrue, LEFT);
            connect(forkTrue, 1, filterTrue, RIGHT);

            Multiplex outputMultiplex = new Multiplex(2);
            connect(filterTrue, OUTPUT, outputMultiplex, 0);
            connect(bigG, OUTPUT, outputMultiplex, 1);



            dwDetector.addProcessor(getEvent);
            dwDetector.addProcessor(forkLTL);
            dwDetector.addProcessor(neg);
            dwDetector.addProcessor(equalAcquire);
            dwDetector.addProcessor(equalRelease);
            dwDetector.addProcessor(forkAcquire);
            dwDetector.addProcessor(forkRelease);
            dwDetector.addProcessor(midUntil);
            dwDetector.addProcessor(midNext);
            dwDetector.addProcessor(implies);

            dwDetector.addProcessor(negImplies);

            dwDetector.addProcessor(bigG);

            //dwDetector.addProcessor(bigNeg);

            dwDetector.addProcessor(forkTrue);
            dwDetector.addProcessor(filterTrue);
            dwDetector.addProcessor(outputMultiplex);

            dwDetector.associateInput(INPUT, getEvent, INPUT);
            dwDetector.associateOutput(OUTPUT, outputMultiplex, OUTPUT);
        }

        Multiplex splitterPlex = new Multiplex(2);
        connect(forkDwDetection, 0, splitterPlex, 0);
        connect(concat2, OUTPUT, splitterPlex, 1);

        ApplyFunction splitter = new ApplyFunction(new Strings.SplitString(":"));
        connect(splitterPlex, OUTPUT, splitter, INPUT);

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

        Iterator it1 = locationHashMap.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry pair = (Map.Entry)it1.next();
            if ((slicedHashMap.containsKey(Integer.parseInt((String)pair.getKey())) && (Boolean) slicedHashMap.get(Integer.parseInt((String)pair.getKey())))
            || !slicedHashMap.containsKey(Integer.parseInt((String)pair.getKey()))) {
                //String[] locationSplit = locationHashMap.get(Integer.toString((Integer) pair.getKey())).split(":");
                String[] locationSplit = ((String)pair.getValue()).split(":");
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
                WakeLockStructure structure = new WakeLockStructure(location, (String)pair.getKey());
                structure.foundCodeSmell();
                structures.put((String)pair.getValue(), structure);
                System.out.println(pair.getValue()+ " is a code smell");
            }
        }

/*
        Iterator it2 = slicedHashMap.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pair = (Map.Entry)it2.next();
            System.out.println(pair.getKey().getClass() + " " + pair.getKey() + " = " + pair.getValue());
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

 */

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
