package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Staging Class.
 *
 * @author Utsav Savalia
 */
public class Staging implements Serializable {
    /**
     * Private var to store addStaging Files.
     */
    private HashMap<String, String> addStaging;

    /**
     * Private var to store removeStaging Files.
     */
    private HashMap<String, String> removeStaging;

    /**
     * The staging constructor.
     */
    public Staging() {
        addStaging = new HashMap<String, String>();
        removeStaging = new HashMap<String, String>();
    }

    /**
     * @param name - name of the file.
     * @param SHA1 - sha1 of the file.
     * Add the file to the addStaging HashMap.
     */
    public void add(String name, String SHA1) {
        addStaging.put(name, SHA1);
    }

    /**
     * @param name - name of the file.
     * @param SHA1 - sha1 of the file.
     * Add file to removeStaging HashMap.
     */
    public void remove(String name, String SHA1) {
        removeStaging.put(name, SHA1);
    }

    /**
     * Empty the hashmap.
     */
    public void clear() {
        addStaging = new HashMap<>();
        removeStaging = new HashMap<>();
    }

    /**
     * @return addStaging HashMap.
     * Getter function to return the addStaging Hashmap.
     */
    public HashMap<String, String> getAddStaging() {
        return addStaging;
    }

    /**
     * @return removeStaging HashMap.
     * Getter function to return the addStaging Hashmap.
     */
    public HashMap<String, String> getRemoveStaging() {
        return removeStaging;
    }
}
