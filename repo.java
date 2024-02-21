package gitlet;

import jdk.jshell.execution.Util;
import org.checkerframework.checker.guieffect.qual.UI;
import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.*;

public class Repo {

    static private File myCWD = new File(".");
    private String Head = "master";
    private Staging myStage;
    String myCWDPath;
    String initialCommitSha;
    String locationOftempCommit;
    File tempPointerToFirstCommit;

    static File gitletFile = Utils.join(myCWD, ".gitlet");
    static File blobsFile = Utils.join(gitletFile, "blobs");
    static File stagingFile = Utils.join(gitletFile, "staging");
    static File stagingAddFile = Utils.join(gitletFile, "staging/staging-add");
    //static File stagingRemoveFile = Utils.join(gitletFile, "staging/staging-remove");
    static File branchesFile = Utils.join(gitletFile, "branches");
    static File commitsFile = Utils.join(gitletFile, "commits");
    static File logFile = Utils.join(gitletFile, "Global-Log");

    public Repo()
    {
        //myCWD = new File(System.getProperty("user.dir"));
        myCWDPath = System.getProperty("user.dir");
        File tempBranchFile = Utils.join(branchesFile, "Head.txt");
        File tempStagingFile = Utils.join(stagingAddFile, "stagingAdd.txt");
        if(tempBranchFile.exists())
        {
            Head = Utils.readContentsAsString(tempBranchFile);
        }
        if(tempStagingFile.exists())
        {
            myStage = Utils.readObject(tempStagingFile, Staging.class);
        }
    }


    public void init() {
        File checkGit = new File(".gitlet");
        if (gitletFile.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        }
        else
        {
            gitletFile.mkdir();
            blobsFile.mkdir();
            branchesFile.mkdir();
            stagingFile.mkdir();
            stagingAddFile.mkdir();
            //stagingRemoveFile.mkdir();
            commitsFile.mkdir();
            logFile.mkdir();

            myStage = new Staging();
            //myStageRemove = new Staging();

            Commit initialCommit = new Commit("initial commit", null, new HashMap<>());
            Utils.writeObject(Utils.join(commitsFile, Utils.sha1(Utils.serialize(initialCommit))), initialCommit);
            Utils.writeContents(Utils.join(branchesFile, "master.txt"), Utils.sha1(Utils.serialize(initialCommit)));
            Utils.writeContents(Utils.join(branchesFile, "Head.txt"), "master");
            //Utils.writeContents(Utils.join(myCWD, "ActiveBranch.txt"), "master");
            Utils.writeObject(Utils.join(stagingAddFile, "stagingAdd.txt"), myStage);
        }
    }

    public void add(String args) {
        File tempFile = new File(args);
        if(!tempFile.exists())
        {
            System.out.println("File does not exist.");
        }
        else
        {
            String tempReadFile = Utils.readContentsAsString(tempFile);
            String tempBlobSHA = Utils.sha1(Utils.serialize(tempReadFile));
            //myStage.add(args, tempBlobSHA);
            Commit temp = mostRecentCommit();
            if(myStage.removeStaging.get(args) != null)
            {
                myStage.removeStaging.remove(args);
                File tempStagingPointer = Utils.join(stagingAddFile, "stagingAdd.txt");
                Utils.writeObject(tempStagingPointer, myStage);
                return;
            }
            if(temp.myBlobContents != null && temp.myBlobContents.get(args) != null && temp.myBlobContents.get(args).equals(tempBlobSHA))
            {
                myStage.addStaging.remove(args, tempBlobSHA);
                File tempStagingPointer = Utils.join(stagingAddFile, "stagingAdd.txt");
                Utils.writeObject(tempStagingPointer, myStage);
                return;
            }

            File tempBlobPointer = Utils.join(blobsFile, tempBlobSHA + ".txt");
            Utils.writeContents(tempBlobPointer, tempReadFile);
            myStage.add(args, tempBlobSHA);
            File tempStagingPointer = Utils.join(stagingAddFile, "stagingAdd.txt");
            Utils.writeObject(tempStagingPointer, myStage);
        }
    }

