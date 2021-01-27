package actions.hmu;

import actions.ConcreteAction;
import structure.Structure;
import utils.CodeLocation;

public class HMUDeletion extends ConcreteAction {

    private String variableName;

    public HMUDeletion(CodeLocation location, String variableName) {
        super(location);
        this.variableName=variableName;
    }

    public void execute(Structure linkedStructure) {
        linkedStructure.deleteElement();
    }

    @Override
    public String generateBreakPoint() {
        String key= this.location.getFileName()+":"+this.location.getLine();
        String tag = "<line-breakpoint enabled=\"true\" suspend=\"NONE\" type=\"java-line\">\n" +
                "   <url>file://$PROJECT_DIR$"+this.location.getPath()+"</url>\n" +
                "   <line>"+(this.location.getLine()-1)+"</line>\n" +
                "   <log-expression expression=\"&quot;"+key+":del:&quot; + System.identityHashCode("+this.variableName+")\" language=\"JAVA\" />\n" +
                "   <properties />\n" +
                "   <option name=\"timeStamp\" value=\"8\" />\n" +
                "</line-breakpoint>";
        return tag;
    }
}
