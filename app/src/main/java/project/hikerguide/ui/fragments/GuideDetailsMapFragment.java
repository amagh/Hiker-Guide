package project.hikerguide.ui.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import project.hikerguide.R;
import project.hikerguide.databinding.FragmentGuideDetailsMapBinding;
import project.hikerguide.models.datamodels.Guide;
import project.hikerguide.models.viewmodels.GuideDetailsMapViewModel;
import project.hikerguide.models.viewmodels.GuideViewModel;
import project.hikerguide.ui.activities.GuideDetailsActivity;
import project.hikerguide.utilities.DataCache;
import timber.log.Timber;

import static project.hikerguide.utilities.Constants.IntentKeys.GUIDE_KEY;

/**
 * Created by Alvin on 8/7/2017.
 */

public class GuideDetailsMapFragment extends MapboxFragment {


    // ** Member Variables ** //
    private FragmentGuideDetailsMapBinding mBinding;
    private Guide mGuide;

    public GuideDetailsMapFragment() {}

    /**
     * Factory for creating a GuideDetailsMapFragment for a specific Guide
     *
     * @param guideId    Guide whose details will be shown in the Fragment
     * @return A GuideDetailsMapFragment with a Bundle attached for displaying details for a Guide
     */
    public static GuideDetailsMapFragment newInstance(String guideId) {
        // Init the Bundle that will be passed with the Fragment
        Bundle args = new Bundle();

        // Put the Guide from the signature into the Bundle
        args.putString(GUIDE_KEY, guideId);

        // Initialize the Fragment and attach the args
        GuideDetailsMapFragment fragment = new GuideDetailsMapFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the View and set the ViewDataBinding
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_guide_details_map, container, false);

        // Retrieve the Guide to populate the GuideViewModel
        if (getArguments() != null && getArguments().getString(GUIDE_KEY) != null) {
            String guideId = getArguments().getString(GUIDE_KEY);
            mGuide = (Guide) DataCache.getInstance().get(guideId);

        } else {
            Timber.d("No Guide passed in the Bundle!");
        }

        // Initialize the Map
        initMap(savedInstanceState);
        mBinding.setHandler(new GuideDetailsMapViewModel((GuideDetailsActivity) getActivity()));

        // Request permission to track user on map
        ((GuideDetailsActivity) getActivity()).requestLocationPermission();

        return mBinding.getRoot();
    }

    /**
     * Sets up the MapboxMap to show the trail
     */
    private void initMap(Bundle savedInstanceState) {
        // Create a GuideViewModel, passing in the Guide
        GuideViewModel vm = new GuideViewModel(getActivity(), this, mGuide);
        vm.addSavedInstanceState(savedInstanceState);

        // Set the ViewModel to the binding
        mBinding.setVm(vm);
    }

    /**
     * Sets the MapView to begin tracking the user's position
     */
    public void trackUserPosition() {

        // Get the ViewModel that has the MapView and begin tracking location
        if (mBinding != null) {
            mBinding.getVm().setTrackUserPosition(true);
        }
    }
}
