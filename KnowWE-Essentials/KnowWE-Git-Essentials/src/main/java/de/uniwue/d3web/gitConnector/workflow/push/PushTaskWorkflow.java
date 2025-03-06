package de.uniwue.d3web.gitConnector.workflow.push;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.raw.merge.GitMergeCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.merge.GitMergeResultSuccess;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandSuccess;
import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandSuccess;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusResultSuccess;
import de.uniwue.d3web.gitConnector.workflow.GitWorkflow;
import de.uniwue.d3web.gitConnector.workflow.push.structs.PushTaskWorkflowResult;

/**
 * Gets triggered when a task is to be committed
 * <p>
 * Still cannot commit if dependencies between tasks are present AND if individual commits contain ignored files (this
 * sucks)
 * TODO: still doesnt feature that it can a) ignore complete commits that only comprise ignored files and b) can undtrack ignored commits
 */
public class PushTaskWorkflow implements GitWorkflow<PushTaskWorkflowResult> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PushTaskWorkflow.class);
	private final GitConnector gitConnector;
	//access for a remote git!
	private final String username;
	private final String passwordOrToken;
	private final GitTask task;

	//this is a very important objects, it is used to track the success of the whole process. Gets renewed whenever execute is called!
	private PushTaskWorkflowResult gitWorkflowResult;

	public PushTaskWorkflow(GitTask task, GitConnector gitConnector, String username, String passwordOrToken) {
		this.gitConnector = gitConnector;
		this.task = task;
		this.username = username;
		this.passwordOrToken = passwordOrToken;
	}

	@Override
	public PushTaskWorkflowResult execute() {
		//we start a new workflow logger!
		gitWorkflowResult = new PushTaskWorkflowResult();
		//verify that our current branch is clean, otherwise there is no way we can even hope to finish this workflow!
		verifyCleanStatus();

		if (!gitWorkflowResult.initialCleanSuccessful()) {
			//sadly we have to abort here...
			gitWorkflowResult.addInitialCleanResult("Unable to continue push, git could not be restored to a clean state!", false);
			return gitWorkflowResult;
		}

		//once we know the git is clean, we can examine the commits to be added on master to get to know what our chances are, the wrapped list is required to prevent concurrent modification
		for (String commit : new ArrayList<>(task.commits)) {
			examineCommitForCherryPickReadiness(commit);
		}
		if (!this.gitWorkflowResult.cherryPickExamineSuccessful()) {
			this.gitWorkflowResult.addCherryPickReadinessResults("Checks for cherry pick were not successful, from now on we can never expect that this cherry-pick will go through! We will attempt nevertheless!", false);
		}

		//from this point here we assume that we can continue to cherry pick
		//step 1 create feature branch from master and cherry pick on top (should work after our previous analysis)

		//lets remember the current branch so that we can restore it after we are done!
		String workingBranch = gitConnector.currentBranch();
		performCherryPickOnFeatureBranch(workingBranch, task.featureBranchName);

		//next step is to merge the feature branch into master
		GitMergeCommandResult gitMergeCommandResult = mergeToMaster(task.featureBranchName);
		if (gitMergeCommandResult instanceof GitMergeResultSuccess) {
			this.gitWorkflowResult.addMergeResults("Successfully merged branch: " + task.featureBranchName + " into master", true);
		}
		else {
			//merge failed, we restore maintenance
			this.gitWorkflowResult.addMergeResults("Merge was not successful, we will return to working branch", false);
			resetToWorkingBranch(workingBranch, "master");
		}

		//and finally we push to origin
		PushCommandResult pushCommandResult = gitConnector.pushToOrigin(this.username, this.passwordOrToken);
		if (pushCommandResult instanceof PushCommandSuccess) {
			this.gitWorkflowResult.addPushResults("Push to origin was successful!", true);
			//just reset to working branch but we are done!
			resetToWorkingBranch(workingBranch, "master");
		}
		else {

			this.gitWorkflowResult.addPushResults("Push to origin failed but everything else worked, reason so far unknown!", false);

			//as everything regarding the cherry-picks worked we just go back to maintenance
			resetToWorkingBranch(workingBranch, "master");
		}

		LOGGER.info(gitWorkflowResult.printWorkflowResult());
		return gitWorkflowResult;
	}

	private GitMergeCommandResult mergeToMaster(String branchName) {
		//TODO we can yet again check for ignored files that have been auto generated in the meanwhile and get rid of them
		boolean success = gitConnector.switchToBranch("master", false);
		//TODO error handling here is pretty bad still
		if (!success) {
			this.gitWorkflowResult.addMergeResults("Unable to switch to master, reason currently unknown ", false);
		}


		GitMergeCommandResult mergeResult = null;
		if (success) {
			//verify that master is clean so that a merge is actually a feasible option
			//get rid of any ignored files TODO
			mergeResult = gitConnector.mergeBranchToCurrentBranch(branchName);
		}

		return mergeResult;
	}

	private void performCherryPickOnFeatureBranch(String workingBranch, String featureBranch) {

		//create feature branch and swap to it
		createFeatureBranchOnLocalMaster(featureBranch);

		//if we encountered an error here we are more or less screwed, all we can do is hop to rest to the previous branch!
		if (!this.gitWorkflowResult.createFeaturetaskSuccessful()) {
			resetToWorkingBranch(workingBranch, workingBranch);
		}

		//if all went well we can now cherry-pick the commits
		cherryPickCommitsOfTask();

		//now verify
		if (!this.gitWorkflowResult.cherryPickSuccessful()) {
			//okay cherry pick failed => back to working branch
			resetToWorkingBranch(workingBranch, featureBranch);
		}
	}

	private void resetToWorkingBranch(String workingBranch, String currentlyExpectedBranch) {
		//verify that we are on the branch that is expected
		String currentBranch = gitConnector.currentBranch();

		if (!currentBranch.equals(currentlyExpectedBranch)) {
			this.gitWorkflowResult.addResetResult("Expected to reside on branch: " + currentlyExpectedBranch + " however we are on branch: " + currentBranch + "\n This is an indicator that something didnt work as expected!", false);
		}

		//verify git is clean and perform our lightweight error recovery
		verifyCleanStatus();

		if (!this.gitWorkflowResult.initialCleanSuccessful()) {
			//this is super bad all we can do now is a reset and hope that one goes through
			ResetCommandResult resetCommandResult = this.gitConnector.rollback().resetToHEAD();
			if (resetCommandResult instanceof ResetCommandSuccess) {
				this.gitWorkflowResult.addResetResult("Successfully reset to HEAD", true);
			}
			else {
				this.gitWorkflowResult.addResetResult("Reset to HEAD failed, reason so far not known", false);
			}
		}

		//if we are still on an error state all hope is lost and we have to give up
		if (this.gitWorkflowResult.requiredReset() && !this.gitWorkflowResult.resetSuccessfull()) {
			this.gitWorkflowResult.addResetResult("Unable to switch back to : " + workingBranch + " this means it is even dangerous to continue working. Please contact an administrator as soon as possible!", false);
			return;
		}

		//ready to switch back to the workingBranch
		boolean success = gitConnector.switchToBranch(workingBranch, false);
		if (!success) {
			this.gitWorkflowResult.addRestoreWorkingBranchResult("Unable to switch back to : " + workingBranch + " this means it is even dangerous to continue working. Please contact an administrator as soon as possible!", false);
		}
		else {
			this.gitWorkflowResult.addRestoreWorkingBranchResult("Successfully switched back to : " + workingBranch + " state is as expected", true);
		}
	}

	private void cherryPickCommitsOfTask() {
		for (String commitHash : task.commits) {
			String result = gitConnector.cherryPick(List.of(commitHash));
			if (!result.isEmpty()) {
				this.gitWorkflowResult.addCherryPickResults("Tried to cherry-pick commit " + commitHash + ": " + result, false);
				//we sadly have to abort the cherry-picking process...
				gitConnector.abortCherryPick();
				break;
			}
			else {
				this.gitWorkflowResult.addCherryPickResults("Successfully cherry-picked commit " + commitHash + ": " + result, true);
			}
		}
	}

	private void createFeatureBranchOnLocalMaster(String branchName) {
		boolean taskCreationSuccess = gitConnector.createBranch(branchName, "master", true);
		if (!taskCreationSuccess) {
			this.gitWorkflowResult.addCreateFeatureBranchResults("Unable to create feature branch: " + branchName + " (based on master), reason unknown", false);
		}
		else {
			this.gitWorkflowResult.addCreateFeatureBranchResults("Successfully created feature branch: " + branchName + " (based on master)", true);
		}
	}

	/**
	 * Examining a commit is not an easy task! We check for all files and see if our master branch has all necessary
	 * commits so that this commit can indeed get cherry-picked without risk (it might work otherwise but this would be
	 * risky!). This method does also adjust the commits in our task, aka it has some built-in smartness in order to repair "broken" tasks.
	 *
	 * 1) It drops, commits that are empty or only contain ignored files
	 * 2) It checks if locked files have commits in git that are not in our task, this might happen due to a manifold of ways, e.g. due to command line intervention.
	 * 	  We can repair such cases always if the missing commits are not part of another task (this would be very very bad and has to be repaired manually anyway)
	 *
	 * @param commit a commit hash for the underlying git repository
	 */
	private void examineCommitForCherryPickReadiness(String commit) {
		//for every file in the commit (usually its 1 file!)
		List<String> changedFiles = gitConnector.listChangedFilesForHash(commit);

		//there are some checks we can perform before we even attempt any cherry-pick checks

		//1. are all files of this commit ignore (that means we dont want this commit among our commits)
		//2. if there are no changed files, we dont cherry-pick empty commits as well
		boolean allIgnored = changedFiles.stream().allMatch(file -> gitConnector.isIgnored(file));
		if(allIgnored || changedFiles.isEmpty()){
			//remove the commit and we are done
			this.task.commits.remove(commit);
			return;
		}

		//3. if some files are ignored (but not all) we are in a very bad state...
		if(changedFiles.stream().anyMatch(file -> gitConnector.isIgnored(file))){
			for(String path : changedFiles){
				if(gitConnector.isIgnored(path)){
					this.gitWorkflowResult.addCherryPickReadinessResults("The commit: "+commit + " contains the file: " + path + " but this file is ignored, so there is no way we can reliably assert that a cherry pick will work! Ending up in this state means it went wrong somewhere else!", false);
				}
			}
		}


		for (String changedFile : changedFiles) {
			//get the commits of that file in master
			List<String> masterCommitsForFile = gitConnector.commitHashesForFileInBranch(changedFile, "master");
			if (masterCommitsForFile.isEmpty()) {
				//we skip further examination, this should not cause trouble as the file is new
				continue;
			}

			//last master commit is necessary
			String lastMasterCommit = masterCommitsForFile.get(masterCommitsForFile.size() - 1);

			//now we get the commits on our working branch (which usually is maintenance)
			List<String> workingBranchCommitsForFile = gitConnector.commitHashesForFileInBranch(changedFile, gitConnector.currentBranch());
			if (workingBranchCommitsForFile.isEmpty()) {
				//this suggests something went entirely wrong as there has to be at least 1 commit!
				this.gitWorkflowResult.addCherryPickReadinessResults("Our working branch " + gitConnector.currentBranch() + " does not even have a single commit for file: " + changedFile + " HELP!!!", false);
				//we abort this operation prematurely as it wouldnt make any sense at this point
				return;
			}

			//next step is to align the commits onto each other, this is not easy as the commit hashes in different branches are not the same, so we have to go by commit message and date
			int indexOfCommitInWorkingBranch = getAlignedIndexOfLatestMasterCommit(lastMasterCommit, workingBranchCommitsForFile);
			if (indexOfCommitInWorkingBranch == -1) {
				this.gitWorkflowResult.addCherryPickReadinessResults("Unable to align the master commit: " + lastMasterCommit + " onto the commits of the working branch: " + gitConnector.currentBranch(), false);
			}

			//if its the commit previous to the one we are about to add to master it should be fine!
			if (indexOfCommitInWorkingBranch == workingBranchCommitsForFile.indexOf(commit) - 1) {
				this.gitWorkflowResult.addCherryPickReadinessResults("For file: " + changedFile + " in commit " + commit + "we would assume that the cherry-pick is about to work", true);
			}
			else {

				//ending up here is pretty bad already, however there is one instance in which we can still hope to recover! And this is if our index is older than expected AND we have all the intermediate changes in our task

				//we access all intermediate commits
				int currentindex = workingBranchCommitsForFile.indexOf(commit);
				int expectedIndex = indexOfCommitInWorkingBranch + 1;

				//this might happen if the commit is already merged to master - i have seen so much shit in my life this is not that weird
				if (expectedIndex > currentindex) {
					int alignedMasterCommit = getAlignedIndexOfLatestMasterCommit(commit, masterCommitsForFile);
					if (alignedMasterCommit != -1) {
						this.gitWorkflowResult.addCherryPickReadinessResults("The commit: " + commit + " is already merged to master, so we dont have to cherry pick that one", true);
						this.task.commits.remove(commit);
					}
					else {
						this.gitWorkflowResult.addCherryPickReadinessResults("No idea whats wrong here, this is and remains a mystery!", false);
						continue;
					}
				}
				else {
					List<String> intermediateCommits = workingBranchCommitsForFile.subList(expectedIndex, currentindex);

					if (task.commits.containsAll(intermediateCommits)) {
						//we are still okay
						this.gitWorkflowResult.addCherryPickReadinessResults("We appeared to have some sneaked in commits, however this is not the case as this task contains already all the sneaky commits", true);
					}
					else {
						//we find all that are not contained in this task, sorting order is oldest to newest
						intermediateCommits.removeAll(task.commits);
						//sorting order is now newest to oldest (required for next loop)
						Collections.reverse(intermediateCommits);

						//log all sneaky commits
						int indexOfCommit = this.task.commits.indexOf(commit);
						for (String sneakyCommit : intermediateCommits) {
							this.gitWorkflowResult.addCherryPickReadinessResults("We found a sneaky commit: " + sneakyCommit, false);
							String taskOfSneakyCommit = findTaskForSneakyCommit(sneakyCommit);

							if (taskOfSneakyCommit != null) {
								//we can not continue here as we have a dependency onto another task!
								this.gitWorkflowResult.addCherryPickReadinessResults("Performing the cherry pick would be a bad idea! The task: " + this.task.taskName + " has a dependency on the task: " + taskOfSneakyCommit + " via the commit: " + sneakyCommit, false);
							}
							else {
								//no task found, we can incorporate the task into the current list of commits
								this.gitWorkflowResult.addCherryPickReadinessResults("The sneaky commit was not part of a task, let us assume we desynced our workcontext from our git repository somehow (might have been caused by a manual commit via CLI), we add that commit to our task, this should ensure that the expected state is to be met", true);
								//find the correct index for this sneaky commit and add it to the task
								this.task.commits.add(indexOfCommit, sneakyCommit);
							}
						}

						//hoenstyl no idea what causes this
						if (intermediateCommits.isEmpty()) {
							this.gitWorkflowResult.addCherryPickReadinessResults("There is at least one commit that sneaked in somehow, this is bad news as this means either our cherry-pick is about to fail or in the future we will get issues by releasing the task that sneaked in between!", false);
						}
					}
				}
			}
		}
	}

	private String findTaskForSneakyCommit(String sneakyCommit) {
		for (String otherTask : task.taskToCommitMap.keySet()) {
			if (this.task.taskToCommitMap.get(otherTask).contains(sneakyCommit)) {
				return otherTask;
			}
		}
		return null;
	}

	/**
	 * For the provided commit @masterCommit finds the corresponding commit in the provided list, checks are performed
	 * by analyzing metadata!. Returned is the index in the list. If no commit could be found returns -1
	 *
	 * @param masterCommit
	 * @param workingBranchCommitsForFile
	 * @return
	 */
	private int getAlignedIndexOfLatestMasterCommit(String masterCommit, List<String> workingBranchCommitsForFile) {

		long timeMaster = gitConnector.commitTimeFor(masterCommit);

		for (int index = workingBranchCommitsForFile.size() - 1; index >= 0; index--) {
			long timeWorkingbranch = gitConnector.commitTimeFor(workingBranchCommitsForFile.get(index));

			if (timeMaster == timeWorkingbranch) {
				//check commit message TODO unsupported in current implementation of git connector so we only go by time now might be sufficient anyway
				return index;
			}
		}

		return -1;
	}

	private void verifyCleanStatus() {
		GitStatusCommandResult status = gitConnector.status().get();
		if (status instanceof GitStatusResultSuccess success) {
			if (success.isClean()) {
				gitWorkflowResult.addInitialCleanResult("Git repository is clean, continue push of task", true);
			}
			else {
				gitWorkflowResult.addInitialCleanResult("Git is not clean, we are not able to continue the push of this task, we attempt to get around this!", false);
				//TODO add fallback! => we can reset ignored but not untracked files!
				//check if our index is out of sync we can do this using a raw command
				String output = RawGitExecutor.executeGitCommand("git update-index --refresh", this.gitConnector.getGitDirectory());
				for (String path : success.getAffectedFiles()) {
					if (output.contains(path)) {
						this.gitWorkflowResult.addInitialCleanResult("The git index is not up to date for file: " + path + " this may have happened if you mix JGit and CLI git in a incompatible way", false);
						//nothing we can do at this point, at least i do not know how!
						return;
					}
				}
			}
		}
		else {
			gitWorkflowResult.addInitialCleanResult("Unable to determine the status of the current git, abort!", false);
		}
	}
}
