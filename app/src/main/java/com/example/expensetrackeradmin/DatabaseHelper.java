package com.example.expensetrackeradmin;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import java.util.ArrayList;
import java.util.List;

import models.Project;
import models.Expense;
import models.Employee;

public class DatabaseHelper extends SQLiteOpenHelper {

    private void insertDefaultAdmins(SQLiteDatabase db) {
        String[][] defaultAdmins = {
                {"The dbfv", "GCH230163", "quanbfclan@gmail.com", "admin"},
                {"dbfv", "GCH230999", "quanldgch230163@gmail.com", "admin"},
        };

        for (String[] admin : defaultAdmins) {
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(COLUMN_EMP_NAME, admin[0]);
            values.put(COLUMN_EMP_CODE, admin[1]);
            values.put(COLUMN_EMP_EMAIL, admin[2]);
            values.put(COLUMN_EMP_ROLE, admin[3]);

            db.insert(TABLE_EMPLOYEES, null, values);
        }
        android.util.Log.d("DatabaseHelper", "Default admins array seeded successfully.");
    }

    private static final String DATABASE_NAME = "ExpenseTracker.db";
    private static final int DATABASE_VERSION = 4;

    // Table names
    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_EMPLOYEES = "employees";

    // Projects Table - Columns
    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_PROJECT_NAME = "name";
    public static final String COLUMN_PROJECT_PASSWORD = "password";
    public static final String COLUMN_PROJECT_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_PROJECT_DESC = "description";
    public static final String COLUMN_PROJECT_START_DATE = "start_date";
    public static final String COLUMN_PROJECT_END_DATE = "end_date";
    public static final String COLUMN_PROJECT_MANAGER = "manager";
    public static final String COLUMN_PROJECT_STATUS = "status";
    public static final String COLUMN_PROJECT_BUDGET = "budget";
    public static final String COLUMN_PROJECT_SPECIAL_REQ = "special_requirements";
    public static final String COLUMN_PROJECT_CLIENT = "client_info";

    // Expenses Table - Columns
    public static final String COLUMN_EXPENSE_ID = "expense_id";
    public static final String COLUMN_EXP_PROJECT_ID = "project_id";
    public static final String COLUMN_EXPENSE_DATE = "expense_date";
    public static final String COLUMN_EXPENSE_AMOUNT = "amount";
    public static final String COLUMN_EXPENSE_CURRENCY = "currency";
    public static final String COLUMN_EXPENSE_TYPE = "type";
    public static final String COLUMN_EXPENSE_PAYMENT_METHOD = "payment_method";
    public static final String COLUMN_EXPENSE_CLAIMANT = "claimant";
    public static final String COLUMN_EXPENSE_STATUS = "payment_status";
    public static final String COLUMN_EXPENSE_DESC = "description";
    public static final String COLUMN_EXPENSE_LOCATION = "location";

    // Employees Table - Columns
    public static final String COLUMN_EMP_ID = "id";
    public static final String COLUMN_EMP_NAME = "name";
    public static final String COLUMN_EMP_CODE = "code";
    public static final String COLUMN_EMP_EMAIL = "email";
    public static final String COLUMN_EMP_ROLE = "role";

    // SQL Statements
    private static final String CREATE_TABLE_PROJECTS = "CREATE TABLE " + TABLE_PROJECTS + " ("
            + COLUMN_PROJECT_ID + " TEXT PRIMARY KEY, "
            + COLUMN_PROJECT_NAME + " TEXT NOT NULL, "
            + COLUMN_PROJECT_PASSWORD + " TEXT NOT NULL, "
            + COLUMN_PROJECT_PASSWORD_HASH + " TEXT NOT NULL, "
            + COLUMN_PROJECT_DESC + " TEXT NOT NULL, "
            + COLUMN_PROJECT_START_DATE + " TEXT NOT NULL, "
            + COLUMN_PROJECT_END_DATE + " TEXT NOT NULL, "
            + COLUMN_PROJECT_MANAGER + " TEXT NOT NULL, "
            + COLUMN_PROJECT_STATUS + " TEXT NOT NULL, "
            + COLUMN_PROJECT_BUDGET + " REAL NOT NULL, "
            + COLUMN_PROJECT_SPECIAL_REQ + " TEXT, "
            + COLUMN_PROJECT_CLIENT + " TEXT"
            + ");";

