package de.d3web.textParser.complexRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Scanner liefert aus der Eingabe einzelne Tokens, die anschliessend
 * vom Parser weiterverarbeitet werden. Zusï¿½tzlich wird ein START-Token
 * am Beginn und ein EOF-Token am Ende der Eingabe eingefï¿½gt.
 *
 * @author Christian Braun
 * @version 1.021
 * @since JDK 1.5
 */
public class Scanner extends ComplexRuleConfiguration {


// Attribute:
// ==========

	/** Speichert die zu parsende Eingabe */
	private List<String> eingabe;

	/** Speichert die einzelnen Tokens */
	private List<Token> tokens;

	/** Die aktuelle Position in der TokenListe */
	private int position;

	/** Speichert den ReportGenerator */
	private ComplexRuleReportGenerator report;





// Konstruktor:
// ============

	/**
	 * Erzeugt einen neuen Scanner und Token-isiert die Eingabe.
	 *
	 * @param eingabe die zu Token-isierende Eingabe
	 * @param report ReportGenerator, mit dem die Fehlermeldungen erzeugt werden sollen
	 * @throws NullPointerException falls einer der Parameter null ist
	 */
	public Scanner(List<String> eingabe, ComplexRuleReportGenerator report, boolean english) {
		if (eingabe == null || report == null) throw new NullPointerException();
		this.eingabe = eingabe;
		this.report = report;
		tokens = new ArrayList();
		position = 0;
		
		//if(english) {
			ComplexRuleConfiguration.setKeywords(english);
		//}else {
			//ComplexRuleConfiguration.setKeywordsGerman();
		//}

		tokens.add(new Token(START, ""));
		scan();
		if (eingabe.isEmpty()) tokens.add(new Token(EOF, ""));
		else tokens.add(new Token(EOF, eingabe.size()-1, eingabe.get(eingabe.size()-1).toString().length()-1, ""));
	}





// Externe Methoden:
// =================


	/**
	 * Liefert die aktuelle Position in der Tokenliste.
	 *
	 * @return aktuelle Position in der Tokenliste
	 */
	public int getPosition() {
		return position;
	}



	/**
	 * Setzt die aktuelle Position um.
	 *
	 * @param position die neue aktuelle Position in der Tokenliste
	 */
	public void setPosition(int position) {
		if (position < 0) position = 0;
		if (position > tokens.size() - 1) position = tokens.size() - 1;
		this.position = position;
	}



	/**
	 * Liefert das nï¿½chste Token aus der Tokenliste und erhï¿½ht die Position um 1.
	 *
	 * @return das nï¿½chste Token aus der Tokenliste
	 */
	public Token getNext() {
		if (position < tokens.size() - 1) position++;
		return getToken(position);
	}
	
	public Token showNext(int offset) {	
		return getToken(position+offset);
	}



	/**
	 * Liefert das i-te Token ab der aktuellen Position, ohne die Position zu verï¿½ndern.
	 * Damit ist eine Vorschau um i Tokens mï¿½glich.
	 *
	 * @param i Index des Tokens, das geliefert werden soll, ab der aktuellen Position.
	 *        i > 0 liefert also ein weiter hinten liegendes Token
	 *        i = 0 das aktuelle Token
	 *        i < 0 bereits geliefert Tokens
	 * @return das i-te Token ab der aktuellen Position, also getToken(getPosition() + i);
	 */
	public Token preview(int i) {
		return getToken(position + i);
	}



	/**
	 * Liefert das i-te Token aus der Tokenliste.
	 *
	 * @param i Index des Tokens, das geliefert werden soll
	 * @return das i-te Token aus der Tokenliste
	 */
	public Token getToken(int i) {
		if (i < 0) i = 0;
		if (i > tokens.size() - 1) i = tokens.size() - 1;
		return tokens.get(i);
	}





// Interne Methoden:
// =================


	/**
	 * Erzeugt die einzelnen Tokens aus der Eingabe.
	 */
	private void scan() {
		
		for (int i = 0; i < eingabe.size(); i++) {
			String zeile = normalizeBlanks(eingabe.get(i).toString());
			if(addCommentToken(zeile,i)) continue;
			int index = 0;
			boolean str = false;
			
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < zeile.length(); j++) {
				char c = zeile.charAt(j);
				if (c == STRING_CHAR) {
					str = !str;
				}
				else if (str || TRENNER.indexOf(c) < 0) {
					if (sb.length() < 1) index = j;
					sb.append(c);
				}
				else {
					addToken(sb.toString(), i, index);
					sb = new StringBuffer();
					String operator = read(zeile.substring(j), getOPERATORs());
					if (!eos(operator)) {
						addToken(operator, i, j);
						j += operator.length()-1;
					}
				}
			}
			addToken(sb.toString(), i, index);
			if (str) report.STRINGENDE_ERWARTET(i);
		}
	}



	private boolean addCommentToken(String zeile, int line) {
	if(zeile.startsWith("//")) {
		if(zeile.substring(2).trim().startsWith("@info")) {
			String ruleComment = zeile.substring(2).trim().substring(5).trim();
			tokens.add(new Token(ruleComment,line,RULE_COMMENT));
		}
		return true;
	}
	return false;
}





	/**
	 * Fï¿½gt der Tokenliste ein neues Token hinzu.
	 *
	 * @param token Das einzufï¿½gende Token
	 * @param zeile Die Zeile, in der das Token beginnt
	 * @param spalte Die Spalte, in der das Token beginnt
	 */
	private void addToken(String token, int zeile, int spalte) {
		if (eos(token)) return;

		int[] indizes = getRemoveIndizes(eingabe.get(zeile).toString());
		int i = transform(indizes, spalte);
		int j = transform(indizes, spalte + token.length());
		String symbol = eingabe.get(zeile).toString().substring(i);
		symbol = remove(symbol, ""+STRING_CHAR).substring(0, j-i);

		String temp = is(token, getTOKENs());
		if (eos(temp)) {
			Token t = tokens.get(tokens.size()-1);
			if (t.is(SYMBOL) && t.getZeile() == zeile) {
				t.setSymbol(t.getSymbol() + " " + symbol);
			}
			else {
				tokens.add(new Token(SYMBOL, zeile, i, symbol));
			}
		}
		else {
			tokens.add(new Token(temp, zeile, i, symbol));
		}
	}



	/**
	 * Transformiert die Spalte eines Tokens, indem die durch die removeBlanks-Methode
	 * entfernten Leerzeichen berï¿½cksichtigt werden.
	 *
	 * @param indizes Array mit den Indizes, an denen die removeBlanks-Methode Zeichen gelï¿½scht hat
	 * @param spalte Die Spalte, in der das Token beginnt
	 * @return Die Spalte, richtig transformiert
	 */
	private int transform(int[] indizes, int spalte) {
		for (int i = 0; i < indizes.length; i++) {
			if (indizes[i] <= spalte) spalte++;
		}
		return spalte;
	}

}
