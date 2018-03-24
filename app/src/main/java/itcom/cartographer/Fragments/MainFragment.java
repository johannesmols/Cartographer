package itcom.cartographer.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import es.dmoral.toasty.Toasty;
import itcom.cartographer.R;

public class MainFragment extends android.support.v4.app.Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true);

        CardView cardHeatmap = view.findViewById(R.id.card_heatmap);
        cardHeatmap.setOnClickListener(cardHeatmapClickListener);

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
                Toasty.info(getActivity(), "test").show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    View.OnClickListener cardHeatmapClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
}
