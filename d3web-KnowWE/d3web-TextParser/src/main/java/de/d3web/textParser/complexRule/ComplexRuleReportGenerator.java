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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.Utils.AnswerNotInKBError;
import de.d3web.textParser.Utils.ConceptNotInKBError;
import de.d3web.textParser.Utils.QuestionNotInKBError;
import de.d3web.textParser.decisionTable.MessageGenerator;

/**
 * Diese Klasse erzeugt diverse (Fehler-)Meldungen und speichert diese
 * in einem Report. Diese Klasse diehnt konkret zur Erzeugung von
 * FehlerMeldungen, die beim Parsen vom RegelParser erzeugt werden.
 *
 * @author Christian Braun
 * @version 1.025
 * @since JDK 1.5
 */
public final class ComplexRuleReportGenerator extends ComplexRuleConfiguration {


//	 Konstanten:
//	 ===========

		/**
		 * Zeichen, mit dem Fehler markiert werden sollen.
		 *
		 *   null --> Fehlerhafte Zeile wird nicht angezeigt
		 *   ""   --> Fehlerhafte Zeile wird angezeigt, Fehler werden aber nicht markiert
		 *   "x"  --> Fehlerhafte Zeile wird angezeigt,
		 *              Fehler werden mit einem x darunter markiert
		 */
		public static final String MARKIERZEICHEN = "^";



// Attribute:
// ==========

	/** Der Report, dem die Meldungen hinzugefï¿½gt werden sollen */
	private Report report;

	/** Speichert die aktuelle Datei */
	private String datei = new String();

	/** Speichert die zu parsende Eingabe */
	private List<String> input = new ArrayList<String>();

	/** Flag, ob die Minmax-Syntax-Meldung schon ausgegeben wurde */
	private boolean minmax = false;





// Konstruktoren:
// ==============

	/**
	 * Erzeugt einen neuen TCReportGenerator mit einem neuen Report.
	 */
	public ComplexRuleReportGenerator() {
		report = new Report();
	}



	/**
	 * Erzeugt einen neuen TCReportGenerator fï¿½r den ï¿½bergebenen Report.
	 *
	 * @param report Der Report, dem die Meldungen hinzugefï¿½gt werden sollen
	 */
	public ComplexRuleReportGenerator(Report report) {
		if (report == null) this.report = new Report();
		else this.report = report;
	}





// Methoden:
// =========


	/**
	 * Liefert den Report mit den ganzen Meldungen.
	 *
	 * @return Report mit den ganzen Meldungen
	 */
	public Report getReport() {
		return report;
	}



	/**
	 * Gibt den gesamten Report als String aus.
	 *
	 * @return Gesamter Report als String
	 */
	public String toString() {
		return report.toString();
	}



	/*
	 * Setzt die ï¿½bergebene Datei als aktuelle Datei.
	 *
	 * @param datei Die aktuelle Datei
	 */
	public void setDatei(String datei) {
		if (datei == null) datei = new String();
		this.datei = datei;
	}



	/*
	 * Setzt die ï¿½bergebene eingelesene Datei als aktuelle eingelesene Datei.
	 *
	 * @param input Die aktuelle eingelesene Datei
	 */
	public void setInput(List<String> input) {
		if (input == null) input = new ArrayList<String>();
		this.input = input;
	}





//	 Markier-Methoden:
//	 =================


	/**
	 * Markiert ein (fehlerhaftes) Zeichen in einer Zeile.
	 *
	 * @param zeile Die Zeile, in der markiert werden soll
	 * @param index Index, an dem die Zeile markiert werden sollen
	 * @return Zeile mit markierten Zeichen
	 */
	private static String mark(String zeile, int index) {
		return mark(zeile, 0, new int[]{index});
	}



