package itcom.cartographer;

import android.content.res.Configuration;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import com.google.maps.model.GeocodingResult;

import java.util.ArrayList;

import itcom.cartographer.Database.Database;

import itcom.cartographer.Fragments.AboutFragment;
import itcom.cartographer.Fragments.MainFragment;
import itcom.cartographer.Fragments.SettingsFragment;
import itcom.cartographer.Utils.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    // The key to be used in bundles to store the current fragment
    private final String BUNDLE_KEY_FRAGMENT = "fragment";

    private DrawerLayout navDrawerLayout;
    private NavigationView navDrawer;
    private Toolbar toolbar;

    private Runnable pendingRunnable;
    private Handler handler;

    // Make sure to be using android.support.v7.app.ActionBarDrawerToggle version.
    // The android.support.v4.app.ActionBarDrawerToggle has been deprecated.
    private ActionBarDrawerToggle drawerToggle;

    private Fragments currentFragment;

    /**
     * Store the current fragment in an enum to reload the fragment when the state changed (e.g. screen rotated)
     */
    private enum Fragments {
        MAIN(0, R.string.fragment_main_title), SETTINGS(1, R.string.fragment_settings_title), ABOUT(2, R.string.fragment_about_title);

        private final int id;
        private final int title;
        Fragments(int id, int title) {
            this.id = id;
            this.title = title;
        }

        public int getId() {
            return id;
        }

        public int getTitle() {
            return title;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Always load the main fragment when the main activity starts
        changeFragment(MainFragment.class);
        currentFragment = Fragments.MAIN;

        setContentView(R.layout.activity_main);

        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        navDrawerLayout = findViewById(R.id.drawer_layout);
        navDrawerLayout.addDrawerListener(drawerToggle);
        navDrawerLayout.addDrawerListener(drawerListener);
        navDrawer = findViewById(R.id.nvView);
        setupDrawerContent(navDrawer);
        drawerToggle = setupDrawerToggle();
        handler = new Handler();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE 1: Make sure to override the method with only a single `Bundle` argument
    // Note 2: Make sure you implement the correct `onPostCreate(Bundle savedInstanceState)` method.
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Save UI changes to the instance state bundle when killing the process
     * @param outState the bundle
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        switch (currentFragment) {
            case MAIN:
                outState.putSerializable(BUNDLE_KEY_FRAGMENT, Fragments.MAIN);
                break;
            case SETTINGS:
                outState.putSerializable(BUNDLE_KEY_FRAGMENT, Fragments.SETTINGS);
                break;
            case ABOUT:
                outState.putSerializable(BUNDLE_KEY_FRAGMENT, Fragments.ABOUT);
                break;
            default:
                outState.putSerializable(BUNDLE_KEY_FRAGMENT, Fragments.MAIN);
                break;
        }
    }

    /**
     * Restore UI state from the instance state bundle
     * This bundle has also been passed to onCreate
     * @param savedInstanceState the bundle
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Fragments restoredFragment = (Fragments) savedInstanceState.getSerializable(BUNDLE_KEY_FRAGMENT);
        if (restoredFragment != null) {
            switch (restoredFragment) {
                case MAIN:
                    changeFragment(MainFragment.class);
                    setTitle(getTitleTimespanForMainFragment());
                    break;
                case SETTINGS:
                    changeFragment(SettingsFragment.class);
                    setTitle(getString(restoredFragment.getTitle()));
                    break;
                case ABOUT:
                    changeFragment(AboutFragment.class);
                    setTitle(getString(restoredFragment.getTitle()));
                    break;
                default:
                    break;
            }
        } else {
            changeFragment(MainFragment.class);
            setTitle(getTitleTimespanForMainFragment());
        }
        currentFragment = restoredFragment;
    }

    /**
     * Change the current fragment in this activity to a new one
     * @param fragment the class of the fragment to be instantiated
     */
    private void changeFragment(Class<? extends Fragment> fragment) {
        try {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.flContent, fragment.newInstance()).commit();

            if (fragment == MainFragment.class) {
                setTitle(getTitleTimespanForMainFragment());
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a custom title that shows the currently selected time span of data. This can be changed by clicking on the calender symbol on the toolbar in the main fragment
     * @return the custom string
     */
    public String getTitleTimespanForMainFragment() {
        PreferenceManager prefs = new PreferenceManager(this);
        Calendar startDate = prefs.getDateRangeStart();
        Calendar endDate = prefs.getDateRangeEnd();

        DateFormat format = android.text.format.DateFormat.getDateFormat(this);

        return format.format(startDate.getTime()) + " - " + format.format(endDate.getTime());
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, navDrawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                }
        );
    }

    public void selectDrawerItem(final MenuItem menuItem) {
        pendingRunnable = new Runnable() {
            @Override
            public void run() {
                // Create a new fragment and specify the fragment to show based on nav item clicked
                Fragment fragment = null;
                Class fragmentClass;
                switch(menuItem.getItemId()) {
                    case R.id.nav_main_menu:
                        fragmentClass = MainFragment.class;
                        currentFragment = Fragments.MAIN;
                        break;
                    case R.id.nav_settings:
                        fragmentClass = SettingsFragment.class;
                        currentFragment = Fragments.SETTINGS;
                        break;
                    case R.id.nav_about:
                        fragmentClass = AboutFragment.class;
                        currentFragment = Fragments.ABOUT;
                        break;
                    default:
                        fragmentClass = MainFragment.class;
                        currentFragment = Fragments.MAIN;
                        break;
                }

                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Insert the fragment by replacing any existing fragment
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            }
        };

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        if (menuItem.getTitle() != getString(R.string.fragment_main_title)) {
            setTitle(menuItem.getTitle());
        } else {
            setTitle(getTitleTimespanForMainFragment());
        }
        // Close the navigation drawer
        navDrawerLayout.closeDrawers();
    }

    DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            invalidateOptionsMenu();

            if (pendingRunnable != null) {
                handler.post(pendingRunnable);
                pendingRunnable = null;
            }
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };
}
