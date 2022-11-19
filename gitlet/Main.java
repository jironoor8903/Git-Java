package gitlet;

import java.io.File;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Jiro Noor
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    private static boolean validCommand = false;
    public static void main(String... args) {
        Repository gitlet = retrieveRepo();
        if (args.length == 0) {
            System.out.println("Please enter a command");
            System.exit(0);
        }
        String function = args[0];
        if (function.equals("init")) {
            gitlet.init();
            validCommand = true;
        }
        if (function.equals("add")) {
            gitlet.add(args[1]);
            validCommand = true;
        }
        if (function.equals("commit")) {
            gitlet.commit(args[1], null);
            validCommand = true;
        }
        if (function.equals("log")) {
            validCommand = true;
            gitlet.log();
        }
        if (function.equals("checkout")) {
            validCommand = true;
            if (args.length == 3) {
                gitlet.checkout(args[2]);
            }
            if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands");
                    return;
                }
                gitlet.checkout(args[1], args[3]);
            }
            if (args.length == 2) {
                gitlet.checkoutBranch(args[1]);
            }
        }
        if (function.equals("rm")) {
            validCommand = true;
            gitlet.rm(args[1]);
        }
        helper(gitlet, args);

        if (!validCommand) {
            System.out.println("No command with that name exists.");
        }

    }
    public static void helper(Repository gitlet, String... args) {
        String function = args[0];
        if (function.equals("global-log")) {
            validCommand = true;
            gitlet.globalLog();
        }
        if (function.equals("find")) {
            validCommand = true;
            gitlet.find(args[1]);
        }
        if (function.equals("branch")) {
            validCommand = true;
            gitlet.branch(args[1]);
        }
        if (function.equals("status")) {
            validCommand = true;
            gitlet.status();
        }
        if (function.equals("rm-branch")) {
            validCommand = true;
            gitlet.removeBranch(args[1]);
        }
        if (function.equals("reset")) {
            gitlet.reset(args[1]);
            validCommand = true;
        }
        if (function.equals("merge")) {
            gitlet.merge(args[1]);
            validCommand = true;
        }
    }


    public static Repository retrieveRepo() {
        File repo = Utils.join(".gitlet", "repo.txt");
        Repository gitlet = new Repository();
        if (repo.exists()) {
            gitlet = Utils.readObject(repo, Repository.class);
        }
        return gitlet;
    }


}
