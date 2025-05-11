package com.example.app.Activities.MyShop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.Adapters.MyShopAdapter.MyShopAdapter;
import com.example.app.Dialog.LoadingDialog;
import com.example.app.Model.Product;
import com.example.app.databinding.ActivityMyProductsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyProductActivity extends AppCompatActivity {
    private ActivityMyProductsBinding binding;
    private ArrayList<Product> ds=new ArrayList<>();
    private MyShopAdapter adapter;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(Color.parseColor("#E8584D"));
        getWindow().setNavigationBarColor(Color.parseColor("#E8584D"));

        userId = getIntent().getStringExtra("userId");
        adapter=new MyShopAdapter(ds, MyProductActivity.this, userId);
        binding.recycleView.setHasFixedSize(true);
        binding.recycleView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));
        binding.recycleView.setAdapter(adapter);
        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.flpAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MyProductActivity.this, AddProductActivity.class);
                intent.putExtra("userId",userId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LoadingDialog dialog=new LoadingDialog(this);
        dialog.show();
        FirebaseDatabase.getInstance().getReference("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ds.clear();
                for (DataSnapshot item:snapshot.getChildren()) {
                    Product tmp=item.getValue(Product.class);
                    if (tmp != null && tmp.getPublisherId()!=null) {
                        if (tmp.getPublisherId().equals(userId) && !tmp.getState().equals("deleted")) {
                            ds.add(tmp);
                        }
                    }
                }
                dialog.dismiss();
                adapter.notifyDataSetChanged();
             }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}