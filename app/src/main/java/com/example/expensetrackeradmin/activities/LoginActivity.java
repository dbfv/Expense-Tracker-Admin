package com.example.expensetrackeradmin.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetrackeradmin.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.example.expensetrackeradmin.helpers.DatabaseHelper;
import com.example.expensetrackeradmin.models.Employee;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String ROLE_ADMIN = "admin";
    private Button btnGoogleLogin;

    // Google & Firebase authentication tools
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;

    // ActivityResultLauncher to handle the Google Sign-In intent result
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                        // Authenticate with Firebase using the Google token
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign in failed", e);
                        Toast.makeText(this, "Google sign-in failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Local Database
        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        btnGoogleLogin.setOnClickListener(v -> initiateGoogleSignIn());
    }

    private void initiateGoogleSignIn() {
        // Clear cached Google account so the account chooser is shown every login attempt.
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String email = user.getEmail();
                            verifyEmployeeAccess(email);
                        }
                    } else {
                        Log.w(TAG, "Firebase authentication failed", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyEmployeeAccess(String email) {
        Employee employee = dbHelper.getEmployeeByEmail(email);

        if (employee != null && ROLE_ADMIN.equalsIgnoreCase(employee.getRole())) {
            // Authorized admin -> Proceed to Dashboard
            Toast.makeText(this, "Welcome to the system!", Toast.LENGTH_SHORT).show();

            // Navigate to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Destroy the login activity

        } else {
            // Unauthorized or non-admin email -> Sign out and show error
            signOutCompletely();
            Toast.makeText(this, "Access denied: Admin role required.", Toast.LENGTH_LONG).show();
        }
    }

    private void signOutCompletely() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
        mGoogleSignInClient.revokeAccess();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                verifyEmployeeAccess(email);
            } else {
                signOutCompletely();
            }
        }
    }
}