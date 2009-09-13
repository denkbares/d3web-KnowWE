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
