package com.example.app.Adapters.Home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.app.Fragments.Home.BagHomeFrg;
import com.example.app.Fragments.Home.TechAccessoryHomeFrg;

public class FoodDrinkAdapter extends FragmentStateAdapter {
    private final String userId;

    public FoodDrinkAdapter(@NonNull Fragment fragment, String id) {
        super(fragment);
        userId = id;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new BagHomeFrg(userId);
        }
        return new TechAccessoryHomeFrg(userId);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
