package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.javatuples.Pair;

public class AddSimulation extends MainSimulation{

	//class variables
	static boolean isFinished;
	static DiffEntry diffEntry;
	static String newFileAsString;
	static List<String> newFileAsListOfStrings;
	static boolean firstCall;
	static List<String> currentOutput;
	static int classStart;
	static int currentPos;
	static boolean foundImport;
	static List<Pair<String,Integer>> importsToAdd;
	static List<String> imports;
	
	/**
	 * Method which is called ones when a DiffEntry has been processed and the
	 * user wants to look at a new one
	 * 
	 * 
	 * @param diffEntry
	 */
	public static void init(DiffEntry diffEntry) {
		isFinished = false;			
		AddSimulation.diffEntry = diffEntry;	
		firstCall = true;
		foundImport = false;
		//get the text of the added file
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
		
		//get the imports
		imports = new ArrayList<>();	
		for(String line: newFileAsListOfStrings) {
			if(line.startsWith("import")) {
				String partsOfImport[] = line.split("\\.");				
				imports.add(partsOfImport[partsOfImport.length-1].substring(0, partsOfImport[partsOfImport.length-1].length()-1));
			}			
		}
						
		//initialize the output
		currentOutput = new ArrayList<>();	
		for(int i=0; i<newFileAsListOfStrings.size();i++) {
			currentOutput.add("");
		}
		
	}
	
	/**
	 * Method which is called by the MainSimulation. Returns the next output to show to 
	 * the user
	 * 
	 * @return output as List of Strings
	 */
	public static List<String> getNext() {		
		//if the method is first called for a DiffEntry
		if(firstCall) {			
			firstCall = false;
			//call method the get initial output
			firstCallFileInit();	
			return currentOutput;
		}	
		//get current number of line in file
		currentPos = getCurrentPosition();
		
		//case that the complete file was processed
		if(currentPos == -1) {
			isFinished = true;	
			//return complete added file (adds not found imports)
			return newFileAsListOfStrings;
		}
		
		if(foundImport) {
			foundImport = false;
			for(Pair<String, Integer> one_import: importsToAdd) {
				currentOutput.set(one_import.getValue1(), one_import.getValue0());
			}
				
		}
		
		//loop to skip empty lines
		while(currentPos < newFileAsListOfStrings.size()) {
			//method or loop in line
			if(newFileAsListOfStrings.get(currentPos).endsWith("{")) {
				simulateLoop();
				return currentOutput;
			}
			//normal not empty line
			else if(!newFileAsListOfStrings.get(currentPos).equals("")) {				
				currentOutput.set(currentPos, newFileAsListOfStrings.get(currentPos));
				//look for a type which is not imported jet
				handleImport(newFileAsListOfStrings.get(currentPos));
				return currentOutput;
			}						
			else {
				currentPos++;
			}
		}
						
		return currentOutput;
	}
	
	

