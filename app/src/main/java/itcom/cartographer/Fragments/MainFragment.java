package itcom.cartographer.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Calendar;

import itcom.cartographer.HeatmapActivity;
import itcom.cartographer.FavPlacesActivity;
import itcom.cartographer.MainActivity;
import itcom.cartographer.R;
import itcom.cartographer.RoutesActivity;
import itcom.cartographer.Utils.PreferenceManager;

public class MainFragment extends android.support.v4.app.Fragment {

    private MainActivity mainActivity;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true);

        CardView cardHeatmap = view.findViewById(R.id.card_heatmap);
        CardView cardRoutes = view.findViewById(R.id.card_third);
        CardView cardFavPlaces = view.findViewById(R.id.card_fav_places);

        cardHeatmap.setOnClickListener(cardHeatmapClickListener);
        cardRoutes.setOnClickListener(cardRoutesClickListener);
        cardFavPlaces.setOnClickListener(cardFavPlacesClickListener);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_main_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_main_toolbar_calendar:
                PreferenceManager prefs = new PreferenceManager(mContext);

                // Don't ask me why the start date dialog gets shown first when first calling the end dialog. I had to switch those in order to show the start dialog first
                showDatePickerDialog(prefs.getDateRangeEnd(), false);
                showDatePickerDialog(prefs.getDateRangeStart(), true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener cardHeatmapClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent changeActivity = new Intent(getActivity(), HeatmapActivity.class);
            startActivity(changeActivity);
        }
    };

    View.OnClickListener cardFavPlacesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent changeActivity = new Intent(getActivity(), FavPlacesActivity.class);
            startActivity(changeActivity);
        }
    };

    View.OnClickListener cardRoutesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent changeActivity = new Intent(getActivity(), RoutesActivity.class);
            startActivity(changeActivity);
        }
    };

    /**
     * Show a dialog with a date picker
     * @param initialDate the date where the dialog will open and have it's default value
     * @param start if this is the dialog for selecting the start or the end of the date range
     */
    private void showDatePickerDialog(Calendar initialDate, boolean start) {
        DatePickerDialog datePickerDialog;
        if (start) {
            datePickerDialog = new DatePickerDialog(mContext, onSetStartDateListener, initialDate.get(Calendar.YEAR), initialDate.get(Calendar.MONTH), initialDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.setMessage(getString(R.string.date_picker_title_start));
        } else {
            datePickerDialog = new DatePickerDialog(mContext, onSetEndDateListener, initialDate.get(Calendar.YEAR), initialDate.get(Calendar.MONTH), initialDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.setMessage(getString(R.string.date_picker_title_end));
        }
        datePickerDialog.show();
    }

    DatePickerDialog.OnDateSetListener onSetStartDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            PreferenceManager prefs = new PreferenceManager(mContext);
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            prefs.setDateRangeStart(cal);
            mainActivity.setTitle(mainActivity.getTitleTimespanForMainFragment());
        }
    };

    DatePickerDialog.OnDateSetListener onSetEndDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            PreferenceManager prefs = new PreferenceManager(mContext);
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            prefs.setDateRangeEnd(cal);
            mainActivity.setTitle(mainActivity.getTitleTimespanForMainFragment());
        }
    };
}
