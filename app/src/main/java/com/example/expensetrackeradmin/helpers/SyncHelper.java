package com.example.expensetrackeradmin.helpers;

import android.content.Context;
import android.util.Log;

import com.example.expensetrackeradmin.models.Employee;
import com.example.expensetrackeradmin.models.Expense;
import com.example.expensetrackeradmin.models.Project;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
        syncProjectsToCloud(null);
    }

    public void syncEmployeesToCloud() {
        syncEmployeesToCloud(null);
    }

    public void syncExpensesToCloud() {
        syncExpensesToCloud(null);
    }

    public void syncAllPushThenPull() {
        syncAllPushThenPull(null);
    }

    public void syncAllPushThenPull(Runnable onComplete) {
        syncAllPushThenPull(false, onComplete);
    }

    public void syncAllPushThenPull(boolean forceFullPush, Runnable onComplete) {
        syncPendingProjectDeletions(() ->
            syncPendingExpenseDeletions(() ->
                syncProjectsToCloud(forceFullPush, () ->
                    syncEmployeesToCloud(forceFullPush, () ->
                        syncExpensesToCloud(forceFullPush, () ->
                            pullProjectsFromCloud(() ->
                                pullEmployeesFromCloud(() ->
                                    pullExpensesFromCloud(onComplete)
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    public void deleteProjectFromCloud(String projectId, Runnable onComplete) {
        if (isBlank(projectId)) {
            runCompletion(onComplete);
            return;
        }

        String normalizedProjectId = projectId.trim();
        Task<Void> removeProjectTask = mDatabase.child("projects").child(normalizedProjectId).removeValue();

        mDatabase.child("expenses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Task<?>> deleteTasks = new ArrayList<>();
                deleteTasks.add(removeProjectTask);

                for (DataSnapshot item : snapshot.getChildren()) {
                    String expenseProjectId = readText(item, "projectId");
                    if (normalizedProjectId.equals(expenseProjectId)) {
                        deleteTasks.add(item.getRef().removeValue());
                    }
                }

                completeWhenTasksFinish(deleteTasks, onComplete);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load expenses for cloud project deletion", error.toException());
                removeProjectTask.addOnCompleteListener(unused -> runCompletion(onComplete));
            }
        });
    }

    public void deleteExpenseFromCloud(String expenseId, Runnable onComplete) {
        if (isBlank(expenseId)) {
            runCompletion(onComplete);
            return;
        }

        mDatabase.child("expenses").child(expenseId.trim()).removeValue()
                .addOnCompleteListener(task -> runCompletion(onComplete));
    }

    private void syncProjectsToCloud(Runnable onComplete) {
        syncProjectsToCloud(false, onComplete);
    }

    private void syncPendingProjectDeletions(Runnable onComplete) {
        List<String> pendingProjectIds = localDb.getPendingProjectDeletions();
        syncPendingProjectDeletionAt(pendingProjectIds, 0, onComplete);
    }

    private void syncPendingProjectDeletionAt(List<String> projectIds, int index, Runnable onComplete) {
        if (projectIds == null || index >= projectIds.size()) {
            runCompletion(onComplete);
            return;
        }

        String projectId = projectIds.get(index);
        if (isBlank(projectId)) {
            syncPendingProjectDeletionAt(projectIds, index + 1, onComplete);
            return;
        }

        deleteProjectFromCloud(projectId, () -> {
            localDb.clearPendingProjectDeletion(projectId);
            syncPendingProjectDeletionAt(projectIds, index + 1, onComplete);
        });
    }

    private void syncPendingExpenseDeletions(Runnable onComplete) {
        List<String> pendingExpenseIds = localDb.getPendingExpenseDeletions();
        syncPendingExpenseDeletionAt(pendingExpenseIds, 0, onComplete);
    }

    private void syncPendingExpenseDeletionAt(List<String> expenseIds, int index, Runnable onComplete) {
        if (expenseIds == null || index >= expenseIds.size()) {
            runCompletion(onComplete);
            return;
        }

        String expenseId = expenseIds.get(index);
        if (isBlank(expenseId)) {
            syncPendingExpenseDeletionAt(expenseIds, index + 1, onComplete);
            return;
        }

        deleteExpenseFromCloud(expenseId, () -> {
            localDb.clearPendingExpenseDeletion(expenseId);
            syncPendingExpenseDeletionAt(expenseIds, index + 1, onComplete);
        });
    }

    private void syncProjectsToCloud(boolean forceFullPush, Runnable onComplete) {
        List<Project> projectsToSync = forceFullPush
                ? localDb.getAllProjectsForSync()
                : localDb.getUnsyncedProjects();
        List<Task<?>> writeTasks = new ArrayList<>();

        for (Project project : projectsToSync) {
            if (project == null || project.getProjectId() == null || project.getProjectId().trim().isEmpty()) {
                continue;
            }

                Map<String, Object> payload = new HashMap<>();
                payload.put("projectId", project.getProjectId());
                payload.put("name", project.getName());
                payload.put("password", project.getPassword());
                payload.put("passwordHash", project.getPasswordHash());
                payload.put("description", project.getDescription());
                payload.put("startDate", project.getStartDate());
                payload.put("endDate", project.getEndDate());
                payload.put("manager", project.getManager());
                payload.put("status", project.getStatus());
                payload.put("budget", project.getBudget());
                payload.put("specialRequirements", project.getSpecialRequirements());
                payload.put("clientInfo", project.getClientInfo());
                payload.put("spentAmount", project.getSpentAmount());

            Task<Void> task = mDatabase.child("projects").child(project.getProjectId())
                    .updateChildren(payload)
                    .addOnSuccessListener(unused -> localDb.setSyncStatus(
                            DatabaseHelper.TABLE_PROJECTS,
                            DatabaseHelper.COLUMN_PROJECT_ID,
                            project.getProjectId(),
                            1
                    ))
                    .addOnFailureListener(error -> Log.e(TAG, "Failed to sync project: " + project.getProjectId(), error));
            writeTasks.add(task);
        }

        completeWhenTasksFinish(writeTasks, onComplete);
    }

    private void syncEmployeesToCloud(Runnable onComplete) {
        syncEmployeesToCloud(false, onComplete);
    }

    private void syncEmployeesToCloud(boolean forceFullPush, Runnable onComplete) {
        List<Employee> employeesToSync = forceFullPush
                ? localDb.getAllEmployeesForSync()
                : localDb.getUnsyncedEmployees();
        List<Task<?>> writeTasks = new ArrayList<>();

        for (Employee employee : employeesToSync) {
            if (employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
                continue;
            }

                Map<String, Object> payload = new HashMap<>();
                payload.put("id", employee.getId());
                payload.put("name", employee.getName());
                payload.put("code", employee.getCode());
                payload.put("email", employee.getEmail());
                payload.put("role", employee.getRole());
                payload.put("joined_projects", employee.getJoinedProjects());
                payload.put("favorite_projects", employee.getFavoriteProjects());

            Task<Void> task = mDatabase.child("employees").child(employee.getId())
                    .updateChildren(payload)
                    .addOnSuccessListener(unused -> localDb.setSyncStatus(
                            DatabaseHelper.TABLE_EMPLOYEES,
                            DatabaseHelper.COLUMN_EMP_ID,
                            employee.getId(),
                            1
                    ))
                    .addOnFailureListener(error -> Log.e(TAG, "Failed to sync employee: " + employee.getId(), error));
            writeTasks.add(task);
        }

        completeWhenTasksFinish(writeTasks, onComplete);
    }

    private void syncExpensesToCloud(Runnable onComplete) {
        syncExpensesToCloud(false, onComplete);
    }

    private void syncExpensesToCloud(boolean forceFullPush, Runnable onComplete) {
        List<Expense> expensesToSync = forceFullPush
                ? localDb.getAllExpensesForSync()
                : localDb.getUnsyncedExpenses();
        List<Task<?>> writeTasks = new ArrayList<>();

        for (Expense expense : expensesToSync) {
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

            Task<Void> task = mDatabase.child("expenses").child(expense.getExpenseId())
                    .setValue(payload)
                    .addOnSuccessListener(unused -> localDb.setSyncStatus(
                            DatabaseHelper.TABLE_EXPENSES,
                            DatabaseHelper.COLUMN_EXPENSE_ID,
                            expense.getExpenseId(),
                            1
                    ))
                    .addOnFailureListener(error -> Log.e(TAG, "Failed to sync expense: " + expense.getExpenseId(), error));
            writeTasks.add(task);
        }

        completeWhenTasksFinish(writeTasks, onComplete);
    }

    private void pullProjectsFromCloud(Runnable onComplete) {
        mDatabase.child("projects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    Project project = item.getValue(Project.class);
                    if (project == null) {
                        continue;
                    }

                    if (project.getProjectId() == null || project.getProjectId().trim().isEmpty()) {
                        project.setProjectId(item.getKey());
                    }

                    localDb.upsertProjectFromCloud(project);
                }
                runCompletion(onComplete);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to pull projects from cloud", error.toException());
                runCompletion(onComplete);
            }
        });
    }

    private void pullEmployeesFromCloud(Runnable onComplete) {
        mDatabase.child("employees").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    Employee employee = parseEmployeeFromSnapshot(item);
                    if (employee == null) {
                        continue;
                    }

                    if (employee.getId() == null || employee.getId().trim().isEmpty()) {
                        employee.setId(item.getKey());
                    }

                    localDb.upsertEmployeeFromCloud(employee);
                }
                runCompletion(onComplete);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to pull employees from cloud", error.toException());
                runCompletion(onComplete);
            }
        });
    }

    private Employee parseEmployeeFromSnapshot(DataSnapshot snapshot) {
        String employeeId = firstNonBlank(readText(snapshot, "id"), snapshot.getKey());
        if (isBlank(employeeId)) {
            return null;
        }

        String role = readText(snapshot, "role");
        if (isBlank(role)) {
            role = "employee";
        }

        Employee employee = new Employee(
                employeeId,
                readText(snapshot, "name"),
                readText(snapshot, "code"),
                readText(snapshot, "email"),
                role
        );
        employee.setJoinedProjects(readStringList(snapshot, "joined_projects", "joinedProjects"));
        employee.setFavoriteProjects(readStringList(snapshot, "favorite_projects", "favoriteProjects"));
        return employee;
    }

    private void pullExpensesFromCloud(Runnable onComplete) {
        mDatabase.child("expenses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    Expense expense = parseExpenseFromSnapshot(item);
                    if (expense == null) {
                        continue;
                    }

                    List<String> imageUrls = parseExpenseImageUrls(item.child("images"));
                    localDb.upsertExpenseFromCloud(expense, imageUrls);
                }
                runCompletion(onComplete);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to pull expenses from cloud", error.toException());
                runCompletion(onComplete);
            }
        });
    }

    private Expense parseExpenseFromSnapshot(DataSnapshot snapshot) {
        String expenseId = firstNonBlank(readText(snapshot, "expenseId"), snapshot.getKey());
        String projectId = readText(snapshot, "projectId");

        if (isBlank(expenseId) || isBlank(projectId)) {
            return null;
        }

        Expense expense = new Expense(
                expenseId,
                projectId,
                readText(snapshot, "date"),
                readDouble(snapshot, "amount"),
                readText(snapshot, "currency"),
                readText(snapshot, "type"),
                readText(snapshot, "paymentMethod"),
                readText(snapshot, "claimant"),
                readText(snapshot, "status"),
                readNullableText(snapshot, "description"),
                readNullableText(snapshot, "location")
        );
        return expense;
    }

    private List<String> parseExpenseImageUrls(DataSnapshot imagesSnapshot) {
        List<String> imageUrls = new ArrayList<>();
        if (imagesSnapshot == null || !imagesSnapshot.exists()) {
            return imageUrls;
        }

        for (DataSnapshot imageItem : imagesSnapshot.getChildren()) {
            Object rawValue = imageItem.getValue();
            if (rawValue instanceof String) {
                String imageUrl = ((String) rawValue).trim();
                if (!imageUrl.isEmpty()) {
                    imageUrls.add(imageUrl);
                }
                continue;
            }

            if (rawValue instanceof Map) {
                Object imageUrlObject = ((Map<?, ?>) rawValue).get("imageUrl");
                if (imageUrlObject != null) {
                    String imageUrl = String.valueOf(imageUrlObject).trim();
                    if (!imageUrl.isEmpty()) {
                        imageUrls.add(imageUrl);
                    }
                }
            }
        }

        return imageUrls;
    }

    private String readText(DataSnapshot snapshot, String key) {
        Object rawValue = snapshot.child(key).getValue();
        return rawValue == null ? "" : String.valueOf(rawValue);
    }

    private String readNullableText(DataSnapshot snapshot, String key) {
        String value = readText(snapshot, key).trim();
        return value.isEmpty() ? null : value;
    }

    private List<String> readStringList(DataSnapshot snapshot, String... keys) {
        List<String> values = new ArrayList<>();
        if (snapshot == null || keys == null) {
            return values;
        }

        for (String key : keys) {
            if (isBlank(key)) {
                continue;
            }

            DataSnapshot child = snapshot.child(key);
            if (!child.exists()) {
                continue;
            }

            Object rawValue = child.getValue();
            appendStringValues(values, rawValue);
            if (!values.isEmpty()) {
                return values;
            }
        }

        return values;
    }

    private void appendStringValues(List<String> target, Object rawValue) {
        if (target == null || rawValue == null) {
            return;
        }

        if (rawValue instanceof List) {
            for (Object item : (List<?>) rawValue) {
                if (item == null) {
                    continue;
                }
                String value = String.valueOf(item).trim();
                if (!value.isEmpty()) {
                    target.add(value);
                }
            }
            return;
        }

        if (rawValue instanceof Map) {
            for (Object item : ((Map<?, ?>) rawValue).values()) {
                if (item == null) {
                    continue;
                }
                String value = String.valueOf(item).trim();
                if (!value.isEmpty()) {
                    target.add(value);
                }
            }
        }
    }

    private double readDouble(DataSnapshot snapshot, String key) {
        Object rawValue = snapshot.child(key).getValue();
        if (rawValue instanceof Number) {
            return ((Number) rawValue).doubleValue();
        }

        if (rawValue == null) {
            return 0d;
        }

        try {
            return Double.parseDouble(String.valueOf(rawValue));
        } catch (NumberFormatException ignored) {
            return 0d;
        }
    }

    private void completeWhenTasksFinish(List<Task<?>> tasks, Runnable onComplete) {
        if (tasks == null || tasks.isEmpty()) {
            runCompletion(onComplete);
            return;
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(unused -> runCompletion(onComplete));
    }

    private void runCompletion(Runnable onComplete) {
        if (onComplete != null) {
            onComplete.run();
        }
    }

    private String firstNonBlank(String primaryValue, String fallbackValue) {
        if (!isBlank(primaryValue)) {
            return primaryValue.trim();
        }
        return fallbackValue == null ? "" : fallbackValue.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}