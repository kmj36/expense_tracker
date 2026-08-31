package command;

import converter.UnsignedIntAndNonZeroConverter;
import core.ExpenseTracker;
import picocli.CommandLine;

import java.util.NoSuchElementException;

@CommandLine.Command(name = "category", description = "category commands", subcommands = {
        CategoryCommand.CategoryAddCommand.class,
        CategoryCommand.CategoryListCommand.class,
        CategoryCommand.CategoryUpdateCommand.class,
        CategoryCommand.CategoryDeleteCommand.class
})
public class CategoryCommand {
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
