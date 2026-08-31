package command;

import converter.Range1To12Converter;
import converter.UnsignedBigDecimalCostConverter;
import core.ExpenseTracker;
import picocli.CommandLine;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Month;

@CommandLine.Command(name = "budget", description = "Set Monthly Budget")
public class BudgetCommand implements Runnable {
    @CommandLine.Option(names = "--month", required = true, description = "Budget of Month", converter = Range1To12Converter.class)
    Integer month;

    @CommandLine.Option(names = "--amount", required = true, description = "Budget Amount", converter = UnsignedBigDecimalCostConverter.class)
    BigDecimal amount;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.setBudget(Month.of(month), amount);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
