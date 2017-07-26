package project.hikerguide.ui.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import project.hikerguide.R;
import project.hikerguide.databinding.ListItemGuideBinding;
import project.hikerguide.databinding.ListItemGuideSearchBinding;
import project.hikerguide.models.datamodels.Guide;
import project.hikerguide.models.viewmodels.GuideViewModel;

/**
 * Created by Alvin on 7/21/2017.
 */

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> {
    // ** Constants ** //
    private static final int NORMAL_VIEW_TYPE = 0;
    private static final int SEARCH_VIEW_TYPE = 1;

    // ** Member Variables ** //
    private List<Guide> mGuideList;
    private ClickHandler mHandler;
    private boolean useSearchLayout;

    public GuideAdapter(ClickHandler clickHandler) {
        mHandler = clickHandler;
    }

    @Override
    public GuideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        int layoutId = -1;

        // Set the layout based on the ViewType returned
        switch (viewType) {
            case NORMAL_VIEW_TYPE:
                layoutId = R.layout.list_item_guide;
                break;

            case SEARCH_VIEW_TYPE:
                layoutId = R.layout.list_item_guide_search;
                break;
        }

        // Create the ViewDataBinding by inflating the layout
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, layoutId, parent, false);
        return new GuideViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(GuideViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        // Return the length of mGuides if it is not null
        if (mGuideList != null) {
            return mGuideList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        // Only use search layout in FragmentSearch
        if (!useSearchLayout) {
            return NORMAL_VIEW_TYPE;
        } else {
            return SEARCH_VIEW_TYPE;
        }
    }

    @Override
    public long getItemId(int position) {
        // Use the hashcode of the FirebaseId as the Id for the Adapter. This will allow a
        // completely different list of Guides to be set and animated correctly.
        return mGuideList.get(position).firebaseId.hashCode();
    }

    /**
     * Sets the resources that will be used to populate the Views in the RecyclerView
     *
     * @param guideList    A List of Guides to be used to bind data to the ViewHolders
     */
    public void setGuides(List<Guide> guideList) {
        // Set the mem var to the List parameter
        mGuideList = guideList;

        if (mGuideList != null) {
            // Notify of change in data
            notifyDataSetChanged();
        }
    }

    /**
     * Adds a Guide to the Array of Guides to be displayed
     *
     * @param guide    Guide to be added
     */
    public void addGuide(Guide guide) {
        // Check whether mGuideList has already been instantiated
        if (mGuideList == null) {
            // Has not been. Create a new List and put the Guide from the signature into it
            mGuideList = new ArrayList<>();
        }

        // Add the Guide to the List
        mGuideList.add(guide);
        notifyItemInserted(mGuideList.size() - 1);
    }

    /**
     * Retrieves the position of a Guide in the Adapter
     *
     * @param guide    The Guide to be matched against the List in the Adapter
     * @return The position of the Guide in the Adapter's List. Returns -1 if no match is found.
     */
    public int getPosition(Guide guide) {
        // Iterate through the List and try to find a match
        for (int i = 0; i < mGuideList.size(); i++) {
            if (mGuideList.get(i).equals(guide)) {
                // If it matches, return the position of the Guide
                return i;
            }
        }

        return -1;
    }

    /**
     * Sets whether the Adapter should use the search layout for guides
     *
     * @param useSearchLayout    Boolean value for whether search layout should be enabled
     */
    public void setUseSearchLayout(boolean useSearchLayout) {
        this.useSearchLayout = useSearchLayout;
    }

    /**
     * For passing information about the clicked guide to the Activity/Fragment
     */
    public interface ClickHandler {
        void onGuideClicked(Guide guide);
    }

    public class GuideViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // ** Member Variables ** //
        ViewDataBinding mBinding;

        public GuideViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());

            mBinding = binding;
            mBinding.getRoot().setOnClickListener(this);
        }

        public void bind(int position) {
            // Get the guide from the correlated position
            Guide guide = mGuideList.get(position);

            // Initialize a GuideViewModel using the Guide from the array and set it to the
            // DataBinding
            if (!useSearchLayout) {
                ((ListItemGuideBinding) mBinding)
                        .setVm(new GuideViewModel(mBinding.getRoot().getContext(), guide));
            } else {
                ((ListItemGuideSearchBinding) mBinding)
                        // Add the position to the Constructor so that color position matches the
                        // color given the track.
                        .setVm(new GuideViewModel(mBinding.getRoot().getContext(), guide, position));
            }
            mBinding.executePendingBindings();
        }

        @Override
        public void onClick(View view) {
            // Get the position of the clicked Adapter
            int position = getAdapterPosition();

            // Pass the corresponding Guide through the ClickHandler
            mHandler.onGuideClicked(mGuideList.get(position));
        }
    }
}
