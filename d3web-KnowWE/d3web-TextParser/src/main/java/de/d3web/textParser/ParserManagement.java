package de.d3web.textParser;

import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import de.d3web.persistence.utilities.URLUtils;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.casesTable.TextParserResource;
import de.d3web.textParser.cocor.extDecisionTreeParser.IncludeManager;
import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.decisiontree.D3DTBuilder;
import de.d3web.kernel.domainModel.*;
import de.d3web.kernel.domainModel.qasets.*;

/**
 * Manages the give files in order to construct diagnosis hierarchies,
 * qcontainer hierarchies and if applicable if at hand decision trees.
 * 
 * @author Christian Haeunke
 * @version build_20050730
 */
public class ParserManagement {

	private KnowledgeBase kb;
	///private KnowledgeBaseManagement kbm;

	private List<String> topLevelQContainers = new ArrayList<String>();

	private List<QContainer> initQuestions = new ArrayList<QContainer>();

	private Report decisionTreeReport = new Report();

	private Report diagnosisReport = new Report();

	private Report qContainerReport = new Report();

	private Report decisionTreeNotes = new Report();

	private boolean automaticalInitQContainers = true;

//	public ParserManagement(URL configFile, URL decisionTree, KnowledgeBase kb,
//			URL diagnosisHierarchy, URL qContainerHierarchy, boolean update) {
//		// String configFileS = checkForFile(configFile);
//		// String decisionTreeS = checkForFile(decisionTree);
//		// String diagnosisHierarchyS = checkForFile(diagnosisHierarchy);
//		// String qContainerHierarchyS = checkForFile(qContainerHierarchy);
//		String configFileS = null;
//		String decisionTreeS = null;
//		String diagnosisHierarchyS = null;
//		String qContainerHierarchyS = null;
//
//		if (configFile != null) {
//			configFileS = configFile.getFile();
//		}
//		if (decisionTree != null) {
//			decisionTreeS = decisionTree.getFile();
//		}
//		if (diagnosisHierarchy != null) {
//			diagnosisHierarchyS = diagnosisHierarchy.getFile();
//		}
//		if (qContainerHierarchy != null) {
//			qContainerHierarchyS = qContainerHierarchy.getFile();
//		}
//
//		setKB(kb);
//		if (diagnosisHierarchyS != null)
//			readDiagnosisHierarchy(diagnosisHierarchyS, update);
//		if (qContainerHierarchyS != null)
//			readQContainerHierarchy(qContainerHierarchyS, update);
//		if (decisionTreeS != null)
//			readDecisionTree(decisionTreeS, update);
//		// COCO: soll eventuell in den Code des QContainerHierarchyParsers
//		// übernommen werden
//		// außerdem ist automaticalInitQContainers überflüssig,
//		// !checkForInitQuestions() reicht völlig aus,
//		// denn nur wenn initQuestions hinzugefügt wurden, wird auch
//		// automaticalInitQContainers auf false gesetzt
//		if (automaticalInitQContainers && !checkForInitQuestions())
//			setInitQuestionAutomatically(kb.getRootQASet().getChildren(),
//					(QContainer) kb.getRootQASet());// TODO die Mehtode
//		fillNotes();
//	}
	
	public ParserManagement(TextParserResource configFile, TextParserResource decisionTree, KnowledgeBase kb,
			TextParserResource diagnosisHierarchy, TextParserResource qContainerHierarchy, boolean update, boolean onlySyntaxCheck, boolean complexPathConditions,boolean uniqueQuestionNames, boolean useNewParser) {
		//TODO: Jochen: syntaxCheckOnly-Modus implementieren
		setKB(kb);
		this.kb = kb;
		if (diagnosisHierarchy != null)
			readDiagnosisHierarchy(diagnosisHierarchy, update);
		if (qContainerHierarchy != null)
			readQContainerHierarchy(qContainerHierarchy, update);
		if (decisionTree != null) {
			readDecisionTree(decisionTree, update, complexPathConditions,uniqueQuestionNames, useNewParser);
		}
		// COCO: soll eventuell in den Code des QContainerHierarchyParsers
		// übernommen werden
		// außerdem ist automaticalInitQContainers überflüssig,
		// !checkForInitQuestions() reicht völlig aus,
		// denn nur wenn initQuestions hinzugefügt wurden, wird auch
		// automaticalInitQContainers auf false gesetzt
		if (automaticalInitQContainers && !checkForInitQuestions())
			setInitQuestionAutomatically(kb.getRootQASet().getChildren(),
					(QContainer) kb.getRootQASet());// TODO die Mehtode
		fillNotes();
	}

