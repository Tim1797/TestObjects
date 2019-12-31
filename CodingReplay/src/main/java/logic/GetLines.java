package logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;

public class GetLines {

	protected static List<String> getListOfChangedLines(DiffEntry diffEntry, Repository repository) {		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		DiffFormatter formatter = new DiffFormatter(out);
																					
		try {
			formatter.setRepository(repository);
			formatter.format(diffEntry);			
		} catch (IOException e) {			
			e.printStackTrace();
		}
		String[] lines = out.toString().split("\n");
		List<String> linesAsList = new ArrayList<>();
		for(String s : lines) {
			linesAsList.add(s);
		}
		formatter.close();
		
		return filterChangedLines(linesAsList);
			
	}
	
	
	/**
	 * 
	 * 
	 * 
	 */
	private static List<String> filterChangedLines(List<String> linesAsList) {
		List<String> listToReturn = new ArrayList<>();
		
		for(String str :linesAsList) {
			if(str.startsWith("-") || str.startsWith("+")) {	
				if(!str.startsWith("---") && !str.startsWith("+++")) {														
					listToReturn.add(str);
				}
			}
		}
			
		
		return listToReturn;
	}
	
	
}