	/**
	 * Markiert (fehlerhafte) Zeichen in einer Zeile.
	 *
	 * @param zeile Die Zeile, in der markiert werden soll
	 * @param trimmed Anzahl der Zeichen, die ï¿½bersprungen werden sollen
	 * @param indizes Array mit den Indizes, an denen die Zeile markiert werden soll
	 * @return Zeile mit markierten Zeichen
	 */
	private static String mark(String zeile, int trimmed, int[] indizes) {
		if (MARKIERZEICHEN == null) return "";
		if (MARKIERZEICHEN.equals("")) return zeile;
		if (zeile == null || indizes == null) return zeile;
		if (indizes.length < 1) return zeile;
		zeile += " ";
		try {
			int pos = 0;
			String mark = "\n";
			for (int i = 0; i < indizes.length; i++) {
				for ( ; pos < indizes[i] + trimmed; pos++) {
					switch (zeile.charAt(pos)) {
						case '\t':
							mark += "\t";
							break;
						case '\n':
							mark = "\n";
							break;
						default:
							mark += " ";
					}
				}
				mark += MARKIERZEICHEN;
				pos++;
			}
			return zeile + mark;
		}
		catch (Exception e) {
			return zeile;
		}
	}



	/**
	 * Markiert ein (fehlerhaftes) Token in einer Zeile.
	 *
	 * @param token Das Token, das markiert werden soll
	 * @return Zeile mit markiertem Token
	 */
	private String mark(Token token) {
		try {
			return mark(input.get(token.getZeile()).toString(), token.getSpalte());
		}
		catch (Exception e) {
			return "";
		}
	}



	/**
	 * Markiert hinter einem (fehlerhaften) Token in einer Zeile.
	 *
	 * @param token Das Token, hinter dem markiert werden soll
	 * @return Zeile mit markiertem Token
	 */
	private String markBehind(Token token) {
		if (MARKIERZEICHEN == null) return "";
		String zeile = "", mark = "\n";
		try {
			boolean str = false;
			zeile = input.get(token.getZeile()).toString();
			if (MARKIERZEICHEN.equals("")) return zeile;
			zeile += " ";
			int spalte = token.getSpalte();
			int symbol = spalte + (token.getSymbol().length());

			for (int i = 0; i < spalte; i++) {
				switch (zeile.charAt(i)) {
					case STRING_CHAR:
						str = !str;
						mark += " ";
						break;
					case '\t':
						mark += "\t";
						break;
					case '\n':
						mark = "\n";
						break;
					default:
						mark += " ";
				}
			}

			for (int i = spalte; i < symbol; i++) {
				switch (zeile.charAt(i)) {
					case STRING_CHAR:
						str = !str;
						mark += " ";
						symbol++;
						break;
					case '\t':
						mark += "\t";
						break;
					case '\n':
						mark = "\n";
						break;
					default:
						mark += " ";
				}
			}
			if (zeile.charAt(symbol) == STRING_CHAR && str) mark += " ";
			mark += MARKIERZEICHEN;
			return zeile + mark;
		}
		catch (Exception e) {
			return zeile;
		}

	}





// Spezial-Funktionen:
// ===================

	/*
	 * Die folgenden Methoden erzeugen jeweils neue Meldungen und fï¿½gen diese dem
	 * Report hinzu.
	 */

	
	
	// Meldungen beim Parsen

	public final void KEINE_DATEI() {
		String meldung = "Keine Datei zum Parsen angegeben";
		report.error(new Message(meldung));
	}

	public final void KEINE_WISSENSBASIS() {
		String meldung = "Keine Wissensbasis zum Einfï¿½gen der Regeln angegeben";
		report.error(new Message(meldung));
	}

	public final void IOEXCEPTION(String meldung) {
		meldung = "IO-Fehler: " + meldung;
		report.error(new Message(meldung, datei));
	}

	public final void STRINGENDE_ERWARTET(int zeile) {
		String meldung = "Abschliessendes Anfï¿½hrungszeichen erwartet";
		String temp = mark(input.get(zeile).toString() + " ", input.get(zeile).toString().length());
		report.warning(new Message(meldung, datei, zeile, temp));
	}

