package structure.iod;

import structure.Structure;
import utils.CodeLocation;

import java.util.Calendar;
import java.util.Date;

public class OnDrawStructure implements Structure {


    protected CodeLocation structureImplementation;
    protected String id;
    protected boolean codeSmellFound;
    private long begin, end;
    private int nbInstanciations;

    public OnDrawStructure(CodeLocation implementation, String id) {
        this.structureImplementation = implementation;
        this.id = id;
        this.codeSmellFound=false;
        this.nbInstanciations = 0;
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
        /*
        if (this.locked) {
            System.out.println("WakeLock defined " + this.structureImplementation.toString() + " has DW code smell (Lock not released).");
            this.foundCodeSmell();
        }
        */
        if (((this.end-this.begin)/1000000.0) > ((1000)/60.0)) {
            this.foundCodeSmell();
        }
        //System.out.println("Coucou ! Début : " + this.begin + " fin : " + this.end + " différence : " + ((this.end-this.begin)/1000000.0) + " and inferior to " + ((1000)/60));
    }

    @Override
    public String getId() {
        return this.id;
    }

    public CodeLocation getLocation() {
        return this.structureImplementation;
    }

    public void begin(long date) {
       //this.begin = Calendar.getInstance().
        this.begin = date;
    }

    public void end(long date) {
        this.end = date;
    }

    public void newInstance() {this.nbInstanciations++;}
}
