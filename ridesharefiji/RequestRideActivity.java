package com.example.ridesharefiji;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

public class RequestRideActivity extends AppCompatActivity {

    private TextInputEditText etFrom, etTo, etDate, etPhone;
    private Button btnSubmitRequest, btnReturnToMenu;
    private DatabaseReference requestsRef, userRef;
    private String currentUserId, currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_ride);

        etFrom = findViewById(R.id.etRequestFrom);
        etTo = findViewById(R.id.etRequestTo);
        etDate = findViewById(R.id.etRequestDate);
        etPhone = findViewById(R.id.etRequestPhone);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        btnReturnToMenu = findViewById(R.id.btnReturnToMenu);

        currentUserId = FirebaseAuth.getInstance().getUid();
        requestsRef = FirebaseDatabase.getInstance().getReference("requests");
        
        if (currentUserId != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            autoFillContact();
        }

        etDate.setOnClickListener(v -> showDatePicker());
        btnSubmitRequest.setOnClickListener(v -> submitRequest());

        btnReturnToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(RequestRideActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void autoFillContact() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("fullName").getValue(String.class);
                    String phone = snapshot.child("email").getValue(String.class); // phone stored as email mapping
                    if (phone != null) etPhone.setText(phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month1 + 1,
                            year1
                    );
                    etDate.setText(selectedDate);
                },
                year,
                month,
                day
        );

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void submitRequest() {
        if (etFrom.getText() == null || etTo.getText() == null || etDate.getText() == null || etPhone.getText() == null) return;

        String from = etFrom.getText().toString().trim();
        String to = etTo.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(from)) {
            etFrom.setError("Pickup location is required");
            etFrom.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(to)) {
            etTo.setError("Destination is required");
            etTo.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(date)) {
            etDate.setError("Date is required");
            etDate.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Contact number is required");
            etPhone.requestFocus();
            return;
        }

        String requestId = requestsRef.push().getKey();
        if (requestId != null && currentUserId != null) {
            // Using currentUserName fetched during auto-fill
            String passengerName = currentUserName != null ? currentUserName : "Passenger";
            
            RideRequest request = new RideRequest(requestId, from, to, date, currentUserId, passengerName, phone);
            requestsRef.child(requestId).setValue(request).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(RequestRideActivity.this, "Request submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RequestRideActivity.this, "Submission failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
