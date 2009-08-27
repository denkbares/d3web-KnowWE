package de.d3web.textParser.decisionTable;

import de.d3web.report.Report;

public abstract class DecisionTableSyntaxChecker implements SyntaxChecker {

	/**
	 * Überprüft, ob die Tabelle eine bestimmte Mindestgröße hat
	 * @param table Zu überprüfende Tabelle
	 * @param minRows minimale Zeilenzahl
	 * @param minCols minimale Spaltenzahl
	 * @return Liste mit allen gefunden Fehlern. Wenn keine Fehler
     * gefunden wurden enthält die Liste keine Elemente
	 */
	protected Report checkTableDimension(DecisionTable table, int minRows, int minCols) {
		Report report = new Report();
		if (table.rows()<minRows)
            report.error(MessageGenerator.notEnoughRows(minRows));
        if (table.columns()<minCols)
            report.error(MessageGenerator.notEnoughColumns(minCols));
        return report;
	}
	
    /**
     * Überprüft die Syntax einer Tabelle, also ob alle nötigen Zellen einen Wert enthalten
     * und ob andere Zellen, die leer sein müssen einen Wert enthalten.
     * @param table Zu überprüfenden Tabelle
     * @return Liste mit allen gefunden Fehlern. Wenn keine Fehler
     * gefunden wurden enthält die Liste keine Elemente
     */
	protected abstract Report checkTableSpecificProperties(DecisionTable table);

	/**
	 * Abgleich mit Angaben aus der Config-Datei
	 * @param table Zu überprüfende Tabelle
	 * @return Liste mit allen Fehlermeldungen oder leere Liste, falls die Prüfung
     * erfolgreich war
     */
	protected abstract Report checkConfigSettings(DecisionTable table);
}
