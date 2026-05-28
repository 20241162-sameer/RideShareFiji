package com.example.ridesharefiji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RideAdapter extends ArrayAdapter<Ride> {

    private final Context context;
    private final List<Ride> rides;

    public RideAdapter(@NonNull Context context, @NonNull List<Ride> rides) {
        super(context, R.layout.item_ride, rides);
        this.context = context;
        this.rides = rides;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ride, parent, false);
        }

        Ride currentRide = rides.get(position);

        TextView tvFrom = convertView.findViewById(R.id.tvItemFrom);
        TextView tvTo = convertView.findViewById(R.id.tvItemTo);
        TextView tvDate = convertView.findViewById(R.id.tvItemDate);
        TextView tvTime = convertView.findViewById(R.id.tvItemTime);
        TextView tvVehicle = convertView.findViewById(R.id.tvItemVehicle);
        TextView tvFare = convertView.findViewById(R.id.tvItemFare);
        TextView tvSeats = convertView.findViewById(R.id.tvItemSeats);
        Button btnCancel = convertView.findViewById(R.id.btnCancelRide);

        if (currentRide != null) {
            tvFrom.setText(currentRide.getFrom() != null ? currentRide.getFrom() : "");
            tvTo.setText(currentRide.getTo() != null ? currentRide.getTo() : "");
            tvDate.setText(String.format("Date: %s", currentRide.getDate() != null ? currentRide.getDate() : ""));
            tvTime.setText(String.format("Time: %s", currentRide.getTime() != null ? currentRide.getTime() : ""));
            tvVehicle.setText(String.format("Vehicle: %s", currentRide.getVehicleReg() != null ? currentRide.getVehicleReg() : ""));
            tvFare.setText(String.format("Fare: $%s", currentRide.getFare() != null ? currentRide.getFare() : "0.00"));
            tvSeats.setText(String.format("%s Seats", currentRide.getSeats() != null ? currentRide.getSeats() : "0"));

            // Show cancel button only if the ride offer belongs to current user
            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (currentUserId != null && currentUserId.equals(currentRide.getUserId())) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> {
                    FirebaseDatabase.getInstance().getReference("rides")
                            .child(currentRide.getId())
                            .removeValue()
                            .addOnSuccessListener(unused -> Toast.makeText(context, "Ride offer cancelled", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
            } else {
                btnCancel.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
