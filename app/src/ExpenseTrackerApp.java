import command.*;
import picocli.CommandLine;

@CommandLine.Command(name = "ExpenseTrackerApp", mixinStandardHelpOptions = true, subcommands = {
    AddCommand.class,
    GetCommand.class,
    ListCommand.class,
    UpdateCommand.class,
    DeleteCommand.class,
    SummaryCommand.class,
    CategoryCommand.class,
    ExportCommand.class,
    BudgetCommand.class
})
public class ExpenseTrackerApp implements Runnable {
    static void main(String... args) {
        int exitCode = new CommandLine(new ExpenseTrackerApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}