package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {

    /**blobuid.*/
    private String branchname;
    /**blobuid.*/
    private String mostrecentcommit;

    public Branch(String name, String commit) {
        this.branchname = name;
        this.mostrecentcommit = commit;
        Utils.writeObject(Utils.join(".gitlet/branches", name + ".txt"), this);
    }

    public String getBranchname() {
        return branchname;
    }

    public String getMostrecentcommit() {
        return mostrecentcommit;
    }

    public void updateCommit(String mostrcommit) {
        this.mostrecentcommit = mostrcommit;
        Utils.writeObject(Utils.join(".gitlet/branches",
                branchname + ".txt"), this);
    }
}
