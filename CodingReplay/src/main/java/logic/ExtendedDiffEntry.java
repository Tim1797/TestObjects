package logic;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;

public class ExtendedDiffEntry {

	String type;
	String name;
	DiffEntry diffEntry;
	List<String> text;
	List<String> changedLines;

	
	public ExtendedDiffEntry(DiffEntry diffEntry, List<String> text, List<String> changedLines) {
		String[] help = diffEntry.getNewPath().split("/");
		name = help[help.length-1].substring(0, help[help.length-1].length()-5);
		//type is empty if its a modification
		type = new String();
		for(String s: text) {
			if(s.contains("class " + name)) {
				type = "class";
				break;
			}
			if(s.contains("interface " + name)) {
				type = "interface";
				break;
			}
		
		}	
		//check if test class
		if(type.equals("class")){
			for(String s: text) {
				if(s.contains("import org.junit")) {
					type = "test";
				}
			}
		}
		
		this.changedLines = changedLines;	
		this.diffEntry = diffEntry;
		this.text = text;
		

	}
	

	public List<ExtendedDiffEntry> getCalledClasses(){
		List<ExtendedDiffEntry> calledClasses = new ArrayList<>();
		
		for(String s: changedLines) {
			for(ExtendedDiffEntry entry: MainSimulation.getExtendedDiffEntries()) {
				//object of class is created
				if(s.contains("new") && s.contains(entry.getName())) {
					calledClasses.add(entry);	
				}	
				//static method is called
				if(s.contains(entry.getName() + ".")) {
					calledClasses.add(entry);
				}
				
			}
		}
								
		return calledClasses;
	}
	
	
	
	public List<ExtendedDiffEntry> getInheritsFrom(){
		//get classes that are extended
		List<ExtendedDiffEntry> inheritsFrom = new ArrayList<>();
		for(String s: changedLines) {
			if(s.contains("class") && s.contains("extends")) {	
				String help = s.split("extends")[1];
											
				for(ExtendedDiffEntry entry: MainSimulation.getExtendedDiffEntries()) {
					if(help.contains(entry.getName()) && !entry.getType().equals("interface")) {
						inheritsFrom.add(entry);							
					}
				}														
			}
		}
			
		return inheritsFrom;
	}
	
	
	
	public List<ExtendedDiffEntry> getImplementedInterfaces(){
		//get implemented interfaces
		List<ExtendedDiffEntry> implementedInterfaces = new ArrayList<>();
		for(String s: changedLines) {			
			if(s.contains("class") && s.contains("implements")) {					
				for(ExtendedDiffEntry entry: MainSimulation.getExtendedDiffEntries()) {
					if(s.contains(entry.getName()) && entry.getType().equals("interface")) {
						implementedInterfaces.add(entry);
					}
				}																									
			}
		}	
		
		return implementedInterfaces;
	}
	
	
	public String getName() {
		return name;
	}
	
	public List<String> getText(){
		return text;
	}
	
	public DiffEntry getDiffEntry() {
		return diffEntry;
	}
	
	public String getType() {
		return type;
	}
	
}
