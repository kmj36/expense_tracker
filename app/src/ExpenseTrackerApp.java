import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import picocli.CommandLine;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Stream;

// 비용관리 트래커 클래스
class ExpenseTracker implements AutoCloseable {
    private static final Path saveDir = Path.of("data"); // 파일 저장 경로
    private static final Path dataJSON = Path.of("expenses.json"); // 비용 데이터 파일 명

    // 비용 컬럼용 클래스
    private static class Expense {
        public Integer id;
        public LocalDate date;
        public String description;
        public BigDecimal amount;
        public Integer categoryID;

        public Integer getId() {
            return id;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public Integer getCategoryID() {
            return categoryID;
        }
    }
    // 비용 데이터 저장(Wrapper) 클래스
    private static class ExpensesData {
        public List<Expense> expenses;
        public HashMap<Integer, String> categories;
        public BigDecimal[] monthlyBudget;

        ExpensesData() {
            expenses = new ArrayList<>();
            categories = new HashMap<>();
            monthlyBudget = new BigDecimal[12];
            Arrays.fill(monthlyBudget, BigDecimal.ZERO);
        }

        public int nextId() {
            return expenses.stream()
                    .mapToInt(Expense::getId)
                    .max()
                    .orElse(0) + 1;
        }

        public int nextCategoryId() {
            return categories.keySet().stream()
                    .max(Integer::compareTo)
                    .orElse(0) + 1;
        }

        public void printExpenseTable(List<Expense> exps) {
            String table = AsciiTable.getTable(AsciiTable.NO_BORDERS, exps, List.of(
                    new Column().header("ID")
                            .headerAlign(HorizontalAlign.CENTER)
                            .with(e -> String.valueOf(e.id)),
                    new Column().header("Category")
                            .headerAlign(HorizontalAlign.CENTER)
                            .with(e -> categories.get(e.categoryID)),
                    new Column().header("Date")
                            .headerAlign(HorizontalAlign.CENTER)
                            .with(e -> e.date.toString()),
                    new Column().header("Description")
                            .headerAlign(HorizontalAlign.CENTER)
                            .with(e -> e.description),
                    new Column().header("Amount")
                            .headerAlign(HorizontalAlign.CENTER)
                            .dataAlign(HorizontalAlign.RIGHT)
                            .with(e -> String.format("$%.2f", e.amount))
            ));

            System.out.println(table);
        }

        public void printCategoryTable() {
            String table = AsciiTable.getTable(AsciiTable.NO_BORDERS, categories.entrySet(), List.of(
                    new Column().header("ID")
                            .headerAlign(HorizontalAlign.CENTER)
                            .with(e -> String.valueOf(e.getKey())),
                    new Column().header("Category")
                            .headerAlign(HorizontalAlign.CENTER)
                            .with(Map.Entry::getValue)
            ));

            System.out.println(table);
        }

        public void checkBudget(Month month) {
            BigDecimal totalMonth = expenses.stream()
                    .filter(e -> e.date.getMonth().equals(month))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal budget = monthlyBudget[month.getValue() - 1];

            if (budget.compareTo(BigDecimal.ZERO) == 0) return;

            BigDecimal ratio = totalMonth.divide(budget, 4, RoundingMode.HALF_UP);
            BigDecimal percent = ratio.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP);

            if (ratio.compareTo(BigDecimal.ONE) >= 0) {
                BigDecimal over = totalMonth.subtract(budget);
                System.out.printf("[Exceeded: %s budget exceeded by $%.2f ($%.2f limit, spent $%.2f)]%n",
                        month, over, budget, totalMonth);
            } else if (ratio.compareTo(new BigDecimal("0.8")) >= 0) {
                System.out.printf("[Warning: %s is at %s%% of budget ($%.2f limit, spent $%.2f)]%n",
                        month, percent, budget, totalMonth);
            }
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

    void add(String description, BigDecimal amount) {
        Expense expense = new Expense();

        expense.id = expenseManager.nextId();
        expense.date = LocalDate.now();
        expense.description = description;
        expense.amount = amount;

        expenseManager.expenses.add(expense);

        System.out.printf("Expense added successfully (ID: %d)%n", expense.id);

        expenseManager.checkBudget(LocalDate.now().getMonth());
    }

    // 카테고리 필드 추가
    void add(String description, BigDecimal amount, Integer categoryID) {
        // 카테고리 키 사전 체크, 키가 없으면 Exception 발생.
        if(!expenseManager.categories.containsKey(categoryID))
            throw new NoSuchElementException(String.format("categoryID %d is Not found.", categoryID));

        Expense expense = new Expense();

        expense.id = expenseManager.nextId();
        expense.date = LocalDate.now();
        expense.description = description;
        expense.amount = amount;

        // 카테고리 필드 대입
        expense.categoryID = categoryID;

        expenseManager.expenses.add(expense);

        System.out.printf("Expense added successfully (ID: %d)%n", expense.id);

        expenseManager.checkBudget(LocalDate.now().getMonth());
    }

