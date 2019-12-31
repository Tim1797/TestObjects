package logic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * CollectionUtil bietet Methoden zur Umwandlung und Behandlung von
 * Collection-Typen an.
 *
 * @autor Gernot Segieth
 * URL: https://wiki.byte-welt.net/wiki/Doppelte_Datens%C3%A4tze_aus_ArrayList_entfernen
 */
public class CollectionUtil {

   /**
    * Entfernt doppelte Einträge aus der übergebenen Liste.
    *
    * @param list eine Liste mit doppelten Einträgen
    * @return die um doppelte Einträge bereinigte Liste
    */
   public static List<?> removeDuplicate(List<?> list) {
      Set<?> set = transformListIntoSet(list);
      return transformSetIntoList(set);
   }

   /**
    * Wandelt eine List in ein Set um. Die Reihenfolge der Objekte
    * in der Liste bleibt dabei auch im Set erhalten.
    *
    * @param list die Liste, die in ein Set verwandelt werden soll
    * @return der Ergebnis-Typ, ein Set
    */
   public static Set<?> transformListIntoSet(List<?> list) {
      Set<Object> set = new LinkedHashSet<>();
      set.addAll(list);
      return set;
   }

   /**
    * Wandelt ein Set in eine List um.
    *
    * @param set das Set, das in eine List verwandelt werden soll
    * @return der Ergebnis-Typ, eine List
    */
   public static List<?> transformSetIntoList(Set<?> set) {
      List<Object> list = new ArrayList<>();
      list.addAll(set);
      return list;
   }
}
