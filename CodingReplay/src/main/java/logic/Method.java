package logic;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a method
 * Each method has a name, a start and end point in its class and text
 * The other parameters are used to control the processing sequence
 * 
 * 
 * @author Tim Hartmann
 *
 */
public class Method {
	//parameters
	String name;
	int start;
	int end;
	List<String> linesOfMethod;
	String currentLine;
	int currentLineNumberInFile;
	int currentLineNumberInMethod;
	
	/**
	 * Constructor to set all parameters
	 * 
	 * @param name
	 * @param start
	 * @param end
	 * @param linesOfMethod
	 */
	public Method(String name, int start, int end, List<String> linesOfMethod) {
		this.name = name;
		this.start = start;
		this.end = end;
		this.linesOfMethod = linesOfMethod;
		currentLine = linesOfMethod.get(0);
		currentLineNumberInFile = start;
		currentLineNumberInMethod = 0;
	}
	
	/**
	 * Method to go to the next line of the method
	 * If the method is processed the currentLine is set to null as a flag
	 * 
	 */
	public void getNextLine() {	
		currentLineNumberInFile++;
		currentLineNumberInMethod++;
		
		if(currentLineNumberInFile <= end) {
			currentLine = linesOfMethod.get(currentLineNumberInMethod);			
		}
		else currentLine = null;
	}
	
	//getter
	
	public String getCurrentLine() {		
		return currentLine;
	}
	
	public int getCurrentLineNumber() {
		return currentLineNumberInFile;
	}
	
	public List<String> getLinesOfMethod(){
		return linesOfMethod;
	}
	
	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Jump to a specific point inside the method
	 * 
	 * @param line
	 */
	public void resetToLine(int line) {
		currentLine = linesOfMethod.get(line-start);
		currentLineNumberInFile = line;
		currentLineNumberInMethod = line-start;
	}
	
	
	/**
	 * Method returns a list of other methods which are called inside this method
	 * 
	 * @return called methods
	 */
	public List<Method> getCalledMethods(){		
		List<Method> calledMethods = new ArrayList<>();
		List<Method> allMethods = AddSimulation.getMethods();	
		for(int i=0;i<linesOfMethod.size();i++) {
			//jump over comments and loops
			if(!linesOfMethod.get(i).contains("/") && !linesOfMethod.get(i).contains("*") && !linesOfMethod.get(i).contains("{")) {
				for(Method m: allMethods) {
					if(linesOfMethod.get(i).contains(m.getName() + "(")) {
						calledMethods.add(m);
					}
				}
			}	
		}
		//call of a method to delete duplicate entries, found on: https://wiki.byte-welt.net/wiki/Doppelte_Datens%C3%A4tze_aus_ArrayList_entfernen
		return (List<Method>) CollectionUtil.removeDuplicate(calledMethods);
	}
	

	
}