    void get(Integer id) {
        Optional<Expense> expense = expenseManager.expenses.stream()
                .filter(c -> c.id.equals(id))
                .findFirst();

        if (expense.isPresent()) {
            Expense e = expense.get();

            expenseManager.printExpenseTable(List.of(e));
        } else
            throw new NoSuchElementException(String.format("Expense Not Found (ID: %d)", id));
    }

    void list() {
        expenseManager.printExpenseTable(expenseManager.expenses);

        expenseManager.checkBudget(LocalDate.now().getMonth());
    }

    // 카테고리별 리스트 출력
    void list(Integer categoryID) {
        // 카테고리 키 사전 체크, 키가 없으면 Exception 발생.
        if(!expenseManager.categories.containsKey(categoryID))
            throw new NoSuchElementException(String.format("categoryID %d is Not found.", categoryID));

        Stream<Expense> FilteredExpenseStream = expenseManager.expenses.stream()
                .filter(c -> Objects.equals(c.categoryID, categoryID));

        System.out.printf("[Category - %s]%n", expenseManager.categories.get(categoryID));

        expenseManager.printExpenseTable(FilteredExpenseStream.toList());

        expenseManager.checkBudget(LocalDate.now().getMonth());
    }

    // 월별 리스트 출력
    void list(Month month) {
        Stream<Expense> FilteredExpenseStream = expenseManager.expenses.stream()
                .filter(c -> Objects.equals(c.date.getMonth(), month));

        expenseManager.printExpenseTable(FilteredExpenseStream.toList());

        expenseManager.checkBudget(month);
    }

    void update(Integer id, String description, BigDecimal amount, Integer categoryID) throws IndexOutOfBoundsException {
        if(description == null && amount == null && categoryID == null)
            throw new IllegalArgumentException("No changed.");

        Optional<Expense> expense = expenseManager.expenses.stream()
                .filter(c -> c.id.equals(id))
                .findFirst();

        if (expense.isPresent()) {
            Expense e = expense.get();

            if (description != null) e.description = description;
            if (amount != null) e.amount = amount;
            if (categoryID != null) e.categoryID = categoryID;

            System.out.printf("Expense updated successfully (ID: %d)%n", id);
        } else
            throw new NoSuchElementException(String.format("Expense Not Found (ID: %d)", id));

        expenseManager.checkBudget(LocalDate.now().getMonth());
    }

    void delete(Integer id) {
        boolean removed = expenseManager.expenses.removeIf(e -> e.id.equals(id));

        if(removed)
            System.out.printf("Expense deleted successfully (ID: %d)%n", id);
        else
            throw new NoSuchElementException(String.format("Expense Not Found (ID: %d)", id));

        expenseManager.checkBudget(LocalDate.now().getMonth());
    }

    void summary() {
        BigDecimal total = expenseManager.expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.printf("Total expenses: $%s%n", total);

        expenseManager.checkBudget(LocalDate.now().getMonth());
    }

    // 월별 비용 요약
    void summary(Month month) {
        BigDecimal totalMonth = expenseManager.expenses.stream()
                .filter(c -> c.date.getMonth().equals(month))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String monthStr = month.toString();
        String firstLetter = monthStr.substring(0, 1);
        String remainLetter = monthStr.substring(1);
        System.out.printf("Total expenses for %s: $%s%n", firstLetter + remainLetter.toLowerCase(), totalMonth);

        expenseManager.checkBudget(month);
    }

    void setBudget(Month month, BigDecimal budget) {
        expenseManager.monthlyBudget[month.getValue()-1] = budget;

        System.out.printf("%s Budget updated successfully (Amount: $%s)%n", month, budget);

        expenseManager.checkBudget(LocalDate.now().getMonth());
    }

    void addCategory(String category) {
        Integer key = expenseManager.nextCategoryId();
        expenseManager.categories.put(key, category);

        System.out.printf("Category '%s' added successfully (ID: %d)%n", category, key);
    }

    void listCategory() {
        expenseManager.printCategoryTable();
    }

    void updateCategory(Integer key, String value) {
        String replacedValue = expenseManager.categories.replace(key, value);
        if(replacedValue == null)
            throw new NoSuchElementException(String.format("categoryID %d is Not found.", key));

        System.out.printf("Category '%s' updated successfully (ID: %d)%n", value, key);
    }

    void deleteCategory(Integer key) {
        String deletedValue = expenseManager.categories.remove(key);
        if(deletedValue == null)
            throw new NoSuchElementException(String.format("categoryID %d is Not found.", key));

        expenseManager.expenses.stream()
                .filter(e -> key.equals(e.categoryID))
                .forEach(e -> e.categoryID = null);

        System.out.printf("Category '%s' deleted successfully (ID: %d)%n", deletedValue, key);
    }

