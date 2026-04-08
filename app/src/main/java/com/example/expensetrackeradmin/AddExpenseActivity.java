package com.example.expensetrackeradmin;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddExpenseActivity extends AppCompatActivity {

    private TextInputEditText etExpenseDate, etAmount, etDescription, etLocation;
    private AutoCompleteTextView spCurrency, spExpenseType, spPaymentMethod, spClaimant, spPaymentStatus;
    private Button btnSaveExpense;
    private DatabaseHelper dbHelper;
    private String projectId;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        dbHelper = new DatabaseHelper(this);
        projectId = getIntent().getStringExtra("PROJECT_ID");

        initViews();
        setupToolbar();

        etExpenseDate.setText(dateFormatter.format(new Date()));
        etExpenseDate.setOnClickListener(v -> showDatePicker(etExpenseDate));

        setupDropdowns();
        loadClaimants();

        btnSaveExpense.setOnClickListener(v -> saveExpenseToDatabase());
    }

    private void initViews() {
        etExpenseDate = findViewById(R.id.etExpenseDate);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        spCurrency = findViewById(R.id.spCurrency);
        spExpenseType = findViewById(R.id.spExpenseType);
        spPaymentMethod = findViewById(R.id.spPaymentMethod);
        spClaimant = findViewById(R.id.spClaimant);
        spPaymentStatus = findViewById(R.id.spPaymentStatus);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarAddExpense);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void showDatePicker(TextInputEditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    targetEditText.setText(dateFormatter.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupDropdowns() {
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "VND"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, currencies);
        spCurrency.setAdapter(currencyAdapter);
        spCurrency.setText("USD", false);

        String[] expenseTypes = {"Travel", "Equipment", "Materials", "Services", "Software/Licenses", "Labour costs", "Utilities", "Miscellaneous"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, expenseTypes);
        spExpenseType.setAdapter(typeAdapter);

        String[] paymentMethods = {"Cash", "Credit Card", "Bank Transfer", "Cheque"};
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, paymentMethods);
        spPaymentMethod.setAdapter(methodAdapter);

        String[] paymentStatuses = {"Paid", "Pending", "Reimbursed"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, paymentStatuses);
        spPaymentStatus.setAdapter(statusAdapter);
    }

    private void loadClaimants() {
        List<String> employeeNames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_EMPLOYEES, new String[]{DatabaseHelper.COLUMN_EMP_NAME}, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                employeeNames.add(cursor.getString(0));
            }
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, employeeNames);
        spClaimant.setAdapter(adapter);
    }

    private void saveExpenseToDatabase() {
        String expenseDate = etExpenseDate.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String currency = spCurrency.getText().toString().trim();
        String expenseType = spExpenseType.getText().toString().trim();
        String paymentMethod = spPaymentMethod.getText().toString().trim();
        String claimant = spClaimant.getText().toString().trim();
        String paymentStatus = spPaymentStatus.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        StringBuilder missingFields = new StringBuilder();
        if (expenseDate.isEmpty()) missingFields.append("• Date of Expense\n");
        if (amountStr.isEmpty()) missingFields.append("• Amount\n");
        if (currency.isEmpty()) missingFields.append("• Currency\n");
        if (expenseType.isEmpty()) missingFields.append("• Type of Expense\n");
        if (paymentMethod.isEmpty()) missingFields.append("• Payment Method\n");
        if (claimant.isEmpty()) missingFields.append("• Claimant\n");
        if (paymentStatus.isEmpty()) missingFields.append("• Payment Status\n");

        if (missingFields.length() > 0) {
            Toast.makeText(this, "Please fill required fields:\n" + missingFields.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_EXPENSE_ID, UUID.randomUUID().toString());
        values.put(DatabaseHelper.COLUMN_EXP_PROJECT_ID, projectId);
        values.put(DatabaseHelper.COLUMN_EXPENSE_DATE, expenseDate);
        values.put(DatabaseHelper.COLUMN_EXPENSE_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_EXPENSE_CURRENCY, currency);
        values.put(DatabaseHelper.COLUMN_EXPENSE_TYPE, expenseType);
        values.put(DatabaseHelper.COLUMN_EXPENSE_PAYMENT_METHOD, paymentMethod);
        values.put(DatabaseHelper.COLUMN_EXPENSE_CLAIMANT, claimant);
        values.put(DatabaseHelper.COLUMN_EXPENSE_STATUS, paymentStatus);
        values.put(DatabaseHelper.COLUMN_EXPENSE_DESC, description);
        values.put(DatabaseHelper.COLUMN_EXPENSE_LOCATION, location);

        long newRowId = db.insert(DatabaseHelper.TABLE_EXPENSES, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving expense.", Toast.LENGTH_SHORT).show();
        }
    }
}