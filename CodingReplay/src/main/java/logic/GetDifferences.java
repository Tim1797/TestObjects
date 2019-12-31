package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import javafx.scene.paint.Color;


public class GetDifferences {
	
	static List<DiffEntry>listOfDiffs = new ArrayList<DiffEntry>();
	
	public static List<DiffEntry> getDifferences(Git given_git, RevCommit choosedCommit1, RevCommit choosedCommit2) {
		Repository repository = given_git.getRepository();
		List<DiffEntry> diffs = new ArrayList<>();
		try {			
			AbstractTreeIterator newTreeParser = prepareTreeParser(repository, choosedCommit1.getName());	
			AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, choosedCommit2.getName());
			diffs = given_git.diff()
					.setNewTree(newTreeParser)
						.setOldTree(oldTreeParser)
						.setPathFilter(PathSuffixFilter.create(".java"))
						//.setPathFilter(PathFilter.create("README.md"))
						.call();
					
			listOfDiffs = diffs;											
		} catch (GitAPIException | IOException e) {			
			e.printStackTrace();
		}	
			
		return listOfDiffs;
	}
	
	
	/**
	 * Method return TreeIterator for the repository
	 * 
	 * 
	 * 
	 * @param repository
	 * @param objectId
	 * @return
	 * @throws IOException
	 */
	private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
            ObjectId treeID = commit.getTree().getId();
            
            try(ObjectReader reader = repository.newObjectReader()){
            	return new CanonicalTreeParser(null, reader,treeID);
            }                             
         }
	}
}
