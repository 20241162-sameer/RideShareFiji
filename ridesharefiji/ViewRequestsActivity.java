package com.example.ridesharefiji;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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

public class ViewRequestsActivity extends AppCompatActivity {

    private ListView listViewRequests;
    private TextView tvEmptyRequests;
    private ProgressBar progressBar;
    private Button btnReturnToMenu;
    private DatabaseReference requestsRef;
    private List<RideRequest> requestList;
    private RideRequestAdapter adapter;
    private ValueEventListener requestsListener;
    private String currentUserRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        listViewRequests = findViewById(R.id.listViewRequests);
        tvEmptyRequests = findViewById(R.id.tvEmptyRequests);
        progressBar = findViewById(R.id.progressBar);
        btnReturnToMenu = findViewById(R.id.btnReturnToMenu);

        requestsRef = FirebaseDatabase.getInstance().getReference("requests");
        requestList = new ArrayList<>();

        // 1. Fetch User Role First
        fetchCurrentUserRoleAndRides();

        btnReturnToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ViewRequestsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void fetchCurrentUserRoleAndRides() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("role")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                currentUserRole = snapshot.getValue(String.class);
                            }
                            // Initialize adapter with the role
                            adapter = new RideRequestAdapter(ViewRequestsActivity.this, requestList, currentUserRole);
                            listViewRequests.setAdapter(adapter);
                            // 2. Then Fetch the requests
                            fetchRequests();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            fetchRequests(); // Fallback
                        }
                    });
        } else {
            fetchRequests();
        }
    }

    private void fetchRequests() {
        requestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RideRequest request = postSnapshot.getValue(RideRequest.class);
                    if (request != null) {
                        // Only add requests that are NOT expired
                        if (!DateUtils.isExpired(request.getDate())) {
                            requestList.add(request);
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                tvEmptyRequests.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ViewRequestsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        requestsRef.addValueEventListener(requestsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestsRef != null && requestsListener != null) {
            requestsRef.removeEventListener(requestsListener);
        }
    }
}
