package com.example.technologyAccessories.Fragments.Order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.technologyAccessories.Activities.Order.OrderActivity;
import com.example.technologyAccessories.Adapters.OrderAdapter.OrderAdapter;
import com.example.technologyAccessories.Model.Bill;
import com.example.technologyAccessories.databinding.FragmentHistoryOrderBinding;

import java.util.ArrayList;


public class HistoryOrderFragment extends Fragment {
    private FragmentHistoryOrderBinding binding;
    private ArrayList<Bill> dsBill;
    private String userId;

    public HistoryOrderFragment(ArrayList<Bill> ds, String id) {
        dsBill = ds;
        userId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryOrderBinding.inflate(inflater,container,false);

        OrderAdapter adapter=new OrderAdapter(getContext(),dsBill, OrderActivity.HISTORY_ORDER,userId);
        binding.ryc.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false));
        binding.ryc.setAdapter(adapter);

        return binding.getRoot();
    }
}