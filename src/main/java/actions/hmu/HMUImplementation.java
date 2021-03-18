package actions.hmu;

import actions.ConcreteAction;
import structure.hmu.ArrayMapStructure;
import structure.hmu.HashMapStructure;
import structure.hmu.MapStructure;
import structure.hmu.SimpleArrayMapStructure;
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

    public MapStructure execute(String id) {
        //System.out.println("Test[Impl] : " + this.location.toString());
        MapStructure toReturn = null;
        if ("HashMap".equals(type)) {
            toReturn = new HashMapStructure(this.location, id, this.variableName);
        } else if ("ArrayMap".equals(type)) {
            toReturn = new ArrayMapStructure(this.location, id, this.variableName);
        } else if ("SimpleArrayMap".equals(type)) {
            toReturn = new SimpleArrayMapStructure(this.location, id, this.variableName);
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
