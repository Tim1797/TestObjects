package simulation;

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

import gui.ShowSimulation_GUI;
import nonStaticClasses.Method;


/**
 * This class is called by the MainSimulation if the current diffEntry refers to an added file
 * The new file is created step by step until the complete file is displayed in the end
 * 
 * 
 * 
 * @author Tim Hartmann
 *
 */
public class AddSimulation extends MainSimulation{

	//class variables
	private static boolean isFinished;
	private static DiffEntry diffEntry;
	private static String newFileAsString;
	private static List<String> newFileAsListOfStrings;
	private static boolean firstCall;
	private static List<String> currentOutput;
	private static int classStart;
	private static int currentPos;
	private static boolean foundImport;
	private static List<Pair<String,Integer>> importsToAdd;
	private static List<String> imports;
	private static List<String> toRemove;
	private static List<String> varsAndConsts;
	private static boolean foundVarOrConst;
	private static List<Pair<String,Integer>> varsAndConstsToAdd;
	private static int methodStart;
	private static List<Method> methods;
	private static Method currentMethod;
	private static int currentMethodNumber;
	
	
	
	/**
	 * Method which is called ones when a DiffEntry has been processed and the
	 * user wants to look at a new one
	 * All the static parameters get set
	 * 
	 * 
	 * @param diffEntry
	 */
	public static void init(DiffEntry diffEntry) {
		//set static values
		isFinished = false;			
		methodStart = 0;
		AddSimulation.diffEntry = diffEntry;	
		firstCall = true;
		foundImport = false;
		foundVarOrConst = false;
		importsToAdd = new ArrayList<>();
		toRemove = new ArrayList<>();
		varsAndConstsToAdd = new ArrayList<>();
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
            
            List<String> help = Arrays.asList(newFileAsString.split("\n"));    
            newFileAsListOfStrings = new ArrayList<>(help);                        
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		//solve encoding problem
		List<String> correct = new ArrayList<>();
		for(String line: newFileAsListOfStrings) {
			String newLine = line.replace("\r", "");
			correct.add(newLine);
		
		}				
		newFileAsListOfStrings = correct;
			
		//get the imports of the class
		getImports();
			
		//get variables and constants, which are declared above the first method
		getVarsAndCons();
				
		//get methods
		setMethods();
				
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
					
			
		//if the current method has been processed
		if(currentMethod.getCurrentLine() == null) {
			currentMethodNumber++;
			//file is processed
			if(currentMethodNumber >= methods.size()) {
				isFinished = true;
				return newFileAsListOfStrings;
			}
			currentMethod = methods.get(currentMethodNumber);
		}
		
		//in the previous call of the getNext Method an import was found
		if(foundImport) {
			foundImport = false;
			for(Pair<String, Integer> one_import: importsToAdd) {
				currentOutput.set(one_import.getValue1(), one_import.getValue0());				
			}
			return currentOutput;	
		}
			
		//in the previous call of the getNext Method a variable or constant was found
		if(foundVarOrConst) {
			foundVarOrConst = false;
			for(Pair<String, Integer> var_con: varsAndConstsToAdd) {
				currentOutput.set(var_con.getValue1(), var_con.getValue0());
			}
			return currentOutput;
		}
		
		//Loop through comments, empty lines and closing brackets
		boolean flag = false;
		while(currentMethod.getCurrentLine().equals("") 
				|| currentMethod.getCurrentLine().matches("\t+") 
				|| currentMethod.getCurrentLine().contains("*")
				|| currentMethod.getCurrentLine().contains("//")
				|| currentMethod.getCurrentLine().endsWith("}")){
			
			currentOutput.set(currentMethod.getCurrentLineNumber(), currentMethod.getCurrentLine());						
			currentMethod.getNextLine();
			if(currentMethod.getCurrentLine() == null) {
				flag = true;				
				break;
			}
		}
		
		//file is not completely processed
		if(flag == false) {
			//method or loop in line
			if(currentMethod.getCurrentLine().endsWith("{")) {				
				//add line which has the start of the loop
				currentOutput.set(currentMethod.getCurrentLineNumber(), currentMethod.getCurrentLine());
				//look for a type which is not imported jet
				handleImports(currentMethod.getCurrentLine());
				handleVariablesAndConstants(currentMethod.getCurrentLine());
				//set closing bracket to output with method
				simulateLoop();
				currentMethod.getNextLine();
				return currentOutput;
			}
			//set normal line
			currentOutput.set(currentMethod.getCurrentLineNumber(), currentMethod.getCurrentLine());
			//look for a type which is not imported jet
			handleImports(currentMethod.getCurrentLine());
			//look for variables to set
			handleVariablesAndConstants(currentMethod.getCurrentLine());	
			
			//set the declaration of a method if the method is called by the currentLine
			//the rest of the method is played after the current method is completely processed
			for(Method m: methods) {
				if(m.getCurrentLine() != null) {
					if(currentMethod.getCurrentLine().contains(m.getName() + "(")) {
						while(m.getCurrentLine().contains("/") || m.getCurrentLine().contains("*")) {							
							currentOutput.set(m.getCurrentLineNumber(), m.getCurrentLine());
							m.getNextLine();
						}	
						if(m.getCurrentLine().contains("{")) {	
							currentOutput.set(m.getCurrentLineNumber(), m.getCurrentLine());
							int resetLine = m.getCurrentLineNumber()+1;
							Stack<String> stack = new Stack<>();
							stack.push("{");
							while(!stack.empty()) {
								m.getNextLine();
								if(m.getCurrentLine().contains("{")) {
									stack.push("{");
								}			
								if(m.getCurrentLine().contains("}")) {	
									stack.pop();
								}
							}
							//Add bracket
							currentOutput.set(m.getCurrentLineNumber(), m.getCurrentLine());	
							m.resetToLine(resetLine);
							break;
						}
					}					
				}
			}				
			currentMethod.getNextLine();
			return currentOutput;
		}	
		
		ShowSimulation_GUI.setNewText(MainSimulation.getNext());				
		return currentOutput;											
	}

	
	
	
	
