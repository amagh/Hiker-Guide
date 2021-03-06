package project.sherpa.ui.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import project.sherpa.R;
import project.sherpa.data.GuideContract;
import project.sherpa.data.GuideProvider;
import project.sherpa.databinding.FragmentFavoritesBinding;
import project.sherpa.models.datamodels.Author;
import project.sherpa.models.datamodels.Guide;
import project.sherpa.models.datamodels.abstractmodels.BaseModel;
import project.sherpa.services.firebaseservice.ModelChangeListener;
import project.sherpa.ui.activities.AttachActivity;
import project.sherpa.ui.activities.abstractactivities.ConnectivityActivity;
import project.sherpa.ui.activities.GuideDetailsActivity;
import project.sherpa.ui.adapters.GuideAdapter;
import project.sherpa.ui.adapters.interfaces.ClickHandler;
import project.sherpa.ui.fragments.abstractfragments.ConnectivityFragment;
import project.sherpa.utilities.DataCache;
import project.sherpa.utilities.FirebaseProviderUtils;

import static project.sherpa.utilities.Constants.IntentKeys.AUTHOR_KEY;
import static project.sherpa.utilities.Constants.IntentKeys.GUIDE_KEY;
import static project.sherpa.utilities.FirebaseProviderUtils.FirebaseType.AUTHOR;
import static project.sherpa.utilities.FirebaseProviderUtils.FirebaseType.GUIDE;

/**
 * Created by Alvin on 8/16/2017.
 */

