import picocli.CommandLine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class expenseTrackerApp {
    private static final Map<String, String> COMMAND_USAGES = Map.of(
            "main", "Usage: "+expenseTrackerApp.class.getName()+" <add|get|list|update|delete|summary|category|export>",
            "add", "Usage: "+expenseTrackerApp.class.getName()+" add --description <text> --amount <number>",
            "get", "Usage: "+expenseTrackerApp.class.getName()+" get --id <number>",
            "list", "Usage: "+expenseTrackerApp.class.getName()+" list [--category <name>]",
            "update", "Usage: "+expenseTrackerApp.class.getName()+" update --id <number> --description <text> --amount <number>",
            "delete", "Usage: "+expenseTrackerApp.class.getName()+" delete --id <number>",
            "summary", "Usage: "+expenseTrackerApp.class.getName()+" summary [--month <1-12>]",
            "export", "Usage: "+expenseTrackerApp.class.getName()+" export --path <filename>",
            "category", "Usage: "+expenseTrackerApp.class.getName()+""
    );

    static void main(String[] args) {
        try {
            if (args.length < 1) throw new IllegalArgumentException(COMMAND_USAGES.get("main"));

            expenseTracker tracker = new expenseTracker();
            String command = args[0];

            switch (command) {
                case "add" -> {
                    if (args.length != 5) throw new IllegalArgumentException(COMMAND_USAGES.get("add")); // 인자 개수 미충족 예외

                    String description = null;
                    Integer amount = null;

                    try {
                        for (int i = 1; i < args.length; i++) {
                            switch (args[i]) {
                                case "--description" -> description = args[++i];
                                case "--amount" -> amount = Integer.parseUnsignedInt(args[++i]); // 부호 없는 정수 파싱 실패 예외

                                case null, default ->
                                        throw new IllegalArgumentException(COMMAND_USAGES.get("add")); // 옵션 이름 불일치 예외
                            }
                        }
                    } catch (NumberFormatException _) {
                        throw new IllegalArgumentException(COMMAND_USAGES.get("add"));
                    }

                    tracker.add(description, amount);
                }

                case "get" -> {
                    if ( !(args.length == 3 && args[1].equals("--id")) ) throw new IllegalArgumentException(COMMAND_USAGES.get("get"));

                    Integer id = null;

                    try {
                        id = Integer.parseUnsignedInt(args[2]);
                    } catch (NumberFormatException _) {
                        throw new IllegalArgumentException(COMMAND_USAGES.get("get"));
                    }

                    tracker.get(id);
                }

                case "list" -> {
                    if(args.length < 2) {
                        tracker.list();
                        return;
                    }

                    if( !(args.length == 3 && args[1].equals("--category")) ) throw new IllegalArgumentException(COMMAND_USAGES.get("list"));

                    String categoryName = args[2];
                    tracker.list(categoryName);
                }

                case "update" -> {

                }

                case "delete" -> {

                }

                case "summary" -> {

                }

                case "export" -> {

                }

                case "category" -> {

                }

                case "budget" -> {

                }
            }
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        /*
        if(args.length < 1) {
            System.out.println(COMMAND_USAGES.get("main"));
            return;
        }

        expenseTracker app = new expenseTracker();
        String command = args[0];

        switch (command) {
            case "add" -> {
                try {
                    if(args.length < 5) throw new IllegalArgumentException(); // 인자 개수 예외

                    String description = "";
                    Integer amount = 0;

                    for(int i = 1; i < args.length; i++) {
                        switch(args[i]) {
                            case "--description" -> description = args[++i];
                            case "--amount" -> amount = Integer.parseInt(args[++i]); // 정수 파싱 실패 예외

                            case null, default -> throw new IllegalArgumentException(); // 옵션 불일치 예외
                        }
                    }

                    app.add(description, amount);
                } catch (IllegalArgumentException _) {
                    System.out.println(COMMAND_USAGES.get("add"));
                } catch (RuntimeException e) {
                    throw new RuntimeException(e);
                }
            }

            case "get" -> {
                if(args.length < 3 || !args[1].equals("--id")) {
                    System.out.println(COMMAND_USAGES.get("get"));
                    return;
                }

                System.out.println("get.");
            }

            case "list" -> {
                // Optional category filter
                if(args.length > 1) {
                    System.out.println("list with category.");
                    return;
                }

                System.out.println("list.");
            }

            case "update" -> {
                if(args.length < 7 || !args[1].equals("--id") || !args[3].equals("--description") || !args[5].equals("--amount")) {
                    System.out.println(COMMAND_USAGES.get("update"));
                    return;
                }
                System.out.println("update.");
            }

            case "delete" -> {

            }

            case "summary" -> {

            }

            case "category" -> {

            }

            case null, default -> System.out.println(COMMAND_USAGES.get("main"));
        }
         */
    }
}