	//getter
	
	/**
	 * Method to return whether the DiffEntry is finished
	 * 
	 * @return isFinished variable
	 */
	public static boolean finishedDiffEntry() {		
		return isFinished;
	}

	public static List<Method> getMethods() {		
		return methods;
	}
	
	

	
	//private methods for outsourced functions
	
	
	/**
	 * Methods searches for the methods of the class and creates a Method object for every one
	 * A methods must be declared with public, private or protected in order for setMethods() to function
	 * 
	 */
	private static void setMethods() {
		methods = new ArrayList<>();
		for(int i=0;i<newFileAsListOfStrings.size();i++) {
			String name = new String();
			int start = 0;
			int end = 0;
			List<String> linesOfMethod = new ArrayList<>();
			if(newFileAsListOfStrings.get(i).endsWith("{") && !newFileAsListOfStrings.get(i).contains("class") 
					&& (newFileAsListOfStrings.get(i).contains("public") || newFileAsListOfStrings.get(i).contains("private") || newFileAsListOfStrings.get(i).contains("protected"))) {
				
				//get comment of method
				int j=i-1;
				List<String> commentWrongOrder = new ArrayList<>();
				while(newFileAsListOfStrings.get(j).contains("*") || newFileAsListOfStrings.get(j).contains("//")) {
					commentWrongOrder.add(newFileAsListOfStrings.get(j));
					j--;
				}
				
				for(int k=commentWrongOrder.size()-1; k>=0; k--) {
					linesOfMethod.add(commentWrongOrder.get(k));
				}
							
				String[] help = newFileAsListOfStrings.get(i).split("\\(");
				String[] help2 = help[0].split(" ");
				name = help2[help2.length-1];		
				start = i;
				end = start;
				linesOfMethod.add(newFileAsListOfStrings.get(end));
				Stack<String> stack = new Stack<String>();
				stack.push("{");
				while(!stack.empty()) {		
					end++;
					linesOfMethod.add(newFileAsListOfStrings.get(end));
					if(newFileAsListOfStrings.get(end).endsWith("{")) {
						stack.push("{");
					}			
					if(newFileAsListOfStrings.get(end).contains("}")) {	
						stack.pop();
					}
					
				}				
				methods.add(new Method(name,start-commentWrongOrder.size(),end,linesOfMethod));
			}
			
		}
		
		if(methods.size() != 0) {
			//change order of methods with deep first search		
			orderMethods();
			
			currentMethod = methods.get(0);
			currentMethodNumber = 0;
		}		
		else {
			isFinished = true;
		}
		
	}



	/**
	 * Method sets the varsAndConsts list by going through the class and searching for
	 * variables and constants
	 * Requires a declaration with private, public, protected or with static
	 * 
	 */
	private static void getVarsAndCons() {		
		varsAndConsts = new ArrayList<>();
		for(String line: newFileAsListOfStrings) {
			if((line.contains("static ") || line.contains("private ") || line.contains("public ") || line.contains("protected ")) && !line.contains("{")) {
				if(line.contains("=")) {
					String partsOfDeclaration[] = line.split("=");
					String[] help = partsOfDeclaration[0].split(" ");
					varsAndConsts.add(help[help.length-1]);
				}
				else {
					String partsOfDeclaration[] = line.split(" ");
					varsAndConsts.add(partsOfDeclaration[partsOfDeclaration.length-1].substring(0,partsOfDeclaration[partsOfDeclaration.length-1].length()-1));
				}						
			}
		}					
	}


