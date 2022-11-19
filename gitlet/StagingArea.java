package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class StagingArea {
    /** currentcommit. */
    private HashMap<String, String> addingarea;
    /** currentcommit. */
    private ArrayList<String> removearea;

    public StagingArea() {
        addingarea = new HashMap<>();
        removearea = new ArrayList<>();
    }
    public HashMap<String, String> getAddingarea() {
        return addingarea;
    }

    public ArrayList<String> getRemovearea() {
        return removearea;
    }

    public static void add(String file, Blob content) {
        File filepath = Utils.join(".gitlet/stagingarea/add", file);
        Utils.writeObject(filepath, content);
    }
    public static void addtoremove(String file, Blob content) {
        File filepath = Utils.join(".gitlet/stagingarea/remove", file);
        Utils.writeObject(filepath, content);
    }

    public static boolean inAdd(String file) {
        return Utils.join(".gitlet/stagingarea/add", file).exists();
    }

    public static boolean inRemove(String file) {
        return Utils.join(".gitlet/stagingarea/remove", file).exists();
    }

    public void setRemovearea(String file) {
        removearea.add(file);
    }




}
