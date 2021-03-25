package structure.nlmr;

import structure.Structure;
import utils.CodeLocation;

public class NLMRStructure implements Structure {
    protected CodeLocation structureImplementation;
    protected String id;
    protected boolean codeSmellFound;
    private long begin, end;

    public NLMRStructure(CodeLocation implementation, String id) {
        this.structureImplementation = implementation;
        this.id = id;
        this.codeSmellFound=false;
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
        if (((this.end-this.begin)/1000000.0) > (5000)) {
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

    public void begin(long memory) {
        this.begin = memory;
    }

    public void end(long memory) {
        this.end = memory;
    }
}
