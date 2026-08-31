package command;

import converter.Range1To12Converter;
import core.ExpenseTracker;
import picocli.CommandLine;

import java.time.Month;

@CommandLine.Command(name = "summary", description = "Print Summary of expense")
public class SummaryCommand implements Runnable {
    @CommandLine.Option(names = "--month", converter = Range1To12Converter.class)
    Integer month;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            if (month == null) c.summary();
            else c.summary(Month.of(month));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
