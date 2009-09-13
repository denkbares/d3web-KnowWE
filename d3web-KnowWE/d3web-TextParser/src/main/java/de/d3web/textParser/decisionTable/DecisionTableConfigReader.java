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

/* Created on 15. December 2004, 16:24 */
package de.d3web.textParser.decisionTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.d3web.kernel.domainModel.Score;
import de.d3web.textParser.KWikiToKnofficePreParser;
import de.d3web.report.Message;
import de.d3web.report.Report;

/**
 * Liest eine Config-Datei ein, extrahiert alle gefundenen Werte und speichert
 * diese in einem Hashtable ab.
 * Eine gï¿½ltige Zeile einer Config Datei enthï¿½lt genau drei Komponenten,
 * welche durch <CODE>|</CODE> getrennt sein mï¿½ssen.
 * Die erste Komponente enthï¿½lt immer den Wert, die zweite die zugelassenen Zeichen
 * und die dritte Komponente gibt Auskunft darï¿½ber, ob der Wert vorhanden sein muss
 * oder nicht.
 * @author  Andreas Klar
 */
public class DecisionTableConfigReader {
    
    /** enthï¿½lt die in der Config-Datei definierten erlaubten Werte zu bestimmten Zellentypen */
    private Map<String, Map<String, Object>> values;
    /** Report, der beim Einlesen der Config-Datei erstellt wird */
    private Report report;
    
    private static final String CONTENTS = "contents";
    private static final String NECESSITY = "necessity";
    
    public static final String LOGICAL_OPERATOR_COLUMN = "logOpColumn";
    public static final String LOGICAL_OPERATOR_ROW = "logOpRow";
    public static final String SIGN = "sign";
    public static final String SCORE = "score";
    
    public static String LOGICAL_OPERATOR_COLUMN_AND = "und";
    public static String LOGICAL_OPERATOR_COLUMN_OR = "oder";
    public static String LOGICAL_OPERATOR_COLUMN_MINMAX = "/";
    public static String LOGICAL_OPERATOR_ROW_AND = "u";
    public static String LOGICAL_OPERATOR_ROW_OR = "o";
    public static String SIGN_POSITIVE = "+";
    public static String SIGN_NEGATIVE = "-";
    
    
    /**
     * Erzeugt eine neue Instanz von ConfigReader mit den Standard-Einstellungen
     * Bewertungen: alle Scores aus d3web.kernel.domainModel.Score -> nicht notwendig
     * Verknï¿½pfungen: und, oder, /  -> notwendig
     * Vorzeichen: +, -  -> nicht notwendig
     * Operatoren: u, o  -> nich notwendig
     */
    public DecisionTableConfigReader() {
    	this.values = createStandardConfigurations();
    	this.report = new Report();
    }

    public DecisionTableConfigReader makeDecisionTableConfigReader(String configFile) {
    	URL url = null;
    	try {
    		url = new File(configFile).toURL();
		} catch (Exception e) {
			report.error(new Message("Config-Datei konnte nicht gelesen werden: " + e,
					configFile.toString()));
		}
    	
        return new DecisionTableConfigReader(url);
    }
    
    /**
     * Erzeugt eine neue Instanz von Config-Reader, indem eine Datei eingelesen und
     * auf Gï¿½ltigkeit ï¿½berprï¿½ft wird. Falls eine gï¿½ltige Config-Datei eingelesen
     * wurde, werden die enstprechenden Werte gespeichert.
     * @param configFile einzulesende Datei
    */
    public DecisionTableConfigReader(URL configFile) {
        this(KWikiToKnofficePreParser.urlToReader(configFile));
    }
    
