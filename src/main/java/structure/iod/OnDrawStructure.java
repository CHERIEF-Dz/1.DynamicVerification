package structure.iod;

import structure.Structure;
import utils.CodeLocation;

import java.util.Calendar;
import java.util.Date;

public class OnDrawStructure implements Structure {


    protected CodeLocation structureImplementation;
    protected String id;
    protected boolean codeSmellFound;
    private String begin, end;

    public OnDrawStructure(CodeLocation implementation, String id) {
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
        /*
        if (this.locked) {
            System.out.println("WakeLock defined " + this.structureImplementation.toString() + " has DW code smell (Lock not released).");
            this.foundCodeSmell();
        }
        */
    }

    @Override
    public String getId() {
        return this.id;
    }

    public CodeLocation getLocation() {
        return this.structureImplementation;
    }

    public void begin(String date) {
       //this.begin = Calendar.getInstance().
        this.begin = date;
    }

    public void end(String date) {
        this.end = date;
    }
}