	public final void TOKEN(String token, Token t) {
		String meldung = "\"" + token + "\" erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void NOT_TOKEN(Token t) {
		String meldung = "\"" + t.getToken() + "\" ist an dieser Stelle syntaktisch nicht erlaubt";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KEINE_REGEL() {
		String meldung = "Keine gï¿½ltige Regel zum Parsen gefunden";
		report.warning(new Message(meldung, datei));
	}

	public final void REGELN(int added, int cleared, int ignored, int replaced) {
		String meldung = "Es wurden " + added + " Regeln hinzugefï¿½gt";
		if (added == 1) meldung = "Es wurden 1 Regel hinzugefï¿½gt";
		if (added == 1 && cleared < 1 && ignored < 1 && replaced < 1)
			 meldung = "Es wurde 1 Regel hinzugefï¿½gt";
		if (cleared == 1) meldung += ", 1 Regel gelï¿½scht";
		else if (cleared > 1) meldung += ", " + cleared + " Regeln gelï¿½scht";
		if (ignored == 1) meldung += ", 1 Regel ignoriert";
		else if (ignored > 1) meldung += ", " + ignored + " Regeln ignoriert";
		if (replaced == 1) meldung += ", 1 Regel ersetzt";
		else if (replaced > 1) meldung += ", " + replaced + " Regeln ersetzt";
		report.note(new Message(meldung, datei));
	}

	public final void MINMAX(String token, Token t) {
		String meldung = "Minmax-Syntax-Fehler: \"" + token + "\" erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
		if (!minmax) {
			meldung = "Minmax-Syntax: minmax (min max) {<Bedingung>; <Bedingung>; ...}";
			report.add(new Message(meldung));
			minmax = true;
		}
	}

	public final void WENN(Token t) {
		String meldung = "Eine Regel muss mit \"" + WENN +
			"\" oder \"" + ENTFERNEN + "\" eingeleitet werden";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KONTEXT(Token t) {
		String meldung = "Es ist nur eine Kontext-Bedingung pro Regel erlaubt";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void AUSSER(Token t) {
		String meldung = "Es ist nur eine Ausser-Bedingung pro Regel erlaubt";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KLAMMER_AUF(Token t) {
		String meldung = "ï¿½ffnende Klammer erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void KLAMMER_ZU(Token t) {
		String meldung = "Schliessende Klammer erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void ECKIGE_KLAMMER_AUF(Token t) {
		String meldung = "'" + ECKIGE_KLAMMER_AUF + "' erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void ECKIGE_KLAMMER_ZU(Token t) {
		String meldung = "'" + ECKIGE_KLAMMER_ZU + "' erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void VERGLEICH(Token t) {
		String meldung = "Bedingung ( <Symptom/Diagnose> <Operator> <Wert> ) erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void OPERATOR(Token t) {
		String meldung = "Operator ( ";
		for (int i = 0; i < VERGLEICHSOPERATOR.length; i++)
			meldung += "'" + VERGLEICHSOPERATOR[i] + "' ";
		meldung += ") erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void WERT(String symbol, Token t) {
		String meldung = "Wert fï¿½r \"" + symbol + "\" erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void AKTION(Token t) {
		String meldung = "Aktion ( <Symptom/Diagnose> = <Wert/Formel> ) erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void ZUWEISUNG(Token t) {
		String meldung = "Zuweisungsoperator ( '" + ASSIGN_OP + "' ) oder Frage(klasse) statt Diagnose erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void KLAMMER_ODER_PAR(Token t) {
		String meldung = "Schliessende Klammer oder weiterer Parameter erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void SOWIE(Token t) {
		String meldung = "\"" + SOWIE +
			" <Aktion>\" oder neue Regel (" + WENN + " ...) erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void FORMEL(Token t) {
		String meldung = "Formel ( Frage / Zahl / Funktion ) oder Antwort erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void NO_POS_INT(String ausdruck, Token t) {
		String meldung = "\"" + ausdruck + "\" ist keine gï¿½ltige ganze Zahl >= 0";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}
	
	public final void MIN_GT_MAX(String ausdruck, Token t) {
		String meldung = "(" + ausdruck + "): Minumum sollte <= Maximum sein";
		report.warning(new Message(meldung, datei, t.getZeile(), mark(t)));
	}
	


	// Meldungen beim Hinzufï¿½gen in die Wissensbasis

	public final void KB_KEIN_SYMPTOM(String ausdruck, Token t) {
		String meldung = "\"" + ausdruck + "\" ist keine gï¿½ltige Frage";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_KEIN_SYMPTOM2(String ausdruck, Token t) {
		String meldung = "Der Operator '" + PLUSGLEICH
			+ "' erfordert eine Frage mit Antwortalternativen, \"" + ausdruck
			+ "\" ist jedoch keine entsprechend gï¿½ltige Frage";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_KEIN_BEGRIFF(String ausdruck, Token t) {
		String meldung = "\"" + ausdruck + "\" ist weder eine gï¿½ltige Frage(klasse) noch eine gï¿½ltige Diagnose";
		ConceptNotInKBError err = new ConceptNotInKBError(meldung, datei, t.getZeile(), mark(t));
		
		err.setObjectName(ausdruck);
		report.error(err);
	}

	public final void KB_KEIN_BEGRIFF1(String ausdruck, Token t) {
	this.KB_KEIN_BEGRIFF1(ausdruck, t,null);
	}
	
	public final void KB_KEIN_BEGRIFF1(String ausdruck, Token t, String assignment) {
		String meldung = "\"" + ausdruck + "\" ist weder eine gï¿½ltige Frage noch eine gï¿½ltige Diagnose";
		ConceptNotInKBError err = new ConceptNotInKBError(meldung, datei, t.getZeile(), mark(t));
		
		if(assignment != null) {
			if(QuestionNotInKBError.isYesOrNo(assignment)) {
		
			err = new QuestionNotInKBError(meldung, datei, t.getZeile(), mark(t));
			err.setKey(MessageGenerator.KEY_INVALID_QUESTION);
			((QuestionNotInKBError)err).setType(QuestionNotInKBError.TYPE_YN);
			}
			if(ConceptNotInKBError.isValidScore(assignment)) {
				err = new ConceptNotInKBError(meldung, datei, t.getZeile(), mark(t));
				err.setKey(MessageGenerator.KEY_INVALID_DIAGNOSIS);
			}
			
		}
		
		err.setObjectName(ausdruck);
		report.error(err);
	}
	

	public final void KB_KEIN_BEGRIFF2(String ausdruck, Token t) {
		String meldung = "Nur Fragen und Diagnosen kann etwas zugewiesen werden; \"" + ausdruck + "\" ist weder eine gï¿½ltige Frage noch eine gï¿½ltige Diagnose";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_KEIN_BEGRIFF3(String ausdruck, Token t) {
		String meldung = "Nur bei Fragen kann eine Antwortalternative hinzugefï¿½gt werden; \"" + ausdruck + "\" ist keine gï¿½ltige Frage";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_KEIN_BEGRIFF4(String ausdruck, Token t) {
		String meldung = "Nur von Fragen und Frageklassen kï¿½nnen Indikationsregeln erzeugt werden; \"" + ausdruck + "\" ist weder eine gï¿½ltige Frage noch eine gï¿½ltige Frageklasse";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}
	
	public final void illegalCharacterInComment(String data, Token t) {
		String meldung = ResourceBundle.getBundle("properties.textParser").getString("illegal_character_rule_comment");
		report.error(new Message(meldung+": "+data, datei, t.getZeile(), mark(t)));
	}

	public final void KB_KEIN_BEGRIFF5(String ausdruck, Token t) {
		String meldung = "Nur Fragen kï¿½nnen mit etwas verglichen werden; \"" + ausdruck + "\" ist keine gï¿½ltige Frage";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void INTERVALL_ERWARTET(String ausdruck, Token t) {
		String meldung = "Intervallgrenzen fï¿½r die Frage \"" + ausdruck + "\" erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void KB_KEIN_INTERVALL(String ausdruck, Token t) {
		String meldung = "[" + ausdruck + "] ist kein gï¿½ltiges Intervall";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}
	public final void KB_FRAGE_ERWARTET(String ausdruck, Token t) {
		String meldung = "Frage fï¿½r die Bedingung \"" + ausdruck + "\" erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void KB_KEIN_SCORE(String wert, String ausdruck, Token t) {
		String meldung = "\"" + wert + "\" ist kein gï¿½ltiges Score fï¿½r die Diagnose \"" + ausdruck + "\"";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_SCORE_ERWARTET(String ausdruck, Token t) {
		String meldung = "Score fï¿½r die Diagnose \"" + ausdruck + "\" erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void KB_KEINE_ANTWORT(String ausdruck, String wert, Token t,Question q) {
		String meldung = "\"" + wert + "\" ist keine gï¿½ltige Antwort zu \"" + ausdruck + "\"";
		ConceptNotInKBError error = new AnswerNotInKBError(meldung, datei, t.getZeile(), mark(t),q);
		error.setKey(MessageGenerator.KEY_INVALID_ANSWER);
		error.setObjectName(wert);
		report.error(error);
	}

	public final void KB_ANTWORT_ERWARTET(String ausdruck, Token t) {
		String meldung = "Antwort zu \"" + ausdruck + "\" erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void KB_ANTWORT_FORMEL_ERWARTET(String ausdruck, Token t) {
		String meldung = "Antwort zu \"" + ausdruck + "\" oder Formel erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void KB_WERT_ERWARTET(String ausdruck, Token t) {
		String meldung = "Antwort / Score / Formel zu \"" + ausdruck + "\" erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void KB_KEIN_STATUS(String ausdruck, Token t) {
		String meldung = "\"" + ausdruck + "\" ist kein gï¿½ltiger Status fï¿½r eine Diagnose";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_STATUS_ERWARTET(String ausdruck, Token t) {
		String meldung = "Status zur Diagnose \"" + ausdruck + "\" erwartet";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void KB_NICHT_MISCHEN(String ausdruck, String ausdruck2, Token t) {
		String meldung = "Fragen und Frageklassen dï¿½rfen nicht gemischt werden: \"" + ausdruck + "\" passt vom Typ nicht zu \"" + ausdruck2 + "\"";
		report.error(new Message(meldung, datei, t.getZeile(), markBehind(t)));
	}

	public final void KB_NUR_EQUALS(Token t) {
		String meldung = "Diese Art der Frage erlaubt nur den Operator '" +
			EQUALS_OP + "'";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_UNBEKANNT_NUR_EQUALS(Token t) {
		String meldung = "Bei unbekannter Antwort muss der Operator '" +
			EQUALS_OP + "' verwendet werden";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_NUR_JA_NEIN(Token t) {
		String meldung = "Diese Art der Frage erlaubt nur die Antworten " +
			JA + " und " + NEIN;
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_KEIN_STRING(String ausdruck, Token t) {
		String meldung = "\"" + ausdruck + "\" enthï¿½llt ungï¿½ltige Zeichen";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}
	
	public final void KB_KEINE_ZAHL(String ausdruck, Token t) {
		String meldung = "\"" + ausdruck + "\" ist keine gï¿½ltige Zahl";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_NOT_BEREICH(String ausdruck, String bereich, Token t) {
		String meldung = "\"" + ausdruck + "\" liegt nicht im Bereich " + bereich + "";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_KEINE_QNUM(String ausdruck, Token t) {
		String meldung = "\"" + ausdruck + "\" ist keine numerische Frage";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_KEINE_FORMEL(String ausdruck, Token t) {
		String meldung = "\"" + ausdruck + "\" ist weder eine gï¿½ltige Zahl noch eine gï¿½ltige Frage";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void KB_NOT_SUPPORTED(Token t) {
		String meldung = "Dieser Frage-Typ wird fï¿½r komplexe Regeln nicht unterstï¿½tzt";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void NOT_IN_KB(Scanner scanner, int start, int ende) {
		String meldung = "Folgende Regel kann nicht gelï¿½scht werden, da sie nicht existiert:\n";
		meldung += scanner.getToken(start).getSymbol();
		for (int i = start+1; i < ende; i++) {
			meldung += " " + scanner.getToken(i).getSymbol();
		}
		report.warning(new Message(meldung, datei, scanner.getToken(start).getZeile()));
	}



	// Einschrï¿½nkungen in der Konfigurationsdatei

	public final void NOT_NICHT(Token t) {
		String meldung = "\"" + t.getToken() + "\" ist in der aktuellen Konfiguration nicht erlaubt";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void NICHT_NUR_LITERALE(Token t) {
		String meldung = "\"" + t.getToken() + "\" ist nur vor Literalen (Vergleiche) erlaubt";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void LOGIKOPERATOR(Token t) {
		String meldung = "\"" + t.getToken() + "\" ist in der aktuellen Konfiguration nicht erlaubt";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void MAXSTUFE(int stufe, int maxStufe, Token t) {
		String meldung = "Diese Bedingung ï¿½bersteigt mit der Stufe von " + stufe
						+ " die in der aktuellen Konfiguration maximale Stufe von " + maxStufe;
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void NOT_KNF(Token t) {
		String meldung = "Diese Bedingung liegt nicht in konjunktiver Normalform vor,"
						+ " wie in der aktuellen Konfiguration gefordert";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}

	public final void NOT_DNF(Token t) {
		String meldung = "Diese Bedingung liegt nicht in disjunktiver Normalform vor,"
						+ " wie in der aktuellen Konfiguration gefordert";
		report.error(new Message(meldung, datei, t.getZeile(), mark(t)));
	}
	
}
