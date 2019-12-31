package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;


/**
 * This class is called by the MainSimulation if the current diffEntry refers to a modification 
 * The old file is loaded and displayed and then modified until the new File is developed
 * 
 * 
 * 
 * @author Tim Hartmann
 *
 */
public class ModifySimulation extends MainSimulation{

	//class variables
	static List<String>processedChangedLines;
	static List<String>changedLines;
	static boolean isFinished;
	static DiffEntry diffEntry;
	static String oldFileAsString;
	static List<String> oldFileAsListOfStrings;
	static String newFileAsString;
	static List<String> newFileAsListOfStrings;
	static int lineNumber;
	static boolean firstCall;
	
	/**
	 * Method which is called ones when a DiffEntry has been processed and the
	 * user wants to look at a new one
	 * 
	 * 
	 * @param diffEntry
	 */
	public static void init(DiffEntry diffEntry) {
		//get information and set all the variables
		changedLines = changedLinesOfEachDiffEntry.get(diffEntry);	
		processedChangedLines = new ArrayList<>();
		ModifySimulation.diffEntry = diffEntry;
		lineNumber = 0;
		isFinished = false;
		firstCall = true;
		//get the text of the old file and store as String
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
            oldFileAsString = new String(loader.getBytes());   
                              
            List help = Arrays.asList(oldFileAsString.split("\n"));    
            oldFileAsListOfStrings = new ArrayList<>(help);
            
            
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		//get the text of the new File and store as String
		try {
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit1.getTree());
            treeWalk.setRecursive(true);          
            treeWalk.setFilter(PathFilter.create(diffEntry.getNewPath()));
            if (!treeWalk.next()) {
                throw new IllegalStateException("Did not find file");
            }                                 
            ObjectId objectId = treeWalk.getObjectId(0);	            
            ObjectLoader loader = git.getRepository().open(objectId);		
            newFileAsString = new String(loader.getBytes());          
            
            List help = Arrays.asList(newFileAsString.split("\n"));    
            newFileAsListOfStrings = new ArrayList<>(help);                        
		} catch (IOException e) {			
			e.printStackTrace();
		}				
	}
	
	
	/**
	 * Method which is called by the MainSimulation. Returns the next output to show to 
	 * the user
	 * 
	 * @return output as List of Strings
	 */
	public static List<String> getNext() {	
		String line;			
		
		//if the method is first called for a DiffEntry
		if(firstCall) {
			//Return the complete old File to show the modifications made to it
			firstCall = false;
			return oldFileAsListOfStrings;
		}
		//store which lines have been processed		
		line = changedLines.get(processedChangedLines.size());	
		processedChangedLines.add(line);
		
		//check whether all the lines have been processed and set flag
		if(changedLines.equals(processedChangedLines)) {
			isFinished = true;			
		}		
		//if the line was deleted
		if(line.startsWith("-")) {
			line = line.substring(1);						
			if(firstCall == false) oldFileAsListOfStrings.remove(line);	
		//if the line was added	
		}else {
			line = line.substring(1);
			lineNumber = getLineOfChange(line, newFileAsListOfStrings,lineNumber);				
			if(firstCall == false) oldFileAsListOfStrings.add(lineNumber, line);						
		}
		//return modified old file							
		return oldFileAsListOfStrings;								
	}

	
	/**
	 * Method to return whether the DiffEntry is finished
	 * 
	 * @return isFinished variable
	 */
	public static boolean finishedDiffEntry() {
		return isFinished;
	}

	
	/**
	 * Method searches for a specific line in a list of Strings and returns this line
	 * 
	 * @param changedLine
	 * @param file
	 * @param startPoint
	 * @return line of change as int
	 */
	private static int getLineOfChange(String changedLine, List<String> file, int startPoint) {
		int count = startPoint;
		for(int i = startPoint; i<file.size(); i++) {
			if(changedLine.equals(file.get(i))) {
				return count;
			}
			else count++;
		}
		return -1;
	}
	
}
