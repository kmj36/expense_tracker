import picocli.CommandLine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

class expenseTracker {
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
    private static class Budget {
        private final int[] monthlyBudget = new int[12];

        void set(Integer month, Integer amount) {

        }

        void list() {

        }

        void update(Integer month, Integer amount) {

        }

        void initZero(Integer month) {

        }
    }
    private final Budget monthlybudget = new Budget();

    // 지출 카테고리
    private static class Category {
        private final Map<Integer, String> categoryMap = new HashMap<>();

        void add(String name) {

        }

        void get(Integer id) {

        }

        void list() {

        }

        void update(Integer id, String name) {

        }

        void delete(Integer id) {

        }
    }
    private final Category categories = new Category();

    // 메서드
    void add(String description, Integer amount) {
        System.out.printf("Added description:%s amount:%d.\n", description, amount);
    }

    void get(Integer id) {
        System.out.printf("Get id:%d\n", id);
    }

    void update(Integer id, String description, Integer amount) {

    }

    void delete(Integer id) {

    }

    void list() {
        System.out.println("print list");
    }

    void list(String categoryName) {
        System.out.printf("print list (filtered category %s).\n", categoryName);
    }

    void summary() {

    }

    void summary(LocalDate month) {

    }

    void exportCSV(String fileName) {

    }
}

@CommandLine.Command(name = "ExpenseTrackerApp", subcommands = {
    AddCommand.class
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

@CommandLine.Command(name = "add", description = "Add new expense item.")
class AddCommand implements Runnable {
    @CommandLine.Option(names = "--description", required = true)
    String description;

    @CommandLine.Option(names = "--amount", required = true, converter = UnsignedIntConverter.class)
    Integer amount;

    @Override
    public void run() {
        new expenseTracker().add(description, amount);
    }
}

class UnsignedIntConverter implements CommandLine.ITypeConverter<Integer> {
    @Override
    public Integer convert(String s) throws Exception {
        return Integer.parseUnsignedInt(s);
    }
}