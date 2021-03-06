package project.sherpa.ui.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import project.sherpa.R;
import project.sherpa.databinding.ListItemAuthorBinding;
import project.sherpa.databinding.ListItemGuideDetailsBinding;
import project.sherpa.databinding.ListItemRatingBinding;
import project.sherpa.databinding.ListItemRatingEditBinding;
import project.sherpa.databinding.ListItemSectionImageBinding;
import project.sherpa.databinding.ListItemSectionTextBinding;
import project.sherpa.models.datamodels.Author;
import project.sherpa.models.datamodels.Guide;
import project.sherpa.models.datamodels.Rating;
import project.sherpa.models.datamodels.Section;
import project.sherpa.models.datamodels.abstractmodels.BaseModel;
import project.sherpa.models.viewmodels.AuthorViewModel;
import project.sherpa.models.viewmodels.GuideViewModel;
import project.sherpa.models.viewmodels.RatingViewModel;
import project.sherpa.models.viewmodels.SectionViewModel;
import project.sherpa.ui.activities.abstractactivities.MapboxActivity;
import timber.log.Timber;

/**
 * Created by Alvin on 7/22/2017.
 */

public class GuideDetailsAdapter extends RecyclerView.Adapter<GuideDetailsAdapter.GuideDetailsViewHolder> {

    // ** Constants ** //
    private static final int GUIDE_VIEW_TYPE            = 0;
    private static final int SECTION_VIEW_TYPE          = 1;
    private static final int SECTION_IMAGE_VIEW_TYPE    = 2;
    private static final int AUTHOR_VIEW_TYPE           = 3;
    private static final int RATING_VIEW_TYPE           = 4;
    private static final int ADD_RATING_VIEW_TYPE       = 5;

