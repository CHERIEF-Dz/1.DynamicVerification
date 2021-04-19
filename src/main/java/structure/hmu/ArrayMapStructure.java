package structure.hmu;

import utils.CodeLocation;

public class ArrayMapStructure extends MapStructure {

    public static int bigArrayMapTreshold=400;
    public boolean isSimple;

    public ArrayMapStructure(CodeLocation implementation, String id, String name, boolean isSimple) {
        super(implementation, id, name);
        this.actualSize = 0;
        this.structureImplementation = implementation;
        this.isSimple = isSimple;
    }

    @Override
    public void addElement() {
        super.addElement();
        if (!this.codeSmellFound) {
            if (this.maximumSize > bigArrayMapTreshold) {
                System.out.println("ArrayMap defined " + this.structureImplementation.toString() + " has HMU code smell (Too much elements).");
                this.foundCodeSmell();
            }
        }
    }
}
