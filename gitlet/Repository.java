package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;


public class Repository implements Serializable {
    /**
     * currentcommit.
     */
    private Commit currentcommit;
    /**
     * currentcommit.
     */
    private String messageone = "There is an untracked file "
            + "in the way; delete it, or add and commit it first.";
    /**
     * currentcommit.
     */
    private static final int FOURTY = 40;

    public Repository() {
        this.currentcommit = null;
    }

    public void init() {
        File fileobject = new File(".gitlet");
        if (fileobject.exists()) {
            System.out.println("Gitlet version-control "
                    + "system already exists in the current directory.");
            System.exit(0);
        } else {
            new File(".gitlet").mkdir();
            new File(".gitlet/blobs").mkdir();
            new File(".gitlet/branches").mkdir();
            new File(".gitlet/stagingarea").mkdir();
            new File(".gitlet/stagingarea/add").mkdir();
            new File(".gitlet/stagingarea/remove").mkdir();
            new File(".gitlet/commit").mkdir();

            Commit initialCommit = new Commit("initial commit",
                    null, new TreeMap<>());
            Utils.writeObject(Utils.join(".gitlet/commit",
                    initialCommit.getHash()), initialCommit);
            currentcommit = initialCommit;
            Utils.writeObject(Utils.join(".gitlet", "repo.txt"), this);
            Branch master = new Branch("master", initialCommit.getCommithash());
            Utils.writeContents(Utils.join(".gitlet", "head.txt"),
                    currentcommit.getHash());
            Utils.writeContents(Utils.join(".gitlet", "currbranch.txt"),
                    master.getBranchname());


        }
    }


