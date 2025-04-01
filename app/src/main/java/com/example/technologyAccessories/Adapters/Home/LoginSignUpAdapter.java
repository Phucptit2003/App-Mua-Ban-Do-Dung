package com.example.technologyAccessories.Adapters.Home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.technologyAccessories.Fragments.Home.LoginFragment;
import com.example.technologyAccessories.Fragments.Home.SignUpFragment;

public class LoginSignUpAdapter extends FragmentStateAdapter {

    public LoginSignUpAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position==0) {
            return  new LoginFragment();
        }
        return new SignUpFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
