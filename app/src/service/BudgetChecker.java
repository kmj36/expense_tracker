package service;

import model.Expense;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.List;

public class BudgetChecker extends TablePrinter {
    public void checkBudget(Month month, List<Expense> expenses, BigDecimal[] monthlyBudget) {
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
