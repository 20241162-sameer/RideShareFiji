package com.example.ridesharefiji;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyActivityActivity extends AppCompatActivity {

    private NonScrollListView lvMyRides, lvMyRequests;
    private TextView tvEmptyMyRides, tvEmptyMyRequests;
    private Button btnReturnToMenu;

    private DatabaseReference ridesRef, requestsRef;
    private String currentUserId;

    private List<Ride> myRideList;
    private List<RideRequest> myRequestList;
    private RideAdapter rideAdapter;
    private RideRequestAdapter requestAdapter;
    private String currentUserRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activity);

        lvMyRides = findViewById(R.id.lvMyRides);
        lvMyRequests = findViewById(R.id.lvMyRequests);
        tvEmptyMyRides = findViewById(R.id.tvEmptyMyRides);
        tvEmptyMyRequests = findViewById(R.id.tvEmptyMyRequests);
        btnReturnToMenu = findViewById(R.id.btnReturnToMenu);

        currentUserId = FirebaseAuth.getInstance().getUid();
        ridesRef = FirebaseDatabase.getInstance().getReference("rides");
        requestsRef = FirebaseDatabase.getInstance().getReference("requests");

        myRideList = new ArrayList<>();
        myRequestList = new ArrayList<>();

        // Initialize ride adapter (standard)
        rideAdapter = new RideAdapter(this, myRideList);
        lvMyRides.setAdapter(rideAdapter);

        // 1. Fetch Role first for request adapter
        fetchCurrentUserRoleAndData();

        lvMyRides.setOnItemClickListener((parent, view, position, id) -> {
            Ride selectedRide = myRideList.get(position);
            Intent intent = new Intent(MyActivityActivity.this, RideDetailsActivity.class);
            intent.putExtra("rideId", selectedRide.getId());
            intent.putExtra("from", selectedRide.getFrom());
            intent.putExtra("to", selectedRide.getTo());
            intent.putExtra("date", selectedRide.getDate());
            intent.putExtra("time", selectedRide.getTime());
            intent.putExtra("fare", selectedRide.getFare());
            intent.putExtra("seats", selectedRide.getSeats());
            intent.putExtra("driverName", selectedRide.getDriverName());
            intent.putExtra("driverContact", selectedRide.getDriverContact());
            intent.putExtra("vehicleReg", selectedRide.getVehicleReg());
            startActivity(intent);
        });

        btnReturnToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(MyActivityActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void fetchCurrentUserRoleAndData() {
        if (currentUserId != null) {
            FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("role")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                currentUserRole = snapshot.getValue(String.class);
                            }
                            // Initialize request adapter with role
                            requestAdapter = new RideRequestAdapter(MyActivityActivity.this, myRequestList, currentUserRole);
                            lvMyRequests.setAdapter(requestAdapter);
                            
                            // 2. Then Fetch Data
                            fetchMyRides();
                            fetchMyRequests();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Fallback
                            requestAdapter = new RideRequestAdapter(MyActivityActivity.this, myRequestList, "");
                            lvMyRequests.setAdapter(requestAdapter);
                            fetchMyRides();
                            fetchMyRequests();
                        }
                    });
        }
    }

    private void fetchMyRides() {
        if (currentUserId == null) return;
        
        ridesRef.orderByChild("userId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myRideList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Ride ride = postSnapshot.getValue(Ride.class);
                    if (ride != null) myRideList.add(ride);
                }
                rideAdapter.notifyDataSetChanged();
                tvEmptyMyRides.setVisibility(myRideList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyActivityActivity.this, "Failed to load rides", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMyRequests() {
        if (currentUserId == null) return;

        requestsRef.orderByChild("userId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myRequestList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RideRequest request = postSnapshot.getValue(RideRequest.class);
                    if (request != null) myRequestList.add(request);
                }
                if (requestAdapter != null) {
                    requestAdapter.notifyDataSetChanged();
                }
                tvEmptyMyRequests.setVisibility(myRequestList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyActivityActivity.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
