package com.snhu.hawkins_cs360;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPrefs;
    private long currentUserId;
    
    private RecyclerView recyclerView;
    private WeightAdapter adapter;
    private FloatingActionButton addBtn;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPrefs.getLong("USER_ID", -1);

        if (currentUserId == -1) {
            // Should not happen unless user bypasses login manually
            logout();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.dashboard_menu);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        recyclerView = findViewById(R.id.weight_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new WeightAdapter(new ArrayList<>(), new WeightAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(WeightRecord record) {
                showWeightDialog(record);
            }

            @Override
            public void onDeleteClick(WeightRecord record) {
                confirmDelete(record);
            }
        });
        recyclerView.setAdapter(adapter);

        addBtn = findViewById(R.id.add_weight_btn);
        addBtn.setOnClickListener(v -> showWeightDialog(null));

        loadWeights();
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_update_goal) {
            promptUpdateGoalWeight();
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return false;
    }

    private void promptUpdateGoalWeight() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Goal Weight");
        builder.setMessage("Please enter your target goal weight (lbs):");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        // try to prefill
        float currentGoal = dbHelper.getGoalWeight(currentUserId);
        if (currentGoal != -1) {
            input.setText(String.valueOf(currentGoal));
        }

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                try {
                    float weight = Float.parseFloat(val);
                    dbHelper.setGoalWeight(currentUserId, weight);
                    Toast.makeText(this, "Goal Updated", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid format", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadWeights() {
        Cursor cursor = dbHelper.getWeightsForUser(currentUserId);
        List<WeightRecord> records = new ArrayList<>();
        
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex("id");
                int dateIdx = cursor.getColumnIndex("date");
                int weightIdx = cursor.getColumnIndex("weight");
                
                do {
                    long id = cursor.getLong(idIdx);
                    String date = cursor.getString(dateIdx);
                    float w = cursor.getFloat(weightIdx);
                    records.add(new WeightRecord(id, currentUserId, date, w));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        adapter.updateList(records);
    }

    private void showWeightDialog(WeightRecord existingRecord) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_weight, null);
        dialog.setContentView(bottomSheetView);

        TextInputEditText weightInput = bottomSheetView.findViewById(R.id.weight_input);
        Button saveBtn = bottomSheetView.findViewById(R.id.save_weight_btn);

        if (existingRecord != null) {
            weightInput.setText(String.valueOf(existingRecord.getWeight()));
            saveBtn.setText("Update Entry");
        }

        saveBtn.setOnClickListener(v -> {
            String val = weightInput.getText().toString();
            if (val.isEmpty()) {
                Toast.makeText(this, "Enter a weight", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float w = Float.parseFloat(val);
                
                if (existingRecord == null) {
                    // Create new
                    String currDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
                    dbHelper.addWeightRecord(currentUserId, currDate, w);
                    Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
                } else {
                    // Update existing
                    dbHelper.updateWeightRecord(existingRecord.getId(), w);
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                }
                
                dialog.dismiss();
                loadWeights();
                checkGoal(w);
                
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void confirmDelete(WeightRecord record) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Record")
            .setMessage("Are you sure you want to delete this weight entry?")
            .setPositiveButton("Delete", (dialog, which) -> {
                dbHelper.deleteWeightRecord(record.getId());
                loadWeights();
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void checkGoal(float recordedWeight) {
        float goal = dbHelper.getGoalWeight(currentUserId);
        if (goal == -1) return; // shouldn't happen unless error
        
        if (recordedWeight <= goal) {
            sendSmsNotification();
        }
    }

    private void sendSmsNotification() {
        boolean smsGranted = sharedPrefs.getBoolean("SMS_GRANTED", false);
        if (!smsGranted) {
            // User denied SMS permission
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            // 5554 is typically the first emulator's phone number
            smsManager.sendTextMessage("5554", null, "Congratulations! You reached your goal weight!", null, null);
            Toast.makeText(this, "Goal Reached! Notification Sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void logout() {
        sharedPrefs.edit().putLong("USER_ID", -1).apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
