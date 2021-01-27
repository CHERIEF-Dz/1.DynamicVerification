package structure;

import utils.CodeLocation;

public class MapStructure implements Structure {

    protected int actualSize;
    protected CodeLocation structureImplementation;
    protected String id;
    protected boolean codeSmellFound;

    public MapStructure(CodeLocation implementation, String id) {
        this.actualSize = 0;
        this.structureImplementation = implementation;
        this.id = id;
        this.codeSmellFound=false;
    }

    public void addElement() {
        actualSize++;
    }

    public void deleteElement() {actualSize--; }

    public void cleanElements() {actualSize=0;}

    public void foundCodeSmell() {
        this.codeSmellFound=true;
    }

    public int getActualSize() {
        return this.actualSize;
    }

    public boolean hasCodeSmell() {
        return this.codeSmellFound;
    }

    public void checkStructure() {
        //
    }
}
