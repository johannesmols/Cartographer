package itcom.cartographer.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;

import itcom.cartographer.R;

public class AboutFragment extends Fragment {

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getView() != null) {
            FrameLayout frameLayout = getView().findViewById(R.id.about);

            AboutView aboutView = AboutBuilder.with(getContext())
                    .setName(R.string.app_name)
                    .setSubTitle(R.string.about_subtitle)
                    .setBrief(R.string.about_brief)
                    .addGitHubLink(R.string.about_github)
                    .addEmailLink(R.string.about_email)
                    .setAppIcon(R.mipmap.ic_launcher_round)
                    .setAppName(R.string.app_name)
                    .setVersionNameAsAppSubTitle()
                    .addFiveStarsAction()
                    .setShowDivider(true)
                    .setWrapScrollView(true)
                    .setLinksAnimated(true)
                    .setShowAsCard(true)
                    .build();

            frameLayout.addView(aboutView);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);
    }
}
