package com.example.app.Activities.Home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.app.Adapters.Home.FindAdapter;
import com.example.app.Model.Product;
import com.example.app.databinding.ActivityFindBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FindActivity extends AppCompatActivity {
    private ActivityFindBinding binding;
    private final DatabaseReference productsReference= FirebaseDatabase.getInstance().getReference("Products");
    private ArrayList<Product> dsAll = new ArrayList<>();
    private FindAdapter adapter;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityFindBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        adapter = new FindAdapter(dsAll, userId,this);
        binding.recycleFoodFinded.setAdapter(adapter);
        initUI();
    }

    private void initUI() {
        getWindow().setStatusBarColor(Color.parseColor("#E8584D"));
        getWindow().setNavigationBarColor(Color.parseColor("#E8584D"));
        // Thêm adapter và layout của adapter
        binding.searhView.setIconifiedByDefault(false);
        StaggeredGridLayoutManager layoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        binding.recycleFoodFinded.setLayoutManager(layoutManager);
        binding.recycleFoodFinded.setHasFixedSize(true);
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //set Sự kiện của ô nhập
        binding.searhView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.getFilter().filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        dsAll = new ArrayList<>();

        productsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item:snapshot.getChildren()) {
                    Product product = item.getValue(Product.class);
                    if (product != null && !product.getState().equals("deleted")) {
                        dsAll.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}