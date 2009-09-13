/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.textParser.complexRule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.d3web.persistence.utilities.URLUtils;




/**
 * Diese Klasse stellt einige Utility-Funktionen zur Verfï¿½gung,
 * die von mehreren Klassen benutzt werden kï¿½nnen.
 *
 * @author Christian Braun
 * @version 1.021
 * @since JDK 1.5
 */
public class Utils {

// Utility-Methoden:
// =================


	/**
	 * ï¿½berprï¿½ft, ob der ï¿½bergebene String null oder leer ist.
	 * (EOS: End of String)
	 *
	 * @param string Der String, der ï¿½berprï¿½ft werden soll
	 * @return true, falls der String null oder leer ist
	 */
	public static boolean eos(String string) {
		return (string == null) || string.equals("");
	}



	/**
	 * Vergleicht zwei Strings, ob sie ungefï¿½hr gleich sind,
	 * d.h. ein Vergleich ohne Berï¿½cksichtigung von Gross- und Kleinschreibung,
	 * Leerzeichen oder Sonderzeichen.
	 *
	 * @param s1 Der String, der verglichen werden soll
	 * @param s2 Der String, mit dem verglichen werden soll
	 * @return true, falls s1 ungefï¿½hr gleich ist mit s2
	 */
	public static boolean equals(String s1, String s2) {
		if(eos(s1) && eos(s2)) return true;
		if(eos(s1) || eos(s2)) return false;
		s1 = make(s1, ComplexRuleConfiguration.NORMALZEICHEN);
		s2 = make(s2, ComplexRuleConfiguration.NORMALZEICHEN);
		return s1.equalsIgnoreCase(s2);
	}



	/**
	 * ï¿½berprï¿½ft, ob der ï¿½bergebene String ungefï¿½hr mit dem ï¿½bergebenen Prï¿½fix beginnt,
	 * d.h. ein Vergleich ohne Berï¿½cksichtigung von Gross- und Kleinschreibung,
	 * Leerzeichen oder Sonderzeichen.
	 *
	 * @param string Der String, der ï¿½berprï¿½ft werden soll
	 * @param prï¿½fix Das Prï¿½fix, auf das geprï¿½ft werden soll
	 * @return true, falls string ungefï¿½hr mit prï¿½fix beginnt
	 */
	public static boolean startsWith(String string, String praefix) {
		if(eos(praefix)) return true;
		if(eos(string)) return false;
		string = removeBlanks(string).toLowerCase();
		praefix = removeBlanks(praefix).toLowerCase();
		return string.startsWith(praefix);
	}



	/**
	 * ï¿½berprï¿½ft, ob der ï¿½bergebene String ungefï¿½hr mit dem ï¿½bergebenen Suffix endet,
	 * d.h. ein Vergleich ohne Berï¿½cksichtigung von Gross- und Kleinschreibung,
	 * Leerzeichen oder Sonderzeichen.
	 *
	 * @param string Der String, der ï¿½berprï¿½ft werden soll
	 * @param suffix Das Suffix, auf das geprï¿½ft werden soll
	 * @return true, falls string ungefï¿½hr mit suffix endet
	 */
	public static boolean endsWith(String string, String suffix) {
		if(eos(suffix)) return true;
		if(eos(string)) return false;
		string = removeBlanks(string).toLowerCase();
		suffix = removeBlanks(suffix).toLowerCase();
		return string.endsWith(suffix);
	}



	/**
	 * Entfernt Leerzeichen, Tabulatoren ('\t'), '\f', '\n', '\r' aus einem String.
	 *
	 * @param string Der String, von dem Leerzeichen entfernt werden sollen
	 * @return ï¿½bergebener String ohne Leerzeichen
	 */
	public static String removeBlanks(String string) {
		if (string == null) return null;
		StringBuffer sb = new StringBuffer(string);
		for (int i = 0; i < sb.length(); i++) {
			switch (sb.charAt(i)) {
				case ' ': case '\t': case '\f': case '\n': case '\r':
				sb.deleteCharAt(i--);
			}
		}
		return sb.toString();
	}



