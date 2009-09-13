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

import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.d3web.textParser.KWikiToKnofficePreParser;

/**
 * Diese Klasse kapselt die Einstellungen des Parsers zur Einschrï¿½nkung der Grammatik.
 * Dabei werden die Einstellungen aus einer Konfigurationsdatei eingelesen.
 *
 * @author Christian Braun
 * @version 1.021
 * @since JDK 1.5
 */
public class ComplexRuleConfigReader extends ComplexRuleConfiguration {


// Konstanten:
// ===========

	/** Signalisiert, dass Bedingungen in konjunktiver Normalform vorliegen mï¿½ssen. */
	public static final int KNF = -1;

	/** Signalisiert, dass Bedingungen in disjunktiver Normalform vorliegen mï¿½ssen. */
	public static final int DNF = -2;

	/** Signalisiert, dass Bedingungen in beliebiger Stufe vorliegen kï¿½nnen. */
	public static final int UNBEGRENZT = -3;





// Attribute:
// ==========

	/** Enthï¿½llt die erlaubten Logik-Verknï¿½pfungs-Operatoren */
	public final String[] erlaubteOperatoren;

	/** Gibt an, ob "nicht" erlaubt ist */
	public final boolean nichtErlaubt;

	/** Gibt an, ob "nicht" nur vor Literalen erlaubt ist */
	public final boolean nichtNurVorLiteralen;

	/** Gibt die Mamimale Stufe von Bedingungen an */
	public final int maxStufe;

	



	/**
	 * Erzeugt einen neuen ParserKonfig und liest die Einstellungen
	 * aus der ï¿½bergebenen Konfigurationsdatei ein.
	 *
	 * @param configdatei Die Konfigurationsdatei, die eingelesen werden soll
	 */
	public ComplexRuleConfigReader makeComplexRuleConfigReader(String configdatei) {
		
		URL config = null;
		try {
			
			config = new URL(configdatei);
		} catch (Exception e) {
			// [TODO]: handle exception
		}
		return new ComplexRuleConfigReader(config);
		
	}



	/**
	 * Erzeugt einen neuen ParserKonfig und liest die Einstellungen
	 * aus der ï¿½bergebenen Konfigurationsdatei ein.
	 *
	 * @param configdatei Die Konfigurationsdatei, die eingelesen werden soll
	 */
	public ComplexRuleConfigReader(URL configdatei) {

		this(KWikiToKnofficePreParser.urlToReader(configdatei));
	}
	
	public ComplexRuleConfigReader(Reader configdatei) {

		// Config-Datei einlesen
	    List<String> eingabe = new ArrayList<String>(0);
		try {
			eingabe = readFile(configdatei, KOMMENTAR);
		}
		catch (Exception e) {}


		// Default-Werte:
		String[] erlaubteOperatoren = new String[] {UND, ODER};
		boolean nichtErlaubt = true;
		boolean nichtNurVorLiteralen = false;
		int maxStufe = UNBEGRENZT;


		// Eingabe lesen
		Iterator<String> it = eingabe.iterator();
		while (it.hasNext()) {
			String zeile = it.next().toString();
			zeile = removeBlanks(zeile).toLowerCase();
			int index = zeile.indexOf("=");
			if (index > -1) {
				String option = zeile.substring(0, index);
				String wert = zeile.substring(index+1);

				// Zeile auslesen
				if (option.equals("operatoren")) {
					if (wert.equals("und")) {
						erlaubteOperatoren = new String[] {UND};
					}
					else if (wert.equals("oder")) {
						erlaubteOperatoren = new String[] {ODER};
					}
					else if (wert.equals("beides")) {
						erlaubteOperatoren = new String[] {UND, ODER};
					}
					else if (wert.equals("nichts")) {
						erlaubteOperatoren = new String[0];
					}
				}
				else if (option.equals("nicht")) {
					if (wert.equals("immer")) {
						nichtErlaubt = true;
						nichtNurVorLiteralen = false;
					}
					else if (wert.equals("literale")) {
						nichtErlaubt = true;
						nichtNurVorLiteralen = true;
					}
					else if (wert.equals("nie")) {
						nichtErlaubt = false;
						nichtNurVorLiteralen = false;
					}
				}
				else if (option.equals("maxstufe")) {
					if (wert.equals("unbegrenzt")) {
						maxStufe = UNBEGRENZT;
					}
					else if (wert.equals("knf")) {
						maxStufe = KNF;
					}
					else if (wert.equals("dnf")) {
						maxStufe = DNF;
					} else {
						try {
							int i = Integer.parseInt(wert);
							if (i >= 0) maxStufe = i;
						}
						catch (NumberFormatException e) {}
					}
				}

			}
		}


		// Werte endgï¿½ltig setzen
		this.erlaubteOperatoren = erlaubteOperatoren;
		this.nichtErlaubt = nichtErlaubt;
		this.nichtNurVorLiteralen = nichtNurVorLiteralen;
		this.maxStufe = maxStufe;
	}



/*
Die Konfigurationsdatei muss dabei in folgendem Format vorliegen:



// Konfigurationsdatei fï¿½r den RegelParser
// =======================================
//
// Hier kï¿½nnen Sie einige Einschrï¿½nkungen der Grammatik vornehmen,
// indem Sie die entsprechenden Werte ï¿½ndern.
// Dabei spielt Gross- und Kleinschreibung keine Rolle.
// Bei fehlenden oder fehlerhaften Einstellungen wird der Standartwert genommen.
//



// Gibt an, welche logischen Verknï¿½pfungs-Operatoren in Bedingungen erlaubt sind.
// (Standart: beides)
//
// Mï¿½gliche Werte:
//   und:      Nur "und" ist als Verknï¿½pfung erlaubt
//   oder:     Nur "oder" ist als Verknï¿½pfung erlaubt
//   beides:   Sowohl "und" als auch "oder" ist als  Verknï¿½pfung erlaubt
//   nichts:   Es sind keine Verknï¿½pfungen erlaubt
//
Operatoren = beides



// Gibt an, wo "nicht" vorkommen darf.
// (Standart: immer)
//
// Mï¿½gliche Werte:
//   immer:      "nicht" darf ï¿½berall stehen
//   literale:   "nicht" darf nur vor Literalen stehen (also nicht vor Klammern)
//   nie:        "nicht" darf nicht vorkommen
//
Nicht = immer



// Gibt an, wie hoch die maximale Stufe der Bedingungen sein dï¿½rfen.
//
// Beispiele:
//   0-stufig:   A   ;   nicht A   ;   B
//   1-stufig:   A oder B   ;   A und B und C
//   2-Stufig:   (A und B) oder C   ;   (A und B) oder (C und D)
//   3-stufig:   A und B oder C und D  <==>  ((A und B) oder C) und D
//   KNF:        (A oder B oder C) und D und (E oder F)
//   DNF:        (A und B und C) oder D oder (E und F)
//
// Hinweis: Die Minmax-Funktion ist einstufig:
//   1-stufig:   minmax(x;y) aus {A; B; C; D; ...}
//
// (Standart: 0)
//
// Mï¿½gliche Werte:
//   unbegrenzt:   Die Stufe ist unbegrenzt
//   Zahl >= 0:    Gibt die entsprechende maximale Stufe an
//   KNF:          Die Bedingungen mï¿½ssen in konjunktiver Normalform vorliegen
//   DNF:          Die Bedingungen mï¿½ssen in disjunktiver Normalform vorliegen
//
MaxStufe = unbegrenzt

*/

}
