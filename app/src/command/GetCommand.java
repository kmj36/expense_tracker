package command;

import converter.UnsignedIntAndNonZeroConverter;
import core.ExpenseTracker;
import picocli.CommandLine;

import java.io.IOException;
import java.util.NoSuchElementException;

@CommandLine.Command(name = "get", description = "Get current expense item from list.")
public class GetCommand implements Runnable {
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntAndNonZeroConverter.class)
    Integer id;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.get(id);
        } catch (NoSuchElementException e) {
            System.out.printf(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
