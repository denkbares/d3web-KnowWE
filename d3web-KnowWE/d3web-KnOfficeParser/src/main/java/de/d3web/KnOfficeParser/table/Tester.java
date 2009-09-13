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

import java.io.File;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.report.Message;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
/**
 * Einfache Testklasse um den Inhalt dieses Packages zu testen 
 * @author Markus Friedrich
 *
 */
public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File("examples\\Muster2.xls");
		ExceltoTextParser parser = new ExceltoTextParser(file, 22);
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
//		CellKnowledgeBuilder ckb = new ScoringRuleBuilder("score");
		CellKnowledgeBuilder ckb = new XCLRelationBuilder("xcl");
//		((XCLRelationBuilder)ckb).setCreateUncompleteFindings(false);
		TableParser tb = new TableParser2();
		D3webBuilder builder = new D3webBuilder(file.toString(), ckb, 0, 0, tb, new SingleKBMIDObjectManager(kbm));
		tb.builder=builder;
		builder.setLazy(true);
		builder.setLazyDiag(true);
		tb.parse(parser.parse());
		
		List<Message> errors=(List<Message>) builder.checkKnowledge();
		for (Message m: errors) {
			System.out.println(m);
		}
	}

}
