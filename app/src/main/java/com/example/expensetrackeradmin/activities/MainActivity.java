package com.example.expensetrackeradmin.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetrackeradmin.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import com.example.expensetrackeradmin.adapters.ProjectAdapter;
import com.example.expensetrackeradmin.helpers.DatabaseHelper;
import com.example.expensetrackeradmin.helpers.SyncTriggerHelper;
import com.example.expensetrackeradmin.models.Project;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ExtendedFloatingActionButton fabAddProject;
    private MaterialButton btnSyncNow;

    private RecyclerView rvProjects;
    private ProjectAdapter adapter;
    private List<Project> projectList;
    private DatabaseHelper dbHelper;
    private com.google.android.material.textfield.TextInputEditText etSearchProject;
    private List<Project> allProjectsList = new ArrayList<>();
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private long lastAutoSyncAttemptMs = 0L;
    private static final long AUTO_SYNC_THROTTLE_MS = 5000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        btnSyncNow = findViewById(R.id.btnSyncNow);
        btnSyncNow.setOnClickListener(v -> triggerManualSync(true));

        fabAddProject = findViewById(R.id.fabAddProject);
        fabAddProject.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddProjectActivity.class);
            startActivity(intent);
        });

        etSearchProject = findViewById(R.id.etSearchProject);

        etSearchProject.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProjects(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        rvProjects = findViewById(R.id.rvProjects);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));

        projectList = new ArrayList<>();
        adapter = new ProjectAdapter(projectList);
        rvProjects.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerNetworkCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjectsFromDB();
        attemptAutoSync();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterNetworkCallback();
    }

    private void loadProjectsFromDB() {
        List<Project> freshList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_PROJECTS, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_NAME));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_PASSWORD));
                String passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_PASSWORD_HASH));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_DESC));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_START_DATE));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_END_DATE));
                String manager = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_MANAGER));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_STATUS));
                double budget = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_BUDGET));
                String specialReq = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_SPECIAL_REQ));
                String clientInfo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_CLIENT));

                Project p = new Project(id, name, password, passwordHash, description, startDate, endDate, manager, status, budget, specialReq, clientInfo);

                double spent = dbHelper.getTotalExpenseForProject(id);
                p.setSpentAmount(spent);
                freshList.add(p);
            }
            cursor.close();
        }
        allProjectsList.clear();
        allProjectsList.addAll(freshList);
        adapter.updateData(allProjectsList);
    }
    private void filterProjects(String text) {
        List<Project> filteredList = new ArrayList<>();

        for (Project project : allProjectsList) {
            if (project.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(project);
            }
        }
        adapter.updateData(filteredList);
    }

    private void triggerSync(boolean forceFullPush, boolean showFeedback) {
        if (!SyncTriggerHelper.isNetworkAvailable(this)) {
            if (showFeedback) {
                Toast.makeText(this, "No network connection.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        SyncTriggerHelper.attemptSyncIfOnline(this, forceFullPush, () -> runOnUiThread(this::loadProjectsFromDB));
        if (showFeedback) {
            Toast.makeText(this, "Sync started", Toast.LENGTH_SHORT).show();
        }
    }

    private void triggerManualSync(boolean showFeedback) {
        triggerSync(true, showFeedback);
    }

    private void attemptAutoSync() {
        long now = System.currentTimeMillis();
        if (now - lastAutoSyncAttemptMs < AUTO_SYNC_THROTTLE_MS) {
            return;
        }
        lastAutoSyncAttemptMs = now;
        triggerSync(false, false);
    }

    private void registerNetworkCallback() {
        if (networkCallback != null) {
            return;
        }

        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return;
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> attemptAutoSync());
            }
        };

        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } catch (SecurityException ignored) {
        }
    }

    private void unregisterNetworkCallback() {
        if (connectivityManager == null || networkCallback == null) {
            return;
        }

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (Exception ignored) {
        }

        networkCallback = null;
    }
}