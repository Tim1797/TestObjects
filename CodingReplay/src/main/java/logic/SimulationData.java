package logic;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;

public class SimulationData {
	
	List<DiffEntry> diffEntryList;
	HashMap<DiffEntry, List<String>> changedLinesOfEachDiffEntry;
	HashMap<DiffEntry, List<Method>> changedMethodsOfEachDiffEntry;
	
	public SimulationData(DiffEntry diffEntry, List<String> changedLines) {
		
	}
	
	
	public void changeOrderOfDiffEntries(DiffEntry diff1, DiffEntry diff2) {
		//put diff1 in front of diff2 in diffEntryList
	}
	
	public void changeOrderOfLines(DiffEntry diff, String s1, String s2) {
		//change order in List of diff in the hashmap
	}
	
	public void putDiffEntriesTogether(DiffEntry diff1, DiffEntry diff2) {
		//change diffEntryList order
	}
	
}
