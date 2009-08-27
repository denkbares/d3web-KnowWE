package de.d3web.textParser.complexRule;

/**
 * Diese Klasse enthällt Konfigurationen und Definitionen für den
 * ComplexRuleParser.
 * 
 * @author Christian Braun
 * @version 1.021
 * @since JDK 1.5
 */
public class ComplexRuleConfiguration extends Utils {

	// Konstanten:
	// ===========

	// Konfigurationen:

	/** String-Markier-Symbol */
	public static final char STRING_CHAR = '"';

	// Hinweis: Diese Konstante muss "normale" Zeichen sein, sie darf kein
	// Steuerzeichen
	// eines "regular expression" sein!

	/** Kommentar-Symbol für einzeilige Kommentare */
	public static final String KOMMENTAR = "//";

	// Definitionen:

	/** Eine gültige Ziffer ist eines der in dieser Konstante abgelegten Zeichen. */
	public static final String ZIFFER = removeBlanks("0 1 2 3 4 5 6 7 8 9");

	/**
	 * Ein gültiger Kleinbuchstabe ist eines der in dieser Konstante abgelegten
	 * Zeichen.
	 */
	public static final String KLEINBUCHSTABE = removeBlanks("a b c d e f g h i j k l m n o p q r s t u v w x y z");

	/**
	 * Ein gültiger Grossbuchstabe ist eines der in dieser Konstante abgelegten
	 * Zeichen.
	 */
	public static final String GROSSBUCHSTABE = removeBlanks("A B C D E F G H I J K L M N O P Q R S T U V W X Y Z");

	/** Ein gültiges Umlaut ist eines der in dieser Konstante abgelegten Zeichen. */
	public static final String UMLAUT = removeBlanks("Ä Ö Ü ä ö ü ß");

	/**
	 * Ein gültiger Buchstabe ist eines der in dieser Konstante abgelegten
	 * Zeichen.
	 */
	public static final String BUCHSTABE = KLEINBUCHSTABE + GROSSBUCHSTABE
			+ UMLAUT;

	/**
	 * Ein gültiges "NormalZeichen" (Buchstabe + Ziffer) ist eines der in dieser
	 * Konstante abgelegten Zeichen.
	 */
	public static final String NORMALZEICHEN = BUCHSTABE + ZIFFER;

	/**
	 * Ein gültiges Sonderzeichen ist eines der in dieser Konstante abgelegten
	 * Zeichen.
	 */
	public static final String SONDERZEICHEN = removeBlanks("! § $ % ? # ~ ^ ° _ | \\ ' . ,");

	/** Ein gültiges Blank ist eines der in dieser Konstante abgelegten Zeichen. */
	public static final String BLANK = " \t";

	/**
	 * Ein gültiger Zeilenumbruch ist eines der in dieser Konstante abgelegten
	 * Zeichen.
	 */
	public static final String LINEBREAK = "\n\r";

	/**
	 * Ein gültiges Zeichen, das überlesen werden soll, ist eines der in dieser
	 * Konstante abgelegten Zeichen.
	 */
	public static final String TRIM = BLANK + LINEBREAK + "\f";

	/**
	 * Ein gültiges Trennzeichen ist eines der in dieser Konstante abgelegten
	 * Zeichen.
	 */
	public static final String TRENNERZEICHEN = removeBlanks("< > = + - * / ( ) [ ] { } ;");

	/**
	 * Ein gültiger Wort-Trenner ist eines der in dieser Konstante abgelegten
	 * Zeichen.
	 */
	public static final String TRENNER = TRENNERZEICHEN + TRIM;

	/**
	 * Ein gültiges Zeichen ist eines der in dieser Konstante abgelegten
	 * Zeichen.
	 */
	public static final String ZEICHEN = BUCHSTABE + SONDERZEICHEN + BLANK;

	/**
	 * Ein gültiger alphanumerischer Ausdruck ist eines der in dieser Konstante
	 * abgelegten Zeichen.
	 */
	public static final String ALPHANUM = NORMALZEICHEN + SONDERZEICHEN + BLANK;

