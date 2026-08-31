package command;

import converter.UnsignedBigDecimalCostConverter;
import core.ExpenseTracker;
import picocli.CommandLine;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@CommandLine.Command(name = "add", description = "Add new expense item.")
public class AddCommand implements Runnable {
    @CommandLine.Option(names = "--description", required = true)
    String description;

    @CommandLine.Option(names = "--amount", required = true, converter = UnsignedBigDecimalCostConverter.class)
    BigDecimal amount;

    @CommandLine.Option(names = "--categoryID", required = false)
    Integer categoryID;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            if (categoryID == null)
                c.add(description, amount);
            else
                c.add(description, amount, categoryID);
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
