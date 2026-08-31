package command;

import converter.UnsignedBigDecimalCostConverter;
import converter.UnsignedIntAndNonZeroConverter;
import core.ExpenseTracker;
import picocli.CommandLine;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.NoSuchElementException;

@CommandLine.Command(name = "update", description = "Update expense item values.")
public class UpdateCommand implements Runnable {
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntAndNonZeroConverter.class)
    Integer id;

    @CommandLine.Option(names = "--description", required = false)
    String description;

    @CommandLine.Option(names = "--amount", required = false, converter = UnsignedBigDecimalCostConverter.class)
    BigDecimal amount;

    @CommandLine.Option(names = "--categoryID", required = false, converter = UnsignedIntAndNonZeroConverter.class)
    Integer categoryID;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.update(id, description, amount, categoryID);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
