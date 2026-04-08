package com.example.expensetrackeradmin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import models.Expense;
import models.Project;

public class ProjectDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvBudget, tvManager, tvSpent, tvProgressPct, tvNoExpenses;
    private Chip chipStatus, chipId;
    private ProgressBar pbProgress;
    private ExtendedFloatingActionButton fabAddExpense;
    private DatabaseHelper dbHelper;
    private String projectId;
    private RecyclerView rvExpenses;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        dbHelper = new DatabaseHelper(this);
        initViews();

        // Lấy ID từ Dashboard
        projectId = getIntent().getStringExtra("PROJECT_ID");

        // Cài đặt Toolbar & Menu
        Toolbar toolbar = findViewById(R.id.toolbarDetails);
        toolbar.setNavigationOnClickListener(v -> finish());

        toolbar.inflateMenu(R.menu.menu_project_details);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                Intent intent = new Intent(ProjectDetailsActivity.this, AddProjectActivity.class);
                intent.putExtra("PROJECT_ID", projectId);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog();
                return true;
            }
            return false;
        });

        // Bắt sự kiện Add Expense
        fabAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectDetailsActivity.this, AddExpenseActivity.class);
            intent.putExtra("PROJECT_ID", projectId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (projectId != null) {
            loadProjectDetails();
            loadExpenses();
        } else {
            Toast.makeText(this, "Project not found!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadExpenses() {
        List<Expense> expenses = dbHelper.getExpensesByProjectId(projectId);
        expenseAdapter.updateData(expenses);
        
        if (expenses.isEmpty()) {
            tvNoExpenses.setVisibility(android.view.View.VISIBLE);
            rvExpenses.setVisibility(android.view.View.GONE);
        } else {
            tvNoExpenses.setVisibility(android.view.View.GONE);
            rvExpenses.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void initViews() {
        chipId = findViewById(R.id.chipDetId);
        chipStatus = findViewById(R.id.chipDetStatus);
        tvName = findViewById(R.id.tvDetName);
        tvBudget = findViewById(R.id.tvDetBudget);
        tvManager = findViewById(R.id.tvDetManager);
        tvSpent = findViewById(R.id.tvDetSpent);
        tvProgressPct = findViewById(R.id.tvDetProgressPct);
        pbProgress = findViewById(R.id.pbDetProgress);
        fabAddExpense = findViewById(R.id.fabAddExpense);
        tvNoExpenses = findViewById(R.id.tvNoExpenses);
        
        rvExpenses = findViewById(R.id.rvExpenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(expenseList, expense -> {
            Intent intent = new Intent(ProjectDetailsActivity.this, ExpenseDetailsActivity.class);
            intent.putExtra("EXPENSE_ID", expense.getExpenseId());
            startActivity(intent);
        });
        rvExpenses.setAdapter(expenseAdapter);
    }

    private void loadProjectDetails() {
        Project project = dbHelper.getProjectById(projectId);

        if (project != null) {
            String shortId = "PRJ-" + project.getProjectId().substring(0, 12).toUpperCase();
            chipId.setText(shortId);

            tvName.setText(project.getName());
            tvManager.setText(project.getManager());

            String status = project.getStatus();
            chipStatus.setText(status.toUpperCase());
            if (status.equalsIgnoreCase("Active")) {
                chipStatus.setChipBackgroundColorResource(R.color.status_active_bg);
                chipStatus.setTextColor(ContextCompat.getColor(this, R.color.status_active_text));
            } else if (status.equalsIgnoreCase("Completed")) {
                chipStatus.setChipBackgroundColorResource(R.color.status_completed_bg);
                chipStatus.setTextColor(ContextCompat.getColor(this, R.color.status_completed_text));
            } else {
                chipStatus.setChipBackgroundColorResource(R.color.status_on_hold_bg);
                chipStatus.setTextColor(ContextCompat.getColor(this, R.color.status_on_hold_text));
            }

            DecimalFormat formatter = new DecimalFormat("$#,##0.00");
            double budget = project.getBudget();
            double spent = project.getSpentAmount();

            tvBudget.setText(formatter.format(budget));
            tvSpent.setText(formatter.format(spent));

            int progressPercentage = budget > 0 ? (int) Math.round((spent / budget) * 100) : 0;
            pbProgress.setProgress(Math.min(progressPercentage, 100));
            tvProgressPct.setText(progressPercentage + "%");

            if (progressPercentage > 100) {
                tvProgressPct.setTextColor(Color.RED);
                tvProgressPct.setText("OVER!");
            } else {
                tvProgressPct.setTextColor(ContextCompat.getColor(this, R.color.on_primary_dark));
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        String projectName = dbHelper.getProjectNameById(projectId);
        if (projectName == null) {
            Toast.makeText(this, "Project not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_project);
        dialog.setTitle("Delete Project");

        TextInputEditText etConfirmName = dialog.findViewById(R.id.etConfirmProjectName);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            String enteredName = etConfirmName.getText().toString().trim();
            if (enteredName.equals(projectName)) {
                if (dbHelper.deleteProject(projectId)) {
                    Toast.makeText(this, "Project deleted successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                } else {
                    Toast.makeText(this, "Error deleting project!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Project name does not match!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}