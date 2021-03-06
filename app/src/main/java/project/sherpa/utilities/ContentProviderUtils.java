package project.sherpa.utilities;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import project.sherpa.R;
import project.sherpa.data.GuideContract;
import project.sherpa.data.GuideProvider;
import project.sherpa.models.datamodels.Area;
import project.sherpa.models.datamodels.Author;
import project.sherpa.models.datamodels.Chat;
import project.sherpa.models.datamodels.Guide;
import project.sherpa.models.datamodels.Message;
import project.sherpa.models.datamodels.Section;
import project.sherpa.models.datamodels.Trail;
import project.sherpa.models.datamodels.abstractmodels.BaseModel;

/**
 * Created by Alvin on 8/7/2017.
 */

public class ContentProviderUtils {

    /**
     * Inserts a model into the database
     *
     * @param context    Interface to global Context
     * @param model      Model to be inserted into the database
     */
    public static void insertModel(Context context, BaseModel model) {

        // Insert the values into the database
        context.getContentResolver().insert(

                // Retrieve the Uri for the model
                getUriForModel(model),

                // Build the ContentValues for the model
                getContentValuesForModel(model));
    }

    /**
     * Bulk inserts an Array of Sections into the database
     *
     * @param context     Interface to global Context
     * @param sections    Array of Sections to be inserted into the database
     */
    public static void bulkInsertSections(Context context, Section... sections) {

        // Init the Array of ContentValues to be bulk inserted
        ContentValues[] sectionValues = new ContentValues[sections.length];

        // Iterate through the Sections and create ContentValues from them
        for (int i = 0; i < sections.length; i++) {
            Section section = sections[i];
            sectionValues[i] = getValuesForSection(section);
        }

        // Bulk insert the values into the database
        context.getContentResolver().bulkInsert(
                GuideProvider.Sections.CONTENT_URI,
                sectionValues);
    }

    /**
     * Bulk inserts an Array of Messages into the database
     *
     * @param context     Interface to global Context
     * @param messages    Array of Messages to be inserted into the database
     */
    public static void bulkInsertMessages(Context context, Message... messages) {

        // Init Array of ContentValues to be bulk inserted
        ContentValues[] messageValues = new ContentValues[messages.length];

        // Create ContentValues for each Message to be inserted
        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            messageValues[i] = getValuesForMessage(message);
        }

