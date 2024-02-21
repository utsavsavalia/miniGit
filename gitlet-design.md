# Gitlet Design Document

**Name**: Utsav Savalia

## Classes and Data Structures
Main.java:
This class does the main method calling. All the commands such as init, add ... when passed as command arguments are received here, and processed to call the respective function.

Commit:
This class contains the tree, which includes the branches of the commit versions.

Log:
This class contains methods to record the Date, Time and commit message for the commits. If called with the primary commit then set it to UTC, else set it to current time.

Repo:
Class that contains all the methods. It serializes and deserializes the objects and all the commands from Main.java are processed here. It does the work of calling the class functions. 

Staging Area
Holds the files before committing them. A hash map is used and then the blobs are matched to see if the name of the string correspond to the same SHA.

## Algorithms
Log
Function inside the Main.java class, goes through all the commits, and gets their metadata. Can be stored inside a file each time a commit is added.

init
Starts the version control by first checking if a ".git" file exits, if it does then aborts the execution with a message. Else uses Utils.join to create ".git" file. Calls the commit class with message as "initial commit"

Add
Add to the staging area, go to the commit class and get the latest commit and compare its serialized blobs with the current file. If they match remove from the staging area, else leave it in the staging area.
The add function uses a Folder to temporarily hold the files.

Commit
Checks if there are files in the staging, if yes then checks if a branch name exits with that file name, if yes then add to the end and repoint the master and head arrows accordingly. If not then create a new branch and have the master and head point to that node. If no files exists in the staging area then print the error message and abort it. Does this by getting the SHA of the files.
Then matches the names with tree branch and checks for any similar existence. The branches use the LinkedList Data structure.

Status
Checks if any files exists in the .git that are not in the commit, then views them as untracked. Else checks if the file differs from the latest commit blob, then adds it to the modified file. Also, checks if the staging area is empty. Else just prints "all clear" message.

Remove
Removes the specified file if it exists, if not then prints out the error message. It does this by checking the latest commit and seeing if that file exits. If the file exists then it reassigns the head to the previous commit and deletes the current node.

Merge
Goes to the tree and checks if a branch with certain file name exists, if does then deserializes the contents of the file and compares the value. If they are not the same then changes the current file to be what the commited one is.
If the file does not exist then it ends with an error messages.

## Persistence
The commit class uses the persistence to write the contents after each commit.

The add method checks the persistence of the recent commit to check if certain file exists, else add it to the staging area.

The status method uses it to check if there is a file that wasn't in the recent commit, and sets the message out accordingly.