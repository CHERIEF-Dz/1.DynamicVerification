package actions.hmu;

import actions.ConcreteAction;
import structure.ArrayMapStructure;
import structure.HashMapStructure;
import structure.SimpleArrayMapStructure;
import structure.Structure;
import utils.CodeLocation;

public class HMUImplementation extends ConcreteAction {

    private String type;
    private String variableName;

    public HMUImplementation(CodeLocation location, String type, String variableName) {
        super(location);
        this.type = type;
        this.variableName = variableName;
    }

    public Structure execute(String id) {
        //System.out.println("Test[Impl] : " + this.location.toString());
        Structure toReturn = null;
        if ("HashMap".equals(type)) {
            toReturn = new HashMapStructure(this.location, id);
        } else if ("ArrayMap".equals(type)) {
            toReturn = new ArrayMapStructure(this.location, id);
        } else if ("SimpleArrayMap".equals(type)) {
            toReturn = new SimpleArrayMapStructure(this.location, id);
        }
        return toReturn;
    }

    @Override
    public String generateBreakPoint() {
        String key= this.location.getFileName()+":"+this.location.getLine();
        String tag = "<line-breakpoint enabled=\"true\" suspend=\"NONE\" type=\"java-line\">\n" +
                "   <url>file://$PROJECT_DIR$"+this.location.getPath()+"</url>\n" +
                "   <line>"+(this.location.getLine())+"</line>\n" +
                "   <log-expression expression=\"&quot;"+key+":impl:&quot; + System.identityHashCode("+this.variableName+")\" language=\"JAVA\" />\n" +
                "   <properties />\n" +
                "   <option name=\"timeStamp\" value=\"8\" />\n" +
                "</line-breakpoint>";
        return tag;
    }
}
