package examplePackage;

public class ModifiedClass {

	static int fib(int n) { 
		if (n <= 2) {
			return n; 
		}			
		return fib(n-1) + fib(n-2); 
    } 
       
    public static void main (String args[]) { 
    	int n = 10; 
    	System.out.println(fib(n)); 
    } 
    
}
