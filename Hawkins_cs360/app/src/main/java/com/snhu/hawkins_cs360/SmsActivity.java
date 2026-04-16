package com.snhu.hawkins_cs360;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SmsActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean isPrefSet = sharedPrefs.getBoolean("SMS_PREF_SET", false);

        if (isPrefSet) {
            proceedToDashboard();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sms);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button grantSmsBtn = findViewById(R.id.grant_sms_btn);
        Button noSmsBtn = findViewById(R.id.no_sms_btn);

        grantSmsBtn.setOnClickListener(v -> requestSmsPermission());
        noSmsBtn.setOnClickListener(v -> rejectSmsPermission());
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        } else {
            // Already granted somehow
            saveSmsPreference(true);
            proceedToDashboard();
        }
    }

    private void rejectSmsPermission() {
        saveSmsPreference(false);
        Toast.makeText(this, "Continuing without SMS notifications.", Toast.LENGTH_SHORT).show();
        proceedToDashboard();
    }

    private void saveSmsPreference(boolean granted) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("SMS_PREF_SET", true);
        editor.putBoolean("SMS_GRANTED", granted);
        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
                saveSmsPreference(true);
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
                saveSmsPreference(false);
            }
            proceedToDashboard();
        }
    }

    private void proceedToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
