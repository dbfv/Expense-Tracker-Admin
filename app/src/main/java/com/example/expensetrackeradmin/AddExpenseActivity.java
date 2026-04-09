package com.example.expensetrackeradmin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import models.Employee;
import models.Expense;

public class AddExpenseActivity extends AppCompatActivity {

    private TextInputEditText etExpenseDate, etAmount, etDescription, etLocation, etClaimantCode;
    private AutoCompleteTextView spCurrency, spExpenseType, spPaymentMethod, spPaymentStatus;
    private TextView tvClaimantDisplay;
    private Button btnSaveExpense;
    private DatabaseHelper dbHelper;
    private String projectId;
    private String expenseId;
    private String selectedClaimantId;
    private boolean isEditMode = false;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        dbHelper = new DatabaseHelper(this);
        projectId = getIntent().getStringExtra("PROJECT_ID");
        expenseId = getIntent().getStringExtra("EXPENSE_ID");

        initViews();
        setupToolbar();
        setupDropdowns();
        setupClaimantCodeLookup();

        // Format số tiền khi nhập
        etAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    etAmount.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[.,]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            long parsed = Long.parseLong(cleanString);
                            String formatted = String.format(Locale.getDefault(), "%,d", parsed).replace(",", ".");
                            current = formatted;
                            etAmount.setText(formatted);
                            etAmount.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    } else {
                        current = "";
                        etAmount.setText("");
                    }
                    etAmount.addTextChangedListener(this);
                }
            }
        });

        if (expenseId != null && !expenseId.isEmpty()) {
            isEditMode = true;
            loadExpenseData(expenseId);
        } else {
            etExpenseDate.setText(dateFormatter.format(new Date()));
        }

        etExpenseDate.setOnClickListener(v -> showDatePicker(etExpenseDate));

        btnSaveExpense.setOnClickListener(v -> saveExpenseToDatabase());
    }

    private void loadExpenseData(String expenseId) {
        Expense expense = dbHelper.getExpenseById(expenseId);
        if (expense != null) {
            projectId = expense.getProjectId();
            etExpenseDate.setText(expense.getDate());
            etAmount.setText(String.valueOf(expense.getAmount()));
            etDescription.setText(expense.getDescription());
            etLocation.setText(expense.getLocation());
            spCurrency.setText(expense.getCurrency(), false);
            spExpenseType.setText(expense.getType(), false);
            spPaymentMethod.setText(expense.getPaymentMethod(), false);
            spPaymentStatus.setText(expense.getStatus(), false);

            Employee claimant = dbHelper.getEmployeeById(expense.getClaimant());
            if (claimant != null) {
                selectedClaimantId = claimant.getId();
                etClaimantCode.setText(claimant.getCode());
                tvClaimantDisplay.setText(claimant.getName() + " - " + claimant.getCode());
                tvClaimantDisplay.setVisibility(View.VISIBLE);
            } else {
                selectedClaimantId = null;
                etClaimantCode.setText(expense.getClaimant());
                String claimantDisplay = expense.getClaimantDisplay();
                if (claimantDisplay != null && !claimantDisplay.trim().isEmpty()) {
                    tvClaimantDisplay.setText(claimantDisplay);
                    tvClaimantDisplay.setVisibility(View.VISIBLE);
                } else {
                    tvClaimantDisplay.setVisibility(View.GONE);
                }
            }

            Toolbar toolbar = findViewById(R.id.toolbarAddExpense);
            toolbar.setTitle("Edit Expense");
            btnSaveExpense.setText("Update Expense");
        }
    }

    private void initViews() {
        etExpenseDate = findViewById(R.id.etExpenseDate);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etClaimantCode = findViewById(R.id.etClaimantCode);
        tvClaimantDisplay = findViewById(R.id.tvClaimantDisplay);
        spCurrency = findViewById(R.id.spCurrency);
        spExpenseType = findViewById(R.id.spExpenseType);
        spPaymentMethod = findViewById(R.id.spPaymentMethod);
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

    private void setupClaimantCodeLookup() {
        etClaimantCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                resolveClaimantByCode(s.toString().trim(), true);
            }
        });
    }

    private Employee resolveClaimantByCode(String claimantCode, boolean showNotFoundMessage) {
        if (claimantCode == null || claimantCode.trim().isEmpty()) {
            selectedClaimantId = null;
            tvClaimantDisplay.setVisibility(View.GONE);
            return null;
        }

        Employee employee = dbHelper.getEmployeeByCode(claimantCode.trim());
        if (employee == null) {
            selectedClaimantId = null;
            if (showNotFoundMessage) {
                tvClaimantDisplay.setText("No user found for this code.");
                tvClaimantDisplay.setVisibility(View.VISIBLE);
            } else {
                tvClaimantDisplay.setVisibility(View.GONE);
            }
            return null;
        }

        selectedClaimantId = employee.getId();
        tvClaimantDisplay.setText(employee.getName() + " - " + employee.getCode());
        tvClaimantDisplay.setVisibility(View.VISIBLE);
        return employee;
    }

    private void saveExpenseToDatabase() {
        String expenseDate = etExpenseDate.getText().toString().trim();
        String amountStr = etAmount.getText().toString().replace(".", "").trim();
        String currency = spCurrency.getText().toString().trim();
        String expenseType = spExpenseType.getText().toString().trim();
        String paymentMethod = spPaymentMethod.getText().toString().trim();
        String claimantCode = etClaimantCode.getText().toString().trim();
        String paymentStatus = spPaymentStatus.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        StringBuilder missingFields = new StringBuilder();
        if (expenseDate.isEmpty()) missingFields.append("• Date of Expense\n");
        if (amountStr.isEmpty()) missingFields.append("• Amount\n");
        if (currency.isEmpty()) missingFields.append("• Currency\n");
        if (expenseType.isEmpty()) missingFields.append("• Type of Expense\n");
        if (paymentMethod.isEmpty()) missingFields.append("• Payment Method\n");
        if (claimantCode.isEmpty()) missingFields.append("• Claimant Code\n");
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

        Employee claimant = resolveClaimantByCode(claimantCode, false);
        if (claimant == null || selectedClaimantId == null) {
            Toast.makeText(this, "Invalid claimant code.", Toast.LENGTH_SHORT).show();
            return;
        }

        Expense expense = new Expense();
        expense.setProjectId(projectId);
        expense.setDate(expenseDate);
        expense.setAmount(amount);
        expense.setCurrency(currency);
        expense.setType(expenseType);
        expense.setPaymentMethod(paymentMethod);
        expense.setClaimant(selectedClaimantId);
        expense.setStatus(paymentStatus);
        expense.setDescription(description);
        expense.setLocation(location);

        if (isEditMode) {
            expense.setExpenseId(expenseId);

            if (dbHelper.updateExpense(expense)) {
                Toast.makeText(this, "Expense updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error updating expense.", Toast.LENGTH_SHORT).show();
            }
        } else {
            expense.setExpenseId(UUID.randomUUID().toString());

            if (dbHelper.insertExpense(expense)) {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error saving expense.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}