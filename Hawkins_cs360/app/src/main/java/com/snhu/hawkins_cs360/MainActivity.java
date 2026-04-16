package com.snhu.hawkins_cs360;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private Button loginBtn;
    private Button registerBtn;
    
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        
        // If already logged in, proceed to Dashboard (or SmsActivity first)
        long currentUserId = sharedPrefs.getLong("USER_ID", -1);
        if (currentUserId != -1) {
            proceedToNextActivity();
            return; // Don't setup login UI
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginBtn = findViewById(R.id.login_btn);
        registerBtn = findViewById(R.id.register_btn);

        loginBtn.setOnClickListener(v -> attemptLogin());
        registerBtn.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptLogin() {
        String user = usernameInput.getText().toString().trim();
        String pass = passwordInput.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = dbHelper.authenticateUser(user, pass);
        if (userId != -1) {
            saveUserId(userId);
            checkGoalWeightAndProceed(userId);
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private void attemptRegistration() {
        String user = usernameInput.getText().toString().trim();
        String pass = passwordInput.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = dbHelper.registerUser(user, pass);
        if (userId != -1) {
            saveUserId(userId);
            promptForGoalWeight(userId);
        } else {
            Toast.makeText(this, "Registration failed or username exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserId(long userId) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong("USER_ID", userId);
        editor.apply();
    }

    private void checkGoalWeightAndProceed(long userId) {
        float goal = dbHelper.getGoalWeight(userId);
        if (goal == -1) {
            promptForGoalWeight(userId);
        } else {
            proceedToNextActivity();
        }
    }

    private void promptForGoalWeight(long userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Goal Weight");
        builder.setMessage("Please enter your target goal weight (lbs):");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String val = input.getText().toString();
                if (!val.isEmpty()) {
                    try {
                        float weight = Float.parseFloat(val);
                        dbHelper.setGoalWeight(userId, weight);
                        proceedToNextActivity();
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Invalid weight format", Toast.LENGTH_SHORT).show();
                        promptForGoalWeight(userId); // Ask again
                    }
                }
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void proceedToNextActivity() {
        // First go to SMS Activity to ensure perms
        Intent intent = new Intent(this, SmsActivity.class);
        startActivity(intent);
        finish();
    }
}