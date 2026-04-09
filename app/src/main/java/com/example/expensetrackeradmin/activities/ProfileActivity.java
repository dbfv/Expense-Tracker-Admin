package com.example.expensetrackeradmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.expensetrackeradmin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.expensetrackeradmin.helpers.DatabaseHelper;
import com.example.expensetrackeradmin.models.Employee;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileCode;
    private TextView tvProfileEmail;
    private TextView tvProfileRole;
    private Button btnLogout;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvProfileName = findViewById(R.id.tvProfileNameValue);
        tvProfileCode = findViewById(R.id.tvProfileCodeValue);
        tvProfileEmail = findViewById(R.id.tvProfileEmailValue);
        tvProfileRole = findViewById(R.id.tvProfileRoleValue);
        btnLogout = findViewById(R.id.btnLogout);

        loadCurrentUserProfile();
        setupLogout();
    }

    private void loadCurrentUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "User session expired.", Toast.LENGTH_SHORT).show();
            goToLoginAndClearBackStack();
            return;
        }

        String email = currentUser.getEmail();
        Employee employee = dbHelper.getEmployeeByEmail(email);

        if (employee == null) {
            tvProfileName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "-");
            tvProfileCode.setText("-");
            tvProfileEmail.setText(email);
            tvProfileRole.setText("-");
            return;
        }

        tvProfileName.setText(employee.getName());
        tvProfileCode.setText(employee.getCode());
        tvProfileEmail.setText(employee.getEmail());
        tvProfileRole.setText(employee.getRole());
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            // 1. Sign out Firebase token and session
            FirebaseAuth.getInstance().signOut();

            // 2. Send user back to Login screen
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);

            // 3. Clear entire back stack to prevent returning with Back button
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // 4. Start Login and close current activity
            startActivity(intent);
            finish();
        });
    }

    private void goToLoginAndClearBackStack() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
