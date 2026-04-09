package com.example.expensetrackeradmin.helpers;

import android.content.Context;
import android.util.Log;

import com.example.expensetrackeradmin.models.Employee;
import com.example.expensetrackeradmin.models.Expense;
import com.example.expensetrackeradmin.models.Project;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncHelper {
    private static final String TAG = "SyncHelper";

    private final DatabaseReference mDatabase;
    private final DatabaseHelper localDb;

    public SyncHelper(Context context) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        localDb = new DatabaseHelper(context);
    }

    public void syncProjectsToCloud() {
        List<Project> unsyncedProjects = localDb.getUnsyncedProjects();

        for (Project project : unsyncedProjects) {
            if (project == null || project.getProjectId() == null || project.getProjectId().trim().isEmpty()) {
                continue;
            }

            mDatabase.child("projects").child(project.getProjectId())
                    .setValue(project)
                    .addOnSuccessListener(unused -> localDb.setSyncStatus(
                            DatabaseHelper.TABLE_PROJECTS,
                            DatabaseHelper.COLUMN_PROJECT_ID,
                            project.getProjectId(),
                            1
                    ))
                    .addOnFailureListener(error -> Log.e(TAG, "Failed to sync project: " + project.getProjectId(), error));
        }
    }

    public void syncEmployeesToCloud() {
        List<Employee> unsyncedEmployees = localDb.getUnsyncedEmployees();

        for (Employee employee : unsyncedEmployees) {
            if (employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
                continue;
            }

            mDatabase.child("employees").child(employee.getId())
                    .setValue(employee)
                    .addOnSuccessListener(unused -> localDb.setSyncStatus(
                            DatabaseHelper.TABLE_EMPLOYEES,
                            DatabaseHelper.COLUMN_EMP_ID,
                            employee.getId(),
                            1
                    ))
                    .addOnFailureListener(error -> Log.e(TAG, "Failed to sync employee: " + employee.getId(), error));
        }
    }

    public void syncExpensesToCloud() {
        List<Expense> unsyncedExpenses = localDb.getUnsyncedExpenses();

        for (Expense expense : unsyncedExpenses) {
            if (expense == null || expense.getExpenseId() == null || expense.getExpenseId().trim().isEmpty()) {
                continue;
            }

            List<String> imageUrls = localDb.getImagesForExpense(expense.getExpenseId());

            Map<String, Object> payload = new HashMap<>();
            payload.put("expenseId", expense.getExpenseId());
            payload.put("projectId", expense.getProjectId());
            payload.put("date", expense.getDate());
            payload.put("amount", expense.getAmount());
            payload.put("currency", expense.getCurrency());
            payload.put("type", expense.getType());
            payload.put("paymentMethod", expense.getPaymentMethod());
            payload.put("claimant", expense.getClaimant());
            payload.put("status", expense.getStatus());
            payload.put("description", expense.getDescription());
            payload.put("location", expense.getLocation());
            payload.put("images", imageUrls);

            mDatabase.child("expenses").child(expense.getExpenseId())
                    .setValue(payload)
                    .addOnSuccessListener(unused -> localDb.setSyncStatus(
                            DatabaseHelper.TABLE_EXPENSES,
                            DatabaseHelper.COLUMN_EXPENSE_ID,
                            expense.getExpenseId(),
                            1
                    ))
                    .addOnFailureListener(error -> Log.e(TAG, "Failed to sync expense: " + expense.getExpenseId(), error));
        }
    }
}