	private boolean checkForInitQuestions() {
		List iq = kb.getInitQuestions();
		if (iq.size() > 0)
			return true;
		return false;
	}

	private void setInitQuestionAutomatically(List elements, QContainer parent) {
		boolean parentInitQuestion = false;
		for (int i = 0; i < elements.size(); i++) {
			QASet view = (QASet) elements.get(i);
			if (view instanceof Question && !parentInitQuestion) {
				initQuestions.add(parent);
				parentInitQuestion = true;
			}
			if (view instanceof QContainer) {
				setInitQuestionAutomatically(view.getChildren(),
						(QContainer) view);
			}
		}
		kb.setInitQuestions(initQuestions);
	}

	// private String checkForFile(URL filename) {
	//    	
	// if(filename != null && filename.exists()) return filename.getPath();
	// else return "";
	// }

	private void fillNotes() {
		if (decisionTreeReport.getErrorCount() <= 0)
			decisionTreeReport.addAll(decisionTreeNotes.getAllMessages());
	}

	private void setKB(KnowledgeBase kb) {
		if (kb != null)
			this.kb = kb;
		else
			this.kb = new KnowledgeBase();
	}

	private Message getMessage(int row, int column, String key,
			String messageType, Object... values) {
		ResourceBundle rb = ResourceBundle.getBundle("properties.textParser");
		String result = rb.getString("parser.error.unknownError") + ": " + key;
		try {
			result = MessageFormat.format(rb.getString(key), values);
		} catch (MissingResourceException e) {
		}
		Message message = new Message(messageType, result, "", row, column, "");
		return message;
	}
	
	private void readDiagnosisHierarchy(TextParserResource in , boolean update) {
		//List dhv = readStream(in);
		doReadDiagnosisHierarchie(in, update);
	}

//	private void readDiagnosisHierarchy(String fileName, boolean update) {
//		if (fileName.endsWith(".txt")) {
//			List dhv = readTxtFile(fileName);
//			doReadDiagnosisHierarchie(fileName, update, dhv);
//		} else if (!fileName.equals(""))
//			diagnosisReport.error(getMessage(0, 0,
//					"parser.diagnosisHierarchy.error.noTxtFile", Message.ERROR,
//					fileName));
//	}
	
	private void doReadDiagnosisHierarchie(TextParserResource in, boolean update) {
		
		
		//START INCLUDE PREPROCESSING
	    URL url = in.getUrl();
	    IncludeManager includer = null;
	    if(url != null) {
	        String path = url.getFile();		
	        path = path.substring(0, path.lastIndexOf("/")+1);
	        includer = new IncludeManager(in.getDataString(),path);
	        String expandedText = includer.expand();	
	        in = new TextParserResource(new StringReader(expandedText));
	    }
	    //END INCLUDE PREPROCESSING
				
		// in KnowME a file is set in the TextParserResource
	    // but in knowledge Wiki (KnowWE) context, there´s only the a Reader set 
		if((in.getFileName() != null && new File(in.getFileName()).length() != 0) ||  in.getReader() != null){
						
			// COCO: Coco in action :-)
			de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner dhs = new de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner(
					in.getReader());
			parseDH(update, dhs,includer);
		}
		else{
			diagnosisReport.error(getMessage(0, 0,
					"parser.diagnosisHierarchy.error.emptyFile",
					Message.ERROR, in.getFileName()!=null?new File(in.getFileName()).getName():"null"));
		}
		
	}

//	private void doReadDiagnosisHierarchie(String fileName, boolean update, List dhv) {
//		if (!dhv.isEmpty() && !isEmpty(dhv)) {
//			// COCO: Coco in action :-)
//			de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner dhs = new de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner(
//					fileName);
//			parseDH(update, dhs);
//		} else {
//			diagnosisReport.error(getMessage(0, 0,
//					"parser.diagnosisHierarchy.error.emptyFile",
//					Message.ERROR, fileName));
//		}
//	}

	private void parseDH(boolean update, de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner dhs, IncludeManager includer) {
		de.d3web.textParser.cocor.diagnosisHierarchyParser.Parser dhp = new de.d3web.textParser.cocor.diagnosisHierarchyParser.Parser(
				dhs, kb, update);
		dhp.Parse();
		List l = dhp.getErrorMessages();
		if(includer != null) {
			l = includer.adaptErrors(l);
		}
		diagnosisReport.addAll(l);
		if (diagnosisReport.getErrorCount() < 1)
			diagnosisReport.note(dhp.getSuccessNote());
	}
	
	public void readQContainerHierarchy(TextParserResource in, boolean update) {
		//List qchv = readStream(in);
		doReadQContainerHierarchy(in,update);
	}

