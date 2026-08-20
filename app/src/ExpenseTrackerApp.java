import picocli.CommandLine;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ExpenseTracker {
    // 지출 리스트 출력
    private static class Expense {
        Integer id;
        Integer categoryId;
        LocalDate date;
        String description;
        Integer amount;
    }
    private final List<Expense> expenses = new ArrayList<>();

    // 지출 예산
    private final int[] monthlyBudget = new int[12];

    // 지출 카테고리
    static class Category {
        private final Map<Integer, String> categoryMap = new HashMap<>();

        void add(String categoryName) { System.out.printf("Create Category %s\n", categoryName); }
        void list() { System.out.println("Listing all Category."); }
        void update(Integer id, String categoryName) { System.out.printf("Update Category id:%d, name:%s\n", id, categoryName); }
        void delete(Integer id) { System.out.printf("Delete Category id:%d", id); }
    }
    public final Category categories = new Category();

    // 메서드
    void add(String description, Integer amount) {
        System.out.printf("Added description:%s amount:%d.\n", description, amount);
    }

    void get(Integer id) {
        System.out.printf("Get id:%d\n", id);
    }

    void list() {
        System.out.printf("print list\n");
    }

    void list(String categoryName) {
        System.out.printf("print list (filtered category %s).\n", categoryName);
    }

    void update(Integer id, String description, Integer amount) {
        System.out.printf("Update Expense id:%d, description:\"%s\", amount:%d\n", id, description, amount);
    }

    void delete(Integer id) {
        System.out.printf("Delete Expense id:%d\n", id);
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

    void exportCSV(Path fileName) {
        System.out.printf("Export CSV to Path: %s", fileName);
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
    ExportCommand.class,
    CategoryCommand.class
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

    @Override
    public void run() {
        new ExpenseTracker().add(description, amount);
    }
}

@CommandLine.Command(name = "get", description = "Get current expense item from list.")
class GetCommand implements Runnable {
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntConverter.class)
    Integer id;

    @Override
    public void run() { new ExpenseTracker().get(id);}
}

@CommandLine.Command(name = "list", description = "List current expense items.")
class ListCommand implements Runnable {
    @CommandLine.Option(names = "--category")
    String Category = "";

    @Override
    public void run() {
        ExpenseTracker app = new ExpenseTracker();
        if(Category.isEmpty()) app.list();
        else app.list(Category);
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
    public void run() { new ExpenseTracker().update(id, description, amount); }
}

@CommandLine.Command(name = "delete", description = "Delete expense item.")
class DeleteCommand implements Runnable {
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntConverter.class)
    Integer id;

    @Override
    public void run() { new ExpenseTracker().delete(id); }
}

@CommandLine.Command(name = "summary", description = "Print Summary of expense")
class SummaryCommand implements Runnable {
    @CommandLine.Option(names = "--month", converter = Range1To12Converter.class)
    Integer month;

    @Override
    public void run() {
        ExpenseTracker app = new ExpenseTracker();
        if (month == null) app.summary();
        else app.summary(Month.of(month));
    }
}

@CommandLine.Command(name = "budget", description = "Set monthly budget")
class BudgetCommand implements Runnable {
    @CommandLine.Option(names="--month", required = true, converter = Range1To12Converter.class)
    Integer month;

    @CommandLine.Option(names="--amount", required = true, converter = UnsignedIntConverter.class)
    Integer amount;

    @Override
    public void run() { new ExpenseTracker().setBudget(Month.of(month), amount); }
}

@CommandLine.Command(name = "export", description = "export expense item to CSV Sheet.")
class ExportCommand implements Runnable {
    @CommandLine.Option(names = "--file", required = true)
    Path filePath;

    @Override
    public void run() { new ExpenseTracker().exportCSV(filePath); }
}

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
        public void run() { new ExpenseTracker().categories.add(categoryName); }
    }

    @CommandLine.Command(name = "list", description = "list category.")
    static class CategoryListCommand implements Runnable {
        @Override
        public void run() { new ExpenseTracker().categories.list(); }
    }

    @CommandLine.Command(name = "update", description = "update category item.")
    static class CategoryUpdateCommand implements Runnable {
        @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntConverter.class)
        Integer id;

        @CommandLine.Option(names = "--name", required = true)
        String categoryName;

        @Override
        public void run() { new ExpenseTracker().categories.update(id, categoryName); }
    }

    @CommandLine.Command(name = "delete", description = "delete category item.")
    static class CategoryDeleteCommand implements Runnable {
        @CommandLine.Option(names = "--id", required = true)
        Integer id;

        @Override
        public void run() { new ExpenseTracker().categories.delete(id); }
    }

}