    void exportCSV(String fileName) {
        if (!fileName.matches("[a-zA-Z0-9_\\-]+")) {
            throw new IllegalArgumentException("fileName contains invalid characters");
        }

        Path exportDir = Path.of(String.format("%s/%s.csv", saveDir, fileName));

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(exportDir.toFile()))) {
            if(!Files.exists(saveDir))
                Files.createDirectory(saveDir);

            bw.write("ID,CategoryID,CategoryName,Date,Description,Amount\n");
            for(Expense expense : expenseManager.expenses) {
                bw.write(expense.id.toString());
                bw.write(',');
                bw.write(Optional.ofNullable(expense.categoryID).orElse(-1).toString());
                bw.write(',');
                bw.write(Optional.ofNullable(expenseManager.categories.get(expense.categoryID)).orElse(""));
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
    CategoryCommand.class,
    ExportCommand.class,
    BudgetCommand.class
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
    public Integer convert(String s) throws IllegalArgumentException {
        int parse = Integer.parseUnsignedInt(s);
        if(parse < 1)
            throw new IllegalArgumentException("A number greater than 0 required.");
        return parse;
    }
}
class UnsignedBigDecimalCostConverter implements CommandLine.ITypeConverter<BigDecimal> {
    @Override
    public BigDecimal convert(String s) throws Exception {
        BigDecimal parse = new BigDecimal(s);
        if (parse.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("A cost greater than 0 is required.");
        return parse;
    }
}
class Range1To12Converter implements CommandLine.ITypeConverter<Integer> {
    @Override
    public Integer convert(String value) throws IllegalArgumentException {
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

    @CommandLine.Option(names = "--amount", required = true, converter = UnsignedBigDecimalCostConverter.class)
    BigDecimal amount;

    @CommandLine.Option(names = "--categoryID", required = false)
    Integer categoryID;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            if(categoryID == null)
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

@CommandLine.Command(name = "get", description = "Get current expense item from list.")
class GetCommand implements Runnable {
    @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntAndNonZeroConverter.class)
    Integer id;

    @Override
    public void run() {
        try (ExpenseTracker c = new ExpenseTracker()) {
            c.get(id);
        } catch (NoSuchElementException  e) {
            System.out.printf(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

@CommandLine.Command(name = "list", description = "List current expense items.")
class ListCommand implements Runnable {
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
            } else if(exclusiveOptions.categoryId != null) {
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

@CommandLine.Command(name = "update", description = "Update expense item values.")
class UpdateCommand implements Runnable {
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

@CommandLine.Command(name = "budget", description = "Set Monthly Budget")
class BudgetCommand implements Runnable {
    @CommandLine.Option(names = "--month", required = true, description = "Budget of Month", converter = Range1To12Converter.class)
    Integer month;

    @CommandLine.Option(names = "--amount", required = true, description = "Budget Amount", converter = UnsignedBigDecimalCostConverter.class)
    BigDecimal amount;

    @Override
    public void run() {
        try(ExpenseTracker c = new ExpenseTracker()) {
            c.setBudget(Month.of(month), amount);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

@CommandLine.Command(name = "category", description = "category commands", subcommands = {
        CategoryCommand.CategoryAddCommand.class,
        CategoryCommand.CategoryListCommand.class,
        CategoryCommand.CategoryUpdateCommand.class,
        CategoryCommand.CategoryDeleteCommand.class
})
class CategoryCommand {
    @CommandLine.Command(name = "add", description = "Add category item.")
    static class CategoryAddCommand implements Runnable {
        @CommandLine.Option(names = "--name", required = true)
        String categoryName;

        @Override
        public void run() {
            try (ExpenseTracker c = new ExpenseTracker()) {
                c.addCategory(categoryName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "list", description = "List All categories.")
    static class CategoryListCommand implements Runnable {
        @Override
        public void run() {
            try (ExpenseTracker c = new ExpenseTracker()) {
                c.listCategory();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "update", description = "update category item.")
    static class CategoryUpdateCommand implements Runnable {
        @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntAndNonZeroConverter.class)
        Integer categoryKey;

        @CommandLine.Option(names = "--name", required = true)
        String categoryName;

        @Override
        public void run() {
            try (ExpenseTracker c = new ExpenseTracker()) {
                c.updateCategory(categoryKey, categoryName);
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "delete", description = "delete category item.")
    static class CategoryDeleteCommand implements Runnable {
        @CommandLine.Option(names = "--id", required = true, converter = UnsignedIntAndNonZeroConverter.class)
        Integer categoryKey;

        @Override
        public void run() {
            try (ExpenseTracker c = new ExpenseTracker()) {
                c.deleteCategory(categoryKey);
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}