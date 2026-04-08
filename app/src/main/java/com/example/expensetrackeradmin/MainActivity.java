package com.example.expensetrackeradmin;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import models.Project;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ExtendedFloatingActionButton fabAddProject;

    private RecyclerView rvProjects;
    private ProjectAdapter adapter;
    private List<Project> projectList;
    private DatabaseHelper dbHelper;
    private com.google.android.material.textfield.TextInputEditText etSearchProject;
    private List<Project> allProjectsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);

        fabAddProject = findViewById(R.id.fabAddProject);
        fabAddProject.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddProjectActivity.class);
            startActivity(intent);
        });

        etSearchProject = findViewById(R.id.etSearchProject);

        // Bắt sự kiện gõ chữ đến đâu lọc đến đó
        etSearchProject.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Gọi hàm lọc dữ liệu mỗi khi người dùng gõ phím
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
    protected void onResume() {
        super.onResume();
        loadProjectsFromDB();
    }

    private void loadProjectsFromDB() {
        List<Project> freshList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_PROJECTS, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_DESC));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_START_DATE));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_END_DATE));
                String manager = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_MANAGER));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_STATUS));
                double budget = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_BUDGET));
                String specialReq = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_SPECIAL_REQ));
                String clientInfo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROJECT_CLIENT));

                Project p = new Project(id, name, description, startDate, endDate, manager, status, budget, specialReq, clientInfo);

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
}