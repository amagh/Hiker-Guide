package project.sherpa.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;
import net.simonvt.schematic.annotation.UniqueConstraint;

import project.sherpa.models.datamodels.Guide;

import project.sherpa.models.datamodels.Chat;
import project.sherpa.models.datamodels.Message;
import project.sherpa.models.datamodels.abstractmodels.BaseModel;

/**
 * Defines the tables and columns to be created for the database
 */

public class GuideContract {
    // Order of insertion into Firebase Database: AUTHOR = AREA > TRAIL > GUIDE > SECTION

    public interface GuideEntry {
        @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement String _ID      = "_id";
        @DataType(DataType.Type.TEXT) String FIREBASE_ID                            = "firebaseId";

        @DataType(DataType.Type.TEXT)
        String TITLE = Guide.TITLE;

        @DataType(DataType.Type.TEXT)
        @References(table = GuideDatabase.TRAILS, column = TrailEntry.FIREBASE_ID)
        String TRAIL_ID                                                             = "trailId";

        @DataType(DataType.Type.TEXT) String TRAIL_NAME                             = "trailName";

        @DataType(DataType.Type.TEXT)
        @References(table = GuideDatabase.AUTHORS, column = AuthorEntry.FIREBASE_ID)
        String AUTHOR_ID                                                            = "authorId";

        @DataType(DataType.Type.TEXT) String AUTHOR_NAME                            = "authorName";
        @DataType(DataType.Type.TEXT) String DATE_ADDED                             = "dateAdded";
        @DataType(DataType.Type.REAL) String RATING                                 = "rating";
        @DataType(DataType.Type.INTEGER) String REVIEWS                             = "reviews";

        @DataType(DataType.Type.REAL) String LATITUDE                               = "latitude";
        @DataType(DataType.Type.REAL) String LONGITUDE                              = "longitude";
        @DataType(DataType.Type.REAL) String DISTANCE                               = "distance";
        @DataType(DataType.Type.REAL) String ELEVATION                              = "elevation";
        @DataType(DataType.Type.INTEGER) String DIFFICULTY                          = "difficulty";
        @DataType(DataType.Type.TEXT) String AREA                                   = "area";

        @DataType(DataType.Type.TEXT) String IMAGE_URI                              = "imageUri";
        @DataType(DataType.Type.TEXT) String GPX_URI                                = "gpxUri";

        @DataType(DataType.Type.INTEGER) String DRAFT                               = "draft";
        @DataType(DataType.Type.INTEGER) String FAVORITE                            = "favorite";
    }

    public interface TrailEntry {
        @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement String _ID      = "_id";
        @DataType(DataType.Type.TEXT) @NotNull String FIREBASE_ID                   = "firebaseId";

        @DataType(DataType.Type.TEXT)
        @References(table = GuideDatabase.AREAS, column = AreaEntry.FIREBASE_ID)
        @NotNull String AREA_ID                                                     = "areaId";

        @DataType(DataType.Type.TEXT) @NotNull String NAME                          = "name";
        @DataType(DataType.Type.TEXT) @NotNull String LOWER_CASE_NAME               = "lowerCaseName";
        @DataType(DataType.Type.TEXT) String NOTES                                  = "notes";

        @DataType(DataType.Type.INTEGER) String DRAFT                               = "draft";
    }

    @UniqueConstraint(
            columns = AuthorEntry.FIREBASE_ID,
            onConflict = ConflictResolutionType.REPLACE)
    public interface AuthorEntry {
        @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement String _ID      = "_id";
        @DataType(DataType.Type.TEXT) @NotNull String FIREBASE_ID                   = "firebaseId";
        @DataType(DataType.Type.TEXT) @NotNull String NAME                          = "name";
        @DataType(DataType.Type.TEXT) @NotNull String LOWER_CASE_NAME               = "lowerCaseName";
        @DataType(DataType.Type.TEXT) @NotNull String USERNAME                      = "username";
        @DataType(DataType.Type.TEXT) @NotNull String LOWER_CASE_USERNAME           = "lowerCaseUsername";
        @DataType(DataType.Type.TEXT) String DESCRIPTION                            = "description";
        @DataType(DataType.Type.INTEGER) String SCORE                               = "score";
        @DataType(DataType.Type.TEXT) String IMAGE_URI                              = "imageUri";

        @DataType(DataType.Type.INTEGER) String DRAFT                               = "draft";
    }

    public interface SectionEntry {
        @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement String _ID      = "_id";
        @DataType(DataType.Type.TEXT) String FIREBASE_ID                            = "firebaseId";

        @DataType(DataType.Type.TEXT)
        @References(table = GuideDatabase.GUIDES, column = GuideEntry.FIREBASE_ID)
        @NotNull String GUIDE_ID                                                    = "guideId";
        
        @DataType(DataType.Type.INTEGER) @NotNull String SECTION                    = "section";
        @DataType(DataType.Type.TEXT) String CONTENT                                = "content";
        @DataType(DataType.Type.TEXT) String IMAGE_URI                              = "imageUri";

        @DataType(DataType.Type.INTEGER) String DRAFT                               = "draft";
    }

    public interface AreaEntry {
        @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement String _ID      = "_id";
        @DataType(DataType.Type.TEXT) @NotNull String FIREBASE_ID                   = "firebaseId";
        @DataType(DataType.Type.TEXT) @NotNull String NAME                          = "name";
        @DataType(DataType.Type.TEXT) @NotNull String LOWER_CASE_NAME               = "lowerCaseName";
        @DataType(DataType.Type.REAL) String LATITUDE                               = "latitude";
        @DataType(DataType.Type.REAL) String LONGITUDE                              = "longitude";
        @DataType(DataType.Type.TEXT) @NotNull String LOCATION                      = "location";

        @DataType(DataType.Type.INTEGER) String DRAFT                               = "draft";
    }

    @UniqueConstraint(
            columns = GuideContract.MessageEntry.FIREBASE_ID,
            onConflict = ConflictResolutionType.REPLACE)
    public interface MessageEntry {
        @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement
        String _ID              = "_id";
        @DataType(DataType.Type.TEXT) @NotNull
        String FIREBASE_ID      = BaseModel.FIREBASE_ID;
        @DataType(DataType.Type.TEXT)
        @References(table = GuideDatabase.AUTHORS, column = AuthorEntry.FIREBASE_ID)
        String AUTHOR_ID        = Message.AUTHOR_ID;
        @DataType(DataType.Type.TEXT)
        String MESSAGE          = Message.MESSAGE;
        @DataType(DataType.Type.TEXT)
        String ATTACHMENT       = Message.ATTACHMENT;
        @DataType(DataType.Type.TEXT)
        String ATTACHMENT_TYPE  = Message.ATTACHMENT_TYPE;
        @DataType(DataType.Type.TEXT) @NotNull
        @References(table = GuideDatabase.CHATS, column = ChatEntry.FIREBASE_ID)
        String CHAT_ID          = Message.CHAT_ID;
        @DataType(DataType.Type.REAL) @NotNull
        String DATE             = Message.DATE;
        @DataType(DataType.Type.INTEGER)
        String STATUS           = Message.STATUS;
    }

    @UniqueConstraint(
            columns = {ChatEntry.FIREBASE_ID},
            onConflict = ConflictResolutionType.REPLACE)
    public interface ChatEntry {
        @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement
        String _ID                  = "_id";
        @DataType(DataType.Type.TEXT) @NotNull
        String FIREBASE_ID          = BaseModel.FIREBASE_ID;
        @DataType(DataType.Type.INTEGER)
        String MESSAGE_COUNT        = Chat.MESSAGE_COUNT;
    }
}
