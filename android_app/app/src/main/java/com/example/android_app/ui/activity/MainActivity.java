package com.example.android_app.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity; // <--- Import this
import android.view.MenuItem; // Required for handling menu item selections (like hamburger icon click)

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle; // Provides the hamburger icon functionality
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Use androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat; // For opening/closing the drawer
import androidx.drawerlayout.widget.DrawerLayout; // The DrawerLayout itself

import com.example.android_app.R; // Your project's R file
import com.example.android_app.ui.fragments.CreateMailFragment; // Your initial main content fragment
import com.example.android_app.ui.fragments.SideBarFragment; // Import your SideBarFragment

public class MainActivity extends AppCompatActivity implements SideBarFragment.SideBarFragmentListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure this is activity_main.xml

        // 1. Setup the Toolbar as the Activity's ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // 3. Setup ActionBarDrawerToggle (the hamburger icon)
        // This links the toolbar to the drawer layout, providing the animation and click handling
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar, // Pass the toolbar to link the hamburger icon
                R.string.navigation_drawer_open, // String resource for opening accessibility
                R.string.navigation_drawer_close // String resource for closing accessibility
        );
        drawerLayout.addDrawerListener(toggle); // Listen for drawer open/close events
        toggle.syncState(); // Synchronize the state of the drawer indicator (e.g., initial hamburger state)

        // 4. Load your default main content fragment (e.g., an Inbox Fragment)
        // This replaces the content in R.id.fragment_container
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateMailFragment())
                    .commit();

            // 5. Load your SideBarFragment into the sidebar_container
            // This ensures the sidebar's UI is built and managed by SideBarFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sidebar_container, new SideBarFragment())
                    .commit();
        }
    }

    // Handle back button press: if drawer is open, close it; otherwise, do default back action
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // This method handles clicks on the hamburger icon (toggle)
    // ActionBarDrawerToggle handles it if you pass it as an argument in onCreate
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true; // ActionBarDrawerToggle handled the click
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCategorySelected(String categoryName) {
        // This method will be called when a category is selected in SideBarFragment
        Log.d("MainActivity", "Category selected: " + categoryName);
        // Here you would typically load the emails for the selected category
        // Example:
        // getSupportFragmentManager().beginTransaction()
        //         .replace(R.id.fragment_container, MailListFragment.newInstance(categoryName, null))
        //         .commit();
        drawerLayout.closeDrawers(); // Close the drawer after selection
    }

    @Override
    public void onLabelSelected(String labelId, String labelName) {
        // This method will be called when a label is selected in SideBarFragment
        Log.d("MainActivity", "Label selected: " + labelName + " (ID: " + labelId + ")");
        // Here you would typically load the emails for the selected label
        // Example:
        // getSupportFragmentManager().beginTransaction()
        //         .replace(R.id.fragment_container, MailListFragment.newInstance(null, labelId))
        //         .commit();
        drawerLayout.closeDrawers(); // Close the drawer after selection
    }
}
