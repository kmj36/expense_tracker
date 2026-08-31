package model;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.*;

// 비용 데이터 저장(Wrapper) 클래스
public class ExpensesData extends service.BudgetChecker {
    public List<Expense> expenses;
    public HashMap<Integer, String> categories;
    public BigDecimal[] monthlyBudget;

    public ExpensesData() {
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
}
