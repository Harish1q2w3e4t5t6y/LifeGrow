package com.example.lifegrow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText e1, e2;
    private Button b1, b2;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        e1 = findViewById(R.id.editTextText);
        e2 = findViewById(R.id.editTextText2);
        b1 = findViewById(R.id.button);
        b2 = findViewById(R.id.button1);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
            if (password.length() < 6) {
                e2.setError("Password must be at least 6 characters long");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            saveUserToFirestore(email);
                            Toast.makeText(Register.this, "Account created successfully.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Register.this, Login.class));
                            finish();
                        } else {
                            Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        b2.setOnClickListener(view -> startActivity(new Intent(this, Login.class)));
    }

    private void saveUserToFirestore(String email) {
        String sanitizedEmail = email.replace(".", "_"); // Firestore-safe email
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        db.collection("users").document(sanitizedEmail).set(user);
    }
}
