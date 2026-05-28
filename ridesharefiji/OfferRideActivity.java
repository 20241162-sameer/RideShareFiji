package com.example.ridesharefiji;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

public class OfferRideActivity extends AppCompatActivity {

    TextInputEditText etFrom, etTo, etDate, etTime, etSeats, etFare;
    TextInputEditText etDriverName, etDriverContact, etVehicleReg;
    Button btnSubmitRide, btnReturnToMenu;

    DatabaseReference ridesRef, userRef;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_ride);

        etFrom = findViewById(R.id.etFrom);
        etTo = findViewById(R.id.etTo);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etSeats = findViewById(R.id.etSeats);
        etFare = findViewById(R.id.etFare);
        etDriverName = findViewById(R.id.etDriverName);
        etDriverContact = findViewById(R.id.etDriverContact);
        etVehicleReg = findViewById(R.id.etVehicleReg);
        btnSubmitRide = findViewById(R.id.btnSubmitRide);
        btnReturnToMenu = findViewById(R.id.btnReturnToMenu);

        ridesRef = FirebaseDatabase.getInstance().getReference("rides");
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            autoFillProfile();
        }

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        btnSubmitRide.setOnClickListener(v -> saveRide());

        btnReturnToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(OfferRideActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void autoFillProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    
                    // Only auto-fill if the user is actually a Driver
                    if ("Driver".equalsIgnoreCase(role)) {
                        String name = snapshot.child("fullName").getValue(String.class);
                        String phone = snapshot.child("email").getValue(String.class); 

                        if (name != null) etDriverName.setText(name);
                        if (phone != null) etDriverContact.setText(phone);
                    } else {
                        Toast.makeText(OfferRideActivity.this, "Note: You are registered as a Passenger.", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    etDate.setText(date);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    etTime.setText(time);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void saveRide() {
        if (etFrom.getText() == null || etTo.getText() == null || etDate.getText() == null || 
            etTime.getText() == null || etSeats.getText() == null || etFare.getText() == null ||
            etDriverName.getText() == null || etDriverContact.getText() == null || etVehicleReg.getText() == null) return;

        String from = etFrom.getText().toString().trim();
        String to = etTo.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String seats = etSeats.getText().toString().trim();
        String fare = etFare.getText().toString().trim();
        String driverName = etDriverName.getText().toString().trim();
        String driverContact = etDriverContact.getText().toString().trim();
        String vehicleReg = etVehicleReg.getText().toString().trim();

        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to) || TextUtils.isEmpty(date)
                || TextUtils.isEmpty(time) || TextUtils.isEmpty(seats) || TextUtils.isEmpty(fare)
                || TextUtils.isEmpty(driverName) || TextUtils.isEmpty(driverContact)
                || TextUtils.isEmpty(vehicleReg)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String rideId = ridesRef.push().getKey();

        if (rideId != null) {
            Ride ride = new Ride(
                    rideId,
                    from,
                    to,
                    date,
                    time,
                    seats,
                    driverName,
                    driverContact,
                    vehicleReg,
                    currentUserId,
                    fare
            );

            ridesRef.child(rideId).setValue(ride)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(OfferRideActivity.this, "Ride submitted successfully", Toast.LENGTH_SHORT).show();

                        etFrom.setText("");
                        etTo.setText("");
                        etDate.setText("");
                        etTime.setText("");
                        etSeats.setText("");
                        etFare.setText("");
                        etVehicleReg.setText("");
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(OfferRideActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
