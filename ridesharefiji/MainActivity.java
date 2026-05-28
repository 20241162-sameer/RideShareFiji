package com.example.ridesharefiji;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Button btnFindRide, btnOfferRide, btnViewRequests, btnMyActivity, btnRequestRide, btnProfile;
    private ImageButton btnLogout;
    private TextView tvWelcomeUser, tvUserRole;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // ✅ Redirect to Login if not authenticated
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        btnFindRide = findViewById(R.id.btnFindRide);
        btnOfferRide = findViewById(R.id.btnOfferRide);
        btnViewRequests = findViewById(R.id.btnViewRequests);
        btnMyActivity = findViewById(R.id.btnMyActivity);
        btnRequestRide = findViewById(R.id.btnRequestRide);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvUserRole = findViewById(R.id.tvUserRole);

        // ✅ Fetch User Details and update UI
        userRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());
        fetchUserRoleAndSetupUI();

        btnFindRide.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FindRideActivity.class)));
        btnOfferRide.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OfferRideActivity.class)));
        btnViewRequests.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ViewRequestsActivity.class)));
        btnMyActivity.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyActivityActivity.class)));
        btnRequestRide.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RequestRideActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void fetchUserRoleAndSetupUI() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    String name = snapshot.child("fullName").getValue(String.class);
                    
                    if (name != null) {
                        tvWelcomeUser.setText(String.format("Welcome, %s!", name));
                    }
                    
                    if (role != null) {
                        tvUserRole.setText(role.toUpperCase());
                        // Set dynamic colors for the badge
                        if ("Driver".equalsIgnoreCase(role)) {
                            int color = ContextCompat.getColor(MainActivity.this, R.color.primary);
                            tvUserRole.setTextColor(color);
                            TextViewCompat.setCompoundDrawableTintList(tvUserRole, android.content.res.ColorStateList.valueOf(color));
                            tvUserRole.getBackground().setTint(0xFFEFF6FF); // Light blue background
                        } else {
                            int color = ContextCompat.getColor(MainActivity.this, R.color.secondary);
                            tvUserRole.setTextColor(color);
                            TextViewCompat.setCompoundDrawableTintList(tvUserRole, android.content.res.ColorStateList.valueOf(color));
                            tvUserRole.getBackground().setTint(0xFFFFF7ED); // Light orange background
                        }
                    }

                    if ("Passenger".equalsIgnoreCase(role)) {
                        // Passengers look for rides and post requests
                        btnOfferRide.setVisibility(View.GONE);
                        btnViewRequests.setVisibility(View.GONE);
                    } else if ("Driver".equalsIgnoreCase(role)) {
                        // Drivers offer rides and look at passenger requests
                        btnRequestRide.setVisibility(View.GONE);
                        btnFindRide.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // If fail, show all buttons as fallback
            }
        });
    }
}
