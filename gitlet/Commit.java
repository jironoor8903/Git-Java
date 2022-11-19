package gitlet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;
import java.util.TreeMap;

public class Commit implements Serializable {
    /**blobuid.*/
    private String _message;
    /**blobuid.*/
    private String timepost;
    /**blobuid.*/
    private String commithash;
    /**blobuid.*/
    private String parenthash;
    /**blobuid.*/
    private String parent2hash;
    /**blobuid.*/
    private TreeMap<String, String> blob;


    public Commit(String message, String parent,
                  TreeMap<String, String> blobber) {
        _message = message;
        this.parenthash = parent;
        this.timepost = getDate();
        this.blob = blobber;
        byte[] commit = Utils.serialize(this);
        String commitHash = Utils.sha1(commit);
        this.commithash = commitHash;
        this.parent2hash = null;
    }

    public Commit(String message, String parent,
                  TreeMap<String, String> blobber, String time) {
        this._message = message;
        this.parenthash = parent;
        this.blob = blobber;
        this.timepost = time;
        byte[] commit = Utils.serialize(this);
        String commitHash = Utils.sha1(commit);
        this.commithash = commitHash;
        this.parent2hash = null;
    }

    public Commit(String message, String parent, String parent2,
                  TreeMap<String, String> blobber) {
        this._message = message;
        this.parenthash = parent;
        this.blob = blobber;
        this.timepost = getDate();
        byte[] commit = Utils.serialize(this);
        String commitHash = Utils.sha1(commit);
        this.commithash = commitHash;
        this.parent2hash = parent2;
    }

    public String getDate() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj =
                DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss yyyy -0800");
        String formattedDate = myDateObj.format(myFormatObj);
        return formattedDate;
    }

    public String getHash() {
        return commithash;
    }


    public boolean existsandisSame(String filename, String kontent) {
        if (blob.containsKey(filename)) {
            String content = Utils.sha1(kontent);
            if (blob.get(filename).equals(content)) {
                return true;
            }
        }
        return false;
    }

    public boolean exists(String filename) {
        if (blob.containsKey(filename)) {
            return true;

        }
        return false;
    }


    public String getMessage() {
        return _message;
    }

    public String getTimepost() {
        return timepost;
    }

    public String getCommithash() {
        return commithash;
    }

    public String getParenthashthash() {
        return parenthash;
    }

    public String getParent2hash() {
        return parent2hash;
    }

    public TreeMap<String, String> getBlob() {
        return blob;
    }

    public void showLog() {
        System.out.println("===");
        String hashid = getHash();
        System.out.println("commit " + hashid);
        String time = getTimepost();
        System.out.println("Date: " + time);
        System.out.println(getMessage());
        System.out.println();
    }
}
