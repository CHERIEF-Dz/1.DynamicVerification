package structure.hmu;

import utils.CodeLocation;

public class SimpleArrayMapStructure extends MapStructure {

    public static int bigArrayMapTreshold=500;

    public SimpleArrayMapStructure(CodeLocation implementation, String id, String name) {
        super(implementation, id, name);
        this.actualSize = 0;
        this.structureImplementation = implementation;
    }

    @Override
    public void addElement() {
        super.addElement();
        if (!this.codeSmellFound) {
            if (this.actualSize > bigArrayMapTreshold) {
                System.out.println("SimpleArrayMap defined at " + this.structureImplementation.toString() + " has HMU code smell (Too much elements).");
                this.foundCodeSmell();
            }
        }
    }
}
