package events.hmu;

import events.ConcreteEvent;
import structure.MapStructure;
import utils.CodeLocation;

public class HMUAddition extends ConcreteEvent {

    private boolean alreadyWarned;
    private String variableName;

    public HMUAddition(CodeLocation location, String variableName) {
        super(location);
        this.alreadyWarned=false;
        this.variableName=variableName;
    }

    public void execute(MapStructure linkedStructure) {
        this.isExecuted=true;
        //System.out.println("Test[Add] : " + this.location.toString());
        linkedStructure.addElement();
        if (linkedStructure.hasCodeSmell() && !alreadyWarned && this.location.getLine() != 0) {
            System.out.println("Due to the addition " + this.location.toString());
            alreadyWarned=true;
        }
        else if (linkedStructure.hasCodeSmell() && !alreadyWarned) {
            System.out.println("Due to the addition " + this.location.toString());
            alreadyWarned=true;
        }
    }

    @Override
    public String generateBreakPoint() {
        String key= this.location.getFileName()+":"+this.location.getLine();
        String tag = "<line-breakpoint enabled=\"true\" suspend=\"NONE\" type=\"java-line\">\n" +
                "   <url>file://$PROJECT_DIR$"+this.location.getPath()+"</url>\n" +
                "   <line>"+(this.location.getLine()-1)+"</line>\n" +
                "   <log-expression expression=\"&quot;"+key+":add:&quot; + System.identityHashCode("+this.variableName+")\" language=\"JAVA\" />\n" +
                "   <properties />\n" +
                "   <option name=\"timeStamp\" value=\"8\" />\n" +
                "</line-breakpoint>";
        return tag;
    }
}