	public static synchronized void setKeywords(boolean english) {
		if(english) {
		WENN = "IF";
		AUSSER = "EXCEPT";
		UND = "AND";
		ODER = "OR";
		NICHT = "NOT";
		DANN = "THEN";
		ENTFERNEN = "REMOVE";
		VERBERGE = "HIDE";
		} else {
			WENN = "WENN";
			AUSSER = "AUSSER";
			UND = "UND";
			ODER = "ODER";
			NICHT = "NICHT";
			DANN = "DANN";
			ENTFERNEN = "ENTFERNEN";
			VERBERGE = "VERBERGE";
		}
	}
//	public static synchronized void setKeywordsGerman() {
//	
//	}

	// Schlüsselwörter:

	/** Schlüsselwort WENN */
	public static String WENN = "WENN";

	/** Schlüsselwort KONTEXT */
	public static final String KONTEXT = "KONTEXT";

	/** Schlüsselwort AUSSER */
	public static String AUSSER = "AUSSER";

	/** Schlüsselwort UND */
	public static String UND = "UND";

	/** Schlüsselwort ODER */
	public static String ODER = "ODER";

	/** Schlüsselwort NICHT */
	public static String NICHT = "NICHT";

	/** Schlüsselwort MINMAX */
	public static final String MINMAX = "MINMAX";

	/** Schlüsselwort DANN */
	public static String DANN = "DANN";

	/** Schlüsselwort SOWIE */
	public static final String SOWIE = "SOWIE";

	/** Schlüsselwort ENTFERNEN */
	public static String ENTFERNEN = "ENTFERNEN";

	/** Schlüsselwort ENTFERNEN */
	public static String VERBERGE = "VERBERGE";

	/** Schlüsselwort INSTANT */
	public static final String INSTANT = "INSTANT";

	/**
	 * Ein gültiges Schlüsselwort ist eines der in dieser Konstante abgelegten
	 * Strings.
	 */
	public static final String[] getKEYS() {
		return sortLength(new String[] { WENN, KONTEXT, AUSSER, UND, ODER,
				NICHT, MINMAX, DANN, SOWIE, ENTFERNEN, VERBERGE, INSTANT });
	}

	// Operatoren:

	/** <-Operator */
	public static final String SMALLER = "<";

	/** >-Operator */
	public static final String GREATER = ">";

	/** =-Operator */
	public static final String ASSIGN_OP = "=";

	/** <=-Operator */
	public static final String SMALLER_EQ = "<=";

	/** >=-Operator */
	public static final String GREATER_EQ = ">=";

	/** ==-Operator */
	public static final String EQUALS_OP = "==";

	/** =<-Operator */
	public static final String SMALLER_EQ2 = "=<";

	/** =>-Operator */
	public static final String GREATER_EQ2 = "=>";

	/**
	 * Ein gültiger Vergleichs-Operator ist einer der in dieser Konstante
	 * abgelegten Strings.
	 */
	public static final String[] VERGLEICHSOPERATOR = sortLength(new String[] {
			SMALLER, GREATER, ASSIGN_OP, SMALLER_EQ, GREATER_EQ, EQUALS_OP,
			SMALLER_EQ2, GREATER_EQ2 });

	/** +-Operator */
	public static final String PLUS = "+";

	/** --Operator */
	public static final String MINUS = "-";

	/** *-Operator */
	public static final String MAL = "*";

	/** /-Operator */
	public static final String GETEILT = "/";

	/**
	 * Ein gültiger Rechen-Operator ist einer der in dieser Konstante abgelegten
	 * Strings.
	 */
	public static final String[] RECHENOPERATOR = sortLength(new String[] {
			PLUS, MINUS, MAL, GETEILT });

	/**
	 * Ein gültiges Vorzeichen ist einer der in dieser Konstante abgelegten
	 * Strings.
	 */
	public static final String[] VORZEICHEN = sortLength(new String[] { PLUS,
			MINUS });

