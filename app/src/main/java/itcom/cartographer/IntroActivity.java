package itcom.cartographer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import itcom.cartographer.Database.ProcessJSON;
import itcom.cartographer.Utils.PreferenceManager;
import itcom.cartographer.Utils.Unzipper;

// From this tutorial: https://www.androidhive.info/2016/05/android-build-intro-slider-app/

public class IntroActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnBack, btnNext;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking for first time launch - before calling setContentView()
        prefManager = new PreferenceManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchMainActivity();
            finish();
        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.intro_activity);

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);

        // Hide back button on first page
        btnBack.setVisibility(View.GONE);

        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[] {
                R.layout.intro_slide_1,
                R.layout.intro_slide_2,
                R.layout.intro_slide_3,
                R.layout.intro_slide_4
        };

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter(this);
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        viewPager.setOffscreenPageLimit(layouts.length);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                if (viewPager.getCurrentItem() > 0) {
                    // move to previous screen
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                if (viewPager.getCurrentItem() < layouts.length - 1) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                }
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;")); // bullet symbol
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[currentPage].setTextColor(colorsActive[currentPage]);
        }
    }

    private void launchMainActivity() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void launchJSONProcessor(Uri file) {
        Intent launcher = new Intent(this, ProcessJSON.class);
        launcher.putExtra(getString(R.string.intent_intro_to_json_uri), file.toString());
        startActivity(launcher);
        finish();
    }

    /**  viewpager change listener */
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'DONE'
            if (position == layouts.length - 1) {
                // last page
                btnNext.setVisibility(View.GONE);
                btnBack.setVisibility(View.VISIBLE);
            } else if (position == 0) {
                // first page. make back button gone
                btnNext.setVisibility(View.VISIBLE);
                btnBack.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setVisibility(View.VISIBLE);
                btnBack.setVisibility(View.VISIBLE);
            }

            actionsForSpecificSlides(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void actionsForSpecificSlides(int position) {
        View view = viewPager.getChildAt(position);

        switch (position) {
            case 0:
                break;
            case 1: // download file
                if (view != null) {
                    Button downloadButton = view.findViewById(R.id.slide_2_download_button);
                    if (downloadButton!= null) {
                        downloadButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // open browser
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://takeout.google.com/settings/takeout"));
                                startActivity(browserIntent);
                            }
                        });
                    }
                }
                break;
            case 2: // unzip
                if (view != null) {
                    Button extractButton = view.findViewById(R.id.slide_3_extract_button);
                    if (extractButton != null) {
                        extractButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // open file dialog
                                Intent browserIntent = new Intent().setType(Intent.normalizeMimeType("*/*")).setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(browserIntent, "Select a file"), 2); // 2 = file chooser for zip file
                            }
                        });
                    }
                }
                break;
            case 3: // import data
                if (view != null) {
                    Button importButton = view.findViewById(R.id.slide_4_import_button);
                    if (importButton != null) {
                        importButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // open file dialog
                                Intent intent = new Intent().setType(Intent.normalizeMimeType("*/*")).setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select a file"), 1); // 1 = file chooser for json file
                            }
                        });
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) { // 1 = file chooser for json file
            Uri selectedFile = data.getData(); // The uri with the location of <></>he file
            if (selectedFile != null) {
                // String extension = MimeTypeMap.getFileExtensionFromUrl(selectedFile.toString());
                // String type = getContentResolver().getType(selectedFile);
                // if (extension != null || type != null) {
                //    if ((extension != null && extension.equals("json")) || (type != null && type.equals("application/json"))) {
                        launchJSONProcessor(selectedFile);
                //    } else {
                //        Toast.makeText(this, getString(R.string.toast_select_json), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) { // 2 = file chooser for zip file
            Uri selectedFile = data.getData();
            if (selectedFile != null) {
                String type = getContentResolver().getType(selectedFile); // mime type map somehow doesn't work with zip files
                if (type != null) {
                    if (type.equals("application/zip") || type.equals("application/x-zip") || type.equals("x-compress") || type.equals("x-compressed") || type.equals("x-zip-compressed")) {
                        new Unzipper(this).unzip(selectedFile);
                    } else {
                        Toast.makeText(this, getString(R.string.toast_select_zip), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private Context context;
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = LayoutInflater.from(context);

            View view = layoutInflater != null ? layoutInflater.inflate(layouts[position], container, false) : null;
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }


    }
}
