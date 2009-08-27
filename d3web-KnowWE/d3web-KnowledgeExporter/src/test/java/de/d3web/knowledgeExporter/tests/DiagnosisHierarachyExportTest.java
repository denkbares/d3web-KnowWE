package de.d3web.knowledgeExporter.tests;

import junit.framework.TestCase;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.txtWriters.DiagnosisHierarchyWriter;

public class DiagnosisHierarachyExportTest extends TestCase {

	private KnowledgeBase kb;
	private KnowledgeManager manager;
	private DiagnosisHierarchyWriter writer;
	

	
	public void setUpWriter() {
		
		//DataManager.getInstance().setBase(kb);
		manager = new KnowledgeManager(kb);
		writer = new DiagnosisHierarchyWriter(manager);
	}
	
	public void setUpKB(){
		kb = new KnowledgeBase();
		Diagnosis root = new Diagnosis();
		root.setText("P000");
		root.setId("root");
		kb.add(root);
	}

	
	public void testSingleDiagnosis() {
		setUpKB();
		Diagnosis d = new Diagnosis();
		d.setText("Diagnosis 1");
		d.setId("1");
		kb.getRootDiagnosis().addChild(d);
		kb.add(d);
		
		setUpWriter();
		assertEquals("Diagnosis has the wrong value: ", "Diagnosis 1\n", writer.writeText());

	}

	public void testSingleDiagnosisHierarchy() {
		
		String[] dTexts = {"diagnosisgroup 1", "group 1", "diagnosis 1", "diagnosis 2" };
		
		String expectedExport = dTexts[0] + "\n"
			+ "- " + dTexts[1] + "\n"
			+ "-- " + dTexts[2] + "\n"
			+ "-- " + dTexts[3]+ "\n";
		
		setUpKB();
		Diagnosis dg1 = new Diagnosis();
		dg1.setText(dTexts[0]);
		dg1.setId("0");
		
		Diagnosis g1 = new Diagnosis();
		g1.setText(dTexts[1]);
		g1.setId("1");
		
		Diagnosis d1 = new Diagnosis();
		d1.setText(dTexts[2]);
		d1.setId("2");
		
		Diagnosis d2 = new Diagnosis();
		d2.setText(dTexts[3]);
		d2.setId("3");
		
		g1.addChild(d1);
		g1.addChild(d2);
		dg1.addChild(g1);
		kb.getRootDiagnosis().addChild(dg1);
		
		kb.add(dg1);
		kb.add(g1);
		kb.add(d1);
		kb.add(d2);
		
		setUpWriter();
		

		//check output
		assertEquals("Wrong export:", expectedExport, writer.writeText());
	}
	
	public void testSingleDiagnosisHierarchyAndIds() {
		
		String[] dTexts = {"diagnosisgroup 1", "group 1", "diagnosis 1", "diagnosis 2" };
		
		String expectedExport = dTexts[0] + " #0\n"
			+ "- " + dTexts[1] + " #1\n"
			+ "-- " + dTexts[2] + " #2\n"
			+ "-- " + dTexts[3]+ " #3\n";
		
		setUpKB();
		Diagnosis dg1 = new Diagnosis();
		dg1.setText(dTexts[0]);
		dg1.setId("0");
		
		Diagnosis g1 = new Diagnosis();
		g1.setText(dTexts[1]);
		g1.setId("1");
		
		Diagnosis d1 = new Diagnosis();
		d1.setText(dTexts[2]);
		d1.setId("2");
		
		Diagnosis d2 = new Diagnosis();
		d2.setText(dTexts[3]);
		d2.setId("3");
		
		g1.addChild(d1);
		g1.addChild(d2);
		dg1.addChild(g1);
		kb.getRootDiagnosis().addChild(dg1);
		
		kb.add(dg1);
		kb.add(g1);
		kb.add(d1);
		kb.add(d2);
		
		setUpWriter();
		writer.setExportDiagnoseHierarchyID(true);

		//check output
		assertEquals("Wrong export:", expectedExport, writer.writeText());
	}
	
