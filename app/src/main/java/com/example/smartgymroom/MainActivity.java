package com.example.smartgymroom;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @SuppressLint({"MissingPermission"})
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.currentSession)
                selectedFragment = new CurrentSessionFragment();
            else {
                selectedFragment = new HistoryFragment();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if (item.getItemId() == R.id.currentSession){
                fragmentTransaction.setCustomAnimations(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                );
            }else {
                fragmentTransaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );
            }
            fragmentTransaction.replace(R.id.fragment_container, selectedFragment);

            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit();
            return true;
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CurrentSessionFragment())
                .commit();

    }


}
