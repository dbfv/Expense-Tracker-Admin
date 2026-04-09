package com.example.expensetrackeradmin.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetrackeradmin.R;
import com.google.android.material.chip.Chip;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.example.expensetrackeradmin.adapters.ExpenseImageAdapter;
import com.example.expensetrackeradmin.helpers.DatabaseHelper;
import com.example.expensetrackeradmin.models.Expense;
import com.example.expensetrackeradmin.models.ExpenseImage;

public class ExpenseDetailsActivity extends AppCompatActivity {

    private TextView tvExpenseType, tvExpenseAmount, tvExpenseCurrency, tvExpenseDate, tvClaimant, tvPaymentMethod, tvLocation, tvDescription;
    private Chip chipExpenseId, chipExpenseStatus;
    private RecyclerView rvExpenseImages;
    private DatabaseHelper dbHelper;
    private ExpenseImageAdapter expenseImageAdapter;
    private final List<String> expenseImageUrls = new ArrayList<>();
    private String expenseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);

        dbHelper = new DatabaseHelper(this);
        initViews();

        expenseId = getIntent().getStringExtra("EXPENSE_ID");

        Toolbar toolbar = findViewById(R.id.toolbarExpenseDetails);
        toolbar.setNavigationOnClickListener(v -> finish());

        toolbar.inflateMenu(R.menu.menu_expense_details);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                Intent intent = new Intent(ExpenseDetailsActivity.this, AddExpenseActivity.class);
                intent.putExtra("EXPENSE_ID", expenseId);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (expenseId != null) {
            loadExpenseDetails();
        } else {
            Toast.makeText(this, "Expense not found!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        chipExpenseId = findViewById(R.id.chipExpenseId);
        chipExpenseStatus = findViewById(R.id.chipExpenseStatus);
        tvExpenseType = findViewById(R.id.tvExpenseType);
        tvExpenseAmount = findViewById(R.id.tvExpenseAmount);
        tvExpenseCurrency = findViewById(R.id.tvExpenseCurrency);
        tvExpenseDate = findViewById(R.id.tvExpenseDate);
        tvClaimant = findViewById(R.id.tvClaimant);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvLocation = findViewById(R.id.tvLocation);
        tvDescription = findViewById(R.id.tvDescription);
        rvExpenseImages = findViewById(R.id.rvExpenseImages);

        rvExpenseImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        expenseImageAdapter = new ExpenseImageAdapter(expenseImageUrls, false, null);
        rvExpenseImages.setAdapter(expenseImageAdapter);
    }

    private void loadExpenseDetails() {
        Expense expense = dbHelper.getExpenseById(expenseId);

        if (expense != null) {
            String shortId = "EXP-" + expense.getExpenseId().substring(0, 8).toUpperCase();
            chipExpenseId.setText(shortId);

            tvExpenseType.setText(expense.getType());

            DecimalFormat formatter = new DecimalFormat("#,##0.00");
            tvExpenseAmount.setText(formatter.format(expense.getAmount()));
            tvExpenseCurrency.setText(expense.getCurrency());

            tvExpenseDate.setText(expense.getDate());
            String claimantDisplay = expense.getClaimantDisplay();
            if (claimantDisplay == null || claimantDisplay.trim().isEmpty()) {
                claimantDisplay = expense.getClaimant();
            }
            tvClaimant.setText(claimantDisplay != null && !claimantDisplay.trim().isEmpty() ? claimantDisplay : "-");
            tvPaymentMethod.setText(expense.getPaymentMethod());
            
            String location = expense.getLocation();
            tvLocation.setText(location != null && !location.isEmpty() ? location : "-");
            
            String desc = expense.getDescription();
            tvDescription.setText(desc != null && !desc.isEmpty() ? desc : "-");

            expenseImageUrls.clear();
            if (expense.getImages() != null) {
                for (ExpenseImage image : expense.getImages()) {
                    if (image.getImageUrl() != null && !image.getImageUrl().trim().isEmpty()) {
                        expenseImageUrls.add(image.getImageUrl());
                    }
                }
            }
            expenseImageAdapter.updateData(expenseImageUrls);
            rvExpenseImages.setVisibility(expenseImageUrls.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);

            String status = expense.getStatus();
            chipExpenseStatus.setText(status.toUpperCase());

            if (status.equalsIgnoreCase("Paid")) {
                chipExpenseStatus.setChipBackgroundColorResource(R.color.status_completed_bg);
                chipExpenseStatus.setTextColor(ContextCompat.getColor(this, R.color.status_completed_text));
            } else if (status.equalsIgnoreCase("Pending")) {
                chipExpenseStatus.setChipBackgroundColorResource(R.color.status_on_hold_bg);
                chipExpenseStatus.setTextColor(ContextCompat.getColor(this, R.color.status_on_hold_text));
            } else if (status.equalsIgnoreCase("Reimbursed")) {
                chipExpenseStatus.setChipBackgroundColorResource(R.color.status_active_bg);
                chipExpenseStatus.setTextColor(ContextCompat.getColor(this, R.color.status_active_text));
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_expense);
        dialog.setTitle("Delete Expense");

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            if (dbHelper.deleteExpense(expenseId)) {
                Toast.makeText(this, "Expense deleted successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            } else {
                Toast.makeText(this, "Error deleting expense!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}