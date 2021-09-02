package structure;

import structure.Structure;
import utils.CodeLocation;

public class HeavyProcessStructure implements Structure {

    protected CodeLocation structureImplementation;
    protected String id;
    protected boolean codeSmellFound;
    private long lastBegin, lastEnd;
    private long averageTime;
    private long worstTime;
    private int nbCalls;
    private boolean hasBeenExecuted;

    public HeavyProcessStructure(CodeLocation implementation, String id) {
        this.structureImplementation = implementation;
        this.id = id;
        this.codeSmellFound=false;
        this.worstTime=0;
        this.nbCalls=0;
        this.averageTime=0;
        this.hasBeenExecuted=false;
    }

    @Override
    public void foundCodeSmell() {
        this.codeSmellFound=true;
    }

    @Override
    public boolean hasCodeSmell() {
        return this.codeSmellFound;
    }

    @Override
    public void checkStructure() {
        if (this.worstTime > (100) && this.hasBeenExecuted) {
            this.foundCodeSmell();
        }
    }
    public long getAverageTime() {return this.averageTime;}

    public long getWorstTime() {return this.worstTime;}

    @Override
    public String getId() {
        return this.id;
    }

    public CodeLocation getLocation() {
        return this.structureImplementation;
    }

    public void begin(long date) {
        this.lastBegin = date;
        this.hasBeenExecuted=true;
    }

    public boolean hasBeenExecuted() {
        return hasBeenExecuted;
    }

    public void end(long date) {
        this.lastEnd = date;
        long elapsedTime = (long) ((this.lastEnd -this.lastBegin)/1000000.0);
        if (elapsedTime>worstTime)
            this.worstTime=elapsedTime;
        this.averageTime=(this.averageTime*this.nbCalls+elapsedTime)/(this.nbCalls+1);
        this.nbCalls++;
    }
}
