package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import gui.ChooseAssumptions_GUI;


public class MainSimulation {

	static List<String> assumptions;
	static Git git;
	static RevCommit commit1;
	static RevCommit commit2;
	static HashMap<DiffEntry, List<String>> changedLinesOfEachDiffEntry;
	static List<DiffEntry> diffEntryList;
	static List<ExtendedDiffEntry> extendedDiffEntryList;
	static int processedLines;
	static Repository repository;
	static boolean firstCall;
	
	

	public static void init() {
		//get all information
		assumptions = ChooseAssumptions_GUI.getAssumptions();
		git = ChooseAssumptions_GUI.getGit();
		commit1 = ChooseAssumptions_GUI.getCommit1();
		commit2 = ChooseAssumptions_GUI.getCommit2();
		diffEntryList = GetDifferences.getDifferences(git, commit1, commit2);	
		repository = git.getRepository();
		firstCall = true;
		changedLinesOfEachDiffEntry = new HashMap<DiffEntry, List<String>>();		
		for(DiffEntry diffEntry : diffEntryList) {
			changedLinesOfEachDiffEntry.put(diffEntry, GetLines.getListOfChangedLines(diffEntry,git.getRepository()));
		}	
		
		
		//TODO: read assumptions and order diffEntryList to change order of file replay
		
		//Create extended DiffEntries
		extendedDiffEntryList = new ArrayList<>();				
		for(DiffEntry entry: diffEntryList) {
			try {
				TreeWalk treeWalk = new TreeWalk(repository);
				treeWalk.addTree(commit1.getTree());
	            treeWalk.setRecursive(true);            
	            treeWalk.setFilter(PathFilter.create(entry.getNewPath()));
	            if (!treeWalk.next()) {
	                throw new IllegalStateException("Did not find file");
	            }                                 
	            ObjectId objectId = treeWalk.getObjectId(0);	            
	            ObjectLoader loader = git.getRepository().open(objectId);		
	            String file = new String(loader.getBytes());   
	                              
	            List help = Arrays.asList(file.split("\n"));    
	            List fileAsListOfStrings = new ArrayList<>(help);
	            extendedDiffEntryList.add(new ExtendedDiffEntry(entry, fileAsListOfStrings, changedLinesOfEachDiffEntry.get(entry)));
	            
			} catch (IOException e) {			
				e.printStackTrace();
			}					
		}
		
		
		
		
		List<ExtendedDiffEntry> list1 = extendedDiffEntryList.get(2).getImplementedInterfaces();
		List<ExtendedDiffEntry> list2 = extendedDiffEntryList.get(2).getInheritsFrom();
		List<ExtendedDiffEntry> list3 = extendedDiffEntryList.get(2).getCalledClasses();
		
		System.out.println();
		
		
	}
	
	public static List<String> getNext() {	
		if(diffEntryList.isEmpty()) {
			List<String> toReturn = new ArrayList<>();
			toReturn.add("The commit is processed!");
			return toReturn;
		}
		DiffEntry diffEntry = diffEntryList.get(0);
				
		if(diffEntry.getChangeType() == ChangeType.MODIFY) {
			if(firstCall == true) {
				ModifySimulation.init(diffEntry);
				firstCall = false;
			}
			if(ModifySimulation.finishedDiffEntry() == true) {	
				firstCall = true;
				diffEntryList.remove(0); 							
			}
			else {
				return ModifySimulation.getNext();
			}
			
		}
									
		else if(diffEntry.getChangeType() == ChangeType.ADD) {	
			if(firstCall == true) {
				AddSimulation.init(diffEntry);
				firstCall = false;
			}
			if(AddSimulation.finishedDiffEntry() == true) {	
				firstCall = true;
				diffEntryList.remove(0); 							
			}
			else {
				return AddSimulation.getNext();
			}
			
		}
		else if(diffEntry.getChangeType() == ChangeType.DELETE) {	
			
			DeleteSimulation.init(diffEntry);										
			diffEntryList.remove(0); 										
			return DeleteSimulation.getNext();
						
		}
													
		return getNext();
	}

	
	
	public static List<ExtendedDiffEntry> getExtendedDiffEntries() {
		
		return extendedDiffEntryList;
	}


	
	
}
