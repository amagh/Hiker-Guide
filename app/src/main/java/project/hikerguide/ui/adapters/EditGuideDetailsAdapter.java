package project.hikerguide.ui.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import project.hikerguide.R;
import project.hikerguide.models.datamodels.Guide;
import project.hikerguide.models.datamodels.Section;
import project.hikerguide.models.datamodels.abstractmodels.BaseModel;

/**
 * Created by Alvin on 7/26/2017.
 */

public class EditGuideDetailsAdapter extends RecyclerView.Adapter<EditGuideDetailsAdapter.EditViewHolder> {
    // ** Constants ** //
    private static final int EDIT_GUIDE_VIEW_TYPE           = 0;
    private static final int EDIT_SECTION_VIEW_TYPE         = 1;
    private static final int EDIT_SECTION_IMAGE_VIEW_TYPE   = 2;

    // ** Member Variables ** //
    private List<BaseModel> mModelList;

    @Override
    public EditViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Instantiate the LayoutInflater to pass the DataBindingUtils
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Get a reference to the layout to be inflated
        int layoutId = -1;

        // Set the layout based on the ViewType
        switch (viewType) {
            case EDIT_GUIDE_VIEW_TYPE:
                layoutId = R.layout.list_item_guide_details_edit;
                break;

            case EDIT_SECTION_VIEW_TYPE:
                layoutId = R.layout.list_item_section_text_edit;
                break;

            case EDIT_SECTION_IMAGE_VIEW_TYPE:
                layoutId = R.layout.list_item_section_image_edit;
                break;
        }

        return new EditViewHolder(DataBindingUtil.inflate(inflater, layoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(EditViewHolder holder, int position) {

        // Bind the data model to the View
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (mModelList != null) {
            return mModelList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {

        // Get the model from the corresponding position in the List
        BaseModel model = mModelList.get(position);

        // Return ViewType based on the type of BaseModel
        if (model instanceof Guide) {
            return EDIT_GUIDE_VIEW_TYPE;
        } else if (model instanceof Section) {

            // There are two types of EditSection layouts. One with image. One without.
            if (((Section) model).hasImage) {
                return EDIT_SECTION_IMAGE_VIEW_TYPE;
            } else {
                return EDIT_SECTION_VIEW_TYPE;
            }
        }

        return super.getItemViewType(position);
    }

    /**
     * Adds a BaseModel to be displayed by the Adapter
     *
     * @param model    BaseModel to be displayed
     */
    public void addModel(BaseModel model) {

        // Instantiate the List if needed
        if (mModelList == null) {
            mModelList = new ArrayList<>();
        }

        // Add the model based on the type of BaseModel it is
        if (model instanceof Guide) {

            // Guide models should always be the first item and there should only be one
            if (mModelList.size() == 0) {

                // First model added. Add to List
                mModelList.add(model);
                notifyItemInserted(0);
            } else if (mModelList.get(0) instanceof Guide) {

                // Guide already exists in List, replace it with the new Guide Object
                mModelList.set(0, model);
                notifyItemChanged(0);
            } else {

                // Other items exist in the List, add the Guide to the first position
                mModelList.add(0, model);
                notifyItemInserted(0);
            }

        } else if (model instanceof Section) {

            // Sections are simply added to a growing List
            mModelList.add(model);
            notifyItemInserted(mModelList.size() - 1);
        }
    }

    class EditViewHolder extends RecyclerView.ViewHolder {
        // ** Member Variables ** //
        ViewDataBinding mBinding;

        private EditViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());

            mBinding = binding;
        }

        private void bind(int position) {

        }
    }
}
