package com.example.android_app.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Gravity; // Import android.view.Gravity
import android.view.View; // Import android.view.View

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.android_app.R;
import com.example.android_app.ui.fragments.CreateMailFragment;
import com.example.android_app.ui.fragments.SideBarFragment;

public class MainActivity extends AppCompatActivity implements SideBarFragment.SideBarFragmentListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        // 1. Initialize ActionBarDrawerToggle WITH the toolbar.
        // This is crucial for the hamburger/arrow icon to appear in the toolbar.
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar, // Pass the toolbar here
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState(); // This ensures the hamburger icon appears correctly and animates.

        // 2. NOW, explicitly set the navigation click listener for the toolbar.
        // This listener will override the default one set by ActionBarDrawerToggle,
        // giving you manual control over opening/closing the drawer with Gravity.LEFT.
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateMailFragment())
                    .commit();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sidebar_container, new SideBarFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        // When the back button is pressed, explicitly close the LEFT drawer if it's open.
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // This method will now primarily handle other menu items you might add to the toolbar.
        // The navigation icon's click is handled by toolbar.setNavigationOnClickListener(),
        // so you don't need to check for android.R.id.home here.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCategorySelected(String categoryName) {
        Log.d("MainActivity", "Category selected: " + categoryName);
        drawerLayout.closeDrawers(); // Close the drawer(s) after selection
    }

    @Override
    public void onLabelSelected(String labelId, String labelName) {
        Log.d("MainActivity", "Label selected: " + labelName + " (ID: " + labelId + ")");
        drawerLayout.closeDrawers(); // Close the drawer(s) after selection
    }
}