
public class Klasse {
	
	private int zahl;
	
	private static void ersteMethode() {
		dritteMethode(16);
		int was = 10;
		vierteMethode(9);
		
	}
	
	private static void zweiteMethode() {
		int z = 10;
	}
	
	private static void dritteMethode(int zahl) {
		int i = 13;
		zahl = 10;	
		zweiteMethode();
	}
	
	private static void vierteMethode(int zahl) {
		int i = 13;
		zahl = 10;		
	}
	
}



