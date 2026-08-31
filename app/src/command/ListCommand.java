package command;

import converter.Range1To12Converter;
import converter.UnsignedIntAndNonZeroConverter;
import core.ExpenseTracker;
import picocli.CommandLine;

import java.time.Month;
import java.util.NoSuchElementException;

@CommandLine.Command(name = "list", description = "List current expense items.")
public class ListCommand implements Runnable {
    @CommandLine.ArgGroup(exclusive = true)
    ExclusiveOptions exclusiveOptions;

    static class ExclusiveOptions {
        @CommandLine.Option(names = "--categoryID", converter = UnsignedIntAndNonZeroConverter.class)
        Integer categoryId;

        @CommandLine.Option(names = "--month", converter = Range1To12Converter.class)
        Integer month;
    }

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            if (exclusiveOptions == null) {
                c.list();
            } else if (exclusiveOptions.categoryId != null) {
                c.list(exclusiveOptions.categoryId);
            } else if (exclusiveOptions.month != null) {
                c.list(Month.of(exclusiveOptions.month));
            } else
                c.list();
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
