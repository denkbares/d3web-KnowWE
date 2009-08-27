package de.d3web.textParser.complexRule;

/**
 * Diese Klasse speichert ein Token sowie die Zeilen- und Spaltennummer,
 * an der dieses Token beginnt. Ein Token ist dabei ein Parse-Element,
 * also ein Schlï¿½sselwort, ein Operator, ein Symbol, ...
 *
 * @author Christian Braun
 * @version 1.021
 * @since JDK 1.5
 */
public class Token {


// Attribute:
// ==========

	/** Das eigentliche Token */
	private String token;

	/** Die Zeile, in der sich das Token befindet */
	private int zeile;

	/** Die Spalte, in der sich das Token befindet */
	private int spalte;

	/** Das Symbol, das dieses Token beschreibt */
	private String symbol;





// Konstruktoren:
// ==============

	/**
	 * Erzeugt ein neues leeres Token.
	 */
	public Token() {
		this("", 0, 0, "");
	}



	/**
	 * Erzeugt ein neues Token mit den angegebenen Parametern.
	 *
	 * @param token Das eigentliche Token
	 */
	public Token(String token) {
		this(token, 0, 0, token);
	}



	/**
	 * Erzeugt ein neues Token mit den angegebenen Parametern.
	 *
	 * @param token Das eigentliche Token
	 * @param zeile Die Zeile, in der sich das Token befindet
	 */
	public Token(String token, int zeile) {
		this(token, zeile, 0, token);
	}



	/**
	 * Erzeugt ein neues Token mit den angegebenen Parametern.
	 *
	 * @param token Das eigentliche Token
	 * @param zeile Die Zeile, in der sich das Token befindet
	 * @param spalte Die Spalte, in der sich das Token befindet
	 */
	public Token(String token, int zeile, int spalte) {
		this(token, zeile, spalte, token);
	}



	/**
	 * Erzeugt ein neues Token mit den angegebenen Parametern.
	 *
	 * @param token Das eigentliche Token
	 * @param symbol Das Symbol, das dieses Token beschreibt
	 */
	public Token(String token, String symbol) {
		this(token, 0, 0, symbol);
	}



	/**
	 * Erzeugt ein neues Token mit den angegebenen Parametern.
	 *
	 * @param token Das eigentliche Token
	 * @param zeile Die Zeile, in der sich das Token befindet
	 * @param symbol Das Symbol, das dieses Token beschreibt
	 */
	public Token(String token, int zeile, String symbol) {
		this(token, zeile, 0, symbol);
	}



	/**
	 * Erzeugt ein neues Token mit den angegebenen Parametern.
	 *
	 * @param token Das eigentliche Token
	 * @param zeile Die Zeile, in der sich das Token befindet
	 * @param spalte Die Spalte, in der sich das Token befindet
	 * @param symbol Das Symbol, das dieses Token beschreibt
	 */
	public Token(String token, int zeile, int spalte, String symbol) {
		setToken(token);
		setZeile(zeile);
		setSpalte(spalte);
		setSymbol(symbol);
	}





// Methoden:
// =========

	/**
	 * Liefert das eigentliche Token.
	 *
	 * @return das eigentliche Token
	 */
	public String getToken() {
		return token;
	}


	/**
	 * Liefert die Zeilennummer, in der sich das Token befindet.
	 *
	 * @return Die Zeilennummer, in der sich das Token befindet oder 0,
	 * falls die Zeilennummer unbekannt ist
	 */
	public int getZeile() {
		return zeile;
	}


	/**
	 * Liefert die Spaltennummer, in der sich das Token befindet.
	 *
	 * @return Die Spaltennummer, in der sich das Token befindet oder 0,
	 * falls die Spaltennummer unbekannt ist
	 */
	public int getSpalte() {
		return spalte;
	}


	/**
	 * Liefert das Symbol, das dieses Token beschreibt.
	 *
	 * @return das Symbol, das dieses Token beschreibt
	 */
	public String getSymbol() {
		return symbol;
	}



	/**
	 * Setzt das eigentliche Token.
	 *
	 * @param token das eigentliche Token
	 */
	public void setToken(String token) {
		if (token == null) token = "";
		this.token = token;
	}


	/**
	 * Setzt die Zeilennummer, in der sich das Token befindet.
	 *
	 * @param zeile Die Zeilennummer, in der sich das Token befindet oder 0,
	 * falls die Zeilennummer unbekannt ist
	 */
	public void setZeile(int zeile) {
		if (zeile < 0) zeile = 0;
		this.zeile = zeile;
	}


	/**
	 * Setzt die Spaltennummer, in der sich das Token befindet.
	 *
	 * @param spalte Die Spaltennummer, in der sich das Token befindet oder 0,
	 * falls die Spaltennummer unbekannt ist
	 */
	public void setSpalte(int spalte) {
		if (spalte < 0) spalte = 0;
		this.spalte = spalte;
	}


	/**
	 * Setzt das Symbol, das dieses Token beschreibt.
	 *
	 * @param symbol Das Symbol, das dieses Token beschreibt
	 */
	public void setSymbol(String symbol) {
		if (symbol == null) symbol = "";
		this.symbol = symbol;
	}



	/**
	 * Vergleicht das eigentliche Token mit dem ï¿½bergebenen Token.
	 *
	 * @param token Token, mit dem das eigentliche Token verglichen werden soll
	 * @return true, falls das eigentliche Token mit dem ï¿½bergebenen Token ï¿½bereinstimmt
	 */
	public boolean is(String token) {
		return this.token.equalsIgnoreCase(token) || this.symbol == token;
	}


	/**
	 * Vergleicht das eigentliche Token mit den ï¿½bergebenen Tokens.
	 *
	 * @param tokens Token, mit denen das eigentliche Token verglichen werden soll
	 * @return true, falls das eigentliche Token mit einem der ï¿½bergebenen Tokens ï¿½bereinstimmt
	 */
	public boolean is(String[] tokens) {
		if (tokens == null) return false;
		boolean ergebnis = false;
		for (int i = 0; i < tokens.length; i++) ergebnis = ergebnis || is(tokens[i]);
		return ergebnis;
	}


	/**
	 * Vergleicht das Token mit dem ï¿½bergebenen Token.
	 *
	 * @param token Token, mit dem das Token verglichen werden soll
	 * @return true, falls das Token mit dem ï¿½bergebenen Token ï¿½bereinstimmt
	 */
	public boolean is(Token token) {
		if (token == null) return false;
		return this.token.equalsIgnoreCase(token.token);
	}



	/**
	 * Liefert eine String-Representation des Tokens.
	 *
	 * @return String-Representation des Tokens
	 */
	@Override
	public String toString() {
		return symbol;
	}

}
