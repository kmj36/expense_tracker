import picocli.CommandLine;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

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
    }
    private static class ExpensesData {
        public Integer sequence;
        public List<Expense> expenses;

        ExpensesData() {
            sequence = 1;
            expenses = new ArrayList<>();
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    private final ExpensesData expenseManager;

    ExpenseTracker() throws IOException {
        Path file = Path.of(String.format("%s/%s", saveDir, dataJSON));

        // data.json 이 존재하지 않는 경우 expenseManager 초기화
        if(!Files.exists(file)) {
            expenseManager = new ExpensesData();
            return;
        }

        String fileData = Files.readString(file);
        expenseManager = mapper.readValue(fileData, new TypeReference<>(){});
    }

    @Override
    public void close() throws Exception {
        Path file = Path.of(String.format("%s/%s", saveDir, dataJSON));
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, expenseManager);
    }

    void add(String description, Integer amount) {
        Expense expense = new Expense();

        expense.id = expenseManager.sequence;
        expense.date = LocalDate.now();
        expense.description = description;
        expense.amount = amount;

        expenseManager.expenses.add(expense);
        ++expenseManager.sequence;

        System.out.printf("Expense added successfully (ID: %d)\n", expense.id);
    }

    void get(Integer id) {
        System.out.printf("Get id:%d\n", id);
    }

    void list() {
        System.out.printf("print list\n");
    }

    void list(Integer categoryId) {
        System.out.printf("print list (filtered category %d).\n", categoryId);
    }

    void update(Integer id, String description, Integer amount) {
        System.out.printf("Expense updated successfully (ID: %d)\n", id);
    }

    void delete(Integer id) {
        System.out.printf("Expense deleted successfully (ID: %d)", id);
    }

    void summary() {
        System.out.println("Summary Expense");
    }

    void summary(Month month) {
        String monthStr = month.toString();
        String firstLetter = monthStr.substring(0, 1);
        String remainLetter = monthStr.substring(1);
        System.out.printf("Summary Expense month: %s\n", firstLetter + remainLetter.toLowerCase());
    }

    void setBudget(Month month, Integer amount) {
        String monthStr = month.toString();
        String firstLetter = monthStr.substring(0, 1);
        String remainLetter = monthStr.substring(1);
        System.out.printf("set Budget for %s : %d", firstLetter + remainLetter.toLowerCase(), amount );
    }
}

@CommandLine.Command(name = "ExpenseTrackerApp", mixinStandardHelpOptions = true, subcommands = {
    AddCommand.class,
    GetCommand.class,
    ListCommand.class,
    UpdateCommand.class,
    DeleteCommand.class,
    SummaryCommand.class,
    BudgetCommand.class,
    //CategoryCommand.class
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

class UnsignedIntConverter implements CommandLine.ITypeConverter<Integer> {
    @Override
    public Integer convert(String s) throws Exception {
        return Integer.parseUnsignedInt(s);
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

    @CommandLine.Option(names = "--amount", required = true, converter = UnsignedIntConverter.class)
    Integer amount;

    @CommandLine.Option(names = "--category", converter = UnsignedIntConverter.class)
    Integer categoryId;

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
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntConverter.class)
    Integer id;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.get(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

@CommandLine.Command(name = "list", description = "List current expense items.")
class ListCommand implements Runnable {
    @CommandLine.Option(names = "--category", converter = UnsignedIntConverter.class)
    Integer CategoryId;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            if(CategoryId == null) c.list();
            else c.list(CategoryId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

@CommandLine.Command(name = "update", description = "Update expense item values.")
class UpdateCommand implements Runnable {
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntConverter.class)
    Integer id;

    @CommandLine.Option(names = "--description", required = true)
    String description;

    @CommandLine.Option(names = "--amount", required = true, converter = UnsignedIntConverter.class)
    Integer amount;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.update(id, description, amount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

@CommandLine.Command(name = "delete", description = "Delete expense item.")
class DeleteCommand implements Runnable {
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntConverter.class)
    Integer id;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.delete(id);
        } catch (Exception e) {
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

@CommandLine.Command(name = "budget", description = "Set monthly budget")
class BudgetCommand implements Runnable {
    @CommandLine.Option(names="--month", required = true, converter = Range1To12Converter.class)
    Integer month;

    @CommandLine.Option(names="--amount", required = true, converter = UnsignedIntConverter.class)
    Integer amount;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.setBudget(Month.of(month), amount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

/*
@CommandLine.Command(name = "category", mixinStandardHelpOptions = true, description = "a Categories.", subcommands = {
        CategoryCommand.CategoryAddCommand.class,
        CategoryCommand.CategoryListCommand.class,
        CategoryCommand.CategoryUpdateCommand.class,
        CategoryCommand.CategoryDeleteCommand.class
})
class CategoryCommand {
    @CommandLine.Command(name = "add", description = "create category item.")
    static class CategoryAddCommand implements Runnable {
        @CommandLine.Option(names="--name", required = true)
        String categoryName;

        @Override
        public void run() {
            try (ExpenseTracker c = new ExpenseTracker()) {
                c.categories.add(categoryName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "list", description = "list category.")
    static class CategoryListCommand implements Runnable {
        @Override
        public void run() {
            try (ExpenseTracker c = new ExpenseTracker()) {
                c.categories.list();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "update", description = "update category item.")
    static class CategoryUpdateCommand implements Runnable {
        @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntConverter.class)
        Integer id;

        @CommandLine.Option(names = "--name", required = true)
        String categoryName;

        @Override
        public void run() {
            try (ExpenseTracker c = new ExpenseTracker()) {
                c.categories.update(id, categoryName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "delete", description = "delete category item.")
    static class CategoryDeleteCommand implements Runnable {
        @CommandLine.Option(names = "--id", required = true)
        Integer id;

        @Override
        public void run() {
            try (ExpenseTracker c = new ExpenseTracker()) {
                c.categories.delete(id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
 */