package structure.iod;

import structure.Structure;
import utils.CodeLocation;

public class OnDrawStructure implements Structure {

    protected CodeLocation structureImplementation;
    protected String id;
    protected boolean codeSmellFound;
    private long lastBegin, lastEnd;
    private int nbInstantiations;
    private long averageTime;
    private long worstTime;
    private long averageInstantiations;
    private long worstInstantations;
    private int nbCalls;

    public OnDrawStructure(CodeLocation implementation, String id) {
        this.structureImplementation = implementation;
        this.id = id;
        this.codeSmellFound=false;
        this.nbInstantiations = 0;
        this.worstTime=0;
        this.nbCalls=0;
        this.averageTime=0;
        this.averageInstantiations=0;
        this.worstInstantations=0;
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
        if (this.worstTime > ((1000)/60.0) || this.worstInstantations > 0) {
            System.out.println("onDraw defined " + this.structureImplementation.toString() + " has IOD code smell.");
            this.foundCodeSmell();
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    public CodeLocation getLocation() {
        return this.structureImplementation;
    }

    public void begin(long date) {
        this.lastBegin = date;
        this.nbInstantiations=0;
    }

    public void end(long date) {
        this.lastEnd = date;
        long elapsedTime = (long) ((this.lastEnd -this.lastBegin)/1000000.0);
        if (elapsedTime>worstTime)
            this.worstTime=elapsedTime;
        this.averageTime=(this.averageTime*this.nbCalls+elapsedTime)/(this.nbCalls+1);
        if (this.nbInstantiations>worstInstantations)
            this.worstInstantations=this.nbInstantiations;
        this.averageInstantiations=(this.averageInstantiations*this.nbCalls+this.nbInstantiations)/(this.nbCalls+1);
        this.nbCalls++;
    }

    public long getAverageTime() {return this.averageTime;}

    public long getWorstTime() {return this.worstTime;}

    public long getAverageInstantiations() {return this.averageInstantiations;}

    public long getWorstInstantations() {return this.worstInstantations;}

    public void newInstance() {this.nbInstantiations++;}
}
