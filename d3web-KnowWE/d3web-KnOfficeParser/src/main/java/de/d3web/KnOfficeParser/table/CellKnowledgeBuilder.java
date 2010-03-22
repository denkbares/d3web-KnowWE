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

package de.d3web.KnOfficeParser.table;

import de.d3web.report.Message;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Diagnosis;
import de.d3web.core.manage.IDObjectManagement;
/**
 * Interface für Builder, die Wissen aus einer Tabellenzelle inklusive derren Kontext generieren
 * @author Markus Friedrich
 *
 */
public interface CellKnowledgeBuilder {
	Message add(IDObjectManagement idom, int line, int column, String file, Condition cond, String text, Diagnosis diag, boolean errorOccured);
}
