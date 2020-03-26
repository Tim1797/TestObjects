package examplePackage;

/**
 * Modification
 * 
 * @author Tim Hartmann
 *
 */
public class ModifiedClass {

	static int fib(int n) { 
		if (n <= 1) {
			return n; 
		}			
		//return the number
		return fib(n-1) + fib(n-2); 
    } 
       
    public static void main (String args[]) { 
    	int n = 7; 
    	System.out.println(fib(n)); 
    } 
    
}
