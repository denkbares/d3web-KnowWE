package de.d3web.textParser.complexRule;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Report;
import de.d3web.textParser.KWikiToKnofficePreParser;

/**
 * Diese Klasse liest eine Datei ein und extrahiert daraus einen parseBaum,
 * der dann in eine Wissensbasis eingefï¿½gt wird.
 * Zusï¿½tzlich wird eine Konfigurationsdatei eingelesen, in der die Grammatik
 * eingeschrï¿½nkt werden kann.
 *
 * @author Christian Braun
 * @version 1.021
 * @since JDK 1.5
 */
public class ComplexRuleParserManagement extends ComplexRuleConfiguration {


// Attribute:
// ==========

	/** Speichert den ReportGenerator */
	private ComplexRuleReportGenerator report;





// Konstruktoren:
// ==============

	/**
	 * Erzeugt einen neuen RegelParser, parst die angegebene Datei und
	 * fï¿½gt den ParseBaum in die angegebene Wissensbasis ein.
	 *
	 * @param eingabedatei Datei, die geparst werden soll
	 * @param kbm Wissensbasis, in die die Regeln eingefï¿½gt werden sollen.
	 * @param configdatei Konfigurationsdatei fï¿½r den Parser
	 * @param update Gibt den Modus an: true --> UPDATE ; false --> REPLACE
	 */
	public ComplexRuleParserManagement(URL eingabedatei, KnowledgeBaseManagement kbm, URL configdatei, boolean update,boolean english) {
		this(KWikiToKnofficePreParser.urlToReader(eingabedatei),kbm, KWikiToKnofficePreParser.urlToReader(configdatei),update,false, english);
	}
	
	public ComplexRuleParserManagement(URL eingabedatei, KnowledgeBaseManagement kbm, URL configdatei, boolean update) {
		this(KWikiToKnofficePreParser.urlToReader(eingabedatei),kbm, KWikiToKnofficePreParser.urlToReader(configdatei),update,false, false);
	}
	
	public ComplexRuleParserManagement(Reader eingabedatei, KnowledgeBaseManagement kbm, Reader configdatei, boolean update, boolean onlySyntaxCheck, boolean english) {
		report = new ComplexRuleReportGenerator();

		// Eingabedatei einlesen
		if (eingabedatei == null) {
			report.KEINE_DATEI();
			return;
		}
		//report.setDatei(eingabedatei.getPath());
		List<String> eingabe;
		try {
			eingabe = readFileAll(eingabedatei);
			eingabe.add(0, "");
			eingabe.add(" ");
		}
		catch (IOException e) {
			report.IOEXCEPTION(e.getMessage());
			return;
		}

		// Ist Wissensbasis vorhanden?
		if (kbm == null) {
			report.KEINE_WISSENSBASIS();
			return;
		}

		// Scannen und Parsen
		report.setInput(eingabe);
		ComplexRuleConfigReader config = new ComplexRuleConfigReader(configdatei);
		Scanner scanner = new Scanner(eingabe, report,english);
		new ComplexRuleParser(scanner, config, report, kbm, update, onlySyntaxCheck);

		report.setDatei("");
		report.setInput(null);
	}



	/**
	 * Erzeugt einen neuen RegelParser, parst die angegebene Datei und
	 * fï¿½gt den ParseBaum in die angegebene Wissensbasis ein.
	 *
	 * @param eingabedatei Datei, die geparst werden soll
	 * @param kbm Wissensbasis, in die die Regeln eingefï¿½gt werden sollen.
	 * @param configdatei Konfigurationsdatei fï¿½r den Parser
	 */
	public ComplexRuleParserManagement(URL eingabedatei, KnowledgeBaseManagement kbm, URL configdatei) {
		this( (eingabedatei == null ? (URL)null : eingabedatei), kbm,
			  (configdatei == null ? (URL)null : configdatei), false);
	}
	
	
	
	/**
	 * Erzeugt einen neuen RegelParser, parst die angegebene Datei und
	 * fï¿½gt den ParseBaum in die angegebene Wissensbasis ein.
	 *
	 * @param eingabedatei Datei, die geparst werden soll
	 * @param kbm Wissensbasis, in die die Regeln eingefï¿½gt werden sollen.
	 * @param configdatei Konfigurationsdatei fï¿½r den Parser
	 * @param update Gibt den Modus an: true --> UPDATE ; false --> REPLACE
	 */
	public ComplexRuleParserManagement makeComplexRuleParserManagement(String eingabedatei, KnowledgeBaseManagement kbm, String configdatei, boolean update) {
		URL eingabe = null;
		URL config = null;
		try {
			eingabe =  new URL(eingabedatei);
			config = new URL(configdatei);
		} catch (Exception e) {
			// [TODO]: handle exception
		}
		return new  ComplexRuleParserManagement((eingabedatei == null ? (URL)null : eingabe), kbm,
			  (configdatei == null ? (URL)null : config), update);
	}



	/**
	 * Erzeugt einen neuen RegelParser, parst die angegebene Datei und
	 * fï¿½gt den ParseBaum in die angegebene Wissensbasis ein.
	 *
	 * @param eingabedatei Datei, die geparst werden soll
	 * @param kbm Wissensbasis, in die die Regeln eingefï¿½gt werden sollen.
	 * @param configdatei Konfigurationsdatei fï¿½r den Parser
	 */
	public ComplexRuleParserManagement makeComplexRuleParserManagement(String eingabedatei, KnowledgeBaseManagement kbm, String configdatei) {
		URL eingabe = null;
		URL config = null;
		try {
			eingabe =  new URL(eingabedatei);
			config = new URL(configdatei);
		} catch (Exception e) {
			// [TODO]: handle exception
		}
		return new  ComplexRuleParserManagement((eingabedatei == null ? (URL)null : eingabe), kbm,
				  (configdatei == null ? (URL)null : config), false);
	}





//	 Externe Methoden:
//	 =================


		/**
		 * Liefert den Report mit Meldungen, die beim Parsen evt. aufgetreten sind.
		 *
		 * @return Report mit Meldungen, die beim Parsen evt. aufgetreten sind.
		 */
		public Report getReport() {
			return report.getReport();
		}
		
}
