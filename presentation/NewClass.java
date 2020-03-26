package examplePackage;

import java.util.ArrayList;

/**
 * New class
 * 
 * @author Tim Hartmann
 *
 */
public class NewClass {
	
	private int value = 8;

	/**
	 * This is the first method that calls two other methods
	 * 
	 */
	public void method1() {
		method5();		
		//use of class variable
		int i = value;
		int j = 15;
		//Add two numbers
		int erg = i + j;
		//print out the solution
		System.out.println(erg);
		method3();
	}
	
	/**
	 * This is the second method
	 * 
	 */
	public void method2() {
		//use of class variable
		int x = 0;
		while(x<10) {
			x++;
		}		
	}
	
	/**
	 * This is the third method
	 * 
	 */
	public void method3() {	
		//use the string of the class
		String toPrint = "This is a String";
		System.out.println(toPrint);
	}
	
	/**
	 * This is the fourth method
	 * 
	 */
	public void method4() {
		int[][] arr = new int[10][10]; 
		for(int i=0;i<10;i++) {
			for(int j=0;j<10;j++) {
				arr[i][j] = i+j;
			}
		}
	}
	
	/**
	 * This is the fifth method
	 * 
	 */
	public void method5() {
		ArrayList<Integer> myList = new ArrayList<>(3);
	}
	

}