	/**
	 * Ersetzt mehrere aufeinanderfolgende Leerzeichen, Tabulatoren ('\t'), '\f', '\n', '\r'
	 * in einem String durch ein einziges Leerzeichen.
	 *
	 * @param string Der String, von dem Leerzeichen normalisiert werden sollen
	 * @return ï¿½bergebener String mit normalisierten Leerzeichen
	 */
	public static String normalizeBlanks(String string) {
		if (string == null) return null;
		boolean blank = false;
		StringBuffer sb = new StringBuffer(string);
		for (int i = 0; i < sb.length(); i++) {
			switch (sb.charAt(i)) {
				case ' ': case '\t': case '\f': case '\n': case '\r':
				if (blank) sb.deleteCharAt(i--);
				else {
					blank = true;
					sb.setCharAt(i, ' ');
				}
				break;

				default:
				blank = false;
			}
		}
		return sb.toString();
	}





	/**
	 * Sortiert das ï¿½bergebene String-Array anhand der Lï¿½nge der Strings,
	 * wobei lï¿½ngere Strings vor kï¿½rzere gestellt werden.
	 *
	 * @param array Das zu sortierende Array
	 * @return Das ï¿½bergebene Array
	 */
	public static String[] sortLength(String[] array) {
		if (array == null) return array;
		try {
			Arrays.sort(array, new Comparator() {
				public int compare(Object o1, Object o2) {
					String s1 = (String) o1;
					String s2 = (String) o2;
					int result = s2.length() - s1.length();
					if (result == 0) result = s1.compareTo(s2);
					return result;
				}
			});
		} catch (Exception e) {}
		return array;
	}



	/**
	 * Fï¿½gt zwei Strings zu einem String-Array zusammen.
	 *
	 * @param s1 Der erste String
	 * @param s2 Der zweite String
	 * @return Ein Array, das die Strings s1 und s2 enthï¿½llt
	 */
	public static String[] join(String s1, String s2) {
		if (s1 == null && s2 == null) return new String[0];
		if (s1 == null) return new String[]{s2};
		if (s2 == null) return new String[]{s1};
		String[] array = new String[2];
		array[0] = s1;
		array[1] = s2;
		return array;
	}



	/**
	 * Fï¿½gt einen String einem String-Array hinzu.
	 * Die Lï¿½nge des neuen Arrays ist dabei Lï¿½nge(array) + 1.
	 *
	 * @param array Das Array
	 * @param string String, der hinzugefï¿½gt werden soll
	 * @return Das neue Array, das die Elemente aus array und den String enthï¿½llt
	 */
	public static String[] join(String[] array, String string) {
		if (array == null && string == null) return new String[0];
		if (array == null) return new String[]{string};
		if (string == null) return array;
		String[] a = new String[array.length + 1];
		for (int i = 0; i < array.length; i++) a[i] = array[i];
		a[array.length] = string;
		return a;
	}



	/**
	 * Fï¿½gt einen String am Anfang einem String-Array hinzu.
	 * Die Lï¿½nge des neuen Arrays ist dabei Lï¿½nge(array) + 1.
	 *
	 * @param array Das Array
	 * @param string String, der am Anfang hinzugefï¿½gt werden soll
	 * @return Das neue Array, das die Elemente aus array und den String am Anfang enthï¿½llt
	 */
	public static String[] join(String string, String[] array) {
		if (array == null && string == null) return new String[0];
		if (array == null) return new String[]{string};
		if (string == null) return array;
		String[] a = new String[array.length + 1];
		a[0] = string;
		for (int i = 0; i < array.length; i++) a[i+1] = array[i];
		return a;
	}



