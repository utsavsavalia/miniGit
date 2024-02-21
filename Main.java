package gitlet;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Utsav Savalia
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {

        Repo callingObj = new Repo();
        if (args.length == 0) {
            System.out.println("Please enter a command");
            return;
        }
        String command = args[0];
        if (command.equals("init")) {
            callingObj.init();
        } else if (command.equals("add")) {
            callingObj.add(args[1]);
        } else if (command.equals("commit")) {
            callingObj.commit(args[1]);
        } else if (command.equals("rm")) {
            callingObj.rm(args[1]);
        } else if (command.equals("reset")) {
            callingObj.reset(args[1]);
        } else if (command.equals("log")) {
            callingObj.log();
        } else if (command.equals("global-log")) {
            callingObj.globalLog();
        } else if (command.equals("find")) {
            callingObj.find(args[1]);
        } else if (command.equals("branch")) {
            callingObj.branch(args[1]);
        } else if (command.equals("merge")) {
            callingObj.merge(args[1]);
        } else if (command.equals("rm-branch")) {
            callingObj.rmbranch(args[1]);
        } else if (command.equals("status")) {
            callingObj.status();
        } else if (command.equals("add-remote")) {
            callingObj.addRemote(args);
        } else if (command.equals("rm-remote")) {
            callingObj.rmRemote(args[1]);
        } else if (command.equals("push")) {
            callingObj.push(args);
        } else if (command.equals("fetch")) {
            callingObj.fetch(args);
        } else if (command.equals("checkout")) {
            if (args.length == 2) {
                callingObj.checkout2(args);
            } else if (args.length == 3) {
                callingObj.checkout3(args);
            } else if (args.length == 4) {
                callingObj.checkout4(args);
            }
        } else {
            System.out.println("No command with that name exists.");
        }
        System.exit(0);
    }
}
