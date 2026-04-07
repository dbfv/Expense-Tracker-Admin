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

public class AddProjectActivity extends AppCompatActivity {

    private TextInputEditText etProjectName, etProjectDesc, etStartDate, etEndDate, etProjectBudget, etSpecialReq, etClientInfo;
    private AutoCompleteTextView spManager, spStatus;
    private Button btnSaveProject;
    private DatabaseHelper dbHelper;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();

        etStartDate.setText(dateFormatter.format(new Date()));

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        loadManagers();
        setupStatusDropdown();
        etProjectBudget.addTextChangedListener(new android.text.TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (!s.toString().equals(current)) {
                    etProjectBudget.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[.]", "");

                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
                            String formatted = formatter.format(parsed).replace(",", ".");

                            current = formatted;
                            etProjectBudget.setText(formatted);
                            etProjectBudget.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                        }
                    } else {
                        current = "";
                        etProjectBudget.setText("");
                    }
                    etProjectBudget.addTextChangedListener(this);
                }
            }
        });

        btnSaveProject.setOnClickListener(v -> saveProjectToDatabase());
    }

    private void initViews() {
        etProjectName = findViewById(R.id.etProjectName);
        etProjectDesc = findViewById(R.id.etProjectDesc);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etProjectBudget = findViewById(R.id.etProjectBudget);
        etSpecialReq = findViewById(R.id.etSpecialReq);
        etClientInfo = findViewById(R.id.etClientInfo);
        spManager = findViewById(R.id.spManager);
        spStatus = findViewById(R.id.spStatus);
        btnSaveProject = findViewById(R.id.btnSaveProject);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarAddProject);
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

    private void loadManagers() {
        List<String> managerNames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_EMPLOYEES, new String[]{DatabaseHelper.COLUMN_EMP_NAME}, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                managerNames.add(cursor.getString(0));
            }
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, managerNames);
        spManager.setAdapter(adapter);
    }

    private void setupStatusDropdown() {
        String[] statuses = {"Active", "Completed", "On Hold"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);
        spStatus.setAdapter(adapter);
    }

    private void saveProjectToDatabase() {
        String name = etProjectName.getText().toString().trim();
        String desc = etProjectDesc.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String manager = spManager.getText().toString().trim();
        String status = spStatus.getText().toString().trim();
        String budgetStr = etProjectBudget.getText().toString().trim();
        String specialReq = etSpecialReq.getText().toString().trim();
        String clientInfo = etClientInfo.getText().toString().trim();

        if (name.isEmpty() || desc.isEmpty() || startDate.isEmpty() || endDate.isEmpty() ||
                manager.isEmpty() || status.isEmpty() || budgetStr.isEmpty()) {
            Toast.makeText(this, "Please enter all required fields (*)", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Date start = dateFormatter.parse(startDate);
            Date end = dateFormatter.parse(endDate);
            if (end != null && start != null && end.before(start)) {
                Toast.makeText(this, "End date must be after start date!", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        double budget;
        try {
            String cleanBudgetStr = budgetStr.replaceAll("[.]", "");
            budget = Double.parseDouble(cleanBudgetStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid budget format!", Toast.LENGTH_SHORT).show();
            return;
        }


        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_PROJECT_ID, UUID.randomUUID().toString());
        values.put(DatabaseHelper.COLUMN_PROJECT_NAME, name);
        values.put(DatabaseHelper.COLUMN_PROJECT_DESC, desc);
        values.put(DatabaseHelper.COLUMN_PROJECT_START_DATE, startDate);
        values.put(DatabaseHelper.COLUMN_PROJECT_END_DATE, endDate);
        values.put(DatabaseHelper.COLUMN_PROJECT_MANAGER, manager);
        values.put(DatabaseHelper.COLUMN_PROJECT_STATUS, status);
        values.put(DatabaseHelper.COLUMN_PROJECT_BUDGET, budget);
        values.put(DatabaseHelper.COLUMN_PROJECT_SPECIAL_REQ, specialReq);
        values.put(DatabaseHelper.COLUMN_PROJECT_CLIENT, clientInfo);

        long newRowId = db.insert(DatabaseHelper.TABLE_PROJECTS, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Project saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving project.", Toast.LENGTH_SHORT).show();
        }
    }
}