	/**
	 * Fï¿½gt zwei String-Arrays zu einem String-Array zusammen.
	 * Die Lï¿½nge des neuen Arrays ist dabei Lï¿½nge(a1) + Lï¿½nge(a2).
	 *
	 * @param a1 Das erste Array
	 * @param a2 Das zweite Array
	 * @return Das neue Array, das die Elemente aus a1 und a2 enthï¿½llt
	 */
	public static String[] join(String[] a1, String[] a2) {
		if (a1 == null && a2 == null) return new String[0];
		if (a1 == null) return a2;
		if (a2 == null) return a1;
		String[] a = new String[a1.length + a2.length];
		int i;
		for (i = 0; i < a1.length; i++) a[i] = a1[i];
		for (int j = 0; j < a2.length; j++) a[i++] = a2[j];
		return a;
	}





	/**
	 * ï¿½berprï¿½ft, ob der als 1. Parameter ï¿½bergebene Ausdruck nur aus Zeichen
	 * besteht, die im als 2. Parameter ï¿½bergebene Ausdruck enthalten sind.
	 *
	 * @param ausdruck Der zu ï¿½berprï¿½fende Ausdruck
	 * @param zeichen Die gï¿½ltigen Zeichen
	 * @return Ein Array mit aufsteigend sortierten Indizes, an denen der Ausdruck
	 *         ungï¿½ltige Zeichen enthï¿½lt.
	 *         Ist der Ausdruck null oder leer, so wird ein Array mit 0 Elementen zurï¿½ckgegeben.
	 *         Ist der Ausdruck gï¿½ltig, wird null zurï¿½ckgegeben.
	 */
	public static int[] is(String ausdruck, String zeichen) {
		if (eos(ausdruck) || zeichen == null) return new int[0];

		// Ungï¿½ltige Zeichen suchen
		ArrayList a = new ArrayList();
		for (int i = 0; i < ausdruck.length(); i++) {
			if (zeichen.indexOf(ausdruck.charAt(i)) < 0) a.add(new Integer(i));
		}

		// Kein ungï¿½ltiges Zeichen gefunden?
		if (a.size() < 1) return null;

		// ArrayList in int[] umwandeln
		int[] indizes = new int[a.size()];
		for (int i = 0; i < a.size(); i++) {
			indizes[i] = ((Integer)a.get(i)).intValue();
		}
		return indizes;
	}



	/**
	 * ï¿½berprï¿½ft, ob der als 1. Parameter ï¿½bergebene Ausdruck einer
	 * der als 2. Parameter ï¿½bergebenen Strings ist.
	 *
	 * @param ausdruck Der zu durchsuchende Ausdruck
	 * @param zeichen Die Strings, von denen ï¿½berprï¿½ft werden soll, ob der
	 * Ausdruck einer davon ist, in einem Array zusammengefasst.
	 * @return Der Ausdruck, falls er in dem Array vorhanden ist, "" sonst.
	 */
	public static String is(String ausdruck, String[] zeichen) {
		if (eos(ausdruck) || zeichen == null) return "";

		for (int i = 0; i < zeichen.length; i++) {
			if (ausdruck.equalsIgnoreCase(zeichen[i])) return zeichen[i];
		}
		return "";
	}



	/**
	 * Liest aus dem als 1. Parameter ï¿½bergebenen Ausdruck so lange Zeichen,
	 * bis ein Zeichen auftritt, das nicht im als 2. Parameter ï¿½bergebene Ausdruck
	 * enthalten ist oder das Ende des 1. Ausdrucks erreicht ist.
	 *
	 * @param ausdruck Ausdruck, aus dem gelesen werden soll
	 * @param zeichen Die gï¿½ltigen Zeichen
	 * @return Ein String mit den gelesenen Zeichen. Ist bereits das erste Zeichen
	 * ungï¿½ltig oder einer der Strings null oder leer, wird "" zurï¿½ckgegeben.
	 */
	public static String read(String ausdruck, String zeichen) {
		if (eos(ausdruck) || eos(zeichen)) return "";

		// gï¿½ltige Zeichen lesen
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ausdruck.length(); i++) {
			if (zeichen.indexOf(ausdruck.charAt(i)) < 0) break;
			sb.append(ausdruck.charAt(i));
		}