    public DecisionTableConfigReader(Reader configFile) {
        this.values = new Hashtable<String, Map<String, Object>>();
        this.report = new Report();
        
      

			BufferedReader reader  = new BufferedReader(configFile);
	       // BufferedReader reader = new BufferedReader(new FileReader(configFile));
	        
	        // add all configurations, line after line
	        try {
	        	String actLine = reader.readLine();
	        	for (int count=1; actLine!=null; count++, actLine = reader.readLine()) {
                    if (!(actLine.startsWith("//") || actLine.equals("")))
                        addConfiguration(configFile, actLine, count);
	        	}
	        }
	        catch (IOException e) {
                report.error(new Message("Config-Datei konnte nicht gelesen werden: " + e,
                						configFile.toString()));
            }
		// }
		// catch (FileNotFoundException e) {
		// report.error(new Message("Config-Datei nicht gefunden",
		// configFile.toString()));
		//        }
        // ï¿½berprï¿½fen, ob das Trennzeichen fï¿½r minmax gï¿½ltig ist
        try {
            Integer.parseInt(LOGICAL_OPERATOR_COLUMN_MINMAX);
            report.error(new Message("minmax-Trennzeichen darf keine Zahl sein",
            						configFile.toString()));
        }
        catch (NumberFormatException e) {}
    }
    
    /**
     * adds the configuration of a single line to this configReader
     * @param configFile name of the config file
     * @param actLine the line to be computed
     * @param lineNo number of <CODE>actLine</CODE> in <CODE>configFile</CODE>
     */
    private void addConfiguration(URL configFile, String actLine, int lineNo) {
    	addConfigurationHelp(configFile, actLine, lineNo);
    }
    
    private void addConfiguration(Reader configFile, String actLine, int lineNo) {
    	addConfigurationHelp(configFile, actLine, lineNo);
    }

	private void addConfigurationHelp(Object configFile, String actLine, int lineNo) {
		String[] components = actLine.split("\\|");
        // each line must consist of 3 components (name, contents, necessity)
        if (components.length!=3) {
            this.report.error(new Message("Ungï¿½ltiger Eintrag",
            					configFile.toString(), lineNo, actLine));
            return;
        }

        Map<String, Object> def = new Hashtable<String, Object>();
        // contents
        String[] v = components[1].split(",");
        List<String> contents = new ArrayList<String>(1);
        for (int i=0; i<v.length; i++) {
            v[i] = v[i].trim();
            if (!v[i].equals(""))
                contents.add(v[i]);
        }
        def.put(CONTENTS, contents);
    
        // necessity
        if (components[2].trim().equals("ja") || components[2].trim().equals("yes"))
            def.put(NECESSITY, new Boolean(true));
        else if (components[2].trim().equals("nein") || components[2].trim().equals("no"))
            def.put(NECESSITY, new Boolean(false));
        else {
            this.report.error(new Message("Ungï¿½ltiger Wert fï¿½r 'Notwendigkeit'",
            					configFile.toString(), lineNo, actLine));
            return;
        }

        this.values.put(components[0].trim(), def);
	}

    /**
     * Creates a map with all standard configurations.
     * This Map can be used to add the standard configurations to a configReader
     * @return Map with all configurations
     */
    private Map<String, Map<String, Object>> createStandardConfigurations() {
    	Map<String, Map<String, Object>> ret = new Hashtable<String, Map<String, Object>>();
    	
    	// scores
    	Map<String, Object> a = new Hashtable<String, Object>();
    	List<String> scores = new ArrayList<String>(1);
    	for (Iterator it = Score.getAllScores().iterator(); it.hasNext(); )
    		scores.add(((Score)it.next()).getSymbol());
    	
    	//FIXME: evil workaround
    	scores.add("P0");
    	scores.add("N8");
    	
    	a.put(CONTENTS, scores);
    	a.put(NECESSITY, new Boolean(false));
    	ret.put(SCORE, a);
    	
    	// logical operators - column
    	Map<String, Object> b = new Hashtable<String, Object>();
    	List<String> logOpCol = new ArrayList<String>(3);
    	logOpCol.add("und");
    	logOpCol.add("oder");
    	logOpCol.add("/");
    	b.put(CONTENTS, logOpCol);
    	b.put(NECESSITY, new Boolean(true));
    	ret.put(LOGICAL_OPERATOR_COLUMN, b);
    	
    	// logical operators - row
    	Map<String, Object> d = new Hashtable<String, Object>();
    	List<String> logOpRow = new ArrayList<String>(2);
    	logOpRow.add("u");
    	logOpRow.add("o");
    	d.put(CONTENTS, logOpRow);
    	d.put(NECESSITY, new Boolean(false));
    	ret.put(LOGICAL_OPERATOR_ROW, d);
    	
    	// signs
    	Map<String, Object> c = new Hashtable<String, Object>();
    	List<String> sign = new ArrayList<String>(2);
    	sign.add("+");
    	sign.add("-");
    	c.put(CONTENTS, sign);
    	c.put(NECESSITY, new Boolean(false));
    	ret.put(SIGN, c);
    	
    	return ret;
    }

