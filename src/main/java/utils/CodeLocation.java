package utils;

public class CodeLocation {
    private String path;
    private String fileName;
    private String methodName;
    private int line;

    public CodeLocation(String fileName, String methodName, int line) {
        this.fileName = fileName;
        this.line = line;
        this.methodName = methodName;
    }

    public int getLine() {return this.line;}

    public String getFileName() {return this.fileName;}

    public String getPath() {return this.path;}

    public String getMethodName() {return this.methodName;}

    public String toString() {
        if (line != 0) {
            return "at line " + line + " in class " + fileName + " and method " + methodName;
        }
        else {
            return "in class " + fileName + " and method " + methodName;
        }
    }
}
