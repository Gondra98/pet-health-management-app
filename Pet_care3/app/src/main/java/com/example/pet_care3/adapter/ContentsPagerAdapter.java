package com.example.pet_care3.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pet_care3.fragment.AlarmFragment;
import com.example.pet_care3.fragment.HealthFragment;
import com.example.pet_care3.fragment.HomeFragment;
import com.example.pet_care3.fragment.SettingFragment;

public class ContentsPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 4;

    public ContentsPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new AlarmFragment();
            case 2:
                return new HealthFragment();
            case 3:
                return new SettingFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