        // Bulk insert
        context.getContentResolver().bulkInsert(
                GuideProvider.Messages.CONTENT_URI,
                messageValues);
    }

    /**
     * Removes a model from the database, using its associated FirebaseId
     *
     * @param context    Interface to global Context
     * @param model      Model to be removed from the database
     */
    public static void deleteModel(Context context, BaseModel model) {
        context.getContentResolver().delete(
                getUriForModel(model),
                GuideContract.GuideEntry.FIREBASE_ID + " = ?",
                new String[] {model.firebaseId});
    }

    /**
     * Removes all Sections for a given Guide from the database
     *
     * @param context    Interface to global Context
     * @param guide      Guide whose associated Sections are to be removed
     */
    public static void deleteSectionsForGuide(Context context, Guide guide) {
        context.getContentResolver().delete(
                GuideProvider.Sections.CONTENT_URI,
                GuideContract.SectionEntry.GUIDE_ID + " = ?",
                new String[] {guide.firebaseId});
    }

    /**
     * Counts the number of Guides in the local database authored by the Author
     *
     * @param context    Interface to global Context
     * @param author     Author to count the number of Guides for
     * @return The number of Guides authored by the Author that exist in the local database
     */
    public static int getGuideCountForAuthor(Context context, Author author) {

        // Query the database for Guides authored by the Author
        Cursor cursor = context.getContentResolver().query(
                GuideProvider.Guides.CONTENT_URI,
                null,
                GuideContract.GuideEntry.AUTHOR_ID + " = ?",
                new String[] {author.firebaseId},
                null);

        // Return the Cursor count
        try {
            if (cursor != null) {
                return cursor.getCount();
            } else {
                return 0;
            }
        } finally {

            // Close the Cursor
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Counts the number of Trails in the database using an Area
     *
     * @param context    Interface to global Context
     * @param area       Area to count the number of associated Trails for
     * @return The number of Trails in the database associated with an Area
     */
    public static int getTrailCountForArea(Context context, Area area) {

        // Query the database for Trails associated with the Area
        Cursor cursor = context.getContentResolver().query(
                GuideProvider.Trails.CONTENT_URI,
                null,
                GuideContract.TrailEntry.AREA_ID + " = ?",
                new String[] {area.firebaseId},
                null);

        // Return the Cursor count
        try {
            if (cursor != null) {
                return cursor.getCount();
            } else {
                return 0;
            }
        } finally {

            // Close the Cursor
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Checks whether a data model already exists in the database
     *
     * @param context    Interface to global Context
     * @param model      Model to be checked against database records to see if it exists
     * @return True if the Model's FirebaseId already exists in the database. False otherwise
     */
    public static boolean isModelInDatabase(Context context, BaseModel model) {

        // Query the database for the model's FirebaseId
        Cursor cursor = context.getContentResolver().query(
                getUriForModel(model),
                null,
                GuideContract.GuideEntry.FIREBASE_ID + " = ?",
                new String[] {model.firebaseId},
                null
        );

        // Check if Cursor is valid
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {

                    // Entry exists in database
                    return true;
                }
            } finally {
                // Close the Cursor
                cursor.close();
            }
        }

        return false;
    }

    /**
     * Checks whether a model has been cached
     *
     * @param context    Interface to global Context
     * @param guide      The Guide to be checked whether it has been cached in the database
     * @return True if the Guide is cached. False otherwise.
     */
    public static boolean isGuideCachedInDatabase(Context context, Guide guide) {
        // Query the database for the model's FirebaseId
        Cursor cursor = context.getContentResolver().query(
                getUriForModel(guide),
                null,
                GuideContract.GuideEntry.FIREBASE_ID + " = ? AND " + GuideContract.GuideEntry.IMAGE_URI + " IS NOT NULL",
                new String[] {guide.firebaseId},
                null
        );

        // Check if Cursor is valid
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {

                    // Entry exists in database
                    return true;
                }
            } finally {
                // Close the Cursor
                cursor.close();
            }
        }

        return false;
    }

    /**
     * Toggles the favorite status of a Guide in the local database
     *
     * @param context    Interface to global Context
     * @param guide      Guide whose favorite status is to be toggled
     */
    public static void toggleFavorite(Context context, Guide guide) {

        // Either insert the Guide or update the value in the database
        if (isModelInDatabase(context, guide)) {

            // ContentValues for modifying the favorite status of a Guide
            ContentValues values = new ContentValues();

            if (guide.isFavorite()) {
                values.put(GuideContract.GuideEntry.FAVORITE, 1);
            } else {
                values.put(GuideContract.GuideEntry.FAVORITE, 0);
            }

            context.getContentResolver().update(
                    GuideProvider.Guides.CONTENT_URI,
                    values,
                    GuideContract.GuideEntry.FIREBASE_ID + " = ?",
                    new String[] {guide.firebaseId});
        } else {
            insertModel(context, guide);
        }
    }

    /**
     * Checks whether a Guide is favorite'd in the local database
     *
     * @param context    Interface to global Context
     * @param guide      Guide to be checked
     * @return True if the Guide is favorite'd in the database. False otherwise.
     */
    public static boolean isGuideFavorite(Context context, Guide guide) {

        // Check whether the Guide exists in the database
        if (!isModelInDatabase(context, guide)) {

            // Does not exist in the database. Cannot be a favorite
            return false;
        } else {

            // Query the database for the Guide
            Cursor cursor = context.getContentResolver().query(
                    GuideProvider.Guides.CONTENT_URI,
                    null,
                    GuideContract.GuideEntry.FIREBASE_ID + " = ?",
                    new String[] {guide.firebaseId},
                    null);

            if (cursor != null) {

                // Create a Guide from the Cursor
                cursor.moveToFirst();
                guide = Guide.createGuideFromCursor(cursor);

                try {

                    // Return the favorite status of the Guide
                    return guide.isFavorite();
                } finally {

                    // Close the Cursor
                    cursor.close();
                }

            } else {
                return false;
            }
        }
    }

    /**
     * Generates an Author data model that will contain all the currently favorite'd Guides from
     * the local database. This is to be used when a User creates a new account and desires to
     * transfer their favorite Guides to their Firebase Database entry
     *
     * @param context    Interface to global Context
     * @return Author Object containing a map of favorite Guides from the local database
     */
    public static Author generateAuthorFromDatabase(Context context) {

        // Init a new Author to be returned
        Author author = new Author();
        author.name = context.getString(R.string.author_name_default);

        // Query the database for favorite Guides
        Cursor cursor = context.getContentResolver().query(
                GuideProvider.Guides.CONTENT_URI,
                null,
                GuideContract.GuideEntry.FAVORITE + " = ?",
                new String[] {"1"},
                null);

        // Check that the Cursor is valid
        if (cursor != null) {
            if (cursor.moveToFirst()) {

                // Map to hold the favorite Guides
                Map<String, String> favoritesMap = new HashMap<>();

                do {

                    // Create a Guide from the Cursor
                    Guide guide = Guide.createGuideFromCursor(cursor);

                    // Add the FirebaseId and Trail name to the Map
                    favoritesMap.put(guide.firebaseId, guide.trailName);

                } while (cursor.moveToNext());

                // Close the Cursor
                cursor.close();

                // Set the favoritesMaps to its favorites
                author.favorites = favoritesMap;

                return author;
            }
        }

        return author;
    }

    /**
     * Syncs the local database to match the online database in terms of User favorites
     *
     * @param context    Interface to global Context
     * @param author     Author to sync the local database to
     */
    public static void cleanDatabase(Context context, Author author) {

        // Remove all entries that are not saved Guides
        context.getContentResolver().delete(
                GuideProvider.Guides.CONTENT_URI,
                GuideContract.GuideEntry.IMAGE_URI + " IS NULL",
                null);

        // Check to see if the Author has any items favorite'd
        if (author.favorites == null) {

            // Set all items that have been saved and are favorite'd to not-favorite as the user
            // will be drawing their favorite list from online from now on.
            ContentValues values = new ContentValues();
            values.put(GuideContract.GuideEntry.FAVORITE, 0);

            context.getContentResolver().update(
                    GuideProvider.Guides.CONTENT_URI,
                    values,
                    GuideContract.GuideEntry.FAVORITE + " = ?",
                    new String[] {"1"});
        } else {

            // Query the database for saved Guides and set the favorite status depending on whether
            // the users Firebase Entry contains the Guide's FirebaseID as a favorite
            Cursor cursor = context.getContentResolver().query(
                    GuideProvider.Guides.CONTENT_URI,
                    null,
                    GuideContract.GuideEntry.IMAGE_URI + " IS NOT NULL",
                    null,
                    null);

            // Ensure Cursor is valid
            if (cursor != null) {

                // Iterate through the database entries and set the favorite status depending on
                // whether the user's online entry contains the Guide
                if (cursor.moveToFirst()) {
                    ArrayList<ContentProviderOperation> operationList = new ArrayList<>();

                    do {

                        // Create a Guide from the Cursor
                        Guide guide = Guide.createGuideFromCursor(cursor);

                        // Init the ContentProviderOperations for the value update
                        ContentProviderOperation.Builder builder = ContentProviderOperation
                                .newUpdate(GuideProvider.Guides.CONTENT_URI)
                                .withSelection(
                                        GuideContract.GuideEntry.FIREBASE_ID + " = ?",
                                        new String[] {guide.firebaseId});

                        // Toggle the favorite status
                        if (author.favorites.containsKey(guide.firebaseId)) {
                            builder.withValue(GuideContract.GuideEntry.FAVORITE, 1);
                        } else {
                            builder.withValue(GuideContract.GuideEntry.FAVORITE, 0);
                        }

                        // Add the operation to the List of operations to be performed
                        operationList.add(builder.build());

                    } while (cursor.moveToNext());

                    // Batch apply the updates to the database
                    try {
                        context.getContentResolver().applyBatch(GuideProvider.AUTHORITY, operationList);
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }
                }

                // Close the Cursor
                cursor.close();
            }

            // Delete Chats and Messages
            context.getContentResolver().delete(GuideProvider.Chats.CONTENT_URI, null, null);
            context.getContentResolver().delete(GuideProvider.Messages.CONTENT_URI, null, null);
        }
    }

    /**
     * Checks whether the offline database already contains a cached guide. This method will be
     * used to limit the number of cached guides in the free version.
     *
     * @param context    Interface to global Context
     * @return True if the database already contains a cached guide. False otherwise.
     */
    public static boolean containsCachedGuide(Context context) {
        Cursor cursor = context.getContentResolver().query(
                GuideProvider.Guides.CONTENT_URI,
                null,
                GuideContract.GuideEntry.IMAGE_URI + " IS NOT NULL AND " + GuideContract.GuideEntry.DRAFT + " IS NULL",
                null,
                null);

        if (cursor != null) {
            try {
                return cursor.getCount() > 0;
            } finally {
                cursor.close();
            }
        }

        return false;
    }

    /**
     * Creates a ContentValues for a Guide data model
     *
     * @param guide    Guide to create ContentValues for
     * @return ContentValues describing a Guide
     */
    private static ContentValues getValuesForGuide(Guide guide) {
        ContentValues values = new ContentValues();

        values.put(GuideContract.GuideEntry.FIREBASE_ID,    guide.firebaseId);
        values.put(GuideContract.GuideEntry.TRAIL_ID,       guide.trailId);
        values.put(GuideContract.GuideEntry.TRAIL_NAME,     guide.trailName);
        values.put(GuideContract.GuideEntry.AUTHOR_ID,      guide.authorId);
        values.put(GuideContract.GuideEntry.AUTHOR_NAME,    guide.authorName);
        values.put(GuideContract.GuideEntry.TITLE,          guide.getTitle());
        values.put(GuideContract.GuideEntry.DATE_ADDED,     guide.getDate());
        values.put(GuideContract.GuideEntry.RATING,         guide.rating);
        values.put(GuideContract.GuideEntry.REVIEWS,        guide.reviews);
        values.put(GuideContract.GuideEntry.LATITUDE,       guide.latitude);
        values.put(GuideContract.GuideEntry.LONGITUDE,      guide.longitude);
        values.put(GuideContract.GuideEntry.DISTANCE,       guide.distance);
        values.put(GuideContract.GuideEntry.ELEVATION,      guide.elevation);
        values.put(GuideContract.GuideEntry.DIFFICULTY,     guide.difficulty);
        values.put(GuideContract.GuideEntry.AREA,           guide.area);

        // Add image Uri if the Guide has an image
        if (guide.getImageUri() != null) {
            values.put(GuideContract.GuideEntry.IMAGE_URI,  guide.getImageUri().toString());
        }

        // Add GPX Uri if the Guide has a GPX
        if (guide.getGpxUri() != null) {
            values.put(GuideContract.GuideEntry.GPX_URI,    guide.getGpxUri().toString());
        }

        // Add column value for whether the Guide is a draft
        if (guide.isDraft()) {
            values.put(GuideContract.GuideEntry.DRAFT,      1);
        }

        if (guide.isFavorite()) {
            values.put(GuideContract.GuideEntry.FAVORITE,   1);
        } else {
            values.put(GuideContract.GuideEntry.FAVORITE,   0);
        }

        return values;
    }

    /**
     * Creates a ContentValues describing a Trail data model
     *
     * @param trail    Trail to be converted to ContentValues
     * @return ContentValues describing the Trail in the signature
     */
    private static ContentValues getValuesForTrail(Trail trail) {
        ContentValues values = new ContentValues();

        values.put(GuideContract.TrailEntry.FIREBASE_ID,        trail.firebaseId);
        values.put(GuideContract.TrailEntry.AREA_ID,            trail.areaId);
        values.put(GuideContract.TrailEntry.NAME,               trail.name);
        values.put(GuideContract.TrailEntry.LOWER_CASE_NAME,    trail.name.toLowerCase());
        values.put(GuideContract.TrailEntry.NOTES,              trail.notes);

        if (trail.isDraft()) {
            values.put(GuideContract.TrailEntry.DRAFT,          1);
        }
        return values;
    }

    /**
     * Creates a ContentValues describing an Author data model
     *
     * @param author    Author to be converted to ContentValues
     * @return ContentValues describing the Author in the signature
     */
    private static ContentValues getValuesForAuthor(Author author) {
        ContentValues values = new ContentValues();

        values.put(GuideContract.AuthorEntry.FIREBASE_ID,           author.firebaseId);
        values.put(GuideContract.AuthorEntry.NAME,                  author.name);
        values.put(GuideContract.AuthorEntry.LOWER_CASE_NAME,       author.name.toLowerCase());
        values.put(GuideContract.AuthorEntry.USERNAME,              author.getUsername());
        values.put(GuideContract.AuthorEntry.LOWER_CASE_USERNAME,   author.getUsername().toLowerCase());
        values.put(GuideContract.AuthorEntry.DESCRIPTION,           author.description);
        values.put(GuideContract.AuthorEntry.SCORE,                 author.score);

        // Add image Uri if the Guide has an image
        if (author.getImageUri() != null) {
            values.put(GuideContract.AuthorEntry.IMAGE_URI,         author.getImageUri().toString());
        }

        if (author.isDraft()) {
            values.put(GuideContract.AuthorEntry.DRAFT,         1);
        }

        return values;
    }

    /**
     * Creates a ContentValues for a Section data model
     *
     * @param section    Section to be converted to ContentValues
     * @return ContentValues describing the Section in the signature
     */
    private static ContentValues getValuesForSection(Section section) {
        ContentValues values = new ContentValues();

        values.put(GuideContract.SectionEntry.FIREBASE_ID,      section.firebaseId);
        values.put(GuideContract.SectionEntry.GUIDE_ID,         section.guideId);
        values.put(GuideContract.SectionEntry.SECTION,          section.section);
        values.put(GuideContract.SectionEntry.CONTENT,          section.content);

        // Add image Uri if the Guide has an image
        if (section.hasImage) {
            values.put(GuideContract.SectionEntry.IMAGE_URI,    section.getImageUri().toString());
        }

        if (section.isDraft()) {
            values.put(GuideContract.SectionEntry.DRAFT,        1);
        }

        return values;
    }

    /**
     * Creates a ContentValues for an Area data model
     *
     * @param area    Area to be converted to ContentValues
     * @return ContentValues describing the Area in the signature
     */
    private static ContentValues getValuesForArea(Area area) {
        ContentValues values = new ContentValues();

        values.put(GuideContract.AreaEntry.FIREBASE_ID,         area.firebaseId);
        values.put(GuideContract.AreaEntry.NAME,                area.name);
        values.put(GuideContract.AreaEntry.LOWER_CASE_NAME,     area.name.toLowerCase());
        values.put(GuideContract.AreaEntry.LATITUDE,            area.latitude);
        values.put(GuideContract.AreaEntry.LONGITUDE,           area.longitude);
        values.put(GuideContract.AreaEntry.LOCATION,            area.location);

        if (area.isDraft()) {
            values.put(GuideContract.AreaEntry.DRAFT,           1);
        }

        return values;
    }

    /**
     * Creates a Content Values for a Message data model
     *
     * @param message    Message to be Converted to ContentValues
     * @return ContentValues describing a Message
     */
    private static ContentValues getValuesForMessage(Message message) {
        ContentValues values = new ContentValues();

        values.put(GuideContract.MessageEntry.FIREBASE_ID,      message.firebaseId);
        values.put(GuideContract.MessageEntry.CHAT_ID,          message.getChatId());
        values.put(GuideContract.MessageEntry.AUTHOR_ID,        message.getAuthorId());
        values.put(GuideContract.MessageEntry.DATE,             message.getDate() == 0
                                                                        ? System.currentTimeMillis()
                                                                        : message.getDate());
        values.put(GuideContract.MessageEntry.MESSAGE,          message.getMessage());
        values.put(GuideContract.MessageEntry.ATTACHMENT,       message.getAttachment());
        values.put(GuideContract.MessageEntry.ATTACHMENT_TYPE,  message.getAttachmentType());

        return values;
    }

    /**
     * Creates a ContentValues for a Chat data model
     *
     * @param chat Chat to be inserted into the database
     */
    public static ContentValues getValuesForChat(Chat chat) {

        // Create a ContentValues for each member of the chat
        ContentValues values = new ContentValues();

        values.put(GuideContract.ChatEntry.FIREBASE_ID,     chat.firebaseId);
        values.put(GuideContract.ChatEntry.MESSAGE_COUNT,   chat.getMessageCount());

        return values;
    }

    /**
     * Creates a ContentValues from a data model Object by checking the type of data object it is
     * and calling the corresponding getValuesFor(Model) method.
     *
     * @param model    BaseModel data model to be converted to ContentValues
     * @return ContentValues describing the data model in the signature
     */
    public static ContentValues getContentValuesForModel(BaseModel model) {

        // Convert to ContentValues based on the class of the data model
        if (model instanceof Guide) {
            return getValuesForGuide((Guide) model);
        } else if (model instanceof Trail) {
            return getValuesForTrail((Trail) model);
        } else if (model instanceof Author) {
            return getValuesForAuthor((Author) model);
        } else if (model instanceof Section) {
            return getValuesForSection((Section) model);
        } else if (model instanceof Area) {
            return getValuesForArea((Area) model);
        } else if (model instanceof Message) {
            return getValuesForMessage((Message) model);
        } else if (model instanceof Chat) {
            return getValuesForChat((Chat) model);
        }

        return null;
    }

    /**
     * Retrieves the Content Uri required to insert values for a given model
     *
     * @param model    The data model to be inserted into the database
     * @return The Uri corresponding to the data model for insertion into the database
     */
    public static Uri getUriForModel(BaseModel model) {

        // Retrieve the Content Uri for insertion based on the class of the BaseModel
        if (model instanceof Guide) {
            return GuideProvider.Guides.CONTENT_URI;
        } else if (model instanceof Trail) {
            return GuideProvider.Trails.CONTENT_URI;
        } else if (model instanceof Author) {
            return GuideProvider.Authors.CONTENT_URI;
        } else if (model instanceof Section) {
            return GuideProvider.Sections.CONTENT_URI;
        } else if (model instanceof Area) {
            return GuideProvider.Areas.CONTENT_URI;
        } else if (model instanceof Message) {
            return GuideProvider.Messages.CONTENT_URI;
        } else if (model instanceof Chat) {
            return GuideProvider.Chats.CONTENT_URI;
        }

        return null;
    }

    /**
     * Checks the number of messages that have been inserted into the local database for a given
     * Chat
     *
     * @param context    Interface to global Context
     * @param chatId     FirebaseId of the Chat to check
     * @return The number of messages in the local database for the Chat
     */
    public static int getMessageCount(Context context, String chatId) {

        // Query the database for the Chat
        Cursor cursor = context.getContentResolver().query(
                GuideProvider.Chats.byId(chatId),
                null, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {

                    // Return the number of messages
                    return Chat.createChatFromCursor(cursor).getMessageCount();
                }
            } finally {

                // Close the Cursor
                cursor.close();
            }
        }

        // Chat is not in database, return 0 for zero messages downloaded
        return 0;
    }
}