		return sb.toString();
	}



	/**
	 * ï¿½berprï¿½ft, ob der als 1. Parameter ï¿½bergebene Ausdruck mit einem
	 * der als 2. Parameter ï¿½bergebenen Strings beginnt.
	 * Das Array muss dabei nach Lï¿½nge sortiert sein,
	 * falls ein Symbol Prï¿½fix eines anderen ist.
	 *
	 * @param ausdruck Der zu durchsuchende Ausdruck
	 * @param zeichen Die Strings, von denen ï¿½berprï¿½ft werden soll, ob der
	 * Ausdruck damit beginnt, in einem Array zusammengefasst.
	 * @return Der erste String aus dem Array, der Prï¿½fix des Ausdrucks ist.
	 * Ist keines der Strings aus dem Array ein Prï¿½fix des Ausdrucks,
	 * wird "" zurï¿½ckgegeben.
	 */
	public static String read(String ausdruck, String[] zeichen) {
		if (ausdruck == null || zeichen == null) return "";

		for (int i = 0; i < zeichen.length; i++) {
			if (ausdruck.startsWith(zeichen[i])) return zeichen[i];
		}
		return "";
	}



	/**
	 * Liest aus dem als 1. Parameter ï¿½bergebenen Ausdruck so lange Zeichen,
	 * bis ein Zeichen auftritt, das im als 2. Parameter ï¿½bergebene Ausdruck
	 * enthalten ist oder das Ende des 1. Ausdrucks erreicht ist.
	 *
	 * @param ausdruck Ausdruck, aus dem gelesen werden soll
	 * @param zeichen Die gï¿½ltigen Trennzeichen
	 * @return Ein String mit den gelesenen Zeichen. Ist bereits das erste Zeichen
	 * ein Trennzeichen oder ausdruck null oder leer, wird "" zurï¿½ckgegeben.
	 */
	public static String readUntil(String ausdruck, String zeichen) {
		if (eos(ausdruck)) return "";
		if (eos(zeichen)) return ausdruck;

		// Zeichen lesen bis zu einem Trennzeichen
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ausdruck.length(); i++) {
			if (zeichen.indexOf(ausdruck.charAt(i)) < 0) sb.append(ausdruck.charAt(i));
			else break;
		}

		return sb.toString();
	}



	/**
	 * Entfernt aus dem String ausdruck alle Zeichen, die in dem String zeichen vorkommen.
	 *
	 * @param ausdruck Ausdruck, aus dem Zeichen entfernt werden sollen
	 * @param zeichen Die Zeichen, die aus ausdruck entfernt werden sollen
	 * @return ï¿½bergebener Ausdruck ohne Zeichen, die in dem String zeichen vorkommen
	 */
	public static String remove(String ausdruck, String zeichen) {
		if (eos(ausdruck)) return "";
		if (eos(zeichen)) return ausdruck;

		// Zu lï¿½schende Zeichen suchen
		StringBuffer sb = new StringBuffer(ausdruck);
		for (int i = 0; i < sb.length(); i++) {
			if (zeichen.indexOf(sb.charAt(i)) >= 0) sb.deleteCharAt(i--);
		}
		return sb.toString();
	}



	/**
	 * Entfernt aus dem String ausdruck alle Zeichen, die in dem String zeichen nicht vorkommen.
	 *
	 * @param ausdruck Ausdruck, aus dem Zeichen entfernt werden sollen
	 * @param zeichen Die Zeichen, die in ausdruck nicht entfernt werden sollen
	 * @return ï¿½bergebener Ausdruck nur mit Zeichen, die in dem String zeichen vorkommen
	 */
	public static String make(String ausdruck, String zeichen) {
		if (eos(ausdruck)) return "";
		if (eos(zeichen)) return ausdruck;

		// Zu lï¿½schende Zeichen suchen
		StringBuffer sb = new StringBuffer(ausdruck);
		for (int i = 0; i < sb.length(); i++) {
			if (zeichen.indexOf(sb.charAt(i)) < 0) sb.deleteCharAt(i--);
		}
		return sb.toString();
	}



	/**
	 * Liefert ein aufsteigend sortiertes Array mit den Indizes der Zeichen aus einem String,
	 * die durch die normalizeBlanks-Methode entfernt werden wï¿½rden.
	 *
	 * @param string Der String, von dem die Indizes geliefert werden sollen
	 * @return Array mit den Indizes der Zeichen, die durch die normalizeBlanks-Methode
	 *         entfernt werden wï¿½rden.
	 */
	public int[] getRemoveIndizes(String string) {
		if (string == null) return new int[0];

		// Zu lï¿½schende Zeichen suchen
		ArrayList a = new ArrayList();
		boolean blank = false;
		for (int i = 0; i < string.length(); i++) {
			switch (string.charAt(i)) {
				case ' ': case '\t': case '\f': case '\n': case '\r':
				if (blank) a.add(new Integer(i));
				else {
					blank = true;
				}
				break;

				default:
				blank = false;
			}
		}

		// ArrayList in int[] umwandeln
		int[] indizes = new int[a.size()];
		for (int i = 0; i < a.size(); i++) {
			indizes[i] = ((Integer)a.get(i)).intValue();
		}
		return indizes;
	}





	/**
	 * Liest die einzelnen Zeilen einer (Text)datei in eine String-ArrayList ein.
	 *
	 * @param datei Die einzulesene Datei
	 * @return Vector mit den Inhalt der (Text)datei, in Zeilen aufgeteilt
	 * @throws IOException falls beim Einlesen ein Fehler auftritt
	 */
	public static ArrayList<String> readFile(URL url) throws IOException {
	    ArrayList<String> liste = new ArrayList<String>();
	    InputStream in = null;
	    try {
	    	in = URLUtils.openStream(url);
		} catch (Exception e) {
			// [TODO]: handle exception
		}
		in = URLUtils.openStream(url);

		
		BufferedReader lnr = new BufferedReader(new InputStreamReader(in));
		//LineNumberReader lnr = new LineNumberReader(new FileReader(datei));
		String zeile = lnr.readLine();
		while (zeile != null) {
			liste.add(zeile);
			zeile = lnr.readLine();
		}
		return liste;
	}
	
	
	public static ArrayList readFile(Reader r) throws IOException {
	    ArrayList liste = new ArrayList();
		
		BufferedReader lnr = new BufferedReader(r);
		//LineNumberReader lnr = new LineNumberReader(new FileReader(datei));
		String zeile = lnr.readLine();
		while (zeile != null) {
			liste.add(zeile);
			zeile = lnr.readLine();
		}
		return liste;
	}



	/**
	 * Liest die einzelnen Zeilen einer (Text)datei in einen String-Vector ein.
	 * Dabei werden gleich einzeilige Kommentare entfernt.
	 *
	 * @param datei Die einzulesene Datei
	 * @param split Der Kommentar-Einleite-String (regular expression)
	 * @return Vector mit den Inhalt der (Text)datei, in Zeilen aufgeteilt
	 * @throws IOException falls beim Einlesen ein Fehler auftritt
	 */
	public static ArrayList readFile(URL url, String split) throws IOException {
	    ArrayList liste = new ArrayList();
	    InputStream in = null;
	    try {
	    	in = URLUtils.openStream(url);
		} catch (Exception e) {
			// [TODO]: handle exception
		}
		in = URLUtils.openStream(url);

		
		BufferedReader lnr = new BufferedReader(new InputStreamReader(in));
		//LineNumberReader lnr = new LineNumberReader(new FileReader(datei));
		String zeile = lnr.readLine();
		while (zeile != null) {
			zeile = zeile.split(split, -1)[0];
			liste.add(zeile);
			zeile = lnr.readLine();
		}
		return liste;
	}
	
	public static byte[] readBytes(Reader r) {
		int zeichen = 0;
		List bytes = new LinkedList();
		while (true) {

			try {
				zeichen = r.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (zeichen == -1)
				break;
			Byte b = new Byte((byte) zeichen);
			bytes.add(b);
		}

		Object[] o = bytes.toArray();
		byte[] byteArray = new byte[o.length];

		for (int i = 0; i < o.length; i++) {
			byteArray[i] = ((Byte) o[i]).byteValue();

		}
		return byteArray;
	}
	
//	public static String readFileToString(URL url) {
//		List list = null;
//		try {
//			list = readFile(url);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String res = "";
//		if(list != null) {
//			for (Object object : list) {
//				String s = ((String)object);
//				res += s+"\n";
//			}
//		}
//		return res;
//	}
	
	public static List readFile(Reader in, String split) throws IOException {
		List liste = new ArrayList();

		
		BufferedReader lnr = new BufferedReader(in);
		String zeile = lnr.readLine();
		while (zeile != null) {
			zeile = zeile.split(split, -1)[0];
			liste.add(zeile);
			zeile = lnr.readLine();
		}
		return liste;
	}
	
	public static List readFileAll(Reader in) throws IOException {
		List liste = new ArrayList();

		
		BufferedReader lnr = new BufferedReader(in);
		String zeile = lnr.readLine();
		while (zeile != null) {
			liste.add(zeile);
			zeile = lnr.readLine();
		}
		return liste;
	}



	/**
	 * Schreibt einen String-Vector in eine (Text)datei.
	 *
	 * @param datei Die (Text)datei in die geschrieben werden soll
	 * @param inhalt Ein Vector mit dem Inhalt, der in die (Text)datei geschrieben werden soll
	 * @throws IOException falls beim Schreiben ein Fehler auftritt
	 */
	public static void writeFile(File datei, ArrayList inhalt) throws IOException {
		FileWriter writer = new FileWriter(datei);
		for (int i = 0; i < inhalt.size(); i++) {
			writer.write(inhalt.get(i).toString());
			writer.write("\n");
		}
		writer.close();
	}



	/**
	 * Schreibt einen String in eine (Text)datei.
	 *
	 * @param datei Die (Text)datei in die geschrieben werden soll
	 * @param inhalt Der Inhalt, der in die (Text)datei geschrieben werden soll
	 * @throws IOException falls beim Schreiben ein Fehler auftritt
	 */
	public static void writeFile(File datei, String inhalt) throws IOException {
		FileWriter writer = new FileWriter(datei);
		writer.write(inhalt);
		writer.close();
	}







// Test- und Debug-Methoden:
// =========================


	/**
	 * Gibt ein int-Array auf der Konsole aus.
	 *
	 * @param array Das Array, das ausgegeben werden soll
	 */
	public static void print(int[] array) {
		if (array == null) {
			System.out.println(array);
			return;
		}

		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			if (i != 0) System.out.print(", ");
			System.out.print(array[i]);
		}
		System.out.println("]");
	}



	/**
	 * Gibt ein Array auf der Konsole aus.
	 *
	 * @param array Das Array, das ausgegeben werden soll
	 */
	public static void print(Object[] array) {
		if (array == null) {
			System.out.println(array);
			return;
		}

		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			if (i != 0) System.out.print(", ");
			System.out.print("\"" + array[i] + "\"");
		}
		System.out.println("]");
	}



	/**
	 * Gibt eine Leerzeile aus.
	 */
	public static void print() {
		System.out.println();
	}



	/**
	 * Gibt ein Objekt auf der Konsole aus.
	 *
	 * @param object Das Objekt, das ausgegeben werden soll
	 */
	public static void print(Object object) {
		System.out.println(object);
	}



	/**
	 * Gibt ein Objekt auf der Konsole aus, ohne anschliessendes "\n".
	 *
	 * @param object Das Objekt, das ausgegeben werden soll
	 */
	public static void printn(Object object) {
		System.out.print(object);
	}

}
