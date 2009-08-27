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
