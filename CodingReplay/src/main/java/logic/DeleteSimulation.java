package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

/**
 * This class is called by the MainSimulation if the current diffEntry refers to a deletion
 * The deleted file is displayed with a comment on the fact it was deleted
 * 
 * 
 * @author Tim Hartmann
 *
 */
public class DeleteSimulation extends MainSimulation{

	//class variables
	static boolean isFinished;
	static DiffEntry diffEntry;
	static String file;
	static List<String> fileAsListOfStrings;
	
	/**
	 * Method which is called ones to set all the static parameters
	 * 
	 * @param diffEntry
	 */
	public static void init(DiffEntry diffEntry) {
		isFinished = false;	
		
		DeleteSimulation.diffEntry = diffEntry;
		//get the text of the deleted file
		try {
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit2.getTree());
            treeWalk.setRecursive(true);            
            treeWalk.setFilter(PathFilter.create(diffEntry.getOldPath()));
            if (!treeWalk.next()) {
                throw new IllegalStateException("Did not find file");
            }                                 
            ObjectId objectId = treeWalk.getObjectId(0);	            
            ObjectLoader loader = git.getRepository().open(objectId);		
            file = new String(loader.getBytes());   
                              
            List help = Arrays.asList(file.split("\n"));    
            fileAsListOfStrings = new ArrayList<>(help);
            
            
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Method which is called by the MainSimulation. Returns the next output to show to the user
	 * Is only called ones in case of the deletion
	 * 
	 * 
	 * @return output as List of Strings
	 */
	public static List<String> getNext() {	
		//display the deleted file to user
		isFinished = true;			
		fileAsListOfStrings.add(0, "####################################DELETE####################################");
		fileAsListOfStrings.add("####################################DELETE####################################");
		return fileAsListOfStrings;
	}
	
	/**
	 * Method to return whether the DiffEntry is finished
	 * Returns true as soon as the getNext() method is called ones
	 * 
	 * @return isFinished variable
	 */
	public static boolean finishedDiffEntry() {		
		return isFinished;
	}

	

}
