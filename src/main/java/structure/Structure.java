package structure;

import utils.CodeLocation;

public interface Structure {
    void foundCodeSmell();
    boolean hasCodeSmell();
    void checkStructure();
    CodeLocation getLocation();
    String getId();
}