    // ** Member Variables ** //
    private SortedList.Callback<BaseModel> mSortedListCallback = new SortedList.Callback<BaseModel>() {
        @Override
        public int compare(BaseModel o1, BaseModel o2) {

            // Create a List of Classes this Adapter accepts to use as an index
            List<Class> classList = new ArrayList<>();
            classList.add(Guide.class);
            classList.add(Section.class);
            classList.add(Author.class);
            classList.add(Rating.class);

            // Compare the items
            if (o1.getClass().equals(Section.class) && o2.getClass().equals(Section.class)) {

                // If both item are Sections, they are compared by their section numbering
                return ((Section) o1).section < ((Section) o2).section
                        ? -1
                        : ((Section) o1).section > ((Section) o2).section
                        ? 1
                        : 0;
            } else if (o1.getClass().equals(Rating.class) && o2.getClass().equals(Rating.class)) {

                if (mUser != null) {
                    if (((Rating) o1).getAuthorId().equals(((Rating) o2).getAuthorId())) {
                        return 0;
                    } else if (((Rating) o1).getAuthorId().equals(mUser.firebaseId) || ((Rating) o1).getRating() == 0) {
                        return -1;
                    } else if (((Rating) o2).getAuthorId().equals(mUser.firebaseId) || ((Rating) o2).getRating() == 0) {
                        return 1;
                    }
                }

                return ((Rating) o1).getDate() < ((Rating) o2).getDate()
                        ? -1
                        : ((Rating) o1).getDate() > ((Rating) o2).getDate()
                        ? 1
                        : 0;
            } else {

                // Compare them using their index in the classList
                return classList.indexOf(o1.getClass()) < classList.indexOf(o2.getClass())
                        ? -1
                        : classList.indexOf(o1.getClass()) > classList.indexOf(o2.getClass())
                        ? 1
                        : 0;
            }
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(BaseModel oldItem, BaseModel newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(BaseModel item1, BaseModel item2) {

            return !((item1.firebaseId == null && item2.firebaseId != null) ||
                    (item1.firebaseId != null && item2.firebaseId == null)) &&
                    (item1.firebaseId == item2.firebaseId || item1.firebaseId.equals(item2.firebaseId));
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemChanged(fromPosition, toPosition);
        }
    };
    private SortedList<BaseModel> mModelsList = new SortedList<>(BaseModel.class, mSortedListCallback);

    private MapboxActivity mActivity;
    private ClickHandler mClickHandler;
    private Author mUser;
    private boolean mIsInEditMode;

    private GuideViewModel mGuideViewModel;

    public GuideDetailsAdapter(MapboxActivity activity, ClickHandler clickHandler) {
        mActivity = activity;
        mClickHandler = clickHandler;
    }

    @Override
    public GuideDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout based on the viewType
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        int layoutId;

        switch (viewType) {
            case GUIDE_VIEW_TYPE:
                layoutId = R.layout.list_item_guide_details;
                break;

            case SECTION_VIEW_TYPE:
                layoutId = R.layout.list_item_section_text;
                break;

            case SECTION_IMAGE_VIEW_TYPE:
                layoutId = R.layout.list_item_section_image;
                break;

            case AUTHOR_VIEW_TYPE:
                layoutId = R.layout.list_item_author;
                break;

            case RATING_VIEW_TYPE:
                layoutId = R.layout.list_item_rating;
                break;

            case ADD_RATING_VIEW_TYPE:
                layoutId = R.layout.list_item_rating_edit;
                break;

            default: throw new UnsupportedOperationException("Unknown view type: " + viewType);
        }

        ViewDataBinding binding = DataBindingUtil.inflate(inflater, layoutId, parent, false);
        return new GuideDetailsViewHolder(binding, this);
    }

    @Override
    public void onBindViewHolder(GuideDetailsViewHolder holder, int position) {

        // For the ViewHolder with the guide details, only bind data if the ViewModel has not been
        // instantiated prior
        if (position > 0 || mGuideViewModel == null) {
            // Bind the data to the Views
            holder.bind(position);
        }
    }

    @Override
    public int getItemCount() {
        return mModelsList.size();
    }

    @Override
    public int getItemViewType(int position) {

        Object model = mModelsList.get(position);

        // Return the ViewType based on the type of model in that position
        if (model instanceof Guide) {
            return GUIDE_VIEW_TYPE;
        } else if (model instanceof Author) {
            return AUTHOR_VIEW_TYPE;
        } else if (model instanceof Section) {
            if (((Section) model).hasImage) {
                return SECTION_IMAGE_VIEW_TYPE;
            } else {
                return SECTION_VIEW_TYPE;
            }
        } else if (model instanceof Rating) {
            if (((Rating) model).getRating() == 0) {

                // If the Rating has a rating of 0, then it must be edited
                mIsInEditMode = true;
            }

            if (mIsInEditMode && ((Rating) model).getAuthorId().equals(mUser.firebaseId)) {

                // For adding a new rating or editing a user's previous rating
                return ADD_RATING_VIEW_TYPE;
            } else {
                return RATING_VIEW_TYPE;
            }
        }

        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        if (mModelsList.get(position).firebaseId != null) {
            return mModelsList.get(position).firebaseId.hashCode();
        } else {
            return 0;
        }
    }

    /**
     * Sets the User to be
     * @param user
     */
    public void setCurrentUser(Author user) {
        mUser = user;

        // Change the layout of the first Rating
        for (int i = 0; i < mModelsList.size(); i++) {
            BaseModel model = mModelsList.get(i);

            if (model instanceof Rating) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    /**
     * Adds a model to the Adapter to be displayed
     *
     * @param model    Model to be added to the Adapter
     */
    public void addModel(final BaseModel model) {
        // Add the Model to the Adapter
        mModelsList.add(model);
    }

    /**
     * Alters the layout used for the User's review to one that is editable
     */
    public void enterEditMode() {

        // Set the memvar to true to indicate the User is editing their review
        mIsInEditMode = true;

        // Iterate through the Models in the list to find the User's Rating
        for (int i = 0; i < mModelsList.size(); i++) {
            BaseModel model = mModelsList.get(i);

            if (!(model instanceof Rating)) {
                continue;
            }

            if (((Rating) model).getAuthorId().equals(mUser.firebaseId)) {

                // Notify layout changed
                notifyItemChanged(i);

                return;
            }
        }
    }

    /**
     * Updates the Rating for the Guide with the User's new rating
     *
     * @param newRating         The new rating that the User assigned to the Guide
     * @param previousRating    The previous rating that the User assigned to the Guide
     */
    public void exitEditMode(int newRating, int previousRating) {

        // Retrieve the Guide
        Guide guide = (Guide) mModelsList.get(0);

        // Update the rating
        guide.rating += newRating - previousRating;
        if (previousRating == 0) {
            guide.reviews++;
        }

        // Turn off edit mode
        mIsInEditMode = false;

        // Notify change of the layout for the User's rating's position in the Adapter
        for (int i = 0; i < mModelsList.size(); i++) {
            BaseModel model = mModelsList.get(i);

            if (!(model instanceof Rating)) {
                continue;
            }

            if (((Rating) model).getAuthorId().equals(mUser.firebaseId)) {
                notifyItemChanged(i);

                return;
            }
        }
    }

    public interface ClickHandler {
        void onClickAuthor(Author author);
    }

    public class GuideDetailsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ViewDataBinding mBinding;
        private GuideDetailsAdapter mAdapter;

        public GuideDetailsViewHolder(ViewDataBinding binding, GuideDetailsAdapter adapter) {
            super(binding.getRoot());

            mBinding = binding;
            mAdapter = adapter;

            if (mBinding instanceof ListItemAuthorBinding) {
                mBinding.getRoot().setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            Author author = (Author) mModelsList.get(position);
            mClickHandler.onClickAuthor(author);
        }

        public void bind(int position) {
            Object model = mModelsList.get(position);

            // Load the correct ViewModel into the ViewDataBinding based on the type of model for
            // the position
            if (model instanceof Guide) {

                // Init the ViewModel for the Guide Details
                if (mGuideViewModel == null) {
                    // Pass in the MapboxActivity for lifecycle purposes
                    mGuideViewModel = new GuideViewModel(mActivity, (Guide) model);
                }

                ((ListItemGuideDetailsBinding) mBinding).setVm(mGuideViewModel);
            } else if (model instanceof Section) {

                // Load the correct ViewDataBinding depending on whether the section has an image
                if (((Section) mModelsList.get(position)).hasImage) {

                    // Set the ratio to be used by the ImageView if it has been set in the Section
                    // ** This allows for the height of the ImageView to be calculated correctly
                    // and maintains the correct position in the RecyclerView upon configuration
                    // change **
                    if (((Section) model).getRatio() != 0) {
                        ((ListItemSectionImageBinding) mBinding).listSectionImageIv
                                .setAspectRatio(((Section) model).getRatio());
                    }

                    ((ListItemSectionImageBinding) mBinding).setVm(
                            new SectionViewModel((Section) model));
                } else {

                    ((ListItemSectionTextBinding) mBinding).setVm(
                            new SectionViewModel((Section) model));
                }
            } else if (model instanceof Author) {

                ((ListItemAuthorBinding) mBinding).setVm(
                        new AuthorViewModel(mActivity, (Author) model));
            } else if (model instanceof Rating) {


                if (mIsInEditMode && ((Rating) model).getAuthorId().equals(mUser.firebaseId)) {

                    // If the Rating does not contain an AuthorId, it is a new Rating for the user
                    // to rate the Guide with
                    RatingViewModel vm = new RatingViewModel(mActivity, (Rating) model, mAdapter, mUser);

                    ((ListItemRatingEditBinding) mBinding).setVm(vm);
                } else {

                    RatingViewModel vm = new RatingViewModel(mActivity, (Rating) model, mAdapter);

                    if (!mModelsList.get(position - 1).getClass().equals(Rating.class)) {
                        vm.showHeading();
                    }

                    ((ListItemRatingBinding) mBinding).setVm(vm);
                }
            }

            // Immediately bind the data into the Views
            mBinding.executePendingBindings();
        }
    }
}
