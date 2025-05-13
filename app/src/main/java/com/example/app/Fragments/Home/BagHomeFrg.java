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
import com.example.app.databinding.FragmentBaloHomeFrgBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;


public class BagHomeFrg extends Fragment {
    private ArrayList<Product> dsBalo;
    private FragmentBaloHomeFrgBinding binding;
    private TechBaloFrgAdapter adapter;
    private String userId;

    public BagHomeFrg(String id) {
        userId = id;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBaloHomeFrgBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity().getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        binding.rycBaloHome.setLayoutManager(linearLayoutManager);
        binding.rycBaloHome.setHasFixedSize(true);
        binding.txtSeemoreBalo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FindActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
        initData();
        adapter=new TechBaloFrgAdapter(dsBalo,userId,getContext());
        binding.rycBaloHome.setAdapter(adapter);

        return view;
    }

    private void initData() {
        dsBalo=new ArrayList<>();

        FirebaseDatabase.getInstance().getReference("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item:snapshot.getChildren()) {
                    Product tmp = item.getValue(Product.class);
                    if (tmp != null && !tmp.getState().equals("deleted") && tmp.getProductType().equalsIgnoreCase("Bag") && !tmp.getPublisherId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        dsBalo.add(tmp);
                    }
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}