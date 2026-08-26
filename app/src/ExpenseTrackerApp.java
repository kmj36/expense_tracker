import picocli.CommandLine;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

class ExpenseTracker implements AutoCloseable {
    private static final Path saveDir = Path.of("data");
    private static final Path dataJSON = Path.of("expenses.json");
    private static class Expense {
        public Integer id;
        public LocalDate date;
        public String description;
        public Integer amount;

        public Integer getId() {
            return id;
        }

        public Integer getAmount() {
            return amount;
        }
    }
    private static class ExpensesData {
        public List<Expense> expenses;

        ExpensesData() {
            expenses = new ArrayList<>();
        }

        public int nextId() {
            return expenses.stream()
                    .mapToInt(Expense::getId)
                    .max()
                    .orElse(0) + 1;
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    private ExpensesData expenseManager;

    ExpenseTracker() throws IOException {
        Path file = Path.of(String.format("%s/%s", saveDir, dataJSON));

        // data.json 이 존재하지 않는 경우 expenseManager 초기화
        if(!Files.exists(file)) {
            expenseManager = new ExpensesData();
            return;
        }

        String fileData = Files.readString(file);
        try {
            expenseManager = mapper.readValue(fileData, new TypeReference<>(){});
        } catch (JacksonException e) {
            expenseManager = new ExpensesData();
        }
    }

    @Override
    public void close() {
        Path file = Path.of(String.format("%s/%s", saveDir, dataJSON));
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, expenseManager);
    }

    void add(String description, Integer amount) {
        Expense expense = new Expense();

        expense.id = expenseManager.nextId();
        expense.date = LocalDate.now();
        expense.description = description;
        expense.amount = amount;

        expenseManager.expenses.add(expense);

        System.out.printf("Expense added successfully (ID: %d)%n", expense.id);
    }

    void get(Integer id) {
        Optional<Expense> expense = expenseManager.expenses.stream()
                .filter(c -> c.id.equals(id))
                .findFirst();

        if (expense.isPresent()) {
            Expense e = expense.get();

            System.out.printf("%-3s %-12s %-12s %s%n", "ID", "Date", "Description", "Amount");
            System.out.printf("%-3d %-12s %-12s $%d%n", e.id, e.date, e.description, e.amount);
        } else
            throw new NoSuchElementException(String.format("Expense Not Found (ID: %d)", id));
    }

    void list() {
        System.out.printf("%-3s %-12s %-12s %s%n", "ID", "Date", "Description", "Amount");
        for(Expense expense : expenseManager.expenses)
            System.out.printf("%-3d %-12s %-12s $%d%n", expense.id, expense.date, expense.description, expense.amount);
    }

    void update(Integer id, String description, Integer amount) throws IndexOutOfBoundsException {
        Optional<Expense> expense = expenseManager.expenses.stream()
                .filter(c -> c.id.equals(id))
                .findFirst();

        if (expense.isPresent()) {
            Expense e = expense.get();

            e.description = description;
            e.amount = amount;

            System.out.printf("Expense updated successfully (ID: %d)%n", id);
        } else
            throw new NoSuchElementException(String.format("Expense Not Found (ID: %d)", id));
    }

    void delete(Integer id) {
        boolean removed = expenseManager.expenses.removeIf(e -> e.id.equals(id));

        if(removed)
            System.out.printf("Expense deleted successfully (ID: %d)%n", id);
        else
            throw new NoSuchElementException(String.format("Expense Not Found (ID: %d)", id));
    }

    void summary() {
        Integer total = expenseManager.expenses.stream()
                .mapToInt(Expense::getAmount)
                .sum();

        System.out.printf("Total expenses: $%d%n", total);
    }

    void summary(Month month) {
        Integer totalMonth = expenseManager.expenses.stream()
                .filter(c -> c.date.getMonth().equals(month))
                .mapToInt(Expense::getAmount)
                .sum();

        String monthStr = month.toString();
        String firstLetter = monthStr.substring(0, 1);
        String remainLetter = monthStr.substring(1);
        System.out.printf("Total expenses for %s: $%d%n", firstLetter + remainLetter.toLowerCase(), totalMonth);
    }

    void exportCSV(String fileName) {
        if (!fileName.matches("[a-zA-Z0-9_\\-]+")) {
            throw new IllegalArgumentException("fileName contains invalid characters");
        }

        Path exportDir = Path.of(String.format("%s/%s.csv", saveDir, fileName));

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(exportDir.toFile()))) {
            if(!Files.exists(saveDir))
                Files.createDirectory(saveDir);

            bw.write("ID,Date,Description,Amount\n");
            for(Expense expense : expenseManager.expenses) {
                bw.write(expense.id.toString());
                bw.write(',');
                bw.write(expense.date.toString());
                bw.write(',');
                bw.write(expense.description);
                bw.write(',');
                bw.write(expense.amount.toString());
                bw.write('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

@CommandLine.Command(name = "ExpenseTrackerApp", mixinStandardHelpOptions = true, subcommands = {
    AddCommand.class,
    GetCommand.class,
    ListCommand.class,
    UpdateCommand.class,
    DeleteCommand.class,
    SummaryCommand.class,
    ExportCommand.class
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

class UnsignedIntAndNonZeroConverter implements CommandLine.ITypeConverter<Integer> {
    @Override
    public Integer convert(String s) throws Exception {
        int parse = Integer.parseUnsignedInt(s);
        if(parse < 1)
            throw new IllegalArgumentException("A number greater than 0 required.");
        return parse;
    }
}
class Range1To12Converter implements CommandLine.ITypeConverter<Integer> {
    @Override
    public Integer convert(String value) throws Exception {
        int num = Integer.parseInt(value);
        if (num < 1 || num > 12) {
            throw new IllegalArgumentException("A month between 1 and 12 is required.");
        }
        return num;
    }
}

@CommandLine.Command(name = "add", description = "Add new expense item.")
class AddCommand implements Runnable {
    @CommandLine.Option(names = "--description", required = true)
    String description;

    @CommandLine.Option(names = "--amount", required = true, converter = UnsignedIntAndNonZeroConverter.class)
    Integer amount;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.add(description, amount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

@CommandLine.Command(name = "get", description = "Get current expense item from list.")
class GetCommand implements Runnable {
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntAndNonZeroConverter.class)
    Integer id;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.get(id);
        } catch (NoSuchElementException  e) {
            System.out.printf("Get expense failed. (Reason: %s)", e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

@CommandLine.Command(name = "list", description = "List current expense items.")
class ListCommand implements Runnable {
    @CommandLine.Option(names = "--category", converter = UnsignedIntAndNonZeroConverter.class)
    Integer CategoryId;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.list();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

@CommandLine.Command(name = "update", description = "Update expense item values.")
class UpdateCommand implements Runnable {
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntAndNonZeroConverter.class)
    Integer id;

    @CommandLine.Option(names = "--description", required = true)
    String description;

    @CommandLine.Option(names = "--amount", required = true, converter = UnsignedIntAndNonZeroConverter.class)
    Integer amount;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.update(id, description, amount);
        } catch (NoSuchElementException e) {
            System.out.printf("Expense update failed. (%s)", e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

@CommandLine.Command(name = "delete", description = "Delete expense item.")
class DeleteCommand implements Runnable {
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

@CommandLine.Command(name = "summary", description = "Print Summary of expense")
class SummaryCommand implements Runnable {
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

@CommandLine.Command(name = "export", description = "Export expenses to csv file.")
class ExportCommand implements Runnable {
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