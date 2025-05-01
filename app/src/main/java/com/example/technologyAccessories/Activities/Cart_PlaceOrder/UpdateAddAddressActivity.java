package com.example.fooddeliveryapplication.Activities.Cart_PlaceOrder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fooddeliveryapplication.CustomMessageBox.FailToast;
import com.example.fooddeliveryapplication.CustomMessageBox.SuccessfulToast;
import com.example.fooddeliveryapplication.GlobalConfig;
import com.example.fooddeliveryapplication.Model.Address;
import com.example.fooddeliveryapplication.databinding.ActivityUpdateAddAddressBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateAddAddressActivity extends AppCompatActivity {
    private ActivityUpdateAddAddressBinding binding;
    private String userId;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateAddAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getStringExtra("userId");
        mode = getIntent().getStringExtra("mode");

        initToolbar();
        setupMode();

        binding.updateComplete.setOnClickListener(view -> handleUpdateOrAdd());
    }

    private void setupMode() {
        if ("add - default".equals(mode)) {
            binding.updateComplete.setText("Complete");
            binding.setDefault.setChecked(true);
            binding.setDefault.setEnabled(false);
        } else if ("add - non-default".equals(mode)) {
            binding.updateComplete.setText("Complete");
        } else if ("update".equals(mode)) {
            binding.updateComplete.setText("Update");
            loadAddressData();
        }
    }

    private void loadAddressData() {
        FirebaseDatabase.getInstance().getReference()
                .child("Address")
                .child(userId)
                .child(GlobalConfig.updateAddressId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Address address = snapshot.getValue(Address.class);
                        if (address != null) {
                            binding.fullName.setText(address.getReceiverName());
                            binding.phoneNumber.setText(address.getReceiverPhoneNumber());
                            binding.detailAddress.setText(address.getDetailAddress());
                            binding.setDefault.setChecked("default".equals(address.getState()));
                            binding.setDefault.setEnabled(!"default".equals(address.getState()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error properly if needed
                    }
                });
    }

    private void handleUpdateOrAdd() {
        if (!validateAddressInfo()) return;

        boolean isComplete = "Complete".contentEquals(binding.updateComplete.getText());

        if (isComplete) {
            addNewAddress();
        } else {
            updateAddress();
        }
    }

    private void addNewAddress() {
        String addressId = FirebaseDatabase.getInstance().getReference().push().getKey();
        GlobalConfig.choseAddressId = addressId;

        Address address = new Address(
                addressId,
                binding.detailAddress.getText().toString().trim(),
                binding.setDefault.isChecked() ? "default" : "",
                binding.fullName.getText().toString().trim(),
                binding.phoneNumber.getText().toString().trim()
        );

        FirebaseDatabase.getInstance().getReference()
                .child("Address")
                .child(userId)
                .child(addressId)
                .setValue(address)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (binding.setDefault.isChecked()) {
                            resetOtherDefaultAddresses(addressId);
                        }
                        new SuccessfulToast(UpdateAddAddressActivity.this, "Added new address!").showToast();
                        finishWithResultOk();
                    }
                });
    }

    private void updateAddress() {
        Address address = new Address(
                GlobalConfig.updateAddressId,
                binding.detailAddress.getText().toString().trim(),
                binding.setDefault.isChecked() ? "default" : "",
                binding.fullName.getText().toString().trim(),
                binding.phoneNumber.getText().toString().trim()
        );

        FirebaseDatabase.getInstance().getReference()
                .child("Address")
                .child(userId)
                .child(GlobalConfig.updateAddressId)
                .setValue(address)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (binding.setDefault.isChecked()) {
                            resetOtherDefaultAddresses(GlobalConfig.updateAddressId);
                        }
                        new SuccessfulToast(UpdateAddAddressActivity.this, "Updated address!").showToast();
                        finishWithResultOk();
                    }
                });
    }

    private void resetOtherDefaultAddresses(String keepAddressId) {
        FirebaseDatabase.getInstance().getReference()
                .child("Address")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Address address = ds.getValue(Address.class);
                            if (address != null && !address.getAddressId().equals(keepAddressId)) {
                                FirebaseDatabase.getInstance()
                                        .getReference()
                                        .child("Address")
                                        .child(userId)
                                        .child(address.getAddressId())
                                        .child("state")
                                        .setValue("");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error properly if needed
                    }
                });
    }

    private void finishWithResultOk() {
        setResult(RESULT_OK, new Intent());
        finish();
    }

    private void initToolbar() {
        getWindow().setStatusBarColor(Color.parseColor("#E8584D"));
        getWindow().setNavigationBarColor(Color.parseColor("#E8584D"));
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("update".equals(mode) ? "Update address" : "Add address");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(view -> finishWithResultOk());
    }

    private boolean validateAddressInfo() {
        if (binding.fullName.getText().toString().trim().isEmpty()) {
            new FailToast(this, "Receiver name must not be empty!").showToast();
            return false;
        }

        if (binding.phoneNumber.getText().toString().trim().isEmpty()) {
            new FailToast(this, "Receiver phone number must not be empty!").showToast();
            return false;
        }

        if (binding.detailAddress.getText().toString().trim().isEmpty()) {
            new FailToast(this, "Detail address must not be empty!").showToast();
            return false;
        }

        return true;
    }
}
