package project.sherpa.models.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.databinding.library.baseAdapters.BR;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import project.sherpa.R;
import project.sherpa.models.datamodels.Author;
import project.sherpa.models.datamodels.Rating;
import project.sherpa.ui.adapters.GuideDetailsAdapter;
import project.sherpa.utilities.FirebaseProviderUtils;
import project.sherpa.utilities.FormattingUtils;

import static project.sherpa.utilities.FirebaseProviderUtils.JPEG_EXT;

/**
 * Created by Alvin on 8/21/2017.
 */

public class RatingViewModel extends BaseObservable {

    // ** Member Variables ** //
    private Context mContext;

    private Rating mOriginalRating;
    private Rating mRating;
    private Author mUser;
    private GuideDetailsAdapter mAdapter;

    private boolean mShowHeading;

    public RatingViewModel(Context context, Rating rating, GuideDetailsAdapter adapter, Author author) {
        mContext = context;

        mRating = rating;

        mOriginalRating = new Rating();
        mOriginalRating.firebaseId = rating.firebaseId;
        mOriginalRating.setGuideId(rating.getGuideId());
        mOriginalRating.setGuideAuthorId(rating.getGuideAuthorId());
        mOriginalRating.setAuthorId(rating.getAuthorId());
        mOriginalRating.setAuthorName(rating.getAuthorName());
        mOriginalRating.setRating(rating.getRating());
        mOriginalRating.setComment(rating.getComment());

        mAdapter = adapter;
        mUser = author;
    }

    public RatingViewModel(Context context, Rating rating, GuideDetailsAdapter adapter) {
        mContext = context;
        mRating = rating;
        mAdapter = adapter;
    }

    public void showHeading() {
        mShowHeading = true;
    }

    @Bindable
    public String getAuthorName() {

        if (mRating.getAuthorId() != null) {

            // Rating does not have an AuthorId because the user is creating a new Rating
            return mRating.getAuthorName();
        } else {
            return mUser.name;
        }
    }

    @Bindable
    public StorageReference getAuthorImage() {

        String id;

        if (mRating.getAuthorId() != null) {

            // Rating does not have an AuthorId because the user is creating a new Rating
            id = mRating.getAuthorId();
        } else {
            id = mUser.firebaseId;
        }

        return FirebaseStorage.getInstance().getReference()
                .child(FirebaseProviderUtils.IMAGE_PATH)
                .child(id + JPEG_EXT);
    }

    @BindingAdapter("authorImage")
    public static void setAuthorImage(ImageView imageView, StorageReference authorRef) {
        if (authorRef != null) {
            Glide.with(imageView.getContext())
                    .using(new FirebaseImageLoader())
                    .load(authorRef)
                    .error(R.drawable.ic_account_circle)
                    .into(imageView);
        }
    }

    @Bindable
    public int getRating() {
        return mRating.getRating();
    }

    public void setRating(int rating) {
        this.mRating.setRating(rating);

        notifyPropertyChanged(BR.rating);
    }

    @Bindable
    public String getRatingDescription() {
        return FormattingUtils.formatRating(mContext, mRating.getRating());
    }

    @Bindable
    public String getComment() {
        return mRating.getComment();
    }

    public void setComment(String comment) {
        this.mRating.setComment(comment);
    }

    @BindingAdapter({"star2", "star3", "star4", "star5", "rating"})
    public static void setRatingStar(ImageView star1, ImageView star2, ImageView star3, ImageView star4, ImageView star5, int rating) {

        // Reset all stars to un-rated
        star1.setImageResource(R.drawable.ic_star);
        star2.setImageResource(R.drawable.ic_star);
        star3.setImageResource(R.drawable.ic_star);
        star4.setImageResource(R.drawable.ic_star);
        star5.setImageResource(R.drawable.ic_star);

        // Set the colors depending on the rating
        switch (rating) {
            case 5:
                star5.setImageResource(R.drawable.ic_star_yellow);

            case 4:
                star4.setImageResource(R.drawable.ic_star_yellow);

            case 3:
                star3.setImageResource(R.drawable.ic_star_yellow);

            case 2:
                star2.setImageResource(R.drawable.ic_star_yellow);

            case 1:
                star1.setImageResource(R.drawable.ic_star_yellow);
        }
    }

    @Bindable
    public int getCommentVisibility() {

        // Set Visibility of the comment TextView depending on whether the Rating has a comment
        if (mRating.getComment() == null || mRating.getComment().isEmpty()) {
            return View.GONE;
        } else {
            return View.VISIBLE;
        }
    }

    @Bindable
    public int getEditRatingVisibility() {

        // Check if the User is logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getUid().equals(mRating.getAuthorId())) {

            // Rating's authorId matches the logged in User's id, show the button for editing the
            // rating
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    @Bindable
    public int getRatingHeadingVisibility() {
        if (mUser != null || mShowHeading) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public void onClickStarRating(View view) {

        switch (view.getId()) {
            case R.id.list_rating_star_1:
                mRating.setRating(1);
                break;

            case R.id.list_rating_star_2:
                mRating.setRating(2);
                break;

            case R.id.list_rating_star_3:
                mRating.setRating(3);
                break;

            case R.id.list_rating_star_4:
                mRating.setRating(4);
                break;

            case R.id.list_rating_star_5:
                mRating.setRating(5);
                break;
        }

        notifyPropertyChanged(BR.rating);
    }

    /**
     * Click response for rate button
     *
     * @param view    View that was clicked
     */
    public void onClickRate (View view) {

        // Check to see if the user has filled out the necessary rating fields
        if (mRating.getRating() == 0) {

            // Inform the user that they must select a rating first
            Toast.makeText(
                    view.getContext(),
                    view.getContext().getString(R.string.toast_missing_rating_error),
                    Toast.LENGTH_LONG)
                    .show();

            return;
        }

        // Check whether the user has previously rated this Guide
        int previousRating = 0;

        if (mUser != null) {
            previousRating = mOriginalRating.getRating();
        }

        // Update the Firebase Database with the rating
        mRating.updateFirebase(previousRating);

        // Force the Adapter to update the Guide with the new Rating
        mAdapter.exitEditMode(getRating(), previousRating);
    }

    public void onClickEditRating(View view) {
        mAdapter.enterEditMode();
    }

    public void onClickCancelEditRating(View view) {
        mAdapter.exitEditMode(mOriginalRating.getRating(), mOriginalRating.getRating());
    }
}
