package structure;

import structure.Structure;
import utils.CodeLocation;

public class WakeLockStructure implements Structure {
    boolean locked;
    protected CodeLocation structureImplementation;
    protected String id;
    protected boolean codeSmellFound;

    public WakeLockStructure(CodeLocation implementation, String id) {
        this.structureImplementation = implementation;
        this.id = id;
        this.locked=false;
        this.codeSmellFound=false;
    }
    @Override
    public void foundCodeSmell() {
        this.codeSmellFound=true;
    }

    public void acquire() {
        this.locked=true;
    }

    public void release() {
        this.locked=false;
    }

    @Override
    public boolean hasCodeSmell() {
        return this.codeSmellFound;
    }

    @Override
    public void checkStructure() {
        if (this.locked) {
            System.out.println("WakeLock defined " + this.structureImplementation.toString() + " has DW code smell (Lock not released).");
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
}