	public void testSingleDiagnosisHierarchyWithReferences() {
		
		String[] dTexts = {"diagnosisgroup 1", "group 1", "diägnosis 1", "diagnosis 2" };
		
		String expectedExport = dTexts[0] + " #0\n"
			+ "- " + dTexts[1] + " #1\n"
			+ "-- " + dTexts[2] + " #2\n"
			+ "-- " + dTexts[3]+ " #3\n";
		
		setUpKB();
		Diagnosis dg1 = new Diagnosis();
		dg1.setText(dTexts[0]);
		dg1.setId("0");
		
		Diagnosis g1 = new Diagnosis();
		g1.setText(dTexts[1]);
		g1.setId("1");
		
		Diagnosis d1 = new Diagnosis();
		d1.setText(dTexts[2]);
		d1.setId("2");
		
		
		Diagnosis d2 = new Diagnosis();
		d2.setText(dTexts[3]);
		d2.setId("3");
		
		g1.addChild(d1);
		g1.addChild(d2);
		dg1.addChild(g1);
		kb.getRootDiagnosis().addChild(dg1);
		
		kb.add(dg1);
		kb.add(g1);
		kb.add(d1);
		kb.add(d2);
		
		setUpWriter();
		writer.setExportDiagnoseHierarchyID(true);

		//check output
		assertEquals("Wrong export:", expectedExport, writer.writeText());
	}
//	
//	@Test
//	public void checkDoubleDiagnosis() {
//	
//		String diagnosis = "diagnosisgroup 1\n" + 
//                "- group 1\n" + 
//                "-- diagnosis 1.1\n" +
//                "-- diagnosis 1.2\n" +
//                "- group 2\n" +
//                "-- diagnosis 2.1\n\n" +
//                "diagnosisgroup 2\n" +
//                "- group 3\n" +
//                "-- diagnosis 3.1\n";
//		 
//		setUpKB(diagnosis);
//		
//		assertEquals("Diagnosis has the wrong value: ", "P000", kb.getDiagnoses().get(0).getText());
//		assertEquals("Diagnosis has the wrong value: ", "diagnosisgroup 1", kb.getDiagnoses().get(1).getText());
//		assertEquals("Diagnosis has the wrong value: ", "group 1", kb.getDiagnoses().get(2).getText());
//		assertEquals("Diagnosis has the wrong value: ", "diagnosis 1.1", kb.getDiagnoses().get(3).getText());
//		assertEquals("Diagnosis has the wrong value: ", "diagnosis 1.2", kb.getDiagnoses().get(4).getText());
//		assertEquals("Diagnosis has the wrong value: ", "group 2", kb.getDiagnoses().get(5).getText());
//		assertEquals("Diagnosis has the wrong value: ", "diagnosis 2.1", kb.getDiagnoses().get(6).getText());
//		assertEquals("Diagnosis has the wrong value: ", "diagnosisgroup 2", kb.getDiagnoses().get(7).getText());
//		assertEquals("Diagnosis has the wrong value: ", "group 3", kb.getDiagnoses().get(8).getText());
//		assertEquals("Diagnosis has the wrong value: ", "diagnosis 3.1", kb.getDiagnoses().get(9).getText());
//	}
//	
//	
//	@Test
//	public void checkDoubleDiagnosisHierarchy() {
//
//		String[] diagnosisArray = { "diagnosisgroup 1", "group 1",
//				"diagnosis 1.1", "diagnosis 1.2", "group 2", "diagnosis 2.1",
//				"diagnosisgroup 2", "group 3", "diagnosis 3.1" };
//
//		 String diagnosis = "diagnosisgroup 1\n" + 
//                 "- group 1\n" + 
//                 "-- diagnosis 1.1\n" +
//                 "-- diagnosis 1.2\n" +
//                 "- group 2\n" +
//                 "-- diagnosis 2.1\n\n" +
//                 "diagnosisgroup 2\n" +
//                 "- group 3\n" +
//                 "-- diagnosis 3.1\n";
//
//		setUpKB(diagnosis);
//
//		// check diagnosis#
//		assertEquals("Incorrect number of diagnosis found!", 10, kb.getDiagnoses().size());
//
//		// check root node
//		assertEquals("Diagnosis hierarchy has wrong root node: ", "P000", kb.getRootDiagnosis().getText());
//		assertEquals("Root node has wrong number of children: ", 2, kb.getRootDiagnosis().getChildren().size());
//		assertEquals("Root node has father: ", 0, kb.getRootDiagnosis().getParents().size());
//
//		// check children#
//		int[] child = { 2, 2, 0, 0 , 1, 0, 1, 1, 0};
//		for (int i = 0; i < diagnosisArray.length; i++) {
//			assertEquals("Node has wrong number of children: ", child[i],
//					lookForDiagnosis(kb, diagnosisArray[i]).getChildren().size());
//			assertTrue("Wrong objecttype found!", checkInstanceOf(kb.getDiagnoses().get(i)));
//		}
//		// check parent#
//		int[] parent = { 1, 1, 1, 1, 1, 1, 1, 1, 1};
//		for (int i = 0; i < diagnosisArray.length; i++) {
//			assertEquals("Node has more than one father: ", parent[i],
//					lookForDiagnosis(kb, diagnosisArray[i]).getParents().size());
//		}
//	}
//	
//	@Test
//	public void checkReports() {
//
//		 String diagnosis = "diagnosisgroup 1\n" + 
//		         "- group 1\n" + 
//		         "-- diagnosis 1.1\n" +
//		         "-- diagnosis 1.2\n" +
//		         "- group 2\n" +
//		         "-- diagnosis 2.1\n\n" +
//		         "diagnosisgroup 2\n" +
//		         "- group 3\n" +
//		         "-- diagnosis 3.1\n";
//		
//		setUpKB(diagnosis);
//		
//		for (Iterator it = output.keySet().iterator(); it.hasNext();) {
//			String key = (String) it.next();
//			Report report = output.get(key);
//
//			assertNotNull("Caution! No report found. ", report);
//			assertEquals("Caution! Report contains errors. ", 0, report
//					.getErrorCount());
//		}
//	}
//	@Test
//	public void checkNotNull() {
//
//		String diagnosis = "answer 1";
//
//		setUpKB(diagnosis);
//
//		assertNotNull("Caution! No knowledgebase available. ", kb);
//		assertNotNull("Caution! No diagnosis available. ", kb.getDiagnoses());
//		assertNotNull("Caution! No questions available. ", kb.getQuestions());
//		assertNotNull("Caution! No beginning question available.", kb.getInitQuestions());
//	}
}
