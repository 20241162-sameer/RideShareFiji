package com.example.ridesharefiji;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RideRequestAdapter extends ArrayAdapter<RideRequest> {

    private final Context context;
    private final List<RideRequest> requests;
    private final String userRole;

    public RideRequestAdapter(@NonNull Context context, @NonNull List<RideRequest> requests, String userRole) {
        super(context, R.layout.item_ride_request, requests);
        this.context = context;
        this.requests = requests;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ride_request, parent, false);
        }

        RideRequest currentRequest = requests.get(position);

        TextView tvFrom = convertView.findViewById(R.id.tvReqFrom);
        TextView tvTo = convertView.findViewById(R.id.tvReqTo);
        TextView tvDate = convertView.findViewById(R.id.tvReqDate);
        TextView tvName = convertView.findViewById(R.id.tvPassengerName);
        Button btnCancel = convertView.findViewById(R.id.btnCancelRequest);
        LinearLayout layoutDriverActions = convertView.findViewById(R.id.layoutDriverActions);
        View btnCall = convertView.findViewById(R.id.btnCallPassenger);
        View btnMsg = convertView.findViewById(R.id.btnMsgPassenger);

        if (currentRequest != null) {
            tvFrom.setText(currentRequest.getFrom() != null ? currentRequest.getFrom() : "");
            tvTo.setText(currentRequest.getTo() != null ? currentRequest.getTo() : "");
            tvDate.setText(String.format("Date: %s", currentRequest.getDate() != null ? currentRequest.getDate() : ""));
            tvName.setText(currentRequest.getPassengerName() != null ? currentRequest.getPassengerName() : "Anonymous");

            String currentUserId = FirebaseAuth.getInstance().getUid();

            // 1. Show Cancel button only for owner
            if (currentUserId != null && currentUserId.equals(currentRequest.getUserId())) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> {
                    FirebaseDatabase.getInstance().getReference("requests")
                            .child(currentRequest.getId())
                            .removeValue();
                });
            } else {
                btnCancel.setVisibility(View.GONE);
            }

            // 2. Show Call/Message actions only for DRIVERS viewing OTHER PEOPLE'S requests
            if ("Driver".equalsIgnoreCase(userRole) && currentUserId != null && !currentUserId.equals(currentRequest.getUserId())) {
                layoutDriverActions.setVisibility(View.VISIBLE);
                
                final String phone = currentRequest.getPassengerPhone();

                btnCall.setOnClickListener(v -> {
                    if (phone != null && !phone.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phone));
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Passenger contact not available", Toast.LENGTH_SHORT).show();
                    }
                });

                btnMsg.setOnClickListener(v -> {
                    if (phone != null && !phone.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("smsto:" + phone));
                        intent.putExtra("sms_body", "Hi " + currentRequest.getPassengerName() + ", I saw your ride request and I can offer you a lift!");
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Passenger contact not available", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                layoutDriverActions.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