    /**
     * Liefert <CODE>true</CODE> zurï¿½ck, falls in der Config-Datei angegeben ist,
     * dass dieser Wert immmer vorhanden sein muss, ansonsten <CODE>false</CODE>
     * @param value Wert, der ï¿½berprï¿½ft werden soll
     * @return <CODE>true</CODE>, falls der Wert vorhanden sein muss,
     * <CODE>false</CODE>, falls der Wert nicht vorhanden sein muss oder
     * oder der Wert nich existiert
     */    
    public boolean isNecessary(String value) {
        if (this.values.containsKey(value)) {
            Map<String, Object> m = this.values.get(value);
            return ((Boolean)m.get(NECESSITY)).booleanValue();
        }
        else
            return false;
    }
    
    /**
     * Liefert alle erlaubten Inhalte, die dieser Wert laut Config-Datei zulï¿½sst
     * @param value Wert, dessen erlaubte Inhalte erfragt werden sollen
     * @return Liste, die alle erlaubten Inhalte enthï¿½lt oder
     * leere Liste, falls der Wert nicht existiert
     */    
    public List<String> getContents(String value) {
        if (this.values.containsKey(value)) {
            Map<String, Object> m = this.values.get(value);
            return (List<String>)m.get(CONTENTS);
        }
        else
            return new ArrayList<String>(1);
    }
    
    /**
     * Liefert den Inhalt zum angegebenen Wert, der in der Config-Datei an der
     * Stelle <CODE>index</CODE> steht.
     * @param value Wert, dessen Inhalt erfragt werden soll
     * @param index Stelle, an welcher der benï¿½tigte Wert steht
     * @return Inhalt zum Wert <CODE>value</CODE>, der an der Stelle <CODE>index</CODE>
     * in der Config-Datei steht
     */    
    public String getContent(String value, int index) {
        if (this.values.containsKey(value)) {
            Map<String, Object> m = this.values.get(value);
            return ((List<String>)m.get(CONTENTS)).get(index);
        }
        else
            return null;
    }
        
    /**
     * ï¿½berprï¿½ft, ob ein Inhalt fï¿½r einen bestimmten Wert erlaubt ist
     * @param type Wert, dessen Inhalt ï¿½berprï¿½ft werden soll
     * @param value Inhalt, der auf Gï¿½ltigkeit geprï¿½ft werden soll
     * @return <CODE>true</CODE>, falls <CODE>value</CODE> ein erlaubter Inhalt fï¿½r <CODE>type</CODE> ist
     * oder <CODE>type</CODE> nicht existiert, ansonsten <CODE>false</CODE>
     */    
    public boolean isValid(String type, String value) {
        if (this.values.containsKey(type)) {
            Map<String, Object> map = this.values.get(type);
            List<String> validValues = (ArrayList<String>)map.get(CONTENTS);
            if (validValues.contains(value)
            		|| validValues.contains(value.toLowerCase())
            		|| validValues.contains(value.toUpperCase()))
                return true;
            else
                return false;
        }
        else
            return true;
    }
    
    public boolean isLogicalOperatorColumn(String value) {
    	return
    	value.equalsIgnoreCase(LOGICAL_OPERATOR_COLUMN_AND) ||
    	value.equalsIgnoreCase(LOGICAL_OPERATOR_COLUMN_OR) ? true : false;  
    	
    }
    
    public boolean isLogicalOperatorRow(String value) {
    	return
    	value.equalsIgnoreCase(LOGICAL_OPERATOR_ROW_AND) ||
    	value.equalsIgnoreCase(LOGICAL_OPERATOR_ROW_OR) ? true : false;  
    	
    }
    
    public Report getReport() {
    	return this.report;
    }
}
