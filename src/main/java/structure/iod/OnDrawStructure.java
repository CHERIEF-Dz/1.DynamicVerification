package structure.iod;

import structure.Structure;
import utils.CodeLocation;

public class OnDrawStructure implements Structure {

    protected CodeLocation structureImplementation;
    protected String id;
    protected boolean codeSmellFound;
    private long begin, end;
    private int nbInstantiations;

    public OnDrawStructure(CodeLocation implementation, String id) {
        this.structureImplementation = implementation;
        this.id = id;
        this.codeSmellFound=false;
        this.nbInstantiations = 0;
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
        System.out.println("Structure : " + this.id + " has : " + this.nbInstantiations);
        if (((this.end-this.begin)/1000000.0) > ((1000)/60.0)) {
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
        this.begin = date;
    }

    public void end(long date) {
        this.end = date;
    }

    public void newInstance() {this.nbInstantiations++;}
}
