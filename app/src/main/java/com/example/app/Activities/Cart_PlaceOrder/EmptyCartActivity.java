package com.example.app.Activities.Cart_PlaceOrder;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.databinding.ActivityEmptyCartBinding;

public class EmptyCartActivity extends AppCompatActivity {
    private ActivityEmptyCartBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmptyCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
    }

    private void initToolbar() {
        getWindow().setStatusBarColor(Color.parseColor("#E8584D"));
        getWindow().setNavigationBarColor(Color.parseColor("#E8584D"));
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Cart");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}