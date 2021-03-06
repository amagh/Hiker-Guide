package project.sherpa.ui.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import project.sherpa.R;
import project.sherpa.databinding.ListItemFriendBinding;
import project.sherpa.models.datamodels.Author;
import project.sherpa.models.viewmodels.AuthorViewModel;
import project.sherpa.models.viewmodels.ListItemFriendViewModel;
import project.sherpa.ui.adapters.callbacks.AlphabeticalAuthorCallback;
import project.sherpa.ui.adapters.interfaces.ClickHandler;

/**
 * Created by Alvin on 9/26/2017.
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    // ** Member Variables ** //
    private SortedListAdapterCallback<Author> mSortedCallback = new AlphabeticalAuthorCallback(this);
    private SortedList<Author> mSortedList = new SortedList<>(Author.class, mSortedCallback);
    private ClickHandler<Author> mClickHandler;
    private boolean mShowSocialButton;

    public FriendAdapter(ClickHandler<Author> clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        ListItemFriendBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.list_item_friend, parent, false);

        return new FriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mSortedList.size();
    }

    /**
     * Sets the data that should be displayed by the Adapter
     *
     * @param friends    Array of Authors whose data should be displayed by the Adapter
     */
    public void setFriendList(Author[] friends) {

        // Convert the Array to a List and run the overloaded method setFriendList(List)
        List<Author> friendList = new ArrayList<>(Arrays.asList(friends));
        setFriendList(friendList);
    }

    /**
     * Sets the data that should be displayed by the Adapter
     *
     * @param friendList    List of Authors whose data should eb displayed by the Adapter
     */
    public void setFriendList(List<Author> friendList) {

        if (friendList == null || friendList.size() == 0) {
            clear();
            return;
        }

        // Start batched updates
        mSortedList.beginBatchedUpdates();

        // Iterate through the SortedList and check to see if any of the items are in friendList
        for (int i = mSortedList.size() - 1; i >= 0; i--) {
            Author listFriend = mSortedList.get(i);
            boolean exists = false;

            // Check for matches against friendList
            for (int j = friendList.size() - 1; j >= 0; j--) {
                Author friend = friendList.get(j);

                if (friend.firebaseId.equals(listFriend.firebaseId)) {

                    // Match found, remove the friend so it is not added to the SortedList
                    friendList.remove(friend);
                    exists = true;
                    break;
                }
            }

            if (!exists) {

                // Friend is not in friendList, so it should be removed from the Adapter
                mSortedList.removeItemAt(i);
            }
        }

        // Add all missing elements from the friendList
        mSortedList.addAll(friendList);

        // End batched updates
        mSortedList.endBatchedUpdates();
    }

    /**
     * Sets whether the list items should display the social quick access button
     *
     * @param show    Boolean value for whether the button should show
     */
    public void showAddSocialButton(boolean show) {
        mShowSocialButton = show;
        notifyDataSetChanged();
    }

    /**
     * Adds an Author to be displayed in the Adapter
     *
     * @param author    Author to be displayed
     */
    public void addFriend(Author author) {
        mSortedList.add(author);
    }

    /**
     * Removes a friend from the Adapter
     *
     * @param friendId    FirebaseId of the friend to be removed
     */
    public void removeFriend(String friendId) {

        // Remove the friend with FirebaseId friendId
        for (int i = 0; i < mSortedList.size(); i++) {
            Author friend = mSortedList.get(i);

            if (friend.firebaseId.equals(friendId)) {
                mSortedList.removeItemAt(i);
                return;
            }
        }
    }

    /**
     * Clears the Adapter
     */
    public void clear() {
        mSortedList.clear();
    }

    /**
     * Returns a List of all the FirebaseIds of the friends in the Adapter
     *
     * @return List of all the FirebaseIds of the friends in the Adapter
     */
    public List<String> getFirebaseIds() {
        List<String> friendIdList = new ArrayList<>();

        for (int i = 0; i < mSortedList.size(); i++) {
            Author friend =  mSortedList.get(i);
            friendIdList.add(friend.firebaseId);
        }

        return friendIdList;
    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // ** Member Variables ** //
        private ListItemFriendBinding mBinding;

        FriendViewHolder(ListItemFriendBinding binding) {
            super(binding.getRoot());

            mBinding = binding;
            mBinding.getRoot().setOnClickListener(this);
        }

        void bind(int position) {

            // Bind the data for the Author at the ViewHolder's position to the ViewHolder's Views
            Author author = mSortedList.get(position);
            AuthorViewModel vm = new AuthorViewModel((AppCompatActivity) mBinding.getRoot().getContext(), author);

            // Show the social quick action button
            ListItemFriendViewModel fvm = new ListItemFriendViewModel(author);

            if (mShowSocialButton) fvm.setShowSocialButton(true);

            mBinding.setVm(vm);
            mBinding.setFvm(fvm);
        }

        @Override
        public void onClick(View view) {

            // Return the Author at the clicked position
            int position = getAdapterPosition();
            Author author = mSortedList.get(position);
            mClickHandler.onClick(author);
        }
    }
}
