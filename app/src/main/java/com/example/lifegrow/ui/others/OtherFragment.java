/*
3)Progress module
4)Notification
5)Correction of basic things like status update if task done, add sound if task done,calendar automically display the date when open that module for that day,etc
 */

package com.example.lifegrow.ui.others;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.lifegrow.Login;
import com.example.lifegrow.R;
import com.example.lifegrow.ui.others.progress.ProgressActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public class OtherFragment extends Fragment {
    private FirebaseAuth mAuth;
    private TextView usernameTextView;
    private FirebaseFirestore db;

    private static final String TAG = "OtherFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_other, container, false);


        TextView privacyPolicy = root.findViewById(R.id.privacyPolicy);
        TextView termsConditions = root.findViewById(R.id.termsConditions);
        TextView contactSupport = root.findViewById(R.id.contactSupport);
// Privacy Policy Dialog
        privacyPolicy.setOnClickListener(v -> showInfoDialog(
                "Privacy Policy",
                "🔒 Privacy & Data Handling\n\n" +
                        "LifeGrow collects minimal personal information such as name and email for account creation and personalized productivity insights. We do not access or store any sensitive information unless explicitly permitted by the user.\n\n" +
                        "📂 Data Storage\n" +
                        "All task-related data is securely stored in Google Firebase Firestore and is associated with the authenticated user account.\n\n" +
                        "🧠 Background Services\n" +
                        "LifeGrow uses background services only for task reminders, notification alerts, and productivity limit enforcement (like social media blocking). These services are optimized for battery efficiency and stop automatically if not needed.\n\n" +
                        "🚫 Third-party Sharing\n" +
                        "We do NOT share, sell, or monetize any user data. Only you can access your task records.\n\n" +
                        "🔐 Encryption\n" +
                        "All communication with our servers is encrypted using HTTPS (TLS 1.3).\n\n" +
                        "📱 Permissions Used\n" +
                        "• Overlay Permission – For blocking overlays\n" +
                        "• Accessibility – For usage tracking and app blocking\n" +
                        "• Notification Access – To show reminders\n\n" +
                        "🧾 Version: v1.3.5 | Last updated: April 10, 2025"
        ));

// Terms & Conditions Dialog
        termsConditions.setOnClickListener(v -> showInfoDialog(
                "Terms & Conditions",
                "📜 Terms of Use\n\n" +
                        "By using LifeGrow, you agree to:\n" +
                        "1️⃣ Use the app only for legal and ethical purposes.\n" +
                        "2️⃣ Not exploit, reverse-engineer, or tamper with the codebase.\n" +
                        "3️⃣ Take responsibility for your content and activities inside the app.\n\n" +
                        "🔧 Fair Usage Policy\n" +
                        "We may limit access to certain features if we detect misuse, automation, or spam-like behavior. Users violating these terms may be suspended without notice.\n\n" +
                        "📬 Communication\n" +
                        "You agree to receive important updates and service announcements related to your account.\n\n" +
                        "📅 Termination\n" +
                        "You can delete your account at any time. We reserve the right to suspend accounts that breach terms.\n\n" +
                        "📌 Governing Law\n" +
                        "All disputes will be governed under the jurisdiction of Indian Law.\n\n" +
                        "🌐 Developed by: Harish\n" +
                        "Company: LifeGrow Innovations\n" +
                        "Contact: 9080001407\n" +
                        "Version: 1.3.5 | Release Date: April 10, 2025"
        ));

// Contact Support Dialog
        contactSupport.setOnClickListener(v -> showInfoDialog(
                "Contact Support",
                "📞 Support & Feedback\n\n" +
                        "Need help or want to suggest a feature?\n\n" +
                        "📧 Email: harishchat448@gmail.com\n" +
                        "📱 Phone: 9080001407\n\n" +
                        "🌐 Website: www.lifegrow.app\n" +
                        "💡 Tip:\n" +
                        "Attach screenshots and a brief description when reporting bugs—it helps us fix them faster!\n\n" +
                        "🧑‍💻 Developer: Harish\n" +
                        "🛠️ App: LifeGrow – Smart Productivity Assistant\n" +
                        "🔧 Tools Used: Android Studio, Firebase, Kotlin/Java\n" +
                        "📦 Version: 1.3.5\n" +
                        "🕐 Support Hours: Mon–Fri, 10AM–6PM IST"
        ));




        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameTextView = root.findViewById(R.id.usernameTextView);
        Button logoutButton = root.findViewById(R.id.logoutButton);
        TextView socialMediaBlocker = root.findViewById(R.id.socialMediaBlocker);
        socialMediaBlocker.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireContext())) {
                Toast.makeText(getContext(), "Please grant overlay permission to continue!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + requireActivity().getPackageName()));
                startActivity(intent);
            } else {
                Intent intent = new Intent(getActivity(), SocialMediaBlockerActivity.class);
                startActivity(intent);
            }
        });


        TextView aiExtensionBlocker = root.findViewById(R.id.aiExtensionBlocker);
        aiExtensionBlocker.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AIExtensionBlockerActivity.class);
            startActivity(intent);
        });

        TextView progressModule = root.findViewById(R.id.progressModule);
        progressModule.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProgressActivity.class);
            startActivity(intent);
        });




        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            if (email != null) {
                fetchUsername(email);
            } else {
                usernameTextView.setText("Guest");
            }
        } else {
            usernameTextView.setText("Guest");
        }

        logoutButton.setOnClickListener(v -> logoutUser());

        return root;
    }
    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }


    private void fetchUsername(String userEmail) {
        String safeEmail = sanitizeEmail(userEmail); // Use same logic as Login
        Log.d(TAG, "Fetching username for: " + safeEmail);

        db.collection("users").document(safeEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("username")) {
                        String username = documentSnapshot.getString("username");

                        if (username != null && !username.isEmpty()) {
                            // Capitalize first letter
                            String formattedUsername = username.substring(0, 1).toUpperCase() + username.substring(1);
                            usernameTextView.setText(formattedUsername);
                        } else {
                            usernameTextView.setText("User");
                        }
                    }
                    else {
                        Log.d(TAG, "Username not found, showing fallback");

                        // Capitalize first letter of the email
                        String formattedEmail = userEmail.substring(0, 1).toUpperCase() + userEmail.substring(1);
                        usernameTextView.setText(formattedEmail);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching username: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to load user info", Toast.LENGTH_SHORT).show();
                    usernameTextView.setText("Unknown User");
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        SharedPreferences prefs = requireActivity().getSharedPreferences("LoginPrefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private String sanitizeEmail(String email) {
        return email.replace(".", "_"); // Same as Login.java
    }
}