	/** Normale Klammer auf */
	public static final String NORMALE_KLAMMER_AUF = "(";

	/** Normale Klammer zu */
	public static final String NORMALE_KLAMMER_ZU = ")";

	/** Eckige Klammer auf */
	public static final String ECKIGE_KLAMMER_AUF = "[";

	/** Eckige Klammer zu */
	public static final String ECKIGE_KLAMMER_ZU = "]";

	/** Geschweifte Klammer auf */
	public static final String GESCHWEIFTE_KLAMMER_AUF = "{";

	/** Geschweifte Klammer zu */
	public static final String GESCHWEIFTE_KLAMMER_ZU = "}";

	/** Komma */
	public static final String KOMMA = ";";

	/**
	 * Eine gültige öffnende Klammer ist einer der in dieser Konstante
	 * abgelegten Strings.
	 */
	public static final String[] KLAMMER_AUF = sortLength(new String[] {
			NORMALE_KLAMMER_AUF, ECKIGE_KLAMMER_AUF, GESCHWEIFTE_KLAMMER_AUF });

	/**
	 * Eine gültige schliessende Klammer ist einer der in dieser Konstante
	 * abgelegten Strings.
	 */
	public static final String[] KLAMMER_ZU = sortLength(new String[] {
			NORMALE_KLAMMER_ZU, ECKIGE_KLAMMER_ZU, GESCHWEIFTE_KLAMMER_ZU });

	/**
	 * Eine gültige Klammer ist einer der in dieser Konstante abgelegten
	 * Strings.
	 */
	public static final String[] KLAMMER = sortLength(join(join(KLAMMER_AUF,
			KLAMMER_ZU), KOMMA));

	/** +=-Operator */
	public static final String PLUSGLEICH = "+=";

	/**
	 * Ein gültiger Operator ist einer der in dieser Konstante abgelegten
	 * Strings.
	 */
	public static final String[] getOPERATORs() {
		return sortLength(join(join(VERGLEICHSOPERATOR, RECHENOPERATOR), join(
				KLAMMER, PLUSGLEICH)));
	}

	/** Ein gültiges Token ist einer der in dieser Konstante abgelegten Strings. */
	public static final String[] getTOKENs() {
		return sortLength(join(getKEYS(), getOPERATORs()));
	}

	// Spezial-Symbole:

	/** Signalisiert ein Symbol */
	public static final String RULE_COMMENT = "comment";

	/** Signalisiert ein Symbol */
	public static final String SYMBOL = "symbol";

	/** Signalisiert den Anfang der Datei */
	public static final String START = "start";

	/** Signalisiert das Ende der Datei */
	public static final String EOF = "eof";

	/** Signalisiert eine bekannte Frage */
	public static final String BEKANNT = "bekannt";

	/** Signalisiert eine bekannte Frage */
	public static final String KNOWN = "known";

	/** Signalisiert eine unbekannte Frage */
	public static final String UNBEKANNT = "unbekannt";

	/** Signalisiert eine unbekannte Antwort */
	public static final String ANTWORT_UNBEKANNT = "unbekannt";

	/** Alternative Signalisierung einer unbekannten Antwort */
	public static final String ANTWORT_EGAL = "egal";
	
	/** Alternative Signalisierung einer unbekannten Antwort */
	public static final String ANTWORT_UNKNOWN = "unknown";
	

	/** Signalisiert die Antwort JA */
	public static final String JA = "ja";

	/** Signalisiert die Antwort YES */
	public static final String YES = "yes";

	/** Alternative Signalisierung der Antwort JA */
	public static final String WAHR = "wahr";

	/** Signalisiert die Antwort NEIN */
	public static final String NEIN = "nein";

	/** Signalisiert die Antwort NO */
	public static final String NO = "no";

	/** Alternative Signalisierung der Antwort NEIN */
	public static final String FALSCH = "falsch";

}
