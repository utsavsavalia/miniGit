package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;

/**
 * Commit class.
 *
 * @author Utsav Savalia
 */
public class Commit implements Serializable {

    /**
     * Private var to store commit message.
     */
    private String myMessage;
    /**
     * Private var to store time stamp.
     */
    private String myTimeStamp;
    /**
     * Private var to store parentHasp.
     */
    private String myParentHash;
    /**
     * Private var to store merged parent Hash.
     */
    private String myParent2Hash;
    /**
     * Private var to store blob files.
     */
    private HashMap<String, String> myBlobContents;

    /**
     * @param m  - message
     * @param p1 - parent1 Hash
     * @param p2 - parent2 Hash
     * @param c  - blobContents
     * The commit basic constructor.
     */
    public Commit(String m, String p1, String p2, HashMap<String, String> c) {
        myMessage = m;
        myParentHash = p1;
        myParent2Hash = p2;
        myBlobContents = c;

        LocalDateTime timeRNow = LocalDateTime.now();
        String timePattern = "EEE MMM dd HH:mm:ss yyyy Z";
        SimpleDateFormat dtFrmt = new SimpleDateFormat(timePattern);

        if (p1 == null) {
            myTimeStamp = dtFrmt.format(0);

        } else {
            myTimeStamp = dtFrmt.format(timeRNow.toEpochSecond(ZoneOffset.UTC));
        }
    }

    /**
     * @param m  - message
     * @param p - parent1 Hash
     * @param c  - blobContents
     * The commit merged constructor.
     */
    public Commit(String m, String p, HashMap<String, String> c) {
        myMessage = m;
        LocalDateTime timeRNow = LocalDateTime.now();
        String timePattern = "EEE MMM dd HH:mm:ss yyyy Z";
        SimpleDateFormat dtFrmt = new SimpleDateFormat(timePattern);

        if (p == null) {
            myTimeStamp = dtFrmt.format(0);
        } else {
            myTimeStamp = dtFrmt.format(timeRNow.toEpochSecond(ZoneOffset.UTC));
        }
        myParentHash = p;
        myBlobContents = c;
    }

    /**
     * @return String logMessage.
     * Function to get Log of commits.
     */
    public String getLog() {
        if (myParent2Hash != null) {
            String ret = "";
            ret += "===\ncommit " + Utils.sha1(Utils.serialize(this));
            ret += "\nMerge: " + myParentHash.substring(0, 7);
            ret += " " + myParent2Hash.substring(0, 7);
            ret += "\nDate: " + myTimeStamp + "\n" + myMessage + "\n";
            return ret;
        } else {
            String ret = "";
            ret += "===\ncommit " + Utils.sha1(Utils.serialize(this));
            ret += "\nDate: " + myTimeStamp + "\n" + myMessage + "\n";
            return ret;
        }
    }

    /**
     * @return String commit message.
     * Function to get private var myMessage.
     */
    public String getMyMessage() {
        return myMessage;
    }

    /**
     * @return String timestamp.
     * Function to get private var myTimeStamp
     */
    public String getMyTimeStamp() {
        return myTimeStamp;
    }

    /**
     * @return String parentHash.
     * Function to get private var myParentHash
     */
    public String getMyParentHash() {
        return myParentHash;
    }

    /**
     * @return String merged parent hash.
     * Function to get private var myParent2Hash.
     */
    public String getMyParent2Hash() {
        return myParent2Hash;
    }

    /**
     * @return HashMap blobContents.
     * Function to get private var myBlobContents.
     */
    public HashMap<String, String> getMyBlobContents() {
        return myBlobContents;
    }
}
