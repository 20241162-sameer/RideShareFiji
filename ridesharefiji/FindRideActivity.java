package com.example.ridesharefiji;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindRideActivity extends AppCompatActivity {

    private TextInputEditText etSearchRide;
    private ListView listViewRides;
    private TextView tvEmptyRides;
    private ProgressBar progressBar;
    private Button btnReturnToMenu;
    private DatabaseReference ridesRef;
    private List<Ride> rideList;
    private List<Ride> filteredList;
    private RideAdapter adapter;
    private ValueEventListener ridesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_ride);

        etSearchRide = findViewById(R.id.etSearchRide);
        listViewRides = findViewById(R.id.listViewRides);
        tvEmptyRides = findViewById(R.id.tvEmptyRides);
        progressBar = findViewById(R.id.progressBar);
        btnReturnToMenu = findViewById(R.id.btnReturnToMenu);

        ridesRef = FirebaseDatabase.getInstance().getReference("rides");
        rideList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new RideAdapter(this, filteredList);
        listViewRides.setAdapter(adapter);

        fetchRides();

        etSearchRide.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRides(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listViewRides.setOnItemClickListener((parent, view, position, id) -> {
            Ride selectedRide = filteredList.get(position);
            Intent intent = new Intent(FindRideActivity.this, RideDetailsActivity.class);
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
            Intent intent = new Intent(FindRideActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void fetchRides() {
        ridesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rideList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Ride ride = postSnapshot.getValue(Ride.class);
                    if (ride != null) {
                        // Only add rides that are NOT expired
                        if (!DateUtils.isExpired(ride.getDate())) {
                            rideList.add(ride);
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (etSearchRide.getText() != null) {
                    filterRides(etSearchRide.getText().toString());
                } else {
                    filterRides("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FindRideActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        ridesRef.addValueEventListener(ridesListener);
    }

    private void filterRides(String query) {
        filteredList.clear();
        String lowerCaseQuery = query.toLowerCase().trim();
        
        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(rideList);
        } else {
            for (Ride ride : rideList) {
                boolean matchesFrom = ride.getFrom() != null && ride.getFrom().toLowerCase().contains(lowerCaseQuery);
                boolean matchesTo = ride.getTo() != null && ride.getTo().toLowerCase().contains(lowerCaseQuery);
                
                if (matchesFrom || matchesTo) {
                    filteredList.add(ride);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            tvEmptyRides.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ridesRef != null && ridesListener != null) {
            ridesRef.removeEventListener(ridesListener);
        }
    }
}
