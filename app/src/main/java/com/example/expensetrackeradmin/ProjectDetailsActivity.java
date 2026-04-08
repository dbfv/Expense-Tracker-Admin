package com.example.expensetrackeradmin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.DecimalFormat;

import models.Project;

public class ProjectDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvBudget, tvManager, tvSpent, tvProgressPct;
    private Chip chipStatus, chipId;
    private ProgressBar pbProgress;
    private ExtendedFloatingActionButton fabAddExpense;
    private DatabaseHelper dbHelper;
    private String projectId;

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

        // Nạp Menu Edit vào Toolbar
        toolbar.inflateMenu(R.menu.menu_project_details);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                // TODO: Chuyển sang EditProjectActivity
                Toast.makeText(this, "Chuẩn bị mở form Edit...", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Bắt sự kiện Add Expense
        fabAddExpense.setOnClickListener(v -> {
            // TODO: Chuyển sang AddExpenseActivity kèm theo projectId
            Toast.makeText(this, "Chuẩn bị mở form New Expense...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (projectId != null) {
            loadProjectDetails();
        } else {
            Toast.makeText(this, "Project not found!", Toast.LENGTH_SHORT).show();
            finish();
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
}