    public Commit mostRecentCommit()
    {
        File temp = Utils.join(branchesFile, Utils.readContentsAsString(Utils.join(branchesFile, "Head.txt")) + ".txt");
        String theCommitSHA1 = Utils.readContentsAsString(temp);
        File commitPointer = Utils.join(commitsFile, theCommitSHA1);
        Commit retObj = Utils.readObject(commitPointer, Commit.class);
        return retObj;
    }

    public void log() {
        Commit tempCommitObj = mostRecentCommit();
        while(tempCommitObj != null)
        {
            System.out.println(tempCommitObj.getLog());
            if(tempCommitObj.myParentHash == null)
            {
                break;
            }
            tempCommitObj = Utils.readObject(Utils.join(commitsFile, tempCommitObj.myParentHash), Commit.class);
        }
    }

    public void globalLog()
    {
        List<String> listOfCommits = Utils.plainFilenamesIn(commitsFile);
        for(String commit : listOfCommits)
        {
            File tempCommitPointer = Utils.join(commitsFile, commit);
            Commit theDeSerializedCommit = Utils.readObject(tempCommitPointer, Commit.class);
            System.out.println(theDeSerializedCommit.getLog());
        }
    }

    public void find(String msg)
    {
        List<String> listOfCommits = Utils.plainFilenamesIn(commitsFile);
        boolean notFound = true;
        for(String commit : listOfCommits)
        {
            File tempCommitPointer = Utils.join(commitsFile, commit);
            Commit theDeSerializedCommit = Utils.readObject(tempCommitPointer, Commit.class);
            if(theDeSerializedCommit.myMessage.equals(msg))
            {
                System.out.println(commit);
                notFound = false;
            }
        }
        if(notFound) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status()
    {
        if(!gitletFile.exists())
        {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        System.out.println("=== Branches ===");
        List<String> listOfBranches = Utils.plainFilenamesIn(branchesFile);
        if(listOfBranches != null && listOfBranches.size() != 0) {
            Collections.sort(listOfBranches);
            for (String branch : listOfBranches) {
                String choppedBranch = branch.substring(0, branch.length() - 4);
                if (choppedBranch.equals("Head")) {

                } else if (choppedBranch.equals(Head)) {
                    System.out.println("*" + choppedBranch);
                } else {
                    System.out.println(choppedBranch);
                }
            }
        }

        System.out.println("\n=== Staged Files ===");
        if(myStage != null && myStage.addStaging != null) {
            List<String> listOfStagedFiles = new ArrayList<String>(myStage.addStaging.keySet());
            if (listOfStagedFiles != null && listOfStagedFiles.size() != 0) {
                Collections.sort(listOfStagedFiles);

                for (String stagedFile : listOfStagedFiles) {
                    System.out.println(stagedFile);
                }
            }
        }


        System.out.println("\n=== Removed Files ===");
        if(myStage != null && myStage.removeStaging != null) {
            List<String> listOfRemovedFiles = new ArrayList<String>(myStage.removeStaging.keySet());
            if (listOfRemovedFiles != null && listOfRemovedFiles.size() != 0) {
                Collections.sort(listOfRemovedFiles);
                for (String removedFile : listOfRemovedFiles) {
                    System.out.println(removedFile);
                }
            }
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===\n");
    }

    public void reset(String args)
    {
        File tempCommitPointer = Utils.join(commitsFile, args);
        if(!tempCommitPointer.exists())
        {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit theNewCommit = Utils.readObject(tempCommitPointer, Commit.class);
        Commit mostRecentCommit = mostRecentCommit();
        List<String> allFiles = Utils.plainFilenamesIn(myCWD);
        for(String f : allFiles)
        {
            if(f.contains(".txt") && mostRecentCommit.myBlobContents != null && mostRecentCommit.myBlobContents.get(f) == null && myStage.addStaging.get(f) == null && myStage.removeStaging.get(f) == null)// && theNewCommit.myBlobContents != null && theNewCommit.myBlobContents.get(f) != null)
            {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        for(String f : allFiles)
        {
            if(mostRecentCommit.myBlobContents != null && (mostRecentCommit.myBlobContents.get(f) != null || myStage.addStaging.get(f) != null || myStage.removeStaging.get(f) != null) && theNewCommit.myBlobContents != null && theNewCommit.myBlobContents.get(f) == null)
            {
                Utils.restrictedDelete(f);
            }
        }
        // remove tracked files that are not present in that commit.
        myStage.clear();
        File tempStagePointer = Utils.join(stagingAddFile, "stagingAdd.txt");
        Utils.writeObject(tempStagePointer, myStage);
        File tempHeadPointer = Utils.join(branchesFile, Head + ".txt");
        Utils.writeContents(tempHeadPointer, args);
    }

    public void branch(String args)
    {
        File tempNewBranch = Utils.join(branchesFile, args + ".txt");
        if(!tempNewBranch.exists())
        {
            File tempFile = Utils.join(branchesFile, Head + ".txt");
            String temp = Utils.readContentsAsString(tempFile);
            File tempBranchPointer = Utils.join(branchesFile, args + ".txt");
            Utils.writeContents(tempBranchPointer, temp);
        }
        else
        {
            System.out.println("A branch with that name already exists.");
        }
    }

    public void rmbranch(String args)
    {
        File tempBranchPointer = Utils.join(branchesFile, args + ".txt");
        if(!tempBranchPointer.exists())
        {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        File tempHeadPointer = Utils.join(branchesFile, "Head.txt");
        if(Utils.readContentsAsString(tempHeadPointer).equals(args))
        {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        tempBranchPointer.delete();
    }

    public void commit(String args)
    {
        if(args.length() == 0)
        {
            System.out.println("Please enter a commit message.");
        }
        if(myStage.addStaging.isEmpty() && myStage.removeStaging.isEmpty())
        {
            System.out.println("No changes added to the commit.");
        }
        Commit recentCommit = mostRecentCommit();
        HashMap<String, String> tempMap = new HashMap<String, String>();
        if(recentCommit.myBlobContents != null)
        {
            for(String key: recentCommit.myBlobContents.keySet())
            {
                tempMap.put(key, recentCommit.myBlobContents.get(key));
            }
        }
        ArrayList<String> listOfAddItems = new ArrayList<>(myStage.addStaging.keySet());
        ArrayList<String> listOfRemoveItems = new ArrayList<>(myStage.removeStaging.keySet());
        for (int x = 0; x < listOfAddItems.size(); x++) {
            String key = listOfAddItems.get(x);
            if(listOfRemoveItems.contains(key))
            {
                listOfRemoveItems.remove(key);
            }
            else {
                tempMap.put(key, myStage.addStaging.get(key));
            }
        }
        if(!listOfRemoveItems.isEmpty()) {
            for (int y = 0; y < listOfRemoveItems.size(); y++)
            {
                tempMap.remove(listOfRemoveItems.get(y));
            }
        }

        Commit tempCommit = new Commit(args, Utils.sha1(Utils.serialize(recentCommit)), tempMap);
        File tempBranchPointer = Utils.join(branchesFile, Head + ".txt");
        File tempCommitPointer = Utils.join(commitsFile, Utils.sha1(Utils.serialize(tempCommit)));
        myStage.clear();
        Utils.writeContents(tempBranchPointer, Utils.sha1(Utils.serialize(tempCommit)));
        Utils.writeObject(tempCommitPointer, tempCommit);
        File tempStagePointer = Utils.join(stagingAddFile, "stagingAdd.txt");
        Utils.writeObject(tempStagePointer, myStage);
    }

    public void rm(String args)
    {
        Commit recentCommit = mostRecentCommit();
        if(myStage.addStaging.get(args) == null && (recentCommit.myBlobContents == null || recentCommit.myBlobContents.get(args) == null))
        {
            System.out.println("No reason to remove the file.");
            return;
        }
        if(myStage.addStaging.get(args) != null)
        {
            myStage.addStaging.remove(args);
        }
        if(recentCommit.myBlobContents != null && recentCommit.myBlobContents.get(args) != null)
        {
            Utils.restrictedDelete(args);
            myStage.remove(args, recentCommit.myBlobContents.get(args));
        }
        File tempStagePointer = Utils.join(stagingAddFile, "stagingAdd.txt");
        Utils.writeObject(tempStagePointer, myStage);
    }

    public void checkout2(String... args)
    {
        String theBranch = args[1];
        if(theBranch.equals(Head))
        {
            System.out.println("No need to checkout the current branch.");
        return;
        }
        File tempBranchFile = Utils.join(branchesFile, theBranch + ".txt");
        if(!tempBranchFile.exists())
        {
            System.out.println("No such branch exists.");
            return;
        }

        //File headFile = Utils.join(branchesFile, Head + ".txt");
        String sha1OfBranch = Utils.readContentsAsString(tempBranchFile);
        File theCommitFilePointer = Utils.join(commitsFile, sha1OfBranch);
        Commit theCommitInsideBranch = Utils.readObject(theCommitFilePointer, Commit.class);

        HashMap<String, String> upcomingContents = theCommitInsideBranch.myBlobContents;
        HashMap<String, String> recentCommitContents = mostRecentCommit().myBlobContents;

        List<String> allFiles = Utils.plainFilenamesIn(myCWD);
        Commit recentCommit = mostRecentCommit();
        for(String tempFile : allFiles)
        {
            if(tempFile.contains(".txt") && recentCommit.myBlobContents == null || recentCommit.myBlobContents.get(tempFile) == null && myStage.addStaging.get(tempFile) == null && myStage.removeStaging.get(tempFile) == null)
            {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }

        Commit commitToCheckout = theCommitInsideBranch;

            for (String f : allFiles) {
                if ((commitToCheckout.myBlobContents == null || !commitToCheckout.myBlobContents.containsKey(f)) && mostRecentCommit().myBlobContents.containsKey(f)) {
                    Utils.restrictedDelete(f);
                }
            }

            /* Overwrite the files with content. */
        if(commitToCheckout.myBlobContents != null) {
            List<String> fileNames = new ArrayList<>(commitToCheckout.myBlobContents.keySet());
            for (String f : fileNames) {
                String sha1OfContents = commitToCheckout.myBlobContents.get(f);
                File tempFile = Utils.join(blobsFile, sha1OfContents + ".txt");
                Utils.writeContents(new File(f), Utils.readContents(tempFile));
            }
        }

        myStage.clear();
        File stageFilePointer = Utils.join(stagingAddFile, "stagingAdd.txt");
        Utils.writeContents(stageFilePointer, Utils.serialize(myStage));
        File branchFilePointer = Utils.join(branchesFile, "Head.txt");
        Utils.writeContents(branchFilePointer, theBranch);
    }

    public void checkout3(String... args)
    {
        if(args[1].contains("++"))
        {
            System.out.println("Incorrect Operands.");
            return;
        }
        String file = args[2];
        File tempHeadFile = Utils.join(branchesFile, Head + ".txt");
        String theCommitID = Utils.readContentsAsString(tempHeadFile);
        File tempCommitPointer = Utils.join(commitsFile, theCommitID);
        Commit theCmtInHead = Utils.readObject(tempCommitPointer, Commit.class);

        //if(theCmtInHead.myBlobContents == null || theCmtInHead.myBlobContents.get(file) == null)
        if(theCmtInHead.myBlobContents == null || theCmtInHead.myBlobContents.get(file) == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        File tempNewFilePointer = Utils.join(myCWD, file);
        File tempContentsPointer = Utils.join(blobsFile, theCmtInHead.myBlobContents.get(file) + ".txt");

        if (tempNewFilePointer.exists()) {
            Utils.restrictedDelete(tempNewFilePointer);
        }
        Utils.writeContents(tempNewFilePointer, Utils.readContents(tempContentsPointer));
    }

    public void checkout4(String... args)
    {
        if(args[2].contains("++"))
        {
            System.out.println("Incorrect Operands.");
            return;
        }
        String commitID = args[1];
        String file = args[3];
        File tempCommitPointer = Utils.join(commitsFile, commitID);
        if(!tempCommitPointer.exists())
        {
            boolean found = false;
            List<String> allCommitFiles = Utils.plainFilenamesIn(commitsFile);
            for(int x = 0; x < allCommitFiles.size(); x++)
            {
                if(allCommitFiles.get(x).contains(commitID))
                {
                    commitID = allCommitFiles.get(x);
                    tempCommitPointer = Utils.join(commitsFile, commitID);
                    found = true;
                }
            }
            if(!found) {
                System.out.println("No commit with that id exists.");
                return;
            }
        }
        Commit cmtInProvidedCmtID = Utils.readObject(tempCommitPointer, Commit.class);
        if(cmtInProvidedCmtID.myBlobContents == null || cmtInProvidedCmtID.myBlobContents.get(file) == null)
        {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File tempNewFilePointer = Utils.join(myCWD, file);
        File tempContentsPointer = Utils.join(blobsFile, cmtInProvidedCmtID.myBlobContents.get(file) + ".txt");

        if (tempNewFilePointer.exists()) {
            Utils.restrictedDelete(tempNewFilePointer);
        }
        Utils.writeContents(tempNewFilePointer, Utils.readContents(tempContentsPointer));
    }

    public void merge(String branch)
    {
        Commit recentCommit = mostRecentCommit();
        List<String> allFiles = Utils.plainFilenamesIn(myCWD);
        for(String tempFile : allFiles)
        {
            if(tempFile.contains(".txt") && recentCommit.myBlobContents == null || recentCommit.myBlobContents.get(tempFile) == null && myStage.addStaging.get(tempFile) == null && myStage.removeStaging.get(tempFile) == null)
            {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        if(!myStage.addStaging.isEmpty() || !myStage.removeStaging.isEmpty())
        {
            System.out.println("You have uncommitted changes.");
            return;
        }
        File tempOtherBranchPointer = Utils.join(branchesFile, branch + ".txt");
        if(!tempOtherBranchPointer.exists())
        {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        File tempMasterPointer = Utils.join(branchesFile, "Head.txt");
        if((Utils.readContentsAsString(tempMasterPointer)).equals(branch))
        {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }


        String commitID = Utils.readContentsAsString(tempOtherBranchPointer);
        File tempCommitPointer = Utils.join(commitsFile, commitID);
        Commit tempCommit = Utils.readObject(tempCommitPointer, Commit.class);
        Commit tempCommit2Merged = null;
        HashSet<String> commitsSet = new HashSet<>();

        Queue<String> parentNotProcessed;

        while(!tempCommit.myMessage.equals("initial commit") || tempCommit.myParentHash != null)
        {
            commitsSet.add(Utils.sha1(Utils.serialize(tempCommit)));
            if(tempCommit2Merged != null)
            {
                commitsSet.add(Utils.sha1(Utils.serialize(tempCommit2Merged)));
            }
            File tempParentCommitPointer = Utils.join(commitsFile, tempCommit.myParentHash);
            tempCommit = Utils.readObject(tempParentCommitPointer, Commit.class);
            if(tempCommit.myParent2Hash != null)
            {
                tempParentCommitPointer = Utils.join(commitsFile, tempCommit.myParent2Hash);
                tempCommit2Merged = Utils.readObject(tempParentCommitPointer, Commit.class);
            }
            else
            {
                tempCommit2Merged = null;
            }
        }


        boolean cont = true;
        boolean doMergeCommit = false;
        boolean conflictOccur = false;


        Commit currHeadCommit = mostRecentCommit();
        String extraPointer = null;
        Commit splitPoint = null;
        if(commitsSet.contains(Utils.sha1(Utils.serialize(currHeadCommit))))
        {
            cont = false;
            splitPoint = currHeadCommit;
            doMergeCommit = true;
        }
        while(cont)
        {
            if(commitsSet.contains(currHeadCommit.myParentHash))
            {
                splitPoint = Utils.readObject(Utils.join(commitsFile, currHeadCommit.myParentHash), Commit.class);
                cont = false;
            }
            if(currHeadCommit.myParent2Hash != null && commitsSet.contains(currHeadCommit.myParent2Hash))
            {
                splitPoint = Utils.readObject(Utils.join(commitsFile, currHeadCommit.myParent2Hash), Commit.class);
                cont = false;
            }
            if(splitPoint == null && currHeadCommit.myParentHash != null)
            {
                currHeadCommit = Utils.readObject(Utils.join(commitsFile, currHeadCommit.myParentHash), Commit.class);
            }
            else
            {
                cont = false;
            }
        }
        if(splitPoint == null && currHeadCommit.myMessage.equals("initial commit") && tempCommit.myMessage.equals("initial commit"))
        {
            splitPoint = currHeadCommit;
        }
        if(Utils.sha1(Utils.serialize(splitPoint)).equals(Utils.readContentsAsString(Utils.join(branchesFile, branch + ".txt"))))
        {
            System.out.println("Given branch is an ancestor of the current branch.");
        }
        if((Utils.sha1(Utils.serialize(splitPoint))).equals(Utils.sha1(Utils.serialize(mostRecentCommit()))))
        {
            String[] args = new String[]{"checkout", branch};
            checkout2(args);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        //Condition 1
        Commit masterHead = recentCommit;
        Commit branchHead = Utils.readObject(tempCommitPointer, Commit.class);

        if(!doMergeCommit) {
            ArrayList<String> allFilesInMasterHead = new ArrayList<>();
            ArrayList<String> allFilesInBranchHead = new ArrayList<>();
            ArrayList<String> allFilesInSplit = new ArrayList<>();
            if(masterHead.myBlobContents != null)
                allFilesInMasterHead = new ArrayList<>(masterHead.myBlobContents.keySet());
            if(branchHead.myBlobContents != null)
                allFilesInBranchHead = new ArrayList<>(branchHead.myBlobContents.keySet());
            if(splitPoint.myBlobContents != null)
                allFilesInSplit = new ArrayList<>(splitPoint.myBlobContents.keySet());
            HashSet<String> allCombinedFiles = new HashSet<>(masterHead.myBlobContents.keySet());
            allCombinedFiles.addAll(branchHead.myBlobContents.keySet());
            for (String file : allCombinedFiles) {
                Boolean inSplit = splitPoint.myBlobContents != null && splitPoint.myBlobContents.containsKey(file);
                Boolean inCurrentHead = masterHead.myBlobContents != null && masterHead.myBlobContents.containsKey(file);
                Boolean inGivenBranch = branchHead.myBlobContents != null && branchHead.myBlobContents.containsKey(file);
                if (inSplit && inGivenBranch && inCurrentHead && !branchHead.myBlobContents.get(file).equals(splitPoint.myBlobContents.get(file)) && masterHead.myBlobContents.get(file).equals(splitPoint.myBlobContents.get(file))) {
                    String[] tempArgs = new String[]{"Checkout", "" + Utils.sha1(Utils.serialize(branchHead)), " -- " , file};
                    checkout4(tempArgs);
                }
//                else if (inGivenBranch && inSplit && inCurrentHead && !masterHead.myBlobContents.get(file).equals(branchHead.myBlobContents.get(file)) && branchHead.myBlobContents.get(file).equals(splitPoint.myBlobContents.get(file))) {
//                }
//                else if (inCurrentHead && !inGivenBranch) {
//                }
//                else if (!inSplit && inCurrentHead) {
//                }
                else if (!inSplit && !inCurrentHead && inGivenBranch) {
                    String[] tempArgs = new String[]{"checkout ", Utils.sha1(Utils.serialize(branchHead)), " -- ", file};
                    checkout4(tempArgs);
                    myStage.addStaging.put(file, Utils.sha1(file));
                }
                else if (inSplit && !inGivenBranch && splitPoint.myBlobContents.get(file).equals(masterHead.myBlobContents.get(file))) {
                    rm(file);
                }
//                else if (!inCurrentHead && inSplit && inGivenBranch && splitPoint.myBlobContents.get(file).equals(branchHead.myBlobContents.get(file)) && masterHead.myBlobContents.get(file) == null) {
//                }
//                else if( !allFilesInBranchHead.contains(file) && allFilesInMasterHead.contains(file))
//                {
//
//                }
                //conflict condition
                else if (inGivenBranch && inCurrentHead && inSplit && !masterHead.myBlobContents.get(file).equals(branchHead.myBlobContents.get(file)) && !masterHead.myBlobContents.get(file).equals(splitPoint.myBlobContents.get(file)) && !branchHead.myBlobContents.get(file).equals(splitPoint.myBlobContents.get(file))){
                    String content = "<<<<<<< HEAD\n";
                    content += Utils.readContentsAsString(Utils.join(blobsFile, masterHead.myBlobContents.get(file) + ".txt"));
                    content += "=======\n";
                    content += Utils.readContentsAsString(Utils.join(blobsFile, branchHead.myBlobContents.get(file) + ".txt"));
                    content +=  ">>>>>>>" + "\n";

                    File tempFilePointer = Utils.join(myCWD, file);
                    Utils.writeContents(tempFilePointer, content);
                    conflictOccur = true;
                }
                else if(!inGivenBranch && inCurrentHead && inSplit && !masterHead.myBlobContents.get(file).equals(splitPoint.myBlobContents.get(file)))
                {
                    String content = "<<<<<<< HEAD\n";
                    File tempBlobPointer = Utils.join(blobsFile, masterHead.myBlobContents.get(file) + ".txt");
                    if(tempBlobPointer.exists()) {
                        content += Utils.readContentsAsString(tempBlobPointer);
                    }
                    content += "=======\n";
                    //content += Utils.readContentsAsString(Utils.join(blobsFile, splitPoint.myBlobContents.get(file) + ".txt"));
                    content +=  ">>>>>>>" + "\n";

                    File tempFilePointer = Utils.join(myCWD, file);
                    Utils.writeContents(tempFilePointer, content);
                    conflictOccur = true;
                }
                else if(inGivenBranch && !inCurrentHead && inSplit && !branchHead.myBlobContents.get(file).equals(splitPoint.myBlobContents.get(file)))
                {
                    String content = "<<<<<<< HEAD\n";
                    //content += Utils.readContentsAsString(Utils.join(blobsFile, masterHead.myBlobContents.get(file) + ".txt"));
                    content += "=======\n";
                    content += Utils.readContentsAsString(Utils.join(blobsFile, branchHead.myBlobContents.get(file) + ".txt"));
                    content +=  ">>>>>>>" + "\n";

                    File tempFilePointer = Utils.join(myCWD, file);
                    Utils.writeContents(tempFilePointer, content);
                    conflictOccur = true;
                }
                else if(!inSplit && inCurrentHead && inGivenBranch && !masterHead.myBlobContents.get(file).equals(branchHead.myBlobContents.get(file)))
                {
                    String content = "<<<<<<< HEAD\n";
                    content += Utils.readContentsAsString(Utils.join(blobsFile, masterHead.myBlobContents.get(file) + ".txt"));
                    content += "=======\n";
                    content += Utils.readContentsAsString(Utils.join(blobsFile, branchHead.myBlobContents.get(file) + ".txt"));
                    content +=  ">>>>>>>" + "\n";

                    File tempFilePointer = Utils.join(myCWD, file);
                    Utils.writeContents(tempFilePointer, content);
                    conflictOccur = true;
                }
            }
        }

        if(conflictOccur)
        {
            System.out.println("Encountered a merge conflict.");
        }

        HashMap<String, String> tempMergedCommitBlobs = new HashMap<>();
        for(String file : Utils.plainFilenamesIn(myCWD))
        {
            tempMergedCommitBlobs.put(file, Utils.sha1(Utils.serialize(Utils.readContentsAsString(Utils.join(myCWD, file)))));
        }
        Commit mergedCommit = new Commit("Merged " + branch + " into " + Utils.readContentsAsString(tempMasterPointer) + ".", Utils.sha1(Utils.serialize(masterHead)), Utils.sha1(Utils.serialize(branchHead)), tempMergedCommitBlobs);
        String sha1IdOfMergedCommit = Utils.sha1(Utils.serialize(mergedCommit));
        File tempMergedCommitPointer = Utils.join(commitsFile, sha1IdOfMergedCommit);
        Utils.writeObject(tempMergedCommitPointer, mergedCommit);


        File tempHeadPointer = Utils.join(branchesFile, "Head.txt");
        File tempCurrBranchPointer = Utils.join(branchesFile, Utils.readContentsAsString(tempHeadPointer) + ".txt");
        Utils.writeContents(tempCurrBranchPointer, sha1IdOfMergedCommit);

        myStage.clear();
        File stageFilePointer = Utils.join(stagingAddFile, "stagingAdd.txt");
        Utils.writeContents(stageFilePointer, Utils.serialize(myStage));
    }
}
