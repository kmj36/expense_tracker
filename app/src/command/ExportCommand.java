package command;

import core.ExpenseTracker;
import picocli.CommandLine;

@CommandLine.Command(name = "export", description = "Export expenses to csv file.")
public class ExportCommand implements Runnable {
    @CommandLine.Option(names = "--fileName", required = true)
    String fileName;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.exportCSV(fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
