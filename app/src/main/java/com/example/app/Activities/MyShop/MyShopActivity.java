package com.example.app.Activities.MyShop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.Activities.OrderSellerManagement.DeliveryManagementActivity;
import com.example.app.databinding.ActivityMyShopBinding;

public class MyShopActivity extends AppCompatActivity {
    private ActivityMyShopBinding binding;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyShopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(Color.parseColor("#E8584D"));
        getWindow().setNavigationBarColor(Color.parseColor("#E8584D"));

        userId = getIntent().getStringExtra("userId");
        binding.cardMyProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MyShopActivity.this, MyProductActivity.class);
                intent.putExtra("userId",userId);
                startActivity(intent);
            }
        });


        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.cardDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MyShopActivity.this, DeliveryManagementActivity.class);
                intent.putExtra("userId",userId);
                startActivity(intent);
            }
        });
    }
}