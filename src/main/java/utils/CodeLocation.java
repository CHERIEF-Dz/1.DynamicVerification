package utils;

public class CodeLocation {
    private String path;
    private String fileName;
    private int line;

    public CodeLocation(String path, String fileName, int line) {
        this.path = path;
        this.fileName = fileName;
        this.line = line;
    }

    public int getLine() {return this.line;}

    public String getFileName() {return this.fileName;}

    public String getPath() {return this.path;}

    public String toString() {
        return "line " + line + " in " + fileName + " at " + path;
    }
}