	public void readQContainerHierarchy(String fileName, boolean update) {
		if (fileName.endsWith(".txt")) {
			List qchv = readTxtFile(fileName);
			doReadQContainerHierarchy(fileName, update, qchv);
		} else if (!fileName.equals(""))
			qContainerReport.error(getMessage(0, 0,
					"parser.qContainerHierarchy.error.noTxtFile",
					Message.ERROR, fileName));
	}
	
	private void doReadQContainerHierarchy(TextParserResource in, boolean update) {
		
			// COCO: Coco in action :-)
			de.d3web.textParser.cocor.qContainerHierarchyParser.Scanner qchs = new de.d3web.textParser.cocor.qContainerHierarchyParser.Scanner(
					in.getReader());
			parseQCH(update, qchs);
		
	}

	private void doReadQContainerHierarchy(String fileName, boolean update, List qchv) {
		if (!qchv.isEmpty() && !isEmpty(qchv)) {
			// COCO: Coco in action :-)
			de.d3web.textParser.cocor.qContainerHierarchyParser.Scanner qchs = new de.d3web.textParser.cocor.qContainerHierarchyParser.Scanner(
					fileName);
			parseQCH(update, qchs);
		} else {
			qContainerReport.error(getMessage(0, 0,
					"parser.qContainerHierarchy.error.emptyFile",
					Message.ERROR, fileName));
		}
	}

	private void parseQCH(boolean update, de.d3web.textParser.cocor.qContainerHierarchyParser.Scanner qchs) {
		de.d3web.textParser.cocor.qContainerHierarchyParser.Parser qchp = new de.d3web.textParser.cocor.qContainerHierarchyParser.Parser(
				qchs, kb, update);
		qchp.Parse();
		qContainerReport.addAll(qchp.getErrorMessages());
		if (qContainerReport.getErrorCount() < 1) {
			if (update)
				qContainerReport.note(qchp.getInitNotes());
			qContainerReport.note(qchp.getSuccessNote());
		}
	}

//	private void doReadDecisionTree(String o, List<String> dtv, boolean update) {
//		if (!dtv.isEmpty() && !isEmpty(dtv)) {
//
//			
//			
//			// COCO: Coco in action :-)
//			de.d3web.textParser.cocor.extDecisionTreeParser.Scanner dts = null;
//			//Switched to extended decisionTreepParser
//			dts = new de.d3web.textParser.cocor.extDecisionTreeParser.Scanner(o);
//
//			parseDT(update, dts);
//
//		} else {
//			decisionTreeReport.error(getMessage(0, 0,
//					"parser.decisionTree.error.emptyFile", Message.ERROR, o));
//		}
//	}

	private void doReadDecisionTree(TextParserResource o, 
			boolean update, boolean complexPathConditions,boolean uniqueQuestionNames, boolean useNewParser) {
			
			//START INCLUDE PREPROCESSING
		
			URL url = o.getUrl();
			IncludeManager includer = null;
			if(url != null) {
			String path = url.getFile();
			path = path.substring(0, path.lastIndexOf("/")+1);
			includer = new IncludeManager(o.getDataString(),path);
			String expandedText = includer.expand();	
			o = new TextParserResource(new StringReader(expandedText));
			}
			//END INCLUDE PREPROCESSING
			
			
			useNewParser = true;
			if(!useNewParser) {
			
			// COCO: Coco in action :-)
			de.d3web.textParser.cocor.extDecisionTreeParser.Scanner dts = null;
//			Switched to EXTENDED decisionTreeParser
			dts = new de.d3web.textParser.cocor.extDecisionTreeParser.Scanner(o.getReader());
			
			
			parseDT(update, dts, complexPathConditions,uniqueQuestionNames, includer);
			} 
			else {
				parseDTNew(o);
			}

		

	}

	private void parseDTNew(TextParserResource o) {
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(kb);
		List<Message> messages = D3DTBuilder.parse(o.getReader(), new SingleKBMIDObjectManager(kbm));
		for (Message message : messages) {
			this.decisionTreeReport.add( message );
		}
	
}

	private void parseDT(boolean update, de.d3web.textParser.cocor.extDecisionTreeParser.Scanner dts, boolean complexPathConditions,boolean uniqueQuestionNames, IncludeManager includer) {
		de.d3web.textParser.cocor.extDecisionTreeParser.Parser dtp = new de.d3web.textParser.cocor.extDecisionTreeParser.Parser(
				dts, kb, update, complexPathConditions,uniqueQuestionNames);
		dtp.Parse();
		List l  = dtp.getErrorMessages();
		if(includer != null) {
			l = includer.adaptErrors(l);
		}
		decisionTreeReport.addAll(l);
		decisionTreeReport.addAll(createMulitipleQuestionAppearenceWarnings(dtp.getQuestionCounts(),uniqueQuestionNames));
		if (decisionTreeReport.getErrorCount() < 1) {
			// if (update)
			// decisionTreeReport.note(dtp.getInitNotes());
			for (Message m : dtp.successMessages) {
				decisionTreeReport.note(m);
			}
		}
	}