	/**
	 * Searches the imports of the class and writes them into the list
	 * 
	 */
	private static void getImports() {
		imports = new ArrayList<>();	
		for(String line: newFileAsListOfStrings) {
			if(line.startsWith("import")) {
				String partsOfImport[] = line.split("\\.");					
				imports.add(partsOfImport[partsOfImport.length-1].substring(0, partsOfImport[partsOfImport.length-1].length()-1));
			}			
		}	
		
		
		
	}


	/**
	 * This method uses deep first search with called methods inside
	 * the currentMethod to change the replayed order of methods
	 * 
	 */
	private static void orderMethods() {
		//iterative deep first search
		List<Method> methodsInOrder = new ArrayList<>();		
		//play an existing constructor first
		String[] help = diffEntry.getNewPath().split("/");
		String name = help[help.length-1].substring(0, help[help.length-1].length()-5);
		for(Method m: methods) {		
			if(m.getName().equals(name)){
				methodsInOrder.add(m);
				break;
			}
		}
			
		for(Method m: methods) {
			//check if the method calls another method
			if(!methodsInOrder.contains(m)) {
				Stack<Method> stack = new Stack<>();
				stack.push(m);
				
				while(!stack.isEmpty()) {
					Method current = stack.pop();
					methodsInOrder.add(current);
					
					for(Method meth: current.getCalledMethods()) {
						if(!methodsInOrder.contains(meth) && !stack.contains(meth)) {
							stack.push(meth);
						}				
					}						
				}
			}
		}
				
		methods = methodsInOrder;
		
	}
	
	
	
	/**
	 * Method is called for every added line
	 * Adds variables and constants to output when they occur in a line
	 * 
	 * 
	 * @param lineToCheck
	 */
	private static void handleVariablesAndConstants(String lineToCheck) {
		String completeLine = new String();
		String clearLineToCheck = lineToCheck.replace("\t", "");
		int lineNumber = 0;
		toRemove = new ArrayList<>();
		for(String var_con: varsAndConsts) {
			if(clearLineToCheck.contains(var_con)) {
				foundVarOrConst = true;
				toRemove.add(var_con);
				for(String lineInFile: newFileAsListOfStrings) {
					if(lineInFile.contains(var_con)) {
						completeLine = lineInFile; 		
						break;
					}			
					lineNumber++;
				}
				varsAndConstsToAdd.add(new Pair<String,Integer>(completeLine,lineNumber));
				lineNumber = 0;
			}
		}
		for(String line: toRemove) {
			varsAndConsts.remove(line);
		}		
	}
	
	
	
	
	/**
	 * Method is called for every line which is added to the output
	 * 
	 * Checks if an import was made for this line. When the next call occurs the
	 * next method adds the matching import to the output
	 * 
	 * 
	 * @param lineToCheck
	 */
	private static void handleImports(String lineToCheck) {		
		String clearLineToCheck = lineToCheck.replace("\t", "");
					
		//method case: check declarations inside brackets
		if(clearLineToCheck.contains("{") && clearLineToCheck.contains("(")){			
			//get text between ()-brackets
			String help = clearLineToCheck.split("\\(")[1];
			String textInBrackets = help.split("\\)")[0];
			String[] parameters = textInBrackets.split(",");
			
			List<String> types = new ArrayList<>();
			for(String parameter: parameters) {
				types.add(parameter.split(" ")[0]);												
			}
					
			for(String type: types) {
				handleFoundType(type);
			}
				
		}
				
		//generic type case
		if (clearLineToCheck.contains("<") && clearLineToCheck.contains(">")) {
			String[] help = clearLineToCheck.split("<");
			String[] help2 = help[1].split(">");
			String type = new String();
			String[] help3 = help[0].split(" ");
			String type2 = new String();
			if(help3.length != 0) {
				type2 = help3[help3.length-1];
			}						
			if(help2.length != 0) {
				type = help2[0];
			}
			
			handleFoundType(type);
			handleFoundType(type2);
		}
		
		//for-Loop case
		if(clearLineToCheck.contains("for\\(")) {
			String[] help = clearLineToCheck.split("for\\(");
			String[] help2 = help[1].split(" ");
			String type = help2[0];
			handleFoundType(type);			
		}
				
		//declaration with "new"
		if(clearLineToCheck.contains("new ")) {
			String[] help = clearLineToCheck.split("new");
			String[] help2 = help[1].split(" ");
			String help3 = help2[1];
			String type = new String();
			if(help3.contains("<")) {
				type = help3.split("<")[0];
			}
			else {
				type = help3.split("\\(")[0];
			}			
			handleFoundType(type);	
			
		}
				
		//class-method case
		if(clearLineToCheck.contains(".")) {
			String[] help = clearLineToCheck.split("\\.");
			String[] help2 = help[0].split(" ");
			
			if(help2.length == 0) {
				String type = help[0];			
				handleFoundType(type);	
			}else {
				String type = help2[help2.length-1];			
				handleFoundType(type);	
			}				
		}
			
		//simple declaration case	
		String[] words = clearLineToCheck.split(" ");			
		for(String word: words) {
			handleFoundType(word);	
		}	
	}
	
	
	/**
	 * Method is called with a type. The method searches for the type in
	 * the imports and sets the flag for the found import, so the import line
	 * is added to the output in the next iteration
	 * 
	 * @param type
	 */
	private static void handleFoundType(String type) {
		String completeImportLine = new String();
		int lineNumber = 0;
		toRemove = new ArrayList<>();
		for(String line: imports) {
			if(line.equals(type)) {
				foundImport = true;
				toRemove.add(line);
				for(String lineInFile: newFileAsListOfStrings) {
					if(lineInFile.endsWith("." + line + ";")) {
						completeImportLine = lineInFile; 		
						break;
					}			
					lineNumber++;
				}	
				importsToAdd.add(new Pair<String, Integer>(completeImportLine, lineNumber));	
			}
		}
		for(String line: toRemove) {
			imports.remove(line);
		}		
	}
	
	
	
