package project.sherpa.models.datamodels;

import android.database.Cursor;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import project.sherpa.data.GuideContract;
import project.sherpa.models.datamodels.abstractmodels.BaseModel;

/**
 * Created by Alvin on 9/13/2017.
 */

public class Message extends BaseModel {

    // ** Constants ** //
    public static final String AUTHOR_ID    = "authorId";
    public static final String AUTHOR_NAME  = "authorName";
    public static final String MESSAGE      = "message";
    public static final String CHAT_ID      = "chatId";
    public static final String DATE         = "date";

    // ** Member Variables ** //
    private String authorId;
    private String authorName;
    private String message;
    private String chatId;
    private long date;

    @Override
    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>();

        map.put(AUTHOR_ID, authorId);
        map.put(AUTHOR_NAME, authorName);
        map.put(MESSAGE, message);
        map.put(CHAT_ID, chatId);
        map.put(DATE, date);

        return map;
    }

    /**
     * Generates a Message data object from a Cursor describing a Message
     *
     * @param cursor    Cursor describing a Message
     * @return Message data object with contents from the Cursor
     */
    public static Message createMessageFromCursor(Cursor cursor) {

        // Get indices for the columns
        int idxFirebaseId   = cursor.getColumnIndex(GuideContract.MessageEntry.FIREBASE_ID);
        int idxAuthorId     = cursor.getColumnIndex(GuideContract.MessageEntry.AUTHOR_ID);
        int idxAuthorName   = cursor.getColumnIndex(GuideContract.AuthorEntry.NAME);
        int idxMessage      = cursor.getColumnIndex(GuideContract.MessageEntry.MESSAGE);
        int idxChatId       = cursor.getColumnIndex(GuideContract.MessageEntry.CHAT_ID);
        int idxDate         = cursor.getColumnIndex(GuideContract.MessageEntry.DATE);

        // Get the data from the Cursor
        String firebaseId   = cursor.getString(idxFirebaseId);
        String authorId     = cursor.getString(idxAuthorId);
        String authorName   = cursor.getString(idxAuthorName);
        String message      = cursor.getString(idxMessage);
        String chatId       = cursor.getString(idxChatId);
        long date           = cursor.getLong(idxDate);

        // Create a new Message with the information
        Message messageObj = new Message();

        messageObj.firebaseId   = firebaseId;
        messageObj.authorId     = authorId;
        messageObj.authorName   = authorName;
        messageObj.message      = message;
        messageObj.chatId       = chatId;
        messageObj.date         = date;

        return messageObj;
    }

    public int compare(Message otherMessage) {
        return this.date < otherMessage.date
                ? -1
                : this.date > otherMessage.date
                ? 1
                : 0;
    }

    //********************************************************************************************//
    //*********************************** Getters & Setters **************************************//
    //********************************************************************************************//


    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getMessage() {
        return message;
    }

    public String getChatId() {
        return chatId;
    }

    public Object getDate() {
        return date > 0
                ? date
                : ServerValue.TIMESTAMP;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
