package gitlet;
import java.io.Serializable;
import java.io.File;

public class Blob implements Serializable {

    /**blobuid.*/
    private String _blobuid;
    /**blobuid.*/
    private String _content;

    public Blob(String content) {
        _content = content;
        _blobuid = Utils.sha1(_content);
    }
    public String getBlobuid() {
        return _blobuid;
    }
    public String getContent() {
        return _content;
    }

    public void saveBlob() {
        File blobber = Utils.join(".gitlet/blobs", _blobuid);
        Utils.writeObject(blobber, this);

    }
    public static Blob recoverBlob(String uid) {
        return Utils.readObject(Utils.join(".gitlet/blobs", uid), Blob.class);
    }
}
