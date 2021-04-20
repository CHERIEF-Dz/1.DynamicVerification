package structure.nlmr;

import structure.Structure;
import utils.CodeLocation;

public class NLMRStructure implements Structure {
    protected CodeLocation structureImplementation;
    protected String id;
    protected boolean codeSmellFound;
    private long lastBegin, lastEnd;
    private long averageMemory;
    private long betterMemory;
    private int nbCalls;
    private boolean hasBeenExecuted;

    public NLMRStructure(CodeLocation implementation, String id) {
        this.structureImplementation = implementation;
        this.id = id;
        this.codeSmellFound=false;
        this.betterMemory=0;
        this.nbCalls=0;
        this.averageMemory=0;
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
        if (this.betterMemory < 1024 && this.hasBeenExecuted) {
            this.foundCodeSmell();
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    public long getAverageMemory() {return this.averageMemory;}

    public long getBetterMemory() {return this.betterMemory;}

    public CodeLocation getLocation() {
        return this.structureImplementation;
    }

    public void begin(long memory) {
        this.lastBegin = memory;
        this.hasBeenExecuted=true;
    }

    public void end(long memory) {
        this.lastEnd = memory;
        long consumedMemory = this.lastBegin - this.lastEnd;
        if (consumedMemory>betterMemory)
            this.betterMemory=consumedMemory;
        this.averageMemory=(this.averageMemory*this.nbCalls+consumedMemory)/(this.nbCalls+1);
        this.nbCalls++;
    }
}
