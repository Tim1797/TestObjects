import org.junit.Test;


public class KundenTest {
	
	@Test
	public void testKauf() {
		Kunde k = new Kunde();
		Produkt p = new Produkt();
		k.kaufProdukt(p);
		Kunde k2 = new Kunde();
		k2.kaufProdukt(p);		
	}
	
}
