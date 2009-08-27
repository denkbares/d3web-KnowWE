package de.d3web.knowledgeExporter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class KnowledgeExporter {
	
//	private URL destURL;
//	private File caseRep;
//	private File fileDest;
//	private Locale locale;
//	private ResourceBundle rb;
	
	private List<KnowledgeWriter> writers = new ArrayList<KnowledgeWriter>();
	private KnowledgeExporterStatus status;
	private File filesDir;
	


	
	public KnowledgeExporter() {
		status = new KnowledgeExporterStatus();
	}


	public KnowledgeExporterStatus getStatus() {
		return this.status;
	}


	public void setExportWriters(List<KnowledgeWriter> writers) {
		this.writers = writers;
	}
	
	
	public void export() throws IOException  {
		status.reset();
		status.setNumberOfJobs(writers.size());
		for (int i = 0; i < writers.size(); i++) {
			KnowledgeWriter writer = writers.get(i);
			status.setCompletedJobsCount(i);
			status.setStatusName(writer.getClass().getSimpleName());
			try {
				writer.writeFile(getOutputFile(writer.getClass().getSimpleName()));
			} catch (IOException e) {
				status.setCompletedJobsCount(status.getNumberOfJobs());
				throw e;
			}
		}
		status.setCompletedJobsCount(writers.size());
		
	}
	
	public File getFilesDir() {
		return this.filesDir;
	}
	
	
	public void setFilesDir(File dir) {
		this.filesDir = dir;
	}
	
	private File getOutputFile(String className) throws IOException {
		if (this.filesDir == null) {
			throw new IOException("No files destination found");
		}
		if (!this.filesDir.isDirectory()) {
			filesDir = filesDir.getParentFile();
		}
		return new File(filesDir + File.separator + KnowledgeManager.getResourceBundle().getString("filename." + className));
	}
	

	
	private ZipOutputStream makeZipOutputStream(String path) {
		FileOutputStream dest = null;
		ZipOutputStream out = null;
		try {
			dest = new FileOutputStream(path);
		} catch (FileNotFoundException e) {
//			reportIOWarning("cannot create FileOutputStream");
		}
		if (dest != null) {

			out = new ZipOutputStream(new BufferedOutputStream(dest));
		}
		return out;
	}




	private void zipTheFiles(File chosenFile, String[] files) {
		//TODO: Name der zip-datei über Extrafeld im Dialog beziehen
		if (chosenFile.isDirectory()) {
			chosenFile = new File(chosenFile.getAbsolutePath()
					+ "//KBexport.zip");
		} else {
			if (!chosenFile.getAbsolutePath().endsWith(".zip")) {
				chosenFile = new File(chosenFile.getAbsolutePath() + ".zip");
			}
		}
		int BUFFER = 2048;
		ZipOutputStream out = makeZipOutputStream(chosenFile.toString());

		BufferedInputStream origin = null;
		byte data[] = new byte[BUFFER];

		try {

			for (int i = 0; i < files.length; i++) {
				FileInputStream fi = new FileInputStream(files[i]);
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(files[i]);
				out.putNextEntry(entry);

				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
				fi.close();

				// delete File when written to the zip file
				System.gc();
				File f = new File(files[i]);
				f.delete();

			}
			out.close();
		} catch (Exception e) {
//			reportIOWarning("cannot write zip-file");
		}
	}
	
//	public void export2() {
	//
//			// [TODO] ueber URLs oder URIs laufen lassen anstatt File
//			// [TODO] loop für wiederholenden code
//			// KnOfficeExporter exporter = new
//			// KnOfficeExporter(DataManager.getInstance().getBase(),dialog.getChosenFile());
	//
//			this.rb = getResourceBundle();
	//
//			File chosenDir = fileDest;
//			if (!fileDest.isDirectory())
//				chosenDir = fileDest.getParentFile();
	//
//			LinkedList<String> allWrittenFiles = new LinkedList<String>();
	//
//			Properties knopr = new Properties(); //knopr = KnOffice-Project
//			
//			this.status.setNumberOfJobs(getNumberOfJobs());
//			this.status.setStatus(KnowledgeExporterStatus.START);
//			
	//
//			// Diagnosehierarchie
//			if (this.exportDiagnosisHierarchy) {
//				status.setStatus(KnowledgeExporterStatus.DIAGNOSIS_HIERARCHY);
//				
//				String id = "diagnosisHierarchy";
//				
//				String fileText = new DiagnosisHierarchyWriter(this).writeText();
//				
//				String name = rb.getString("filename." + id) + ".txt";
//					//prop.getProperty("filename." + id) + ".txt";
//				String path = chosenDir + File.separator + name;
	//
//				writeFile(path, fileText);
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
	//
//			// DiagnoseScores-Tabelle
//			if (this.exportDiagnoseScores) {
//				status.setStatus(KnowledgeExporterStatus.DIAGNOSIS_SCORE);
//				
//				String id = "diagnosticScores";
//				
//				String name = rb.getString("filename." + id) + ".xls";
//				String path = chosenDir + File.separator + name;
//				
//				DiagnosisScoresWriter.makeWriter(this).writeFile(
//						new File(path));
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
//			
//			// Entscheidungsbaum
//			if (this.exportDecisionTree) {
//				status.setStatus(KnowledgeExporterStatus.DECISION_TREE);
//				
//				String id = "decisionTree";
//				DecisionTreeWriter writer = new DecisionTreeWriter(this);
//				String fileText = writer.writeText();
	//
//				String name = rb.getString("filename." + id) + ".txt";
//				String path = chosenDir + File.separator + name;
//				
//				writeFile(path, fileText);
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
	//
//			// Frageklassenhierarchie
//			if (this.exportQuestionClassHierarchie) {
//				status.setStatus(KnowledgeExporterStatus.QUESTIONCLASS_HIERARCHIE);
//				String id = "questionClassHierarchy";
//				QClassHierarchyWriter writer = new QClassHierarchyWriter(this);
//				// writer.setAbstraction(abstraction);
//				String fileText = writer.writeText();
//				
//				String name = rb.getString("filename." + id) + ".txt";
//				String path = chosenDir + File.separator + name;
//				
//				writeFile(path, fileText);
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
	//
//			// Heuristische-Entscheidungstabelle
//			if (this.exportHeuristicDecisionTables) {
//				status.setStatus(KnowledgeExporterStatus.HEURISTIC_DECISION_TABLES);
//				String id = "heuristicDescisionTables";
//				
//				String name = rb.getString("filename." + id) + ".xls";
//				String path = chosenDir + File.separator + name;
//				
//				DecisionTableWriter.makeWriter(this).writeFile(
//						new File(path));
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
	//
//			// Indikationstabelle
//			if (this.exportIndicationTable) {
//				status.setStatus(KnowledgeExporterStatus.INDICATION_TABLE);
//				String id = "indicationTable";
//				
//				String name = rb.getString("filename." + id) + ".xls";
//				String path = chosenDir + File.separator + name;
//				
//				IndicationTableWriter indiWriter = IndicationTableWriter
//						.makeWriter(this);
//				indiWriter.setDefaultColumnWidth(10);
//				indiWriter.writeFile(new File(path));
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
//			
//			// Symptomabstraktionstabelle
//			if (this.exportAbstractionTable) {
//				status.setStatus(KnowledgeExporterStatus.ABSTRACTION_TABLE);
//				String id = "symptomAbstractionTable";
//				
//				String name = rb.getString("filename." + id) + ".xls";
//				String path = chosenDir + File.separator + name;
//				
//				AbstractionTableWriter.makeWriter(this).writeFile(
//						new File(path));
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
	//
//			// ComplexRules
//			if (this.exportRules) {
//				status.setStatus(KnowledgeExporterStatus.RULES);
//				String id = "rules";
//				String fileText = new RuleWriter(this).writeText();
	//
//				String name = rb.getString("filename." + id) + ".xls";
//				String path = chosenDir + File.separator + name;
//				
//				writeFile(path, fileText);
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
	//
//			// Attributtabellen
//			if (this.exportAttributeTable) {
//				status.setStatus(KnowledgeExporterStatus.ATTRUBUTE_TABLE);
//				String id = "attributeTable";
//				
//				String name = rb.getString("filename." + id) + ".xls";
//				String path = chosenDir + File.separator + name;
//				
//				AttributeTableWriter.makeWriter(this).writeFile(
//						new File(path));
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
	//
//			// Überdeckungsrelationstabellen
//			if (this.exportSetCoveringTable) {
//				status.setStatus(KnowledgeExporterStatus.SET_COVERING_TABLE);
//				
//				String id = "setCoveringTable";
//				
//				String name = rb.getString("filename." + id) + ".xls";
//				String path = chosenDir + File.separator + name;
//				
//				SetCoveringTableWriter.makeWriter(this).writeFile(
//						new File(path));
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
//			
//			// XCL
//			if (this.exportXCL) {
//				status.setStatus(KnowledgeExporterStatus.XCL);
//				
//				String id = "extendedCoveringList";
//				
//				XCLWriter writer = new XCLWriter(this);
//				// writer.setAbstraction(abstraction);
//				String fileText = writer.writeText();
//				
//				String name = rb.getString("filename." + id) + ".txt";
//				String path = chosenDir + File.separator + name;
//				
//				writeFile(path, fileText);
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
	//
//			//Ähnlichkeitswissenstabellen
//			if (this.exportSimilarityTable) {
//				status.setStatus(KnowledgeExporterStatus.SIMILARITY_TABLE);
//				
//				String id = "similarityTable";
//				
//				String name = rb.getString("filename." + id) + ".xls";
//				String path = chosenDir + File.separator + name;
//				
//				SimilarityTableWriter.makeWriter(this).writeFile(
//						new File(path));
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
//			
//			/// Fälle
//			
//			//externe Fallbasis
//			/*if (dialog.getexternalCaseRepCB().isSelected()) {
//				String id = "externalCaseRep";
//				
//				String name = dialog.getResourceBundle().getString(id+"_filename")+".xls";
//				String path = chosenDir + File.separator + name;
//				
//				CasesWriter.makeWriter(this,dialog.getChosenCaseRep()).writeFile(
//						new File(path));
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}*/
//			
//			//zur Zeit alle Fälle in eine Datei:
//			
//			if (externalCaseRep | caseManagement) {
//				String id = "cases";
//				
//				String name = rb.getString("filename." + id) + ".xls";
//				String path = chosenDir + File.separator + name;
//				
//				CasesWriter.makeWriter(this, caseRep, externalCaseRep, 
//						caseManagement, seperateDiagnoses, splitMCQuestions).writeFile(new File(path));
//				allWrittenFiles.add(path);
//				knopr.setProperty(id, name);
//			}
//			
//			
////			//Projekt-File
////			String name = rb.getString("filename.project") + ".KnOffice";
////			String path = chosenDir + File.separator + name;
////			
////			File file = new File(path);
////			
////			FileOutputStream out = new FileOutputStream(file);
////			
////			allWrittenFiles.add(path);
	////
////			knopr.store(out,"KnowME knowledge-base, represented as Office-documents");
	////
////			out.close();
	//
//			
	//
//			//Zippen
//			if (toBeZipped) {
//				String[] fileArray = new String[allWrittenFiles.size()];
//				int i = 0;
//				for (Iterator<String> iter = allWrittenFiles.iterator(); iter
//						.hasNext();) {
//					String element = iter.next();
//					fileArray[i] = element;
//					i++;
//				}
//				zipTheFiles(fileDest, fileArray);
//			}
//			status.setStatus(KnowledgeExporterStatus.DONE);
//		}



}
