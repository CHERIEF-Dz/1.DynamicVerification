package structure;

import utils.CodeLocation;

public abstract class MapStructure implements Structure {

    protected int actualSize;
    protected CodeLocation structureImplementation;
    protected String id;
    protected boolean codeSmellFound;
    protected String name;
    protected int maximumSize;

    public MapStructure(CodeLocation implementation, String id, String name) {
        this.actualSize = 0;
        this.maximumSize=0;
        this.structureImplementation = implementation;
        this.id = id;
        this.name = name;
        this.codeSmellFound=false;
    }

    public void addElement() {
        actualSize++;
        if (actualSize>maximumSize)
            maximumSize=actualSize;
    }

    public void setElements(int size) {
        actualSize=size;
        if (actualSize>maximumSize)
            maximumSize=actualSize;
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
        }
    public int getMaximumSize() {return this.maximumSize;}

    public String getName() {return this.name;}

    public String getId() {return this.id;}

    public CodeLocation getLocation() {
        return this.structureImplementation;
    }
}