	/**
	 * Method returns the next line in which is output is not equal to the file
	 * 
	 * 
	 * @return int
	 */
	private static int getFirstUnequalLineNumber() {
		
		for(int i=methodStart+1;i<newFileAsListOfStrings.size(); i++) {
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
		
		//The stack is used to get the correct bracket to end the loop
		Stack<String> stack = new Stack<String>();		
		currentPos = currentMethod.getCurrentLineNumber();
		stack.push("{");
		while(!stack.empty()) {
			currentPos++;
			if(newFileAsListOfStrings.get(currentPos).contains("{")) {
				stack.push("{");
			}			
			if(newFileAsListOfStrings.get(currentPos).contains("}")) {	
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
	private static void firstCallFileInit() {
		//set header, add package if there is one
		for(int line = 0; line<newFileAsListOfStrings.size(); line++) {
			currentOutput.set(line, newFileAsListOfStrings.get(line));
			if(newFileAsListOfStrings.get(line).startsWith("import") || newFileAsListOfStrings.get(line).contains("class")) {
				currentOutput.set(line, "");
				break;
			}
			if(newFileAsListOfStrings.get(line).startsWith("package") ) {
				break;
			}
			
		}
		//tuple of the class declaration as String and its line number	
		Pair<String, Integer> declaration = declaration();
		currentOutput.set((int)declaration.getValue1(), (String)declaration.getValue0());	
		int commentLine = declaration.getValue1()-1;
		while(newFileAsListOfStrings.get(commentLine).contains("//") || newFileAsListOfStrings.get(commentLine).contains("*")) {
			currentOutput.set(commentLine,newFileAsListOfStrings.get(commentLine));
			commentLine--;
		}
		
		
		//set the bracket at the end of the file
		currentOutput.set(newFileAsListOfStrings.size()-1, "}");
		
			
		
	}
	
	
	/**
	 * Method to get the String of the class declaration and the line number of it
	 * 
	 * 
	 * @return Pair<String, Integer>
	 */
	private static Pair<String, Integer> declaration() {
		classStart = 0;
		String name = new String();
		String decLine = new String();
		//get the name of the class
		String[] path = diffEntry.getNewPath().split("/");		
		for(String s: path) {
			if(s.endsWith(".java")) {				
				name = s.substring(0, s.length()-5);
			}
		}		
		
		//search for the declaration in file
		for(String line: newFileAsListOfStrings) {
			if(line.contains("class " + name) || line.contains("interface " + name) || line.contains("abstract class " + name)) {
				decLine = line;				
				break;
			}		
			classStart++;
		}
		
		methodStart = classStart;
		for(int i=classStart+1;i<newFileAsListOfStrings.size();i++) {
			if(newFileAsListOfStrings.get(i).contains("{")) {
				return new Pair<String, Integer>(decLine, classStart);
			}
			methodStart++;
		}
		
		return new Pair<String, Integer>(decLine, classStart);
		
		
		
	}
}