public class FavoritesFragment extends ConnectivityFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // ** Constants ** //
    private static final int FAVORITES_LOADER = 1126;

    // ** Member Variables ** //
    private FragmentFavoritesBinding mBinding;
    private GuideAdapter mAdapter;
    private List<Guide> mGuideList;
    private Author mAuthor;

    private ModelChangeListener<Author> mUserListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the View using DataBindingUtils
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorites, container, false);
        bindFirebaseProviderService(true);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mBinding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_favorites));

        // Show the ProgressBar
        mBinding.favoritesPb.setVisibility(View.VISIBLE);

        // Initialize the RecyclerView
        initRecyclerView();

        // Check to see if data can be loaded from cache on config change
        if (savedInstanceState != null) {

            // Load user's data from cache
            if (savedInstanceState.containsKey(AUTHOR_KEY)) {
                String authorId = savedInstanceState.getString(AUTHOR_KEY);
                mAuthor = (Author) DataCache.getInstance().get(authorId);
            }

            // Load Guides from cache
            if (savedInstanceState.containsKey(GUIDE_KEY)) {
                List<String> guideIdList = savedInstanceState.getStringArrayList(GUIDE_KEY);

                loadFavoritesFromCache(guideIdList);
            }
        }

        // Load ads if applicable
        loadAdViewModel(mBinding);

        // Begin listening for network status changes
        ((ConnectivityActivity) getActivity()).addConnectivityCallback(this);

        return mBinding.getRoot();
    }

    /**
     * Initializes the elements required for the RecyclerView
     */
    private void initRecyclerView() {

        // Initialize the List to be used by the Adapter
        mGuideList = new ArrayList<>();

        // Init the Adapter and set the click response
        mAdapter = new GuideAdapter(new ClickHandler<Guide>() {
            @Override
            public void onClick(Guide guide) {

                DataCache.getInstance().store(guide);

                if (getActivity() instanceof AttachActivity) {
                    // Return the result
                    Intent intent = new Intent();
                    intent.putExtra(GUIDE_KEY, guide.firebaseId);

                    ((AttachActivity) getActivity()).finishWithAttachment(intent);
                } else {
                    // Open the GuideDetailsActivity
                    Intent intent = new Intent(getActivity(), GuideDetailsActivity.class);
                    intent.putExtra(GUIDE_KEY, guide.firebaseId);
                    intent.putExtra(AUTHOR_KEY, guide.authorId);

                    startActivity(intent);
                }
            }
        });

        // Set the list that will be used by the Adapter
        mAdapter.setGuides(mGuideList);

        // Set the Adapter and the LayoutManager
        mBinding.favoritesRv.setAdapter(mAdapter);
        mBinding.favoritesRv.setLayoutManager(new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.guide_columns),
                StaggeredGridLayoutManager.VERTICAL));
    }

    @Override
    public void onConnected() {
        super.onConnected();

        if (mGuideList == null || mGuideList.size() == 0) {
        } else {
            mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());
        }
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();

        // Hide the ProgressBar
        mBinding.favoritesPb.setVisibility(View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Generate the CursorLoader for the favorites in the database
        return new CursorLoader(
                getActivity(),
                GuideProvider.Guides.CONTENT_URI,
                null,
                GuideContract.GuideEntry.FAVORITE + " = ?",
                new String[] {"1"},
                GuideContract.GuideEntry.TRAIL_NAME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Populate the Adapter with the database entries
        if (mGuideList.size() == 0 && data != null) {
            if (data.moveToFirst()) {

                // Retrieve of list of FirebaseIds corresponding to the favorite Guides
                List<String> guideIdList = new ArrayList<>();
                do {
                    guideIdList.add(Guide.createGuideFromCursor(data).firebaseId);
                } while (data.moveToNext());

                // Hide ProgressBar
                mBinding.favoritesPb.setVisibility(View.GONE);

                // Retrieve the Guides from Firebase Databse
                getGuides(guideIdList);
            } else {
                showEmptyText();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mAuthor != null) {
            outState.putString(AUTHOR_KEY, mAuthor.firebaseId);
        }

        if (mGuideList != null && mGuideList.size() > 0) {
            ArrayList<String> guideIdList = new ArrayList<>();

            for (Guide guide : mGuideList) {
                guideIdList.add(guide.firebaseId);
            }

            outState.putStringArrayList(GUIDE_KEY, guideIdList);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mUserListener != null) mService.registerModelChangeListener(mUserListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mUserListener != null) mService.unregisterModelChangeListener(mUserListener);
    }

    @Override
    protected void onServiceConnected() {
        loadCurrentUser();
    }

    /**
     * Loads the favorites for the user either from a local database if they do not have a Firebase
     * Account or the online database if they do
     */
    private void loadCurrentUser() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            loadFavoritesFromDatabase();
        } else {

            mUserListener = new ModelChangeListener<Author>(AUTHOR, user.getUid()) {
                @Override
                public void onModelReady(Author model) {
                    mAuthor = model;
                    loadFavorites(mAuthor);
                }

                @Override
                public void onModelChanged() {
                    updateFavorites();
                }
            };

            mService.registerModelChangeListener(mUserListener);
        }
    }

    /**
     * Retrieves the user's favorite'd guides from Firebase
     *
     * @param author    User to retrieve favorite items for
     */
    private void loadFavorites(Author author) {

        // Pass the Author data model to the Adapter so the Guides can set their
        // favorite status appropriately
        mAdapter.setAuthor(author);

        // Verify that the Author has a list of favorites
        if (author.favorites == null) {
            showEmptyText();

            return;
        }

        // Retrieve the User's favorite'd Guides and sort them alphabetically by Trail
        // name
        List<Map.Entry<String, String>> entryList = new LinkedList<>(author.favorites.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        // Init List of Guide FirebaseIds to pass to be retrieved from Firebase Database
        List<String> guideIdList = new ArrayList<>();

        // Add each key from the sorted List
        for (Map.Entry<String, String> entry : entryList) {
            guideIdList.add(entry.getKey());
        }

        // Retrieve the Guides from Firebase Database
        getGuides(guideIdList);

        if (guideIdList.size() == 0) {
            showEmptyText();
        }
    }

    /**
     * Loads Guides from cache to populate the Adapter
     *
     * @param guideIdList    List of FirebaseId for Guides to load from cache
     */
    private void loadFavoritesFromCache(List<String> guideIdList) {

        // Ensure List has items to load
        if (guideIdList != null && guideIdList.size() > 0) {

            // Hide the Progressbar
            mBinding.favoritesPb.setVisibility(View.GONE);

            // Load each Guide from cache
            for (String guideId : guideIdList) {
                Guide guide = (Guide) DataCache.getInstance().get(guideId);

                mAdapter.addGuide(guide);
            }
        }
    }

    /**
     * Hides the ProgressBar and shows the text indicating there are not favorite items to be
     * displayed
     */
    private void showEmptyText() {
        mBinding.favoritesPb.setVisibility(View.GONE);
        mBinding.favoritesEmptyTv.setVisibility(View.VISIBLE);
    }

    /**
     * Retrieves each Guide on the Author's list of favorite Guides
     *
     * @param guideIdList    List of FirebaseIds representing the favorite Guides to be retrieved
     *                         from Firebase Databse
     */
    private void getGuides(List<String> guideIdList) {

        if (guideIdList == null || guideIdList.size() == 0) {
            return;
        }

        // Iterate through the List and retrieve each Guide from Firebase
        for (String firebaseId : guideIdList) {
            addGuideToAdapter(firebaseId);
        }
    }

    /**
     * Downloads a Guide and adds them to the Adapter
     *
     * @param guideId    FirebaseId of the Guide to be added to the Adapter
     */
    private void addGuideToAdapter(String guideId) {

        ModelChangeListener<Guide> guideListener = new ModelChangeListener<Guide>(GUIDE, guideId) {
            @Override
            public void onModelReady(Guide model) {

                // Add the Guide to the Adapter
                mAdapter.addGuide(model);

                // Hide ProgressBar
                mBinding.favoritesPb.setVisibility(View.GONE);

                mService.unregisterModelChangeListener(this);
            }

            @Override
            public void onModelChanged() {

            }
        };

        mService.registerModelChangeListener(guideListener);
    }

    /**
     * Modifies the Adapter to match the updated list of favorites from the Author
     */
    private void updateFavorites() {

        if (mAuthor.favorites == null) {

            // No favorites. Clear the Adapter
            mAdapter.clear();
            return;
        }

        List<String> newFavoritesList = new ArrayList<>(mAuthor.favorites.keySet());
        List<String> adapterIdList = mAdapter.getFirebaseIds();

        // Remove any Guides that are no longer favorites
        for (String guideId : adapterIdList) {
            if (!newFavoritesList.contains(guideId)) {
                mAdapter.removeGuide(guideId);

                if (mGuideList.size() == 0) {
                    showEmptyText();
                }
            }
        }

        // Add any guides that have been favorite'd
        for (String guideId : newFavoritesList) {
            if (!adapterIdList.contains(guideId)) {
                addGuideToAdapter(guideId);
            }
        }
    }

    /**
     * Loads the list of favorites from the database
     */
    private void loadFavoritesFromDatabase() {

        // Init the CursorLoader
        getActivity().getSupportLoaderManager().initLoader(FAVORITES_LOADER, null, this);
    }
}
