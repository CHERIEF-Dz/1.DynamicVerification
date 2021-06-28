package manager;

import events.hp.HPEnter;
import events.iod.IODEnter;
import events.iod.IODExit;
import events.iod.IODNew;
import structure.iod.OnDrawStructure;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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
}