    public void add(String file) {
        File fileobject = new File(file);
        if (!fileobject.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String content = Utils.readContentsAsString(fileobject);
        if (!currentcommit.existsandisSame(file, content)) {
            Blob blob = new Blob(content);
            StagingArea.add(file, blob);
        }
        List<String> filesinremove = Utils.plainFilenamesIn(".gitlet/"
                + "stagingarea/remove");
        if (filesinremove.contains(file)) {
            File filetoremove = Utils.join(".gitlet/stagingarea/remove", file);
            filetoremove.delete();
        }
    }

    public void rm(String file) {
        boolean noReason = false;
        List<String> filesinadd = Utils.plainFilenamesIn
                (".gitlet/stagingarea/add");
        if (filesinadd.contains(file)) {
            File filetoremove = Utils.join(".gitlet/stagingarea/add", file);
            filetoremove.delete();
        } else {
            noReason = true;
        }
        if (trackedornah(file)) {
            File fileobject = new File(file);
            StagingArea.addtoremove(file, null);
            fileobject.delete();
            noReason = false;
        }
        if (noReason) {
            System.out.println("No reason to remove the file.");
        }
    }

    public boolean trackedornah(String filename) {
        String parent = Utils.readContentsAsString
                (Utils.join(".gitlet", "head.txt"));
        File headpath = Utils.join(".gitlet/commit", parent);
        Commit header = Utils.readObject(headpath, Commit.class);
        if (header.getBlob().containsKey(filename)) {
            return true;
        }
        return false;
    }

    public void commit(String message, String mergeCommit) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        List<String> filenamestoadd = Utils.plainFilenamesIn
                (".gitlet/stagingarea/add");
        List<String> filenamestoremove = Utils.plainFilenamesIn
                (".gitlet/stagingarea/remove");
        if (filenamestoremove.size() == 0 && filenamestoadd.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        File head = Utils.join(".gitlet", "head.txt");
        String currentHeadHash = Utils.readContentsAsString(head);
        File currenthead = Utils.join(".gitlet/commit", currentHeadHash);
        Commit current = Utils.readObject(currenthead, Commit.class);
        TreeMap<String, String> treemap = new TreeMap<>();
        TreeMap<String, String> oldfile = current.getBlob();
        treemap.putAll(oldfile);
        String parent = current.getHash();

        for (String file : filenamestoadd) {
            File filetoadd = Utils.join(".gitlet/stagingarea/add", file);
            Blob blob = Utils.readObject(filetoadd, Blob.class);
            treemap.put(file, blob.getBlobuid());
            blob.saveBlob();
            filetoadd.delete();
        }
        for (String file : filenamestoremove) {
            treemap.remove(file);
            File filetoremove = Utils.join(".gitlet/stagingarea/remove", file);
            filetoremove.delete();
        }


        Commit newCommit = new Commit(message, parent, mergeCommit, treemap);
        currentcommit = newCommit;
        File newcommitpath = Utils.join(".gitlet", "commit");
        Utils.writeObject(Utils.join
                (newcommitpath, newCommit.getHash()), newCommit);
        Utils.writeContents(Utils.join
                (".gitlet", "head.txt"), newCommit.getCommithash());
        String branchname = Utils.readContentsAsString
                (Utils.join(".gitlet/", "currbranch.txt"));
        Branch currentbranch = Utils.readObject(Utils.join(".gitlet/branches",
                branchname + ".txt"), Branch.class);
        currentbranch.updateCommit(newCommit.getCommithash());
        Utils.writeObject(Utils.join(".gitlet", "repo.txt"), this);
    }

    public void log() {
        String parent = Utils.readContentsAsString(Utils.join
                (".gitlet", "head.txt"));

        while (parent != null) {
            File headpath = Utils.join(".gitlet/commit", parent);
            Commit header = Utils.readObject(headpath, Commit.class);
            header.showLog();
            parent = header.getParenthashthash();
        }
    }

    public void checkout(String filename) {
        String parent = Utils.readContentsAsString(
                Utils.join(".gitlet", "head.txt"));
        checkout(parent, filename);
    }

    public void checkout(String commitid, String filename) {
        File cwdir = new File(".");
        File cwdirwfile = Utils.join(cwdir, filename);
        if (cwdirwfile.exists()) {
            cwdirwfile.delete();
        }
        Commit header;
        if (commitid.length() < FOURTY) {
            header = shortCommitId(commitid);
        } else {
            File headpath = Utils.join(".gitlet/commit", commitid);
            if (!headpath.exists()) {
                System.out.println("No commit with that id exists.");
                return;
            }
            header = Utils.readObject(headpath, Commit.class);
        }

        if (header.exists(filename)) {
            File filepath = Utils.join(".", filename);
            Blob blob = Blob.recoverBlob(header.getBlob().get(filename));
            Utils.writeContents(filepath, blob.getContent());
        } else {
            System.out.println("File does not exist in that commit");
        }

    }

    public void checkoutBranch(String branchname) {
        List<String> branches = Utils.plainFilenamesIn(".gitlet/branches");
        if (!branches.contains(branchname + ".txt")) {
            System.out.println("No such branch exists.");
            return;
        }
        String currbranch = Utils.readContentsAsString
                (Utils.join(".gitlet", "currbranch.txt"));
        if (branchname.equals(currbranch)) {
            System.out.println("No need to checkout the current branch");
            return;
        }
        Branch branch = Utils.readObject(Utils.join(".gitlet/branches",
                branchname + ".txt"), Branch.class);
        String mostrecentcommit = branch.getMostrecentcommit();
        Commit mostrcommit = Utils.readObject(Utils.join(".gitlet/commit",
                mostrecentcommit), Commit.class);
        String headCommit = Utils.readContentsAsString
                (Utils.join(".gitlet/", "head.txt"));
        Commit head = Utils.readObject
                (Utils.join(".gitlet/commit", headCommit), Commit.class);
        List<String> currentWorking = Utils.plainFilenamesIn(".");
        for (String file : currentWorking) {
            if (!head.exists(file) && !StagingArea.inAdd(file)
                    || StagingArea.inRemove(file)) {
                String content = Utils.readContentsAsString(new File(file));
                if (mostrcommit.exists(file)) {
                    if (!mostrcommit.getBlob().get(file)
                            .equals(Utils.sha1(content))) {
                        System.out.println("There is an untracked file "
                                + "in the way; delete it, or add "
                                + "and commit it first.");
                        return;
                    }
                }
            }
        }
        for (Map.Entry<String, String> entry
                : mostrcommit.getBlob().entrySet()) {
            checkout(mostrecentcommit, entry.getKey());
        }
        for (Map.Entry<String, String> entry : head.getBlob().entrySet()) {
            if (!mostrcommit.getBlob().containsKey(entry.getKey())) {
                File currentFile = new File(entry.getKey());
                currentFile.delete();
            }
        }
        cbHelper();
        Utils.writeContents(
                Utils.join(".gitlet", "head.txt"), mostrcommit.getHash());
        Utils.writeContents(
                Utils.join(".gitlet", "currbranch.txt"),
                branch.getBranchname());
        Utils.writeObject(Utils.join(".gitlet", "repo.txt"), this);
    }

    public void cbHelper() {
        List<String> filesinadd =
                Utils.plainFilenamesIn(".gitlet/stagingarea/add");
        for (String file : filesinadd) {
            Utils.join(".gitlet/stagingarea/add", file).delete();
        }
        List<String> filesinremove
                = Utils.plainFilenamesIn(".gitlet/stagingarea/remove");
        for (String file : filesinremove) {
            Utils.join(".gitlet/stagingarea/remove", file).delete();
        }
    }

    public void globalLog() {
        List<String> commitList = Utils.plainFilenamesIn(".gitlet/commit");
        for (String filename : commitList) {
            File headpath = Utils.join(".gitlet/commit", filename);
            Commit header = Utils.readObject(headpath, Commit.class);
            header.showLog();
        }
    }

    public void find(String commitMessage) {
        boolean found = false;
        List<String> commitList = Utils.plainFilenamesIn(".gitlet/commit");
        for (String filename : commitList) {
            File headpath = Utils.join(".gitlet/commit", filename);
            Commit header = Utils.readObject(headpath, Commit.class);
            if (header.getMessage().equals(commitMessage)) {
                found = true;
                System.out.println(header.getCommithash());
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }

    }

    public void branch(String branchname) {
        List<String> branchlist = Utils.plainFilenamesIn(".gitlet/branches");
        if (branchlist.contains(branchname + ".txt")) {
            System.out.println(" A branch with that name already exists");
            System.exit(0);
        }
        File headpath = Utils.join(".gitlet", "head.txt");
        String commitHash = Utils.readContentsAsString(headpath);
        Branch newbranch = new Branch(branchname, commitHash);
    }

    public void removeBranch(String branchname) {
        File headpath = Utils.join(".gitlet", "currbranch.txt");
        if (branchname.equals(Utils.readContentsAsString(headpath))) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        List<String> branchlist = Utils.plainFilenamesIn(".gitlet/branches");
        if (!branchlist.contains(branchname + ".txt")) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        File branchFile = new File(".gitlet/branches/" + branchname + ".txt");
        branchFile.delete();
    }

    public void status() {
        if (!new File(".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        String branchname = Utils.readContentsAsString(
                Utils.join(".gitlet", "currbranch.txt"));
        System.out.println("=== Branches ===");
        List<String> filebranches = Utils.plainFilenamesIn(".gitlet/branches");
        for (String branch : filebranches) {
            branch = branch.substring(0, branch.length() - 4);
            if (branch.equals(branchname)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> filenamestoadd =
                Utils.plainFilenamesIn(".gitlet/stagingarea/add");
        for (String file : filenamestoadd) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> removedfiles =
                Utils.plainFilenamesIn(".gitlet/stagingarea/remove");
        for (String file : removedfiles) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public String getheadCommit() {
        String headCommit = Utils.readContentsAsString(
                Utils.join(".gitlet/", "head.txt"));
        return headCommit;
    }

    public void reset(String commitid) {
        List<String> commitlist = Utils.plainFilenamesIn(".gitlet/commit");
        Commit commit;
        if (commitid.length() < FOURTY) {
            commit = shortCommitId(commitid);
        } else {
            if (!commitlist.contains(commitid)) {
                System.out.println("No commit with that id exists.");
                return;
            }
            File path = Utils.join(".gitlet/commit", commitid);
            commit = Utils.readObject(path, Commit.class);
        }
        String headCommit = getheadCommit();
        Commit head = Utils.readObject(
                Utils.join(".gitlet/commit", headCommit), Commit.class);
        List<String> currentWorking = Utils.plainFilenamesIn(".");
        for (String file : currentWorking) {
            if (!head.exists(file) && !StagingArea.inAdd(file)
                    || StagingArea.inRemove(file)) {
                String content = Utils.readContentsAsString(new File(file));
                if (commit.exists(file)) {
                    if (!commit.getBlob().
                            get(file).equals(Utils.sha1(content))) {
                        System.out.println("There is an untracked file"
                                + " in the way; delete it, or "
                                + "add and commit it first.");
                        return;
                    }
                }
            }
        }
        for (Map.Entry<String, String> entry : commit.getBlob().entrySet()) {
            checkout(commit.getHash(), entry.getKey());
        }
        for (Map.Entry<String, String> entry : head.getBlob().entrySet()) {
            if (!commit.getBlob().containsKey(entry.getKey())) {
                File currentFile = new File(entry.getKey());
                currentFile.delete();
            }
        }
        List<String> filesinadd =
                Utils.plainFilenamesIn(".gitlet/stagingarea/add");
        for (String file : filesinadd) {
            Utils.join(".gitlet/stagingarea/add", file).delete();
        }
        List<String> filesinremove =
                Utils.plainFilenamesIn(".gitlet/stagingarea/remove");
        for (String file : filesinremove) {
            Utils.join(".gitlet/stagingarea/remove", file).delete();
        }
        Utils.writeContents(
                Utils.join(".gitlet", "head.txt"), commit.getHash());
        String branchname = Utils.readContentsAsString(
                Utils.join(".gitlet/", "currbranch.txt"));
        Branch currentbranch = Utils.readObject(Utils.join(".gitlet/branches",
                branchname + ".txt"), Branch.class);
        currentbranch.updateCommit(commit.getCommithash());
        Utils.writeObject(Utils.join(".gitlet", "repo.txt"), this);
    }

    public Commit shortCommitId(String commitid) {
        List<String> commits = Utils.plainFilenamesIn(".gitlet/commit");
        for (String commit : commits) {
            if (commit.startsWith(commitid)) {
                Commit answer = Utils.readObject(
                        Utils.join(".gitlet/commit", commit),
                        Commit.class);
                return answer;
            }
        }
        System.out.println("No commit with that id exists.");
        System.exit(0);
        return null;
    }

    public void merge(String givenbranch) {
        helper1(givenbranch);
    }

    public File getbranchwgiven(String givenbranch) {
        File branchwgiven = new File(
                ".gitlet/branches/" + givenbranch + ".txt");
        return branchwgiven;
    }

    public File getcurrentbr() {
        File currentbr = new File(".gitlet/currbranch.txt");
        return currentbr;
    }

    public void checkerhelper(String currentbranchname, String givenbranch) {
        if (currentbranchname.equals(givenbranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    public void helper1(String givenbranch) {
        File branchwgiven = getbranchwgiven(givenbranch);
        if (!branchwgiven.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        File currentbr = getcurrentbr();
        String currentbranchname = Utils.readContentsAsString(currentbr);
        checkerhelper(currentbranchname, givenbranch);
        List<String> inadd = Utils.plainFilenamesIn(".gitlet/stagingarea/add");
        List<String> inremove =
                Utils.plainFilenamesIn(".gitlet/stagingarea/remove");
        if (inadd.size() != 0 || inremove.size() != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        String currentbranchasstring = Utils.readContentsAsString(
                Utils.join(".gitlet", "currbranch.txt"));
        Branch currentbranch = Utils.readObject(
                Utils.join(".gitlet/branches",
                        currentbranchasstring + ".txt"), Branch.class);
        Branch branch2 = Utils.readObject(
                Utils.join(".gitlet/branches",
                        givenbranch + ".txt"), Branch.class);
        Commit splitpoint = findSplitPoint(currentbranch, branch2);
        List<String> currentWorking = Utils.plainFilenamesIn(".");
        String headCommit = Utils.readContentsAsString(
                Utils.join(".gitlet/", "head.txt"));
        Commit head = Utils.readObject(
                Utils.join(".gitlet/commit", headCommit), Commit.class);
        Commit branchCum = Utils.readObject(
                Utils.join(".gitlet/commit",
                        branch2.getMostrecentcommit()), Commit.class);
        for (String file : currentWorking) {
            if (!head.exists(file) && !StagingArea.inAdd(file)
                    || StagingArea.inRemove(file)) {
                String content = Utils.readContentsAsString(new File(file));
                if (branchCum.exists(file)) {
                    if (!branchCum.getBlob().get(file)
                            .equals(Utils.sha1(content))) {
                        System.out.println(messageone);
                        System.exit(0);
                    }
                }
            }
        }
        if (splitpoint.getHash().equals(branch2.getMostrecentcommit())) {
            System.out.println("Given branch "
                    + "is an ancestor of the current branch.");
            System.exit(0);
        }
        if (currentbranch.getMostrecentcommit().equals(splitpoint.getHash())) {
            checkoutBranch(givenbranch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        helper2(givenbranch, headCommit, head, branch2,
                currentbranchasstring, currentbranch, splitpoint, branchCum);
    }
    public void helper2(String givenbranch, String headCommit, Commit head,
                        Branch branch2, String currentbranchasstring,
                        Branch currentbranch, Commit splitpoint,
                        Commit branchCum) {
        File path = new File(
                ".gitlet/branches/" + givenbranch + ".txt");
        boolean foundConflict = false;
        TreeMap<String, String> copy = new TreeMap<>(branchCum.getBlob());
        Set<String> mergedFiles = copy.keySet();
        for (Map.Entry<String, String> entry
                : splitpoint.getBlob().entrySet()) {
            mergedFiles.remove(entry.getKey());
            if (branchCum.getBlob().containsKey(entry.getKey())
                    && head.getBlob().containsKey(entry.getKey())) {
                if (!branchCum.getBlob().get(entry.getKey())
                        .equals(entry.getValue())
                        && head.getBlob().get(entry.getKey())
                        .equals(entry.getValue())) {
                    checkout(branchCum.getHash(), entry.getKey());
                    add(entry.getKey());
                } else if (entry.getValue().equals
                        (branchCum.getBlob().get(entry.getKey()))
                        && !head.getBlob().get(entry.getKey()).
                        equals(entry.getValue())) {
                    continue;
                } else if (branchCum.getBlob().get(entry.getKey()).equals
                        (head.getBlob().get(entry.getKey()))) {
                    continue;
                } else if (!head.getBlob().get(entry.getKey())
                        .equals(branchCum.getBlob().get(entry.getKey()))) {
                    ultimatehelper(head, entry, branchCum);
                    foundConflict = true;
                }
            } else if (!branchCum.getBlob().containsKey(entry.getKey())
                    && !head.getBlob().containsKey(entry.getKey())) {
                continue;
            } else if (!branchCum.getBlob().containsKey(entry.getKey())) {
                if (entry.getValue().
                        equals(head.getBlob().get(entry.getKey()))) {
                    rm(entry.getKey());
                } else {
                    notfinalchecker(head, entry);
                    foundConflict = true;
                }
            } else if (!head.getBlob().
                    containsKey(entry.getKey())) {
                if (entry.getValue().equals(branchCum.
                        getBlob().get(entry.getKey()))) {
                    continue;
                } else {
                    finalchecker(branchCum, entry);
                    foundConflict = true;
                }
            }
        }
        helper3(mergedFiles, head, branchCum,
                foundConflict, givenbranch, currentbranch);
    }

    public void ultimatehelper(Commit head, Map.Entry<String,
            String> entry, Commit branchCum) {
        String content = "";
        content += "<<<<<<< HEAD\n";
        String blobHash = head.getBlob().get(entry.getKey());
        Blob blobber = Utils.readObject
                (Utils.join(".gitlet/blobs", blobHash), Blob.class);
        content += blobber.getContent();
        content += "=======\n";
        blobHash = branchCum.getBlob().get(entry.getKey());
        blobber = Utils.readObject(Utils.join(".gitlet/blobs",
                blobHash), Blob.class);
        content += blobber.getContent();
        content += ">>>>>>>\n";
        Utils.writeContents(new File(entry.getKey()), content);
        add(entry.getKey());
    }

    public void finalchecker(Commit branchCum,
                             Map.Entry<String, String> entry) {
        String content = "";
        content += "<<<<<<< HEAD\n";
        content += "=======\n";
        String blobHash = branchCum.getBlob().get(entry.getKey());
        Blob blobber = Utils.readObject(
                Utils.join(".gitlet/blobs", blobHash), Blob.class);
        content += blobber.getContent();
        content += ">>>>>>>\n";
        Utils.writeContents(new File(entry.getKey()), content);
        add(entry.getKey());
    }

    public void notfinalchecker(Commit head, Map.Entry<String, String> entry) {
        String content = "";
        content += "<<<<<<< HEAD\n";
        String blobHash = head.getBlob().get(entry.getKey());
        Blob blobber = Utils.readObject
                (Utils.join(".gitlet/blobs", blobHash), Blob.class);
        content += blobber.getContent();
        content += "=======\n";
        content += ">>>>>>>\n";
        Utils.writeContents(new File(entry.getKey()), content);
        add(entry.getKey());
    }


    public void helper3(Set<String> mergedFiles, Commit head,
                        Commit branchCum, boolean foundConflict,
                        String givenbranch, Branch currentbranch) {
        for (String mergeFile : mergedFiles) {
            if (!head.getBlob().containsKey(mergeFile)) {
                checkout(branchCum.getHash(), mergeFile);
                add(mergeFile);
            } else {
                if (!head.getBlob().get(mergeFile).equals
                        (branchCum.getBlob().get(mergeFile))) {
                    String content = "";
                    content += "<<<<<<< HEAD\n";
                    String blobHash = head.getBlob().get(mergeFile);
                    Blob blobber = Utils.readObject(
                            Utils.join(".gitlet/blobs", blobHash), Blob.class);
                    content += blobber.getContent();
                    content += "=======\n";
                    blobHash = branchCum.getBlob().get(mergeFile);
                    blobber = Utils.readObject(
                            Utils.join(".gitlet/blobs", blobHash), Blob.class);
                    content += blobber.getContent();
                    content += ">>>>>>>\n";
                    Utils.writeContents(new File(mergeFile), content);
                    add(mergeFile);
                    foundConflict = true;
                }
            }
        }
        commit("Merged " + givenbranch + " into "
                + currentbranch.getBranchname() + ".", branchCum.getHash());
        if (foundConflict) {
            System.out.println("Encountered a merge conflict.");
        }

    }


    public Commit findSplitPoint(Branch branch1, Branch branch2) {
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(branch2.getMostrecentcommit());
        HashMap<String, String> branch2commits = new HashMap<>();
        while (!queue.isEmpty()) {
            String commitId = queue.poll();
            if (!branch2commits.containsKey(commitId)) {
                branch2commits.put(commitId, null);
                Commit commit = Utils.readObject(
                        Utils.join(".gitlet/commit", commitId), Commit.class);
                if (commit.getParenthashthash()
                        != null) {
                    if (!branch2commits
                            .containsKey(commit.getParenthashthash())) {
                        queue.add(commit.getParenthashthash());
                    }
                }
                if (commit.getParent2hash() != null) {
                    if (!branch2commits.containsKey(commit.getParent2hash())) {
                        queue.add(commit.getParent2hash());
                    }
                }
            }
        }
        ArrayDeque<String> queue2 = new ArrayDeque<>();
        queue2.add(branch1.getMostrecentcommit());
        HashMap<String, String> markCommits = new HashMap<>();
        while (!queue2.isEmpty()) {
            String commitId = queue2.poll();
            if (!markCommits.containsKey(commitId)) {
                markCommits.put(commitId, null);
                Commit commit = Utils.readObject(
                        Utils.join(".gitlet/commit", commitId), Commit.class);
                if (branch2commits.containsKey(commitId)) {
                    return commit;
                }
                if (commit.getParenthashthash() != null) {
                    if (!markCommits.containsKey(commit.getParenthashthash())) {
                        queue2.add(commit.getParenthashthash());
                    }
                }
                if (commit.getParent2hash() != null) {
                    if (!markCommits.containsKey(commit.getParent2hash())) {
                        queue2.add(commit.getParent2hash());
                    }
                }
            }
        }
        return null;
    }


}
