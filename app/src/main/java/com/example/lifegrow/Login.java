package com.example.lifegrow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginActivity"; // <-- TAG for Logcat
    private EditText e1, e2;
    private Button b1, b2,b3;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        e1 = findViewById(R.id.editTextText);
        e2 = findViewById(R.id.editTextText2);
        b1 = findViewById(R.id.button);
        b2 = findViewById(R.id.button1);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        Log.d(TAG, "onCreate: isLoggedIn = " + isLoggedIn);
        // Auto-login if user is already logged in
        if (isLoggedIn && mAuth.getCurrentUser() != null) {
            navigateToMain();
            return;
        }

        b1.setOnClickListener(view -> {
            String email = e1.getText().toString().trim();
            String password = e2.getText().toString().trim();

            if (email.isEmpty()) {
                e1.setError("Email is required");
                return;
            }
            if (password.isEmpty()) {
                e2.setError("Password is required");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveLoginState(); // Save login state
                                checkIfUsernameExists(sanitizeEmail(email));
                            }
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                            Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        b2.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
            finish();
        });


    }

    // Save login state in SharedPreferences
    private void saveLoginState() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }



    private void checkIfUsernameExists(String userEmail) {
        db.collection("users").document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("username")) {
                        navigateToMain();
                    } else {
                        askForUsername(userEmail);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(Login.this, "Failed to check username", Toast.LENGTH_SHORT).show());
    }

    private void askForUsername(String userEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Username");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String username = input.getText().toString().trim();
            if (!username.isEmpty()) {
                saveUsername(userEmail, username);
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                askForUsername(userEmail);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveUsername(String userEmail, String username) {
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("username", username);

        db.collection("users").document(userEmail)
                .set(userUpdate)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Username saved!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save username", Toast.LENGTH_SHORT).show());
    }

    private void navigateToMain() {
        startActivity(new Intent(Login.this, MainActivity.class));
        finish();
    }

    private String sanitizeEmail(String email) {
        return email.replace(".", "_");
    }
}
