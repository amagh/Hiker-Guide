package project.hikerguide.ui.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.hikerguide.R;
import project.hikerguide.data.GuideDatabase;
import project.hikerguide.databinding.FragmentSearchBinding;
import project.hikerguide.firebasedatabase.DatabaseProvider;
import project.hikerguide.firebasestorage.StorageProvider;
import project.hikerguide.models.datamodels.Guide;
import project.hikerguide.ui.activities.GuideDetailsActivity;
import project.hikerguide.ui.adapters.GuideAdapter;
import project.hikerguide.utilities.ColorGenerator;
import project.hikerguide.utilities.FirebaseProviderUtils;
import project.hikerguide.utilities.GpxUtils;
import project.hikerguide.utilities.SaveUtils;

import static android.app.Activity.RESULT_OK;
import static project.hikerguide.firebasedatabase.DatabaseProvider.GEOFIRE_PATH;
import static project.hikerguide.ui.activities.GuideDetailsActivity.IntentKeys.GUIDE_KEY;
import static project.hikerguide.utilities.StorageProviderUtils.GPX_EXT;
import static project.hikerguide.utilities.StorageProviderUtils.GPX_PATH;

/**
 * Created by Alvin on 7/25/2017.
 */

public class SearchFragment extends MapboxFragment {
    // ** Constants ** //
    private static final int PLACES_REQUEST_CODE = 6498;



