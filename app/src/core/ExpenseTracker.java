package core;

import model.Expense;
import model.ExpensesData;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

// 비용관리 트래커 클래스
public class ExpenseTracker implements AutoCloseable {
    private static final Path saveDir = Path.of("data"); // 파일 저장 경로
    private static final Path dataJSON = Path.of("expenses.json"); // 비용 데이터 파일 명

    private static final ObjectMapper mapper = new ObjectMapper();
    private ExpensesData expenseManager;

    public ExpenseTracker() throws IOException {
        Path file = Path.of(String.format("%s/%s", saveDir, dataJSON));

        // 디렉터리가 없으면 생성
        if (!Files.exists(saveDir)) {
            Files.createDirectories(saveDir);
        }

        // 파일이 없으면 빈 데이터로 초기화 후 파일까지 생성
        if (!Files.exists(file)) {
            expenseManager = new ExpensesData();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, expenseManager);
            return;
        }

        String fileData = Files.readString(file);
        try {
            expenseManager = mapper.readValue(fileData, new TypeReference<>() {
            });
        } catch (JacksonException e) {
            expenseManager = new ExpensesData();
        }
    }

    @Override
    public void close() {
        Path file = Path.of(String.format("%s/%s", saveDir, dataJSON));
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, expenseManager);
    }

    public void add(String description, BigDecimal amount) {
        Expense expense = new Expense();

        expense.id = expenseManager.nextId();
        expense.date = LocalDate.now();
        expense.description = description;
        expense.amount = amount;

        expenseManager.expenses.add(expense);

        System.out.printf("Expense added successfully (ID: %d)%n", expense.id);

        expenseManager.checkBudget(LocalDate.now().getMonth(), expenseManager.expenses, expenseManager.monthlyBudget);
    }

    // 카테고리 필드 추가
    public void add(String description, BigDecimal amount, Integer categoryID) {
        // 카테고리 키 사전 체크, 키가 없으면 Exception 발생.
        if (!expenseManager.categories.containsKey(categoryID))
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

        expenseManager.checkBudget(LocalDate.now().getMonth(), expenseManager.expenses, expenseManager.monthlyBudget);
    }

    public void get(Integer id) {
        Optional<Expense> expense = expenseManager.expenses.stream()
                .filter(c -> c.id.equals(id))
                .findFirst();

        if (expense.isPresent()) {
            Expense e = expense.get();

            expenseManager.printExpenseTable(List.of(e), expenseManager.categories);
        } else
            throw new NoSuchElementException(String.format("Expense Not Found (ID: %d)", id));
    }

    public void list() {
        expenseManager.printExpenseTable(expenseManager.expenses, expenseManager.categories);

        expenseManager.checkBudget(LocalDate.now().getMonth(), expenseManager.expenses, expenseManager.monthlyBudget);
    }

    // 카테고리별 리스트 출력
    public void list(Integer categoryID) {
        // 카테고리 키 사전 체크, 키가 없으면 Exception 발생.
        if (!expenseManager.categories.containsKey(categoryID))
            throw new NoSuchElementException(String.format("categoryID %d is Not found.", categoryID));

        Stream<Expense> FilteredExpenseStream = expenseManager.expenses.stream()
                .filter(c -> Objects.equals(c.categoryID, categoryID));

        System.out.printf("[Category - %s]%n", expenseManager.categories.get(categoryID));

        expenseManager.printExpenseTable(FilteredExpenseStream.toList(), expenseManager.categories);

        expenseManager.checkBudget(LocalDate.now().getMonth(), expenseManager.expenses, expenseManager.monthlyBudget);
    }

    // 월별 리스트 출력
    public void list(Month month) {
        Stream<Expense> FilteredExpenseStream = expenseManager.expenses.stream()
                .filter(c -> Objects.equals(c.date.getMonth(), month));

        expenseManager.printExpenseTable(FilteredExpenseStream.toList(), expenseManager.categories);

        expenseManager.checkBudget(month, expenseManager.expenses, expenseManager.monthlyBudget);
    }

    public void update(Integer id, String description, BigDecimal amount, Integer categoryID) throws IndexOutOfBoundsException {
        if (description == null && amount == null && categoryID == null)
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

        expenseManager.checkBudget(LocalDate.now().getMonth(), expenseManager.expenses, expenseManager.monthlyBudget);
    }

    public void delete(Integer id) {
        boolean removed = expenseManager.expenses.removeIf(e -> e.id.equals(id));

        if (removed)
            System.out.printf("Expense deleted successfully (ID: %d)%n", id);
        else
            throw new NoSuchElementException(String.format("Expense Not Found (ID: %d)", id));

        expenseManager.checkBudget(LocalDate.now().getMonth(), expenseManager.expenses, expenseManager.monthlyBudget);
    }

    public void summary() {
        BigDecimal total = expenseManager.expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.printf("Total expenses: $%s%n", total);

        expenseManager.checkBudget(LocalDate.now().getMonth(), expenseManager.expenses, expenseManager.monthlyBudget);
    }

    // 월별 비용 요약
    public void summary(Month month) {
        BigDecimal totalMonth = expenseManager.expenses.stream()
                .filter(c -> c.date.getMonth().equals(month))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String monthStr = month.toString();
        String firstLetter = monthStr.substring(0, 1);
        String remainLetter = monthStr.substring(1);
        System.out.printf("Total expenses for %s: $%s%n", firstLetter + remainLetter.toLowerCase(), totalMonth);

        expenseManager.checkBudget(month, expenseManager.expenses, expenseManager.monthlyBudget);
    }

    public void setBudget(Month month, BigDecimal budget) {
        expenseManager.monthlyBudget[month.getValue() - 1] = budget;

        System.out.printf("%s Budget updated successfully (Amount: $%s)%n", month, budget);

        expenseManager.checkBudget(LocalDate.now().getMonth(), expenseManager.expenses, expenseManager.monthlyBudget);
    }

    public void addCategory(String category) {
        Integer key = expenseManager.nextCategoryId();
        expenseManager.categories.put(key, category);

        System.out.printf("Category '%s' added successfully (ID: %d)%n", category, key);
    }

    public void listCategory() {
        expenseManager.printCategoryTable(expenseManager.categories);
    }

    public void updateCategory(Integer key, String value) {
        String replacedValue = expenseManager.categories.replace(key, value);
        if (replacedValue == null)
            throw new NoSuchElementException(String.format("categoryID %d is Not found.", key));

        System.out.printf("Category '%s' updated successfully (ID: %d)%n", value, key);
    }

    public void deleteCategory(Integer key) {
        String deletedValue = expenseManager.categories.remove(key);
        if (deletedValue == null)
            throw new NoSuchElementException(String.format("categoryID %d is Not found.", key));

        expenseManager.expenses.stream()
                .filter(e -> key.equals(e.categoryID))
                .forEach(e -> e.categoryID = null);

        System.out.printf("Category '%s' deleted successfully (ID: %d)%n", deletedValue, key);
    }

    public void exportCSV(String fileName) {
        if (!fileName.matches("[a-zA-Z0-9_\\-]+")) {
            throw new IllegalArgumentException("fileName contains invalid characters");
        }

        Path exportDir = Path.of(String.format("%s/%s.csv", saveDir, fileName));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(exportDir.toFile()))) {
            if (!Files.exists(saveDir))
                Files.createDirectory(saveDir);

            bw.write("ID,CategoryID,CategoryName,Date,Description,Amount\n");
            for (Expense expense : expenseManager.expenses) {
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
