package com.example.ridesharefiji;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RideDetailsActivity extends AppCompatActivity {

    private TextView tvRideId, tvFrom, tvTo, tvSeats, tvDate, tvTime, tvDriverName, tvDriverContact, tvVehicleReg, tvFare;
    private Button btnReturnToMenu, btnCallDriver, btnMessageDriver, btnBookRide;
    private ImageButton btnBack;
    private DatabaseReference rideRef;
    private String rideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);

        tvRideId = findViewById(R.id.tvRideId);
        tvFrom = findViewById(R.id.tvFrom);
        tvTo = findViewById(R.id.tvTo);
        tvSeats = findViewById(R.id.tvSeats);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvFare = findViewById(R.id.tvFare);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvDriverContact = findViewById(R.id.tvDriverContact);
        tvVehicleReg = findViewById(R.id.tvVehicleReg);
        btnReturnToMenu = findViewById(R.id.btnReturnToMenu);
        btnCallDriver = findViewById(R.id.btnCallDriver);
        btnMessageDriver = findViewById(R.id.btnMessageDriver);
        btnBookRide = findViewById(R.id.btnBookRide);
        btnBack = findViewById(R.id.btnBack);

        // ✅ Get data from intent
        rideId = getIntent().getStringExtra("rideId");
        String from = getIntent().getStringExtra("from");
        String to = getIntent().getStringExtra("to");
        String seats = getIntent().getStringExtra("seats");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String fare = getIntent().getStringExtra("fare");
        String driverName = getIntent().getStringExtra("driverName");
        String driverContact = getIntent().getStringExtra("driverContact");
        String vehicleReg = getIntent().getStringExtra("vehicleReg");

        // ✅ Initial display
        tvRideId.setText(String.format("ID: %s", rideId != null ? rideId : ""));
        tvFrom.setText(from != null ? from : "");
        tvTo.setText(to != null ? to : "");
        tvDate.setText(date != null ? date : "");
        tvSeats.setText(seats != null ? seats : "0");
        tvTime.setText(time != null ? time : "");
        tvFare.setText(String.format("$%s", fare != null ? fare : "0.00"));
        tvDriverName.setText(driverName != null ? driverName : "Unknown Driver");
        tvDriverContact.setText(driverContact != null ? driverContact : "No Number");
        tvVehicleReg.setText(vehicleReg != null ? vehicleReg : "");

        if (rideId != null) {
            rideRef = FirebaseDatabase.getInstance().getReference("rides").child(rideId);
            observeSeats();
        }

        // ✅ Booking Logic
        btnBookRide.setOnClickListener(v -> bookSeat());

        // ✅ Call Driver Logic
        btnCallDriver.setOnClickListener(v -> {
            if (driverContact != null && !driverContact.isEmpty()) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + driverContact));
                startActivity(dialIntent);
            } else {
                Toast.makeText(this, "No contact number available", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Message Driver Logic
        btnMessageDriver.setOnClickListener(v -> {
            if (driverContact != null && !driverContact.isEmpty()) {
                String message = "Hi " + (driverName != null ? driverName : "Driver") + 
                        ", I'm interested in your ride from " + (from != null ? from : "...") + 
                        " to " + (to != null ? to : "...") + " on " + (date != null ? date : "...") + ".";
                
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                smsIntent.setData(Uri.parse("smsto:" + driverContact));
                smsIntent.putExtra("sms_body", message);
                startActivity(smsIntent);
            } else {
                Toast.makeText(this, "No contact number available", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Back to exit
        btnBack.setOnClickListener(v -> finish());

        btnReturnToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(RideDetailsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void observeSeats() {
        rideRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String currentSeats = snapshot.child("seats").getValue(String.class);
                    if (currentSeats != null) {
                        tvSeats.setText(currentSeats);
                        try {
                            int count = Integer.parseInt(currentSeats);
                            if (count <= 0) {
                                btnBookRide.setEnabled(false);
                                btnBookRide.setText(R.string.ride_full);
                            } else {
                                btnBookRide.setEnabled(true);
                                btnBookRide.setText(R.string.book_ride);
                            }
                        } catch (NumberFormatException e) {
                            // Error parsing seats
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void bookSeat() {
        rideRef.child("seats").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String seatsStr = task.getResult().getValue(String.class);
                if (seatsStr != null) {
                    try {
                        int count = Integer.parseInt(seatsStr);
                        if (count > 0) {
                            int newCount = count - 1;
                            rideRef.child("seats").setValue(String.valueOf(newCount))
                                    .addOnSuccessListener(unused -> Toast.makeText(RideDetailsActivity.this, "Seat booked successfully!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(RideDetailsActivity.this, "Booking failed", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Sorry, no seats available", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Error processing seat count", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
