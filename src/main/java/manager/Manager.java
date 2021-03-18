package manager;

public interface Manager {
    void checkStructures();
    void generateCSV(String outputPath);
    String getBreakpoints();
}
