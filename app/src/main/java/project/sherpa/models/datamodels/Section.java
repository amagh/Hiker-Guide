package project.sherpa.models.datamodels;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import project.sherpa.data.GuideContract;
import project.sherpa.models.datamodels.abstractmodels.BaseModel;
import project.sherpa.models.datamodels.abstractmodels.BaseModelWithImage;

/**
 * Created by Alvin on 7/17/2017.
 */

public class Section extends BaseModelWithImage implements Parcelable {
    // ** Constants ** //
    private static final String GUIDE_ID = "guideId";
    private static final String SECTION = "section";
    private static final String CONTENT = "content";
    private static final String HAS_IMAGE = "hasImage";

    // ** Member Variables ** //
    public String guideId;
    public int section;
    public String content;

    private float ratio;    // The ratio to be used for the ImageView

    public Section() {}

    public Section(String guideId, int section, String content) {
        this.guideId = guideId;
        this.section = section;
        this.content = content;
    }

    public Section(int section, String content) {
        this.section = section;
        this.content = content;
    }

    /**
     * Creates a Section using the values described in a Cursor
     *
     * @param cursor    Cursor describing a Section of a Guide
     * @return Section with the values contained in the Cursor
     */
    public static Section createSectionFromCursor(Cursor cursor) {

        // Index the columns of the Cursor
        int idxFirebaseId       = cursor.getColumnIndex(GuideContract.SectionEntry.FIREBASE_ID);
        int idxGuideId          = cursor.getColumnIndex(GuideContract.SectionEntry.GUIDE_ID);
        int idxSection          = cursor.getColumnIndex(GuideContract.SectionEntry.SECTION);
        int idxContent          = cursor.getColumnIndex(GuideContract.SectionEntry.CONTENT);
        int idxImageUri         = cursor.getColumnIndex(GuideContract.SectionEntry.IMAGE_URI);
        int idxDraft            = cursor.getColumnIndex(GuideContract.SectionEntry.DRAFT);

        // Retrieve the values from the Cursor
        String firebaseId       = cursor.getString(idxFirebaseId);
        String guideId          = cursor.getString(idxGuideId);
        int section             = cursor.getInt(idxSection);
        String content          = cursor.getString(idxContent);
        String imageUriString   = cursor.getString(idxImageUri);
        boolean draft           = cursor.getInt(idxDraft) == 1;

        // Instantiate a new Section with the values
        Section section0        = new Section();
        section0.firebaseId     = firebaseId;
        section0.guideId        = guideId;
        section0.section        = section;
        section0.content        = content;
        section0.setDraft(draft);

        if (imageUriString != null) {
            File imageFile = new File(Uri.parse(imageUriString).getPath());
            section0.setImageUri(imageFile);
        }

        return section0;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put(GUIDE_ID, guideId);
        map.put(SECTION, section);
        map.put(CONTENT, content);
        map.put(HAS_IMAGE, hasImage);

        return map;
    }

    @Exclude
    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    /**
     * Updates the values of the Section with new Values from an updated Section
     *
     * @param newModelValues    BaseModel containing the new Values
     */
    @Override
    public void updateValues(BaseModel newModelValues) {

        // Cast newModelValues to a Section
        if (!(newModelValues instanceof Section)) return;
        Section newSectionValues = (Section) newModelValues;

        // Check to ensure newSectionValues has the same FirebaseId
        if (!newSectionValues.firebaseId.equals(firebaseId)) return;

        guideId = newSectionValues.guideId;
        section = newSectionValues.section;
        content = newSectionValues.content;
    }

    //********************************************************************************************//
    //***************************** Parcelable Related Methods ***********************************//
    //********************************************************************************************//


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(firebaseId);
        parcel.writeInt(section);
        parcel.writeString(content);

        if (hasImage) {
            parcel.writeString(getImageUri().toString());
        }

        if (isDraft()) {
            parcel.writeInt(1);
        } else {
            parcel.writeInt(0);
        }
    }

    public static final Parcelable.Creator<Section> CREATOR = new Parcelable.Creator<Section>() {
        @Override
        public Section createFromParcel(Parcel parcel) {
            return new Section(parcel);
        }

        @Override
        public Section[] newArray(int i) {
            return new Section[i];
        }
    };

    private Section(Parcel parcel) {
        firebaseId = parcel.readString();
        section = parcel.readInt();
        content = parcel.readString();

        String imageUriString = parcel.readString();
        if (imageUriString != null) {
            hasImage = true;
            imageUri = Uri.parse(imageUriString);
        }

        if (parcel.readInt() == 1) {
            setDraft(true);
        }
    }
}
