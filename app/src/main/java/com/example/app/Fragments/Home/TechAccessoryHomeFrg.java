package com.example.app.Fragments.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app.Activities.Home.FindActivity;
import com.example.app.Adapters.Home.TechBaloFrgAdapter;
import com.example.app.Model.Product;
import com.example.app.databinding.FragmentDrinkHomeFrgBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class TechAccessoryHomeFrg extends Fragment {
    private FragmentDrinkHomeFrgBinding binding;
    private ArrayList<Product> dsFood;
    private TechBaloFrgAdapter adapter;
    private String userId;

    public TechAccessoryHomeFrg(String id) {
        userId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDrinkHomeFrgBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        initData();
        initUI();
        return view;
    }

    private void initUI() {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity().getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        binding.rycDrinkHome.setLayoutManager(linearLayoutManager);
        adapter=new TechBaloFrgAdapter(dsFood, userId,getContext());
        binding.rycDrinkHome.setAdapter(adapter);
        binding.rycDrinkHome.setHasFixedSize(true);
        binding.txtSeemoreDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FindActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }


    private void initData() {
        dsFood = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsFood.clear();  // Xóa danh sách trước khi cập nhật dữ liệu mới
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product product = ds.getValue(Product.class);

                    if (product != null) {
                        // Lấy giá trị từ product và kiểm tra null trước khi sử dụng
                        String state = product.getState() != null ? product.getState() : "";
                        String productType = product.getProductType() != null ? product.getProductType() : "";
                        String publisherId = product.getPublisherId() != null ? product.getPublisherId() : "";
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                                : "";

                        if (!state.equals("deleted") &&
                                productType.equalsIgnoreCase("TechAccessory") &&
                                !publisherId.equals(currentUserId)) {
                            dsFood.add(product);
                        }
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