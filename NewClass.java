/**
 * Copyright 2020 Uni Osnabrueck
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package examplePackage;

/**
 * New class
 * 
 * @author Tim Hartmann
 *
 */
public class NewClass {
	
	//class variables
	private int value = 8;
	private int value2;
	private String str;
	
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
		value2 = 0;
		while(value2<10) {
			value2++;
		}
		
	}
	
	/**
	 * This is the third method
	 * 
	 */
	public void method3() {
		String s = "Dritte Methode";
		//use the string of the class
		System.out.println(s + str);
	}
	
	/**
	 * This is the fourth method
	 * 
	 */
	public void method4() {
		int s = 17;
	}
	
	/**
	 * This is the fifth method
	 * 
	 */
	public void method5() {
		int j = 13;
	}
	

}
