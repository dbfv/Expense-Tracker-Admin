package com.example.expensetrackeradmin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private void insertDefaultAdmins(SQLiteDatabase db) {
        String[][] defaultAdmins = {
                {"The dbfv", "GCH230163", "quanbfclan@gmail.com"},
                {"dbfv", "GCH230999", "quanldgch230163@gmail.com"},
        };

        for (String[] admin : defaultAdmins) {
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(COLUMN_EMP_NAME, admin[0]);
            values.put(COLUMN_EMP_CODE, admin[1]);
            values.put(COLUMN_EMP_EMAIL, admin[2]);

            db.insert(TABLE_EMPLOYEES, null, values);
        }
        android.util.Log.d("DatabaseHelper", "Default admins array seeded successfully.");
    }

    private static final String DATABASE_NAME = "ExpenseTracker.db";
    private static final int DATABASE_VERSION = 2;

    // Table names
    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_EMPLOYEES = "employees";

    // Projects Table - Columns
    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_PROJECT_NAME = "name";
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

    // SQL Statements
    private static final String CREATE_TABLE_PROJECTS = "CREATE TABLE " + TABLE_PROJECTS + " ("
            + COLUMN_PROJECT_ID + " TEXT PRIMARY KEY, "
            + COLUMN_PROJECT_NAME + " TEXT NOT NULL, "
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
            + COLUMN_EMP_EMAIL + " TEXT UNIQUE NOT NULL"
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEES);
        onCreate(db);
    }
}