    private static final String CREATE_TABLE_EXPENSES = "CREATE TABLE " + TABLE_EXPENSES + " ("
            + COLUMN_EXPENSE_ID + " TEXT PRIMARY KEY, "
            + COLUMN_EXP_PROJECT_ID + " TEXT NOT NULL, "
            + COLUMN_EXPENSE_DATE + " TEXT NOT NULL, "
            + COLUMN_EXPENSE_AMOUNT + " REAL NOT NULL, "
            + COLUMN_EXPENSE_CURRENCY + " TEXT NOT NULL, "
            + COLUMN_EXPENSE_TYPE + " TEXT NOT NULL, "
            + COLUMN_EXPENSE_PAYMENT_METHOD + " TEXT NOT NULL, "
            + COLUMN_EXPENSE_CLAIMANT + " TEXT NOT NULL, "
            + COLUMN_EXPENSE_STATUS + " TEXT NOT NULL, "
            + COLUMN_EXPENSE_DESC + " TEXT, "
            + COLUMN_EXPENSE_LOCATION + " TEXT, "
            + "FOREIGN KEY(" + COLUMN_EXP_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COLUMN_PROJECT_ID + ") ON DELETE CASCADE"
            + ");";
    private static final String CREATE_TABLE_EMPLOYEES = "CREATE TABLE " + TABLE_EMPLOYEES + " ("
            + COLUMN_EMP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EMP_NAME + " TEXT NOT NULL, "
            + COLUMN_EMP_CODE + " TEXT UNIQUE NOT NULL, "
            + COLUMN_EMP_EMAIL + " TEXT UNIQUE NOT NULL, "
            + COLUMN_EMP_ROLE + " TEXT NOT NULL DEFAULT 'employee'"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PROJECTS);
        db.execSQL(CREATE_TABLE_EXPENSES);
        db.execSQL(CREATE_TABLE_EMPLOYEES);

        insertDefaultAdmins(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_EMPLOYEES + " ADD COLUMN " + COLUMN_EMP_ROLE + " TEXT NOT NULL DEFAULT 'employee'");
            } catch (Exception ignored) {
            }

            db.execSQL("UPDATE " + TABLE_EMPLOYEES
                    + " SET " + COLUMN_EMP_ROLE + " = 'admin'"
                    + " WHERE " + COLUMN_EMP_CODE + " IN ('GCH230163','GCH230999')");

                db.execSQL("UPDATE " + TABLE_EXPENSES
                    + " SET " + COLUMN_EXPENSE_CLAIMANT + " = ("
                    + "SELECT e." + COLUMN_EMP_ID + " FROM " + TABLE_EMPLOYEES + " e"
                    + " WHERE e." + COLUMN_EMP_CODE + " = " + TABLE_EXPENSES + "." + COLUMN_EXPENSE_CLAIMANT + " COLLATE NOCASE"
                    + " LIMIT 1)"
                    + " WHERE EXISTS ("
                    + "SELECT 1 FROM " + TABLE_EMPLOYEES + " e"
                    + " WHERE e." + COLUMN_EMP_CODE + " = " + TABLE_EXPENSES + "." + COLUMN_EXPENSE_CLAIMANT + " COLLATE NOCASE)");

                db.execSQL("UPDATE " + TABLE_EXPENSES
                    + " SET " + COLUMN_EXPENSE_CLAIMANT + " = ("
                    + "SELECT e." + COLUMN_EMP_ID + " FROM " + TABLE_EMPLOYEES + " e"
                    + " WHERE e." + COLUMN_EMP_NAME + " = " + TABLE_EXPENSES + "." + COLUMN_EXPENSE_CLAIMANT
                    + " LIMIT 1)"
                    + " WHERE EXISTS ("
                    + "SELECT 1 FROM " + TABLE_EMPLOYEES + " e"
                    + " WHERE e." + COLUMN_EMP_NAME + " = " + TABLE_EXPENSES + "." + COLUMN_EXPENSE_CLAIMANT + ")");
        }
        
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PROJECTS + " ADD COLUMN " + COLUMN_PROJECT_PASSWORD + " TEXT NOT NULL DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_PROJECTS + " ADD COLUMN " + COLUMN_PROJECT_PASSWORD_HASH + " TEXT NOT NULL DEFAULT ''");
            } catch (Exception ignored) {
            }
        }
    }
    public double getTotalExpenseForProject(String projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalSpent = 0.0;

        String query = "SELECT SUM(" + COLUMN_EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_EXP_PROJECT_ID + " = ?";
        android.database.Cursor cursor = db.rawQuery(query, new String[]{projectId});

        if (cursor.moveToFirst()) {
            totalSpent = cursor.getDouble(0);
        }
        cursor.close();

        return totalSpent;
    }

    public Project getProjectById(String projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Project project = null;

        Cursor cursor = db.query(TABLE_PROJECTS, null, COLUMN_PROJECT_ID + "=?", new String[]{projectId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_NAME));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_PASSWORD));
            String passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_PASSWORD_HASH));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_DESC));
            String start = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_START_DATE));
            String end = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_END_DATE));
            String manager = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_MANAGER));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_STATUS));
            double budget = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_BUDGET));
            String special = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_SPECIAL_REQ));
            String client = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_CLIENT));

            project = new Project(id, name, password, passwordHash, desc, start, end, manager, status, budget, special, client);
            project.setSpentAmount(getTotalExpenseForProject(id));

            cursor.close();
        }
        return project;
    }

    public boolean deleteProject(String projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_PROJECTS, COLUMN_PROJECT_ID + "=?", new String[]{projectId});
        return rowsDeleted > 0;
    }

    public String getProjectNameById(String projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String name = null;
        Cursor cursor = db.query(TABLE_PROJECTS, new String[]{COLUMN_PROJECT_NAME}, COLUMN_PROJECT_ID + "=?", new String[]{projectId}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(0);
            cursor.close();
        }
        return name;
    }

    public boolean verifyProjectPassword(String projectId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = MD5Util.md5(password);
        Cursor cursor = db.query(TABLE_PROJECTS, new String[]{COLUMN_PROJECT_PASSWORD}, 
                COLUMN_PROJECT_ID + "=?", new String[]{projectId}, null, null, null);
        boolean isValid = false;
        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            isValid = storedPassword != null && storedPassword.equals(hashedPassword);
            cursor.close();
        }
        return isValid;
    }

    public boolean insertExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_ID, expense.getExpenseId());
        values.put(COLUMN_EXP_PROJECT_ID, expense.getProjectId());
        values.put(COLUMN_EXPENSE_DATE, expense.getDate());
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_CURRENCY, expense.getCurrency());
        values.put(COLUMN_EXPENSE_TYPE, expense.getType());
        values.put(COLUMN_EXPENSE_PAYMENT_METHOD, expense.getPaymentMethod());
        values.put(COLUMN_EXPENSE_CLAIMANT, expense.getClaimant());
        values.put(COLUMN_EXPENSE_STATUS, expense.getStatus());
        values.put(COLUMN_EXPENSE_DESC, expense.getDescription());
        values.put(COLUMN_EXPENSE_LOCATION, expense.getLocation());

        long result = db.insert(TABLE_EXPENSES, null, values);
        return result != -1;
    }

    public Employee getEmployeeByCode(String employeeCode) {
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Employee employee = null;

        Cursor cursor = db.query(
                TABLE_EMPLOYEES,
                new String[]{COLUMN_EMP_ID, COLUMN_EMP_NAME, COLUMN_EMP_CODE, COLUMN_EMP_EMAIL, COLUMN_EMP_ROLE},
            COLUMN_EMP_CODE + " = ? COLLATE NOCASE",
                new String[]{employeeCode.trim()},
                null,
                null,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
            employee = new Employee(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_CODE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ROLE))
            );
            }
            cursor.close();
        }

        return employee;
    }

    public Employee getEmployeeById(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Employee employee = null;

        Cursor cursor = db.query(
                TABLE_EMPLOYEES,
                new String[]{COLUMN_EMP_ID, COLUMN_EMP_NAME, COLUMN_EMP_CODE, COLUMN_EMP_EMAIL, COLUMN_EMP_ROLE},
                COLUMN_EMP_ID + "=?",
                new String[]{employeeId.trim()},
                null,
                null,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                employee = new Employee(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_CODE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ROLE))
                );
            }
            cursor.close();
        }

        return employee;
    }

    public Employee getEmployeeByEmail(String employeeEmail) {
        if (employeeEmail == null || employeeEmail.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Employee employee = null;

        Cursor cursor = db.query(
                TABLE_EMPLOYEES,
                new String[]{COLUMN_EMP_ID, COLUMN_EMP_NAME, COLUMN_EMP_CODE, COLUMN_EMP_EMAIL, COLUMN_EMP_ROLE},
                COLUMN_EMP_EMAIL + " = ? COLLATE NOCASE",
                new String[]{employeeEmail.trim()},
                null,
                null,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                employee = new Employee(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_CODE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ROLE))
                );
            }
            cursor.close();
        }

        return employee;
    }

    private String formatEmployeeDisplay(Employee employee) {
        return employee.getName() + " - " + employee.getCode();
    }

    public String getEmployeeDisplayByClaimantValue(String claimantValue) {
        if (claimantValue == null || claimantValue.trim().isEmpty()) {
            return "-";
        }

        String trimmedValue = claimantValue.trim();

        Employee byId = getEmployeeById(trimmedValue);
        if (byId != null) {
            return formatEmployeeDisplay(byId);
        }

        Employee byCode = getEmployeeByCode(trimmedValue);
        if (byCode != null) {
            return formatEmployeeDisplay(byCode);
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_EMPLOYEES,
                new String[]{COLUMN_EMP_ID, COLUMN_EMP_NAME, COLUMN_EMP_CODE, COLUMN_EMP_EMAIL, COLUMN_EMP_ROLE},
                COLUMN_EMP_NAME + "=?",
                new String[]{trimmedValue},
                null,
                null,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Employee byName = new Employee(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_CODE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ROLE))
                );
                cursor.close();
                return formatEmployeeDisplay(byName);
            }
            cursor.close();
        }

        return trimmedValue;
    }

    public Expense getExpenseById(String expenseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Expense expense = null;

        Cursor cursor = db.query(TABLE_EXPENSES, null, COLUMN_EXPENSE_ID + "=?", new String[]{expenseId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String claimantValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_CLAIMANT));
            expense = new Expense(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXP_PROJECT_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DATE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_CURRENCY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_PAYMENT_METHOD)),
                claimantValue,
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DESC)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_LOCATION))
            );
            expense.setClaimantDisplay(getEmployeeDisplayByClaimantValue(claimantValue));
            cursor.close();
        }
        return expense;
    }

    public List<Expense> getExpensesByProjectId(String projectId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_EXPENSES, null, COLUMN_EXP_PROJECT_ID + "=?",
                new String[]{projectId}, null, null, COLUMN_EXPENSE_DATE + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String claimantValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_CLAIMANT));
                Expense expense = new Expense(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXP_PROJECT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DATE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_CURRENCY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_PAYMENT_METHOD)),
                    claimantValue,
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_STATUS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DESC)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_LOCATION))
                );
                expense.setClaimantDisplay(getEmployeeDisplayByClaimantValue(claimantValue));
                expenses.add(expense);
            }
            cursor.close();
        }
        return expenses;
    }

    public boolean updateExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_DATE, expense.getDate());
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_CURRENCY, expense.getCurrency());
        values.put(COLUMN_EXPENSE_TYPE, expense.getType());
        values.put(COLUMN_EXPENSE_PAYMENT_METHOD, expense.getPaymentMethod());
        values.put(COLUMN_EXPENSE_CLAIMANT, expense.getClaimant());
        values.put(COLUMN_EXPENSE_STATUS, expense.getStatus());
        values.put(COLUMN_EXPENSE_DESC, expense.getDescription());
        values.put(COLUMN_EXPENSE_LOCATION, expense.getLocation());

        int rowsUpdated = db.update(TABLE_EXPENSES, values, COLUMN_EXPENSE_ID + "=?", new String[]{expense.getExpenseId()});
        return rowsUpdated > 0;
    }

    public boolean deleteExpense(String expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_EXPENSES, COLUMN_EXPENSE_ID + "=?", new String[]{expenseId});
        return rowsDeleted > 0;
    }
}