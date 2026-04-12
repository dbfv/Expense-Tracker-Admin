package com.example.expensetrackeradmin.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import com.example.expensetrackeradmin.utils.MD5Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.expensetrackeradmin.models.Project;
import com.example.expensetrackeradmin.models.Expense;
import com.example.expensetrackeradmin.models.Employee;
import com.example.expensetrackeradmin.models.ExpenseImage;

public class DatabaseHelper extends SQLiteOpenHelper {

    private void insertDefaultAdmins(SQLiteDatabase db) {
        String[][] defaultAdmins = {
                {"The dbfv", "GCH230163", "quanbfclan@gmail.com", "admin"},
                {"dbfv", "GCH230999", "quanldgch230163@gmail.com", "user"},
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
    private static final int DATABASE_VERSION = 7;

    // Table names
    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_EMPLOYEES = "employees";
    public static final String TABLE_EXPENSE_IMAGES = "ExpenseImages";
    public static final String TABLE_PENDING_DELETIONS = "pending_deletions";

    public static final String COLUMN_PENDING_ENTITY_TYPE = "entity_type";
    public static final String COLUMN_PENDING_ENTITY_ID = "entity_id";

    public static final String ENTITY_TYPE_PROJECT = "project";
    public static final String ENTITY_TYPE_EXPENSE = "expense";

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

    // ExpenseImages Table - Columns
    public static final String COLUMN_EXP_IMAGE_ID = "imageId";
    public static final String COLUMN_EXP_IMAGE_EXPENSE_ID = "expenseId";
    public static final String COLUMN_EXP_IMAGE_URL = "imageUrl";

    // Employees Table - Columns
    public static final String COLUMN_EMP_ID = "id";
    public static final String COLUMN_EMP_NAME = "name";
    public static final String COLUMN_EMP_CODE = "code";
    public static final String COLUMN_EMP_EMAIL = "email";
    public static final String COLUMN_EMP_ROLE = "role";
    public static final String COLUMN_SYNC_STATUS = "sync_status";

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
            + COLUMN_PROJECT_CLIENT + " TEXT, "
            + COLUMN_SYNC_STATUS + " INTEGER NOT NULL DEFAULT 0"
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
                + COLUMN_SYNC_STATUS + " INTEGER NOT NULL DEFAULT 0, "
            + "FOREIGN KEY(" + COLUMN_EXP_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COLUMN_PROJECT_ID + ") ON DELETE CASCADE"
            + ");";
    private static final String CREATE_TABLE_EMPLOYEES = "CREATE TABLE " + TABLE_EMPLOYEES + " ("
            + COLUMN_EMP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EMP_NAME + " TEXT NOT NULL, "
            + COLUMN_EMP_CODE + " TEXT UNIQUE NOT NULL, "
            + COLUMN_EMP_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COLUMN_EMP_ROLE + " TEXT NOT NULL DEFAULT 'employee', "
                + COLUMN_SYNC_STATUS + " INTEGER NOT NULL DEFAULT 0"
            + ");";
        private static final String CREATE_TABLE_EXPENSE_IMAGES = "CREATE TABLE " + TABLE_EXPENSE_IMAGES + " ("
            + COLUMN_EXP_IMAGE_ID + " TEXT PRIMARY KEY, "
            + COLUMN_EXP_IMAGE_EXPENSE_ID + " TEXT NOT NULL, "
            + COLUMN_EXP_IMAGE_URL + " TEXT NOT NULL, "
            + "FOREIGN KEY(" + COLUMN_EXP_IMAGE_EXPENSE_ID + ") REFERENCES " + TABLE_EXPENSES + "(" + COLUMN_EXPENSE_ID + ") ON DELETE CASCADE"
            + ");";
    private static final String CREATE_TABLE_PENDING_DELETIONS = "CREATE TABLE " + TABLE_PENDING_DELETIONS + " ("
            + COLUMN_PENDING_ENTITY_TYPE + " TEXT NOT NULL, "
            + COLUMN_PENDING_ENTITY_ID + " TEXT NOT NULL, "
            + "PRIMARY KEY(" + COLUMN_PENDING_ENTITY_TYPE + ", " + COLUMN_PENDING_ENTITY_ID + ")"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PROJECTS);
        db.execSQL(CREATE_TABLE_EXPENSES);
        db.execSQL(CREATE_TABLE_EMPLOYEES);
        db.execSQL(CREATE_TABLE_EXPENSE_IMAGES);
        db.execSQL(CREATE_TABLE_PENDING_DELETIONS);

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

        if (oldVersion < 5) {
            try {
                db.execSQL(CREATE_TABLE_EXPENSE_IMAGES);
            } catch (Exception ignored) {
            }
        }

        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PROJECTS + " ADD COLUMN " + COLUMN_SYNC_STATUS + " INTEGER NOT NULL DEFAULT 0");
            } catch (Exception ignored) {
            }

            try {
                db.execSQL("ALTER TABLE " + TABLE_EXPENSES + " ADD COLUMN " + COLUMN_SYNC_STATUS + " INTEGER NOT NULL DEFAULT 0");
            } catch (Exception ignored) {
            }

            try {
                db.execSQL("ALTER TABLE " + TABLE_EMPLOYEES + " ADD COLUMN " + COLUMN_SYNC_STATUS + " INTEGER NOT NULL DEFAULT 0");
            } catch (Exception ignored) {
            }
        }

        if (oldVersion < 7) {
            try {
                db.execSQL(CREATE_TABLE_PENDING_DELETIONS);
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
        if (projectId == null || projectId.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(
                    TABLE_EXPENSE_IMAGES,
                    COLUMN_EXP_IMAGE_EXPENSE_ID + " IN (SELECT " + COLUMN_EXPENSE_ID + " FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_EXP_PROJECT_ID + "=?)",
                    new String[]{projectId}
            );
            db.delete(TABLE_EXPENSES, COLUMN_EXP_PROJECT_ID + "=?", new String[]{projectId});
            int rowsDeleted = db.delete(TABLE_PROJECTS, COLUMN_PROJECT_ID + "=?", new String[]{projectId});
            if (rowsDeleted <= 0) {
                return false;
            }

            ContentValues pendingDeletion = new ContentValues();
            pendingDeletion.put(COLUMN_PENDING_ENTITY_TYPE, ENTITY_TYPE_PROJECT);
            pendingDeletion.put(COLUMN_PENDING_ENTITY_ID, projectId);
            db.insertWithOnConflict(TABLE_PENDING_DELETIONS, null, pendingDeletion, SQLiteDatabase.CONFLICT_IGNORE);

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
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

    public boolean insertProject(Project project) {
        if (project == null || project.getProjectId() == null || project.getProjectId().trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROJECT_ID, project.getProjectId());
        values.put(COLUMN_PROJECT_NAME, project.getName());
        values.put(COLUMN_PROJECT_PASSWORD, project.getPassword());
        values.put(COLUMN_PROJECT_PASSWORD_HASH, project.getPasswordHash());
        values.put(COLUMN_PROJECT_DESC, project.getDescription());
        values.put(COLUMN_PROJECT_START_DATE, project.getStartDate());
        values.put(COLUMN_PROJECT_END_DATE, project.getEndDate());
        values.put(COLUMN_PROJECT_MANAGER, project.getManager());
        values.put(COLUMN_PROJECT_STATUS, project.getStatus());
        values.put(COLUMN_PROJECT_BUDGET, project.getBudget());
        values.put(COLUMN_PROJECT_SPECIAL_REQ, project.getSpecialRequirements());
        values.put(COLUMN_PROJECT_CLIENT, project.getClientInfo());
        values.put(COLUMN_SYNC_STATUS, 0);

        return db.insert(TABLE_PROJECTS, null, values) != -1;
    }

    public boolean updateProject(Project project) {
        if (project == null || project.getProjectId() == null || project.getProjectId().trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROJECT_NAME, project.getName());
        values.put(COLUMN_PROJECT_PASSWORD, project.getPassword());
        values.put(COLUMN_PROJECT_PASSWORD_HASH, project.getPasswordHash());
        values.put(COLUMN_PROJECT_DESC, project.getDescription());
        values.put(COLUMN_PROJECT_START_DATE, project.getStartDate());
        values.put(COLUMN_PROJECT_END_DATE, project.getEndDate());
        values.put(COLUMN_PROJECT_MANAGER, project.getManager());
        values.put(COLUMN_PROJECT_STATUS, project.getStatus());
        values.put(COLUMN_PROJECT_BUDGET, project.getBudget());
        values.put(COLUMN_PROJECT_SPECIAL_REQ, project.getSpecialRequirements());
        values.put(COLUMN_PROJECT_CLIENT, project.getClientInfo());
        values.put(COLUMN_SYNC_STATUS, 2);

        return db.update(TABLE_PROJECTS, values, COLUMN_PROJECT_ID + "=?", new String[]{project.getProjectId()}) > 0;
    }

    public boolean insertEmployee(Employee employee) {
        if (employee == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (employee.getId() != null && !employee.getId().trim().isEmpty()) {
            try {
                values.put(COLUMN_EMP_ID, Integer.parseInt(employee.getId().trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        values.put(COLUMN_EMP_NAME, employee.getName());
        values.put(COLUMN_EMP_CODE, employee.getCode());
        values.put(COLUMN_EMP_EMAIL, employee.getEmail());
        values.put(COLUMN_EMP_ROLE, employee.getRole() == null || employee.getRole().trim().isEmpty() ? "employee" : employee.getRole());
        values.put(COLUMN_SYNC_STATUS, 0);

        return db.insert(TABLE_EMPLOYEES, null, values) != -1;
    }

    public boolean updateEmployee(Employee employee) {
        if (employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMP_NAME, employee.getName());
        values.put(COLUMN_EMP_CODE, employee.getCode());
        values.put(COLUMN_EMP_EMAIL, employee.getEmail());
        values.put(COLUMN_EMP_ROLE, employee.getRole() == null || employee.getRole().trim().isEmpty() ? "employee" : employee.getRole());
        values.put(COLUMN_SYNC_STATUS, 2);

        return db.update(TABLE_EMPLOYEES, values, COLUMN_EMP_ID + "=?", new String[]{employee.getId().trim()}) > 0;
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
        values.put(COLUMN_SYNC_STATUS, 0);

        long result = db.insert(TABLE_EXPENSES, null, values);
        return result != -1;
    }

    public boolean insertExpenseImage(String expenseId, String imageUrl) {
        if (expenseId == null || expenseId.trim().isEmpty() || imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXP_IMAGE_ID, UUID.randomUUID().toString());
        values.put(COLUMN_EXP_IMAGE_EXPENSE_ID, expenseId);
        values.put(COLUMN_EXP_IMAGE_URL, imageUrl);
        return db.insert(TABLE_EXPENSE_IMAGES, null, values) != -1;
    }

    public void replaceExpenseImages(String expenseId, List<String> imageUrls) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_EXPENSE_IMAGES, COLUMN_EXP_IMAGE_EXPENSE_ID + "=?", new String[]{expenseId});
            if (imageUrls != null) {
                for (String imageUrl : imageUrls) {
                    if (imageUrl == null || imageUrl.trim().isEmpty()) {
                        continue;
                    }
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_EXP_IMAGE_ID, UUID.randomUUID().toString());
                    values.put(COLUMN_EXP_IMAGE_EXPENSE_ID, expenseId);
                    values.put(COLUMN_EXP_IMAGE_URL, imageUrl);
                    db.insert(TABLE_EXPENSE_IMAGES, null, values);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<ExpenseImage> getExpenseImagesByExpenseId(String expenseId) {
        List<ExpenseImage> images = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_EXPENSE_IMAGES,
                new String[]{COLUMN_EXP_IMAGE_ID, COLUMN_EXP_IMAGE_EXPENSE_ID, COLUMN_EXP_IMAGE_URL},
                COLUMN_EXP_IMAGE_EXPENSE_ID + "=?",
                new String[]{expenseId},
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                images.add(new ExpenseImage(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXP_IMAGE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXP_IMAGE_EXPENSE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXP_IMAGE_URL))
                ));
            }
            cursor.close();
        }

        return images;
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
            expense.setImages(getExpenseImagesByExpenseId(expense.getExpenseId()));
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
        values.put(COLUMN_SYNC_STATUS, 2);

        int rowsUpdated = db.update(TABLE_EXPENSES, values, COLUMN_EXPENSE_ID + "=?", new String[]{expense.getExpenseId()});
        return rowsUpdated > 0;
    }

    public boolean deleteExpense(String expenseId) {
        if (expenseId == null || expenseId.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Foreign key ON DELETE CASCADE will handle images
            int rowsDeleted = db.delete(TABLE_EXPENSES, COLUMN_EXPENSE_ID + "=?", new String[]{expenseId});
            if (rowsDeleted <= 0) {
                return false;
            }

            ContentValues pendingDeletion = new ContentValues();
            pendingDeletion.put(COLUMN_PENDING_ENTITY_TYPE, ENTITY_TYPE_EXPENSE);
            pendingDeletion.put(COLUMN_PENDING_ENTITY_ID, expenseId);
            db.insertWithOnConflict(TABLE_PENDING_DELETIONS, null, pendingDeletion, SQLiteDatabase.CONFLICT_IGNORE);

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<Project> getUnsyncedProjects() {
        List<Project> projectList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PROJECTS +
                " WHERE " + COLUMN_SYNC_STATUS + " IN (0, 2)";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Project project = new Project();
                    project.setProjectId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_ID)));
                    project.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_NAME)));
                    project.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_PASSWORD)));
                    project.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_PASSWORD_HASH)));
                    project.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_DESC)));
                    project.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_START_DATE)));
                    project.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_END_DATE)));
                    project.setManager(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_MANAGER)));
                    project.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_STATUS)));
                    project.setBudget(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_BUDGET)));
                    project.setSpecialRequirements(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_SPECIAL_REQ)));
                    project.setClientInfo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_CLIENT)));
                    project.setSpentAmount(getTotalExpenseForProject(project.getProjectId()));

                    projectList.add(project);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return projectList;
    }

    public List<Project> getAllProjectsForSync() {
        List<Project> projectList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PROJECTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Project project = new Project();
                    project.setProjectId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_ID)));
                    project.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_NAME)));
                    project.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_PASSWORD)));
                    project.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_PASSWORD_HASH)));
                    project.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_DESC)));
                    project.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_START_DATE)));
                    project.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_END_DATE)));
                    project.setManager(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_MANAGER)));
                    project.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_STATUS)));
                    project.setBudget(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_BUDGET)));
                    project.setSpecialRequirements(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_SPECIAL_REQ)));
                    project.setClientInfo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_CLIENT)));
                    project.setSpentAmount(getTotalExpenseForProject(project.getProjectId()));
                    projectList.add(project);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return projectList;
    }

    public List<Expense> getUnsyncedExpenses() {
        List<Expense> expenseList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EXPENSES +
                " WHERE " + COLUMN_SYNC_STATUS + " IN (0, 2)";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
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
                    expenseList.add(expense);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return expenseList;
    }

    public List<Expense> getAllExpensesForSync() {
        List<Expense> expenseList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EXPENSES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
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
                    expenseList.add(expense);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return expenseList;
    }

    public List<Employee> getUnsyncedEmployees() {
        List<Employee> employeeList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EMPLOYEES +
                " WHERE " + COLUMN_SYNC_STATUS + " IN (0, 2)";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Employee employee = new Employee(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_CODE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ROLE))
                    );
                    employeeList.add(employee);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return employeeList;
    }

    public List<Employee> getAllEmployeesForSync() {
        List<Employee> employeeList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EMPLOYEES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Employee employee = new Employee(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_CODE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMP_ROLE))
                    );
                    employeeList.add(employee);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return employeeList;
    }

    public List<String> getImagesForExpense(String expenseId) {
        List<String> imageUrls = new ArrayList<>();
        if (expenseId == null || expenseId.trim().isEmpty()) {
            return imageUrls;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_EXPENSE_IMAGES,
                new String[]{COLUMN_EXP_IMAGE_URL},
                COLUMN_EXP_IMAGE_EXPENSE_ID + "=?",
                new String[]{expenseId},
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXP_IMAGE_URL));
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    imageUrls.add(imageUrl);
                }
            }
            cursor.close();
        }

        return imageUrls;
    }

    public boolean upsertProjectFromCloud(Project project) {
        if (project == null || project.getProjectId() == null || project.getProjectId().trim().isEmpty()) {
            return false;
        }

        String projectId = project.getProjectId().trim();
        SQLiteDatabase db = this.getWritableDatabase();
        if (hasPendingLocalChanges(db, TABLE_PROJECTS, COLUMN_PROJECT_ID, projectId)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_PROJECT_NAME, safeText(project.getName()));
        values.put(COLUMN_PROJECT_PASSWORD, safeText(project.getPassword()));
        values.put(COLUMN_PROJECT_PASSWORD_HASH, safeText(project.getPasswordHash()));
        values.put(COLUMN_PROJECT_DESC, safeText(project.getDescription()));
        values.put(COLUMN_PROJECT_START_DATE, safeText(project.getStartDate()));
        values.put(COLUMN_PROJECT_END_DATE, safeText(project.getEndDate()));
        values.put(COLUMN_PROJECT_MANAGER, safeText(project.getManager()));
        values.put(COLUMN_PROJECT_STATUS, safeText(project.getStatus()));
        values.put(COLUMN_PROJECT_BUDGET, project.getBudget());
        values.put(COLUMN_PROJECT_SPECIAL_REQ, project.getSpecialRequirements());
        values.put(COLUMN_PROJECT_CLIENT, project.getClientInfo());
        values.put(COLUMN_SYNC_STATUS, 1);

        int rowsUpdated = db.update(TABLE_PROJECTS, values, COLUMN_PROJECT_ID + "=?", new String[]{projectId});
        if (rowsUpdated > 0) {
            return true;
        }

        values.put(COLUMN_PROJECT_ID, projectId);
        return db.insert(TABLE_PROJECTS, null, values) != -1;
    }

    public boolean upsertEmployeeFromCloud(Employee employee) {
        if (employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
            return false;
        }

        String employeeId = employee.getId().trim();
        SQLiteDatabase db = this.getWritableDatabase();
        if (hasPendingLocalChanges(db, TABLE_EMPLOYEES, COLUMN_EMP_ID, employeeId)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_EMP_NAME, safeText(employee.getName()));
        values.put(COLUMN_EMP_CODE, safeText(employee.getCode()));
        values.put(COLUMN_EMP_EMAIL, safeText(employee.getEmail()));
        values.put(COLUMN_EMP_ROLE, safeTextWithDefault(employee.getRole(), "employee"));
        values.put(COLUMN_SYNC_STATUS, 1);

        int rowsUpdated = db.update(TABLE_EMPLOYEES, values, COLUMN_EMP_ID + "=?", new String[]{employeeId});
        if (rowsUpdated > 0) {
            return true;
        }

        try {
            values.put(COLUMN_EMP_ID, Integer.parseInt(employeeId));
        } catch (NumberFormatException ignored) {
            return false;
        }
        return db.insert(TABLE_EMPLOYEES, null, values) != -1;
    }

    public boolean upsertExpenseFromCloud(Expense expense, List<String> imageUrls) {
        if (expense == null || expense.getExpenseId() == null || expense.getExpenseId().trim().isEmpty()) {
            return false;
        }

        String expenseId = expense.getExpenseId().trim();
        SQLiteDatabase db = this.getWritableDatabase();
        if (hasPendingLocalChanges(db, TABLE_EXPENSES, COLUMN_EXPENSE_ID, expenseId)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_EXP_PROJECT_ID, safeText(expense.getProjectId()));
        values.put(COLUMN_EXPENSE_DATE, safeText(expense.getDate()));
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_CURRENCY, safeText(expense.getCurrency()));
        values.put(COLUMN_EXPENSE_TYPE, safeText(expense.getType()));
        values.put(COLUMN_EXPENSE_PAYMENT_METHOD, safeText(expense.getPaymentMethod()));
        values.put(COLUMN_EXPENSE_CLAIMANT, safeText(expense.getClaimant()));
        values.put(COLUMN_EXPENSE_STATUS, safeText(expense.getStatus()));
        values.put(COLUMN_EXPENSE_DESC, expense.getDescription());
        values.put(COLUMN_EXPENSE_LOCATION, expense.getLocation());
        values.put(COLUMN_SYNC_STATUS, 1);

        int rowsUpdated = db.update(TABLE_EXPENSES, values, COLUMN_EXPENSE_ID + "=?", new String[]{expenseId});
        if (rowsUpdated == 0) {
            values.put(COLUMN_EXPENSE_ID, expenseId);
            if (db.insert(TABLE_EXPENSES, null, values) == -1) {
                return false;
            }
        }

        replaceExpenseImages(expenseId, imageUrls);
        return true;
    }

    private boolean hasPendingLocalChanges(SQLiteDatabase db, String tableName, String idColumn, String idValue) {
        int currentStatus = getRecordSyncStatus(db, tableName, idColumn, idValue);
        return currentStatus == 0 || currentStatus == 2;
    }

    private int getRecordSyncStatus(SQLiteDatabase db, String tableName, String idColumn, String idValue) {
        Cursor cursor = db.query(
                tableName,
                new String[]{COLUMN_SYNC_STATUS},
                idColumn + "=?",
                new String[]{idValue},
                null,
                null,
                null
        );

        if (cursor == null) {
            return -1;
        }

        try {
            if (!cursor.moveToFirst()) {
                return -1;
            }
            return cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNC_STATUS));
        } finally {
            cursor.close();
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String safeTextWithDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    public void setSyncStatus(String tableName, String idColumn, String idValue, int status) {
        if (tableName == null || tableName.trim().isEmpty()
                || idColumn == null || idColumn.trim().isEmpty()
                || idValue == null || idValue.trim().isEmpty()) {
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, status);
        db.update(tableName, values, idColumn + " = ?", new String[]{idValue});
    }

    public void updateProjectSyncStatus(String projectId, int status) {
        setSyncStatus(TABLE_PROJECTS, COLUMN_PROJECT_ID, projectId, status);
    }

    public List<String> getPendingProjectDeletions() {
        return getPendingDeletionsByType(ENTITY_TYPE_PROJECT);
    }

    public List<String> getPendingExpenseDeletions() {
        return getPendingDeletionsByType(ENTITY_TYPE_EXPENSE);
    }

    private List<String> getPendingDeletionsByType(String type) {
        List<String> ids = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PENDING_DELETIONS,
                new String[]{COLUMN_PENDING_ENTITY_ID},
                COLUMN_PENDING_ENTITY_TYPE + "=?",
                new String[]{type},
                null,
                null,
                null
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PENDING_ENTITY_ID));
                    if (id != null && !id.trim().isEmpty()) {
                        ids.add(id.trim());
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return ids;
    }

    public void clearPendingProjectDeletion(String projectId) {
        clearPendingDeletion(ENTITY_TYPE_PROJECT, projectId);
    }

    public void clearPendingExpenseDeletion(String expenseId) {
        clearPendingDeletion(ENTITY_TYPE_EXPENSE, expenseId);
    }

    private void clearPendingDeletion(String type, String id) {
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(
                TABLE_PENDING_DELETIONS,
                COLUMN_PENDING_ENTITY_TYPE + "=? AND " + COLUMN_PENDING_ENTITY_ID + "=?",
                new String[]{type, id.trim()}
        );
    }
}