	private List<Message> createMulitipleQuestionAppearenceWarnings(HashMap<Question, Integer> questionCounts,boolean uniqueQuestionNames) {
		List<Message> list = new ArrayList<Message>();
		Set<Question> s = questionCounts.keySet();
		for (Question question : s) {
			Integer cnt = questionCounts.get(question);
			
			String text = ResourceBundle.getBundle("properties.textParser").getString("parser.decisionTree.warning.multipleQuestionAppearence.unique");
			if(!uniqueQuestionNames) {
				text = ResourceBundle.getBundle("properties.textParser").getString("parser.decisionTree.warning.multipleQuestionAppearence.nonUnique");
			}
			text = text.replace("{0}", question.getText());
			text = text.replace("{1}", Integer.toString(cnt.intValue()));
			list.add(new Message(Message.WARNING,text,null,-1,-1,null ));
		}
		return list;
	}

	public void readDecisionTree(TextParserResource in, boolean update, boolean complexPathConditions,boolean uniqueQuestionNames, boolean useNewParser) {
		
		doReadDecisionTree(in, update, complexPathConditions, uniqueQuestionNames, useNewParser);
	}

//	public void readDecisionTree(String fileName, boolean update) {
//		if (fileName.endsWith(".txt")) {
//			List dtv = readTxtFile(fileName);
//			doReadDecisionTree(fileName, dtv, update);
//		} else if (!fileName.equals(""))
//			decisionTreeReport.error(getMessage(0, 0,
//					"parser.decisionTree.error.noTxtFile", Message.ERROR,
//					fileName));
//
//	}

	public boolean isEmpty(List lines) {
		if (lines.isEmpty()) {
			return true;
		}
		for (int i = 0; i < lines.size(); i++) {
			if (!lines.get(i).toString().trim().equals("")) {
				return false;
			}
		}
		return true;
	}

	public List<String> readTxtFile(URL file) {
		List<String> lines = new ArrayList<String>();
		try {
			InputStream in = null;
			try {
				in = URLUtils.openStream(file);
			} catch (Exception e) {
				decisionTreeReport.error(new Message(
						"Fehler beim Einlesen der Datei " + file.getFile()
								+ " !"));
			}

			BufferedReader buff = new BufferedReader(new InputStreamReader(in));

			while (buff.ready() == true) {
				String check = buff.readLine();
				lines.add(check);
			}
			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
			decisionTreeReport.error(new Message(
					"Fehler beim Einlesen der Datei " + file.getFile() + " !"));
		}
		return lines;
	}

	private List<String> readStream(InputStream in) {
		List<String> lines = new ArrayList<String>();
		BufferedReader buff = new BufferedReader(new InputStreamReader(in));

		try {
			while (buff.ready() == true) {
				String check = buff.readLine();
				lines.add(check);

				
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			decisionTreeReport
					.error(new Message("Fehler beim lesen des InputStream "
							+ in.toString() + " !"));

		}
		return lines;
	}

	private List<String> readURL(URL url) {
		if (url == null) {
			return null;
		}
		List<String> lines = null;
		try {

			InputStream in = URLUtils.openStream(url);
			lines = readStream(in);

		} catch (Exception e) {
			decisionTreeReport.error(new Message(
					"Fehler beim Einlesen der Datei " + url.toString() + " !"));
		}
		return lines;
	}

	public List<String> readConfigTxtFile(String fileName) {

		File f = new File(fileName);
		String shortFileName = f.getName();
		URL configURL = this.getClass().getClassLoader().getResource(
				shortFileName);

		return readURL(configURL);

	}

	public List<String> readTxtFile(String fileName) {

		URL url = null;
		try {

			File f = new File(fileName);

			url = f.toURL();

		} catch (Exception e) {
			decisionTreeReport.error(new Message(
					"Fehler beim Einlesen der Datei " + fileName + " !"));

		}
		return readURL(url);

	}

	public List<String> getRootQContainer() {
		return topLevelQContainers;
	}

	public Report getDecisionTreeReport() {
		return decisionTreeReport;
	}

	public Report getDiagnosisReport() {
		return diagnosisReport;
	}

	public Report getQConatinerReport() {
		return qContainerReport;
	}

	public KnowledgeBase getKb() {
		return kb;
	}
}