    // ** Member Variables ** //
    private FragmentSearchBinding mBinding;
    private MapboxMap mMapboxMap;
    private GuideAdapter mAdapter;
    private GeoQuery mGeoQuery;
    private List<Guide> mGuideList;
    private Map<String, PolylineOptions> mGuidePolylineMap;
    private String highlightedId;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get a reference to the ViewDataBinding and inflate the View
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);

        // Launch the AutoCompleteSearchWidget
        launchPlacesSearch();

        // Attach the MapView to the Fragment's Lifecycle
        attachMapView(mBinding.searchMv);
        mBinding.searchMv.onCreate(savedInstanceState);

        // Get a reference of the MapboxMap to manipulate camera position and add Polylines
        mBinding.searchMv.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMapboxMap = mapboxMap;

                // Set map style
                mMapboxMap.setStyle(Style.OUTDOORS);

                mMapboxMap.setOnCameraIdleListener(new MapboxMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {

                        // Get the camera's position
                        LatLng position = mMapboxMap.getCameraPosition().target;

                        // Create a GeoLocation that can be used to search Firebase Database for
                        // guides in the nearby area
                        GeoLocation location = new GeoLocation(
                                position.getLatitude(),
                                position.getLongitude());

                        // Use GeoFire to query for Guides in the area that was searched
                        queryGeoFire(location);
                    }
                });
            }
        });

        // Set an OnClickListener to launch the PlaceAutocompleteSearchWidget when clicked
        mBinding.searchSv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPlacesSearch();
            }
        });

        mAdapter = new GuideAdapter(new GuideAdapter.ClickHandler() {
            @Override
            public void onGuideClicked(Guide guide) {

                // Launch the Activity detailing the Guide when the user clicks on it
                Intent intent = new Intent(getActivity(), GuideDetailsActivity.class);
                intent.putExtra(GUIDE_KEY, guide);

                startActivity(intent);
            }

            @Override
            public void onGuideLongClicked(Guide guide) {

                // Highlight the trail associated with the long-pressed Guide
                highLightPolylineForGuide(guide);
            }
        });

        // Set the GuideAdapter to use the search layout
        mAdapter.setUseSearchLayout(true);

        // Set the LayoutManager and Adapter for the RecyclerView
        mBinding.searchResultsRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.searchResultsRv.setAdapter(mAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACES_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // Get the Place selected by the user
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);

                // Convert the Google LatLng to the Mapbox LatLng so the camera can be properly
                // positioned
                com.google.android.gms.maps.model.LatLng result = place.getLatLng();
                LatLng target = new LatLng(result.latitude, result.longitude);

                if (mMapboxMap != null) {
                    // Move the camera to the correct position
                    mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(target)
                            .zoom(10)
                            .build()), 1500);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Uses GeoFire to query the FirebaseDatabase for Guides in the vicinity of the search location
     *
     * @param location    Coordinates to search the Firebase Database for nearby Guides
     */
    private void queryGeoFire(GeoLocation location) {

        // Get a reference to the GeoFire path in the Firebase Database
        DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference()
                .child(GEOFIRE_PATH);

        if (mGeoQuery == null) {

            // Use GeoFire to build a query
            GeoFire geofire = new GeoFire(firebaseRef);
            mGeoQuery = geofire.queryAtLocation(location, 10);

            mAdapter.setGuides(mGuideList);

            mGeoQuery.addGeoQueryEventListener(geoQueryEventListener);
        } else {
            // Set the center to the new location
            mGeoQuery.setCenter(location);
        }
    }

    /**
     * Retrieves a Guide data model from the Firebase Database
     *
     * @param firebaseId       The ID of the guide to be retrieved
     */
    private void getGuide(String firebaseId) {

        // Get a reference to the Guide in the Firebase Database using the firebaseId
        final DatabaseReference guideReference = FirebaseDatabase.getInstance().getReference()
                .child(GuideDatabase.GUIDES)
                .child(firebaseId);

        guideReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Convert the DataSnapshot to a Guide
                Guide guide = (Guide) FirebaseProviderUtils.getModelFromSnapshot(
                        DatabaseProvider.FirebaseType.GUIDE,
                        dataSnapshot);

                // Add the Guide to the Adapter
                mAdapter.addGuide(guide);

                // Get the GPX File for the Guide
                getGpxForGuide(guide);

                // Remove the Listener
                guideReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Downloads the GPX File for a Guide
     *
     * @param guide         Guide whose GPX File is to be downloaded
     */
    private void getGpxForGuide(final Guide guide) {

        // Create a file in the temp files directory that has a constant name so it can be
        // referenced later without downloading another copy while it exists in the cache
        final File tempGpxFile = SaveUtils.createTempFile(
                StorageProvider.FirebaseFileType.GPX_FILE,
                guide.firebaseId);

        // Get a reference to the GPX File in Firebase Storage
        StorageReference gpxReference = FirebaseStorage.getInstance().getReference()
                .child(GPX_PATH)
                .child(guide.firebaseId + GPX_EXT);

        // Check if the File has been previously downloaded
        if (tempGpxFile.length() == 0) {
            // Download the file
            gpxReference.getFile(tempGpxFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Add the Polyline representing the trail to the map
                            addPolylineOptionsToMap(guide, tempGpxFile);
                        }
                    });
        } else {
            addPolylineOptionsToMap(guide, tempGpxFile);
        }
    }

    /**
     * Adds a Polyline representing a Guide's trail as calculated by its GPX File
     *
     * @param guide     Guide associated with the GPX File
     * @param gpxFile   GPX File containing coordinates representing a trail
     */
    private void addPolylineOptionsToMap(final Guide guide, File gpxFile) {

        // Generate the MapboxOptions that will contain the Polyline
        GpxUtils.getMapboxOptions(gpxFile, new GpxUtils.MapboxOptionsListener() {
            @Override
            public void onOptionReady(MarkerOptions markerOptions, PolylineOptions polylineOptions) {

                mGuidePolylineMap.put(guide.firebaseId, polylineOptions);

                // Add the Polyline to the MapboxMap
                mMapboxMap.addPolyline(polylineOptions
                        .width(2)
                        // Set the color using the the colorPosition and the ColorGenerator
                        .color(ColorGenerator.getColor(getActivity(), mAdapter.getPosition(guide.firebaseId))));
            }
        });
    }

    /**
     * Launches the Places AutoCompleteSearch Widget in an overlay for searching for areas by name
     */
    private void launchPlacesSearch() {
        try {
            // Build the Intent to launch the Widget in overlay mode
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(getActivity());

            // Start Intent
            startActivityForResult(intent, PLACES_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the Polyline so that their colors match the colors of their Guide based on the
     * position of the Guide in mAdapter
     */
    private void updatePolylineColors() {
        for (String firebaseId : mGuidePolylineMap.keySet()) {
            mGuidePolylineMap.get(firebaseId)
                    .color(ColorGenerator.getColor(getActivity(), mAdapter.getPosition(firebaseId)));
        }
    }

    /**
     * Highlights a Polyline associated with a Guide to make it more visible on the map
     *
     * @param guide    Guide that was long-pressed by the user
     */
    private void highLightPolylineForGuide(Guide guide) {

        // Highlight the track when the user long-clicks a guide to make it more visible
        if (highlightedId != null) {

            // Reset the color of the previously highlighted track
            mGuidePolylineMap.get(highlightedId)
                    .color(ColorGenerator.getColor(getActivity(), mAdapter.getPosition(highlightedId)))
                    .width(2);

            if (guide.firebaseId.equals(highlightedId)) {

                // If the highlighted track is the same as the one that was long-pressed,
                // set the variable for the highlighted track to null to de-select it
                highlightedId = null;
                return;
            }
        }

        // Set the variable for the highlighted track to the Firebaseid of the long-pressed
        // item
        highlightedId = guide.firebaseId;

        // Highlight the selected track
        PolylineOptions highlightedPolyLine = mGuidePolylineMap.get(guide.firebaseId);
        highlightedPolyLine
                .color(ContextCompat.getColor(getActivity(), R.color.yellow_a200))
                .width(4);

        // Remove and re-add the Polyline to set it to the top of the map and ensure it
        // isn't obscured by other Polylines
        mMapboxMap.removePolyline(highlightedPolyLine.getPolyline());
        mMapboxMap.addPolyline(highlightedPolyLine);
    }

    private final GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {

            // Initialize the list to hold the Guides
            if (mGuideList == null) {
                mGuideList = new ArrayList<>();
            }

            // Init a List to hold the PolylineOptions if it is null
            if (mGuidePolylineMap == null) {
                mGuidePolylineMap = new HashMap<>();
            }

            // Get the Guide data model that entered the search area
            getGuide(key);
        }

        @Override
        public void onKeyExited(String key) {

            // Remove the Guide and its Polyline track
            mAdapter.removeGuide(key);
            mMapboxMap.removePolyline(mGuidePolylineMap.get(key).getPolyline());
            mGuidePolylineMap.remove(key);

            // Update the colors of the lines so they match the new position of the Guides
            updatePolylineColors();
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {

        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    };
}