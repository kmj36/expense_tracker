package service;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import model.Expense;

import java.util.List;
import java.util.Map;

public class TablePrinter {
    public void printExpenseTable(List<Expense> exps, Map<Integer, String> categories) {
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

    public void printCategoryTable(Map<Integer, String> categories) {
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
}
