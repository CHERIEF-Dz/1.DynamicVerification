package structure.hmu;

import utils.CodeLocation;

public class ArrayMapStructure extends MapStructure {

    public static int bigArrayMapTreshold=500;

    public ArrayMapStructure(CodeLocation implementation, String id) {
        super(implementation, id);
        this.actualSize = 0;
        this.structureImplementation = implementation;
    }

    @Override
    public void addElement() {
        super.addElement();
        if (!this.codeSmellFound) {
            if (this.actualSize > bigArrayMapTreshold) {
                System.out.println("ArrayMap defined " + this.structureImplementation.toString() + " has HMU code smell (Too much elements).");
                this.foundCodeSmell();
            }
        }
    }
}
