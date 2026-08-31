package command;

import converter.UnsignedIntAndNonZeroConverter;
import core.ExpenseTracker;
import picocli.CommandLine;

import java.io.IOException;
import java.util.NoSuchElementException;

@CommandLine.Command(name = "delete", description = "Delete expense item.")
public class DeleteCommand implements Runnable {
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntAndNonZeroConverter.class)
    Integer id;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.delete(id);
        } catch (NoSuchElementException e) {
            System.out.printf("Expense delete failed. (%s)", e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
