# expense_tracker

This is a practice project written in Java, based on the Expense Tracker project from roadmap.sh.
https://roadmap.sh/projects/expense-tracker

## Features

- Add, view, update, and delete expenses
- Categorize expenses and manage categories (add, list, update, delete)
- List expenses filtered by category or by month
- Print expenses and categories in a formatted table
- View a summary of total expenses, optionally filtered by month
- Set a monthly budget and receive a warning/exceeded notice when spending approaches or exceeds it
- Export expenses to a CSV file
- Save data in JSON format

## Requirements

- Java 21 (or higher) is required.
- A terminal that supports ASCII color codes and Unicode is required. Using an unsupported terminal may cause characters to display incorrectly (garbled text).

## How to run

Clone the expense_tracker repository and run the following commands:

```bash
git clone https://github.com/kmj36/expense_tracker.git
cd expense_tracker
```

Compile with the required library on the classpath, then run:

```bash
javac -cp "lib\*" -sourcepath app\src -d out app\src\ExpenseTrackerApp.java

java -cp "out;lib/*" ExpenseTrackerApp    # Windows
java -cp "out:lib/*" ExpenseTrackerApp    # macOS/Linux
```

## Usage

```bash
# Show available commands
java -cp "out;lib/*" ExpenseTrackerApp

# Add an expense
java -cp "out;lib/*" ExpenseTrackerApp add --description "Groceries" --amount 45.50
java -cp "out;lib/*" ExpenseTrackerApp add --description "Groceries" --amount 45.50 --categoryID 1

# Get a single expense by ID
java -cp "out;lib/*" ExpenseTrackerApp get --id 1

# List all expenses
java -cp "out;lib/*" ExpenseTrackerApp list

# List expenses filtered by category or month (mutually exclusive)
java -cp "out;lib/*" ExpenseTrackerApp list --categoryID 1
java -cp "out;lib/*" ExpenseTrackerApp list --month 5

# Update an expense
java -cp "out;lib/*" ExpenseTrackerApp update --id 1 --description "Groceries and snacks" --amount 52.00 --categoryID 2

# Delete an expense
java -cp "out;lib/*" ExpenseTrackerApp delete --id 1

# View summary of all expenses, or a specific month
java -cp "out;lib/*" ExpenseTrackerApp summary
java -cp "out;lib/*" ExpenseTrackerApp summary --month 5

# Set a monthly budget
java -cp "out;lib/*" ExpenseTrackerApp budget --month 5 --amount 500.00

# Manage categories
java -cp "out;lib/*" ExpenseTrackerApp category add --name "Food"
java -cp "out;lib/*" ExpenseTrackerApp category list
java -cp "out;lib/*" ExpenseTrackerApp category update --id 1 --name "Groceries"
java -cp "out;lib/*" ExpenseTrackerApp category delete --id 1

# Export expenses to a CSV file
java -cp "out;lib/*" ExpenseTrackerApp export --fileName "expenses"
```

## Data storage

Expense, category, and budget data is stored in `data/expenses.json`. The file is created automatically on first run if it does not already exist.