	private static void handleImport(String lineToCheck) {
		String clearLineToCheck = lineToCheck.replace("\t", "");
		String completeImportLine = new String();
		int lineNumber = 0;
		
		//TODO: 1. word vor "<" oder 2. word in "<>" oder 3. in Methodendek. oder schleife in den ()
		
		//method case: check declarations inside brackets
		if(clearLineToCheck.contains("{") &&
				(clearLineToCheck.startsWith("privat") || clearLineToCheck.startsWith("protected") || clearLineToCheck.startsWith("public") || clearLineToCheck.startsWith("static") || clearLineToCheck.startsWith("void"))) {
			
			//get text between ()-brackets
			String help = clearLineToCheck.split("(")[1];
			String textInBrackets = help.split(")")[0];
			String[] parameters = textInBrackets.split(",");
			
			List<String> types = new ArrayList<>();
			for(String parameter: parameters) {
				types.add(parameter.split(" ")[0]);												
			}
			for(String line: imports) {
				for(String type: types) {
					if(line.equals(type)) {
						foundImport = true;
						imports.remove(line);
						for(String lineInFile: newFileAsListOfStrings) {
							if(lineInFile.endsWith(line + ";")) {
								completeImportLine = lineInFile; 							
							}			
							lineNumber++;
						}	
						importsToAdd.add(new Pair<String, Integer>(completeImportLine, lineNumber));						
					}
				}
			}
					
			
		}
		//generic type case
		if (clearLineToCheck.contains("<") && clearLineToCheck.contains(">")) {
			
		}
		//simple declaration case
		
		String[] words = clearLineToCheck.split(" ");
		for(String line: imports) {				
			if(words[0].equals(line)) {
				//remove processed import from list
				imports.remove(line);
				foundImport = true;
				for(String lineInFile: newFileAsListOfStrings) {
					if(lineInFile.endsWith(line + ";")) {
						completeImportLine = lineInFile; 
						break;
					}			
					lineNumber++;
				}
					
				importsToAdd.add(new Pair<String, Integer>(completeImportLine, lineNumber));
				break;
			}
		}
				
	}
	
	
	/**
	 * Method returns the next line in which is output is not equal to the file
	 * 
	 * 
	 * @return int
	 */
	private static int getCurrentPosition() {
		for(int i=classStart+1;i<newFileAsListOfStrings.size(); i++) {
			if(!newFileAsListOfStrings.get(i).equals(currentOutput.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	
	
	/**
	 * Method to simulate a loop construct like a method or an if/while/for-Loop
	 * 
	 * The line with the Loop and the brackets are printed into the output
	 * 
	 */
	private static void simulateLoop() {
		//add line which has the start of the loop
		currentOutput.set(currentPos, newFileAsListOfStrings.get(currentPos));
		//look for a type which is not imported jet
		handleImport(newFileAsListOfStrings.get(currentPos));
		//The stack is used to get the correct bracket to end the loop
		Stack<String> stack = new Stack<String>();
		stack.push("{");
		while(!stack.empty()) {
			currentPos++;
			if(newFileAsListOfStrings.get(currentPos).endsWith("{")) {
				stack.push("{");
			}
			else if(newFileAsListOfStrings.get(currentPos).endsWith("}")) {
				stack.pop();
			}
		}
		//Add bracket
		currentOutput.set(currentPos, newFileAsListOfStrings.get(currentPos));		
	}
	
	
	
	/**
	 * Adds a header, the package and the class declaration into the output and returns the line
	 * under the class declaration
	 * 
	 * 
	 * @return start of programming as int
	 */
	private static int firstCallFileInit() {
		//tuple of the class declaration as String and its line number
		Pair<String, Integer> classDeclaration = classDeclaration();
		currentOutput.set((int)classDeclaration.getValue1(), (String)classDeclaration.getValue0());	
		//set the bracket at the end of the file
		currentOutput.set(newFileAsListOfStrings.size()-1, "}");
		//add package if there is one
		if(newFileAsListOfStrings.get(0).startsWith("package")) {
			currentOutput.set(0, newFileAsListOfStrings.get(0));
		}	
		//skip the imports
		int i=0;
		while(!newFileAsListOfStrings.get(i).startsWith("import") && !newFileAsListOfStrings.get(i).startsWith("public class")) {
			currentOutput.set(i, newFileAsListOfStrings.get(i));
			i++;
		}
		//return line number
		return (int) classDeclaration.getValue1();	
	}
	
	
	/**
	 * Method to get the String of the class declaration and the line number of it
	 * 
	 * 
	 * 
	 * @return
	 */
	private static Pair<String, Integer> classDeclaration() {
		classStart = 0;
		String className = new String();
		//get the name of the class
		String[] path = diffEntry.getNewPath().split("/");
		for(String s: path) {
			if(s.endsWith(".java")) {				
				className = s.substring(0, s.length()-5);
			}
		}		
		//search for the declaration in file
		for(String line: newFileAsListOfStrings) {
			if(line.equals("public class " + className + " {") || line.equals("public class " + className + "{")) {
				return new Pair<String, Integer>("public class " + className + " {", classStart);
			}
			classStart++;
		}
		
		return null;
		
	}
	
	
	/**
	 * Method to return whether the DiffEntry is finished
	 * 
	 * @return isFinished variable
	 */
	public static boolean finishedDiffEntry() {		
		return isFinished;
	}

	

}
