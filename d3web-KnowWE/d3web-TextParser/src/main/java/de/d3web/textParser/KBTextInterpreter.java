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

/*
 * Created on 26.04.2005
 */

package de.d3web.textParser;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.rule.D3ruleBuilder;
import de.d3web.caserepository.CaseObject;
import de.d3web.caserepository.sax.CaseObjectListCreator;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.supportknowledge.DCElement;
import de.d3web.kernel.supportknowledge.DCMarkup;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.persistence.xml.PersistenceManager;
import de.d3web.persistence.xml.XCLModelPersistenceHandler;
import de.d3web.persistence.xml.mminfo.MMInfoPersistenceHandler;
import de.d3web.persistence.xml.shared.SharedPersistenceHandler;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.casesTable.CasesTableParser;
import de.d3web.textParser.casesTable.TextParserResource;
import de.d3web.textParser.cocor.extDecisionTreeParser.IncludeManager;
import de.d3web.textParser.complexRule.ComplexRuleParserManagement;
import de.d3web.textParser.complexRule.Utils;
import de.d3web.textParser.decisionTable.AttributeConfigReader;
import de.d3web.textParser.decisionTable.AttributeTableKnowledgeGenerator;
import de.d3web.textParser.decisionTable.AttributeTableSyntaxChecker;
import de.d3web.textParser.decisionTable.AttributeTableValueChecker;
import de.d3web.textParser.decisionTable.DecisionTableConfigReader;
import de.d3web.textParser.decisionTable.DecisionTableParserManagement;
import de.d3web.textParser.decisionTable.DecisionTableRuleGenerator;
import de.d3web.textParser.decisionTable.DiagnosisScoreTableRuleGenerator;
import de.d3web.textParser.decisionTable.DiagnosisScoreTableSyntaxChecker;
import de.d3web.textParser.decisionTable.DiagnosisScoreTableValueChecker;
import de.d3web.textParser.decisionTable.HeuristicDecisionTableRuleGenerator;
import de.d3web.textParser.decisionTable.HeuristicDecisionTableSyntaxChecker;
import de.d3web.textParser.decisionTable.HeuristicDecisionTableValueChecker;
import de.d3web.textParser.decisionTable.IndicationTableRuleGenerator;
import de.d3web.textParser.decisionTable.IndicationTableSyntaxChecker;
import de.d3web.textParser.decisionTable.IndicationTableValueChecker;
import de.d3web.textParser.decisionTable.KnowledgeGenerator;
import de.d3web.textParser.decisionTable.NewDecisionTableParserManagement;
import de.d3web.textParser.decisionTable.NewHeuristicDecisionTableRuleGenerator;
import de.d3web.textParser.decisionTable.NewHeuristicDecisionTableSyntaxChecker;
import de.d3web.textParser.decisionTable.NewHeuristicDecisionTableValueChecker;
import de.d3web.textParser.decisionTable.ReplacementTableKnowledgeGenerator;
import de.d3web.textParser.decisionTable.ReplacementTableSyntaxChecker;
import de.d3web.textParser.decisionTable.ReplacementTableValueChecker;
import de.d3web.textParser.decisionTable.SimilarityTableKnowledgeGenerator;
import de.d3web.textParser.decisionTable.SimilarityTableSyntaxChecker;
import de.d3web.textParser.decisionTable.SimilarityTableValueChecker;
import de.d3web.textParser.decisionTable.SymptomAbstractionTableRuleGenerator;
import de.d3web.textParser.decisionTable.SymptomAbstractionTableSyntaxChecker;
import de.d3web.textParser.decisionTable.SymptomAbstractionTableValueChecker;
import de.d3web.textParser.decisionTable.SyntaxChecker;
import de.d3web.textParser.decisionTable.ValueChecker;
import de.d3web.textParser.knowledgebaseConfig.KnowledgebaseConfigParser;
import de.d3web.textParser.xclPatternParser.XCLParserHelper;

/**
 * This class calls all text-parsers using text-based input files. The
 * information retrieved by the parsers is written into a knowledgebase if no
 * errors occur. Otherwise each parser returns a Report containing all errors
 * which were found during the parsing process.
 * 
 * @author Andreas Klar, Christian Haeunke
 */
public class KBTextInterpreter {

	public static final String DH_HIERARCHY = "diagnosis-hierarchy-file";

	public static final String QCH_HIERARCHY = "qContainer-hierarchy-file";

	public static final String QU_DEC_TREE = "question-decisionTree";

	public static final String QU_DIA_TABLE = "diagnosis-score-table";

	public static final String SET_COVERING_TABLE = "set-covering-table";

	public static final String SET_COVERING_LIST = "set-covering-list";
	
	public static final String CASE_LIST = "case-list";

	public static final String QU_RULE_DIA_TABLE = "heuristic-decision-table";

	public static final String QU_RULE_DIA_TABLE_NEW = "heuristic-decision-table-new";
	
	public static final String SYM_ABSTRACTION_TABLE = "symptom-abstraction-table";

	public static final String INDICATION_TABLE = "diagnosis-indication-table";

	public static final String ATTR_TABLE = "attribute-table";

	public static final String SIMILARITY_TABLE = "similiarity-table";

	public static final String COMPL_RULES = "complex-rules";

	public static final String CONFIG_DEC_TREE = "config-decisionTree";

	public static final String CONFIG_ATTR_TABLE = "config-attribute-table";

	public static final String CONFIG_COMPL_RULES = "config-complex-rules";

	public static final String KNOWLEDGEBASE = "knowledgebase";

	public static final String REPLACEMENT_TABLE = "replacement-table";

	public static final String CASES_TABLE = "cases-table";

	public static final String EXTERNAL_CASEREP = "external-caserepository";

	public static final String EXTERNAL_CASEREP_TABLE = "exernal-caserepository-table";

	public static final String KNOWLEDGEBASE_CONFIG = "knowledgebase-config";
	
	public static final String ANNOTATED_TEXT = "annotated-text";
	
	public static final String MULTIMEDIA_FOLDER = "multimedia-folder";
	
	public static final String SOURCE_TEXT = "source-text";

	private KnowledgeBaseManagement kbm;

	private KnowledgeBase knowledge;

	private HashMap<String, String> configParameters;

	private Collection<CaseObject> caseManCases;

	private Collection<CaseObject> extCaseRepCases;

	private Report qchReport;

	private Report dhReport;

	private Report dtReport;

	public KBTextInterpreter() {
		this(null);
	}

	public KBTextInterpreter(KnowledgeBase kb) {
		if (kb == null)
			this.kbm = KnowledgeBaseManagement.createInstance();
		else
			this.kbm = KnowledgeBaseManagement.createInstance(kb);
		this.knowledge = kbm.getKnowledgeBase();
	}

	/**
	 * Calls the different text-parsers and incremently fills the knowledgebase.
	 * Returns the Reports from all the parsing processes in a Map.
	 * 
	 * @param input
	 *            a Map containing the input files
	 * @param update
	 *            <CODE>false</CODE>, if existing knowledge for any object
	 *            should be replaced by the newly read knowledge, <CODE>true<CODE>,
	 *            if knowledge should not be overwritten
	 * @return a Map containing Reports of the computation of the input files
	 */

	public Map<String, Report> interpreteKBTexts(Map<String, URL> input,
			String kbTitle, boolean update) {
		Map<String, Report> output = new Hashtable<String, Report>();
		DecisionTableRuleGenerator.reset();
		AttributeTableKnowledgeGenerator.reset();

		// try to load existing knowledgebase
		if (input.containsKey(KBTextInterpreter.KNOWLEDGEBASE)) {
			if (!loadKnowledgeBase(input.get(KBTextInterpreter.KNOWLEDGEBASE))) {
				Report knowledgeBaseReport = new Report();
				knowledgeBaseReport.warning(new Message(
						"Bestehende Wissensbasis konnte nicht geladen werden"
								+ "\nSchreibe neue Wissensbasis", input.get(
								KBTextInterpreter.KNOWLEDGEBASE).getPath()));
				output
						.put(KBTextInterpreter.KNOWLEDGEBASE,
								knowledgeBaseReport);
			}
		}

		// set kb title
		if (kbTitle != null) {
			setKBTitle(kbTitle);
		}
		Map<String, TextParserResource> resourceMap = new HashMap();
		for (Iterator iter = input.keySet().iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			URL url = input.get(element);
			if (url != null) {
				resourceMap.put(element, TextParserResource
						.makeTextParserResource(url, true));
			}
		}

		return interpreteKBTextReaders(resourceMap, kbTitle, update, false);
	}

	public Map<String, Report> interpreteKBTextReaders(
			Map<String, TextParserResource> input, String kbTitle,
			boolean update, boolean onlySyntaxCheck) {
		Map<String, Report> output = new Hashtable<String, Report>();
		DecisionTableRuleGenerator.reset();
		AttributeTableKnowledgeGenerator.reset();

		// set kb title
		if (kbTitle != null) {
			setKBTitle(kbTitle);
		}

		// call kb config parser
		if (input.containsKey(KBTextInterpreter.KNOWLEDGEBASE_CONFIG)) {
			TextParserResource table = input
					.get(KBTextInterpreter.KNOWLEDGEBASE_CONFIG);
			if (table != null) {
				Report report = parseKnowledgebaseConfig(table);
				output.put(KBTextInterpreter.KNOWLEDGEBASE_CONFIG, report);
			}
		}else {
			Report report = parseKnowledgebaseConfig(new TextParserResource(new StringReader("")));
			output.put(KBTextInterpreter.KNOWLEDGEBASE_CONFIG, report);
		}

		// call hierarchy parser
		if (input.containsKey(KBTextInterpreter.DH_HIERARCHY)
				|| input.containsKey(KBTextInterpreter.QCH_HIERARCHY)
				|| input.containsKey(KBTextInterpreter.QU_DEC_TREE)) {
			TextParserResource dhFile = input
					.get(KBTextInterpreter.DH_HIERARCHY);
			TextParserResource qchFile = input
					.get(KBTextInterpreter.QCH_HIERARCHY);
			TextParserResource decisionTree = input
					.get(KBTextInterpreter.QU_DEC_TREE);

			TextParserResource configDT = input
					.get(KBTextInterpreter.CONFIG_DEC_TREE);

			if (!(dhFile == null && qchFile == null && decisionTree == null)) {
				parseHierarchy(decisionTree, configDT, dhFile, qchFile, update,
						onlySyntaxCheck);
				output.put(KBTextInterpreter.QCH_HIERARCHY, qchReport);
				output.put(KBTextInterpreter.DH_HIERARCHY, dhReport);
				output.put(KBTextInterpreter.QU_DEC_TREE, dtReport);
			}
		}

		// call diagnosis score table parser
		if (input.containsKey(KBTextInterpreter.QU_DIA_TABLE)) {
			TextParserResource table = input
					.get(KBTextInterpreter.QU_DIA_TABLE);
			if (table != null) {
				Report report = parseDiagnosisScoreTable(table, null, update,
						onlySyntaxCheck);
				output.put(KBTextInterpreter.QU_DIA_TABLE, report);
			}
		}

		// call heuristic decision table parser
		if (input.containsKey(KBTextInterpreter.QU_RULE_DIA_TABLE)) {
			TextParserResource table = input
					.get(KBTextInterpreter.QU_RULE_DIA_TABLE);
			if (table != null) {
				Report report = parseHeuristicDecisionTable(table, null, update);
				output.put(KBTextInterpreter.QU_RULE_DIA_TABLE, report);
			}
		}
		
//		 call new heuristic decision table parser
		if (input.containsKey(KBTextInterpreter.QU_RULE_DIA_TABLE_NEW)) {
			TextParserResource table = input
					.get(KBTextInterpreter.QU_RULE_DIA_TABLE_NEW);
			if (table != null) {
				Report report = parseNewHeuristicDecisionTable(table, null, update);
				output.put(KBTextInterpreter.QU_RULE_DIA_TABLE_NEW, report);
			}
		}
		


		// call set covering list parser
		if (input.containsKey(KBTextInterpreter.SET_COVERING_LIST)) {
			TextParserResource listData = input
					.get(KBTextInterpreter.SET_COVERING_LIST);
			
			if (listData != null) {
				Report report = parseSetCoveringList(listData);
				output.put(KBTextInterpreter.SET_COVERING_LIST, report);
			}
		}

		// call diagnosis indication table parser
		if (input.containsKey(KBTextInterpreter.INDICATION_TABLE)) {
			TextParserResource table = input
					.get(KBTextInterpreter.INDICATION_TABLE);
			if (table != null) {
				Report report = parseIndicationTable(table, update);
				output.put(KBTextInterpreter.INDICATION_TABLE, report);
			}
		}

		// // call symptom interpretation table parser
		if (input.containsKey(KBTextInterpreter.SYM_ABSTRACTION_TABLE)) {
			TextParserResource table = input
					.get(KBTextInterpreter.SYM_ABSTRACTION_TABLE);
			if (table != null) {
				Report report = parseSymptomAbstractionTable(table, update);
				output.put(KBTextInterpreter.SYM_ABSTRACTION_TABLE, report);
			}
		}

		// call attribute table parser
		if (input.containsKey(KBTextInterpreter.ATTR_TABLE)) {
			TextParserResource table = input.get(KBTextInterpreter.ATTR_TABLE);
			TextParserResource attributeList = input
					.get(KBTextInterpreter.CONFIG_ATTR_TABLE);
			if (table != null && attributeList != null /*
														 * &&
														 * attributeList.exists()
														 */) {
				Report report = parseAttributeTable(table, attributeList
						.getReader(), update);
				output.put(KBTextInterpreter.ATTR_TABLE, report);
			}
		}

		// call similarity table parser
		if (input.containsKey(KBTextInterpreter.SIMILARITY_TABLE)) {
			TextParserResource table = input
					.get(KBTextInterpreter.SIMILARITY_TABLE);
			if (table != null) {
				Report report = parseSimilarityTable(table, update);
				output.put(KBTextInterpreter.SIMILARITY_TABLE, report);
			}
		}

		// call complex rule parser
		if (input.containsKey(KBTextInterpreter.COMPL_RULES)) {
			TextParserResource complex = input
					.get(KBTextInterpreter.COMPL_RULES);
			TextParserResource config = input
					.get(KBTextInterpreter.CONFIG_COMPL_RULES);
			if (config != null /* && !config.exists() */)
				config = null;
			if (complex != null) {
				SingleKBMIDObjectManager idom = new SingleKBMIDObjectManager(kbm);
				D3ruleBuilder builder = new D3ruleBuilder(complex.getFileName(), true, idom);
		        Collection<Message> col = builder.addKnowledge(complex.getReader(), idom, null);
		        
		        List<Message> errors= (List<Message>) col;
		        Report complex_report = new Report( errors );
				output.put(KBTextInterpreter.COMPL_RULES, complex_report);
				
//				Report complex_report = parseComplexRules(complex.getReader(),
//						null, update, onlySyntaxCheck);
//				output.put(KBTextInterpreter.COMPL_RULES, complex_report);
			}
		}

		// // call replacement table parser
		if (input.containsKey(KBTextInterpreter.REPLACEMENT_TABLE)) {
			TextParserResource replace = input
					.get(KBTextInterpreter.REPLACEMENT_TABLE);
			if (replace != null) {
				Report replacement_report = parseReplacementTable(replace);
				output.put(KBTextInterpreter.REPLACEMENT_TABLE,
						replacement_report);
			}
		}

		// call case management cases table parser
		if (input.containsKey(KBTextInterpreter.CASES_TABLE)) {
			TextParserResource file = input.get(KBTextInterpreter.CASES_TABLE);
			if (file != null) {
				Report report = parseCasesTable(file, update);
				output.put(KBTextInterpreter.CASES_TABLE, report);
			}
		}
		


		// //NO KWIKI SUPPORT !
		// call external caserepository table parser
		if (input.containsKey(KBTextInterpreter.EXTERNAL_CASEREP_TABLE)) {
			TextParserResource file = input
					.get(KBTextInterpreter.EXTERNAL_CASEREP_TABLE);
			TextParserResource caserep = input
					.get(KBTextInterpreter.EXTERNAL_CASEREP);
			if (file != null && caserep != null) {
				Report report = parseExternalCaseRepTable(file, caserep, update);
				output.put(KBTextInterpreter.EXTERNAL_CASEREP_TABLE, report);
			}
		}

		return output;

	}


	



	private Report parseKnowledgebaseConfig(TextParserResource table) {
		KnowledgebaseConfigParser parser = new KnowledgebaseConfigParser(table);

		Report r = parser.parse(knowledge);
		this.configParameters = parser.getConfigMap();
		return r;

	}

	private Report parseExternalCaseRepTable(TextParserResource file,
			TextParserResource caserep, boolean update) {
		CasesTableParser parser = new CasesTableParser(kbm);
		extCaseRepCases = parser.parseFile(file);

		Report report = parser.getReport();

		if (update) {
			String targetFile = caserep.getFileName();

			if (!targetFile.endsWith(".xml") && !targetFile.endsWith(".XML")) {
				targetFile += ".xml";
			}

			CaseObjectListCreator c = new CaseObjectListCreator();
			File casefile = new File(targetFile);
			if (casefile.isFile()) {
				List previousExtCases = c.createCaseObjectList(caserep
						.getReader(), knowledge);
				extCaseRepCases = updateCases(previousExtCases, extCaseRepCases);
				report.note(new Message("Contents of external caserepository "
						+ targetFile + " will be " + "updated"));
			} else {
				report.warning(new Message("The external caserepository "
						+ targetFile + " to be "
						+ "updated does not exist, a new one will be written"));
			}
		}

		return report;
	}

	private Report parseCasesTable(TextParserResource file, boolean update) {
		CasesTableParser parser = new CasesTableParser(kbm);
		caseManCases = parser.parseFile(file);

		if (update) {
			Collection previousDefaultCaseRep = knowledge
					.getDefaultCaseRepository();
			caseManCases = updateCases(previousDefaultCaseRep, caseManCases);
		}

		return parser.getReport();
	}

	private boolean loadKnowledgeBase(URL kbFile) {
		try {
			URL fileURL = kbFile.toURI().toURL();
			PersistenceManager mgr = PersistenceManager.getInstance();
			KnowledgeBase kb = mgr.load(fileURL);
			if (kb != null) {
				this.knowledge = kb;
				this.kbm = KnowledgeBaseManagement.createInstance(kb);
				return true;
			}
			return false;
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).warning(
					"knowledgeBase could not be loaded: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Returns the previously created knowledgebase
	 * 
	 * @return KnowledgeBase
	 */
	public KnowledgeBase getKnowledgeBase() {
		return knowledge;
	}

	public Collection<CaseObject> getCaseManCases() {
		return caseManCases;
	}

	public Collection<CaseObject> getExtCaseRepCases() {
		return extCaseRepCases;
	}

	/**
	 * Writes the knowledgebase to the output file which is represented by the
	 * specified path
	 * 
	 * @param outputFile
	 *            the path of the file which the knowledgebase should be written
	 *            to
	 * @return <CODE>true<CODE> if writing process was succesful, <CODE>false<CODE>
	 *         if not
	 */
	public boolean saveKnowledgeBase(String outputFile) {
		return saveKnowledgeBase(new File(outputFile));
	}

	/**
	 * Writes the knowledgebase to the specified output file
	 * 
	 * @param outputFile
	 *            the file which the knowledgebase should be written to
	 * @return <CODE>true<CODE> if writing process was succesful, <CODE>false<CODE>
	 *         if not
	 */
	public boolean saveKnowledgeBase(File outputFile) {
		try {
			URL fileURL = outputFile.toURI().toURL();
			PersistenceManager mgr = PersistenceManager.getInstance();
			mgr.addPersistenceHandler(new MMInfoPersistenceHandler());
			mgr.addPersistenceHandler(new SharedPersistenceHandler());
			mgr.addPersistenceHandler(new XCLModelPersistenceHandler());
			mgr.save(this.knowledge, fileURL);
			return true;
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).warning(
					"KnowledgeBase could not be saved: " + e.getMessage());
			return false;
		}
	}

	public boolean isConfirmationRequired() {
		return !DecisionTableRuleGenerator.existingRules.isEmpty();
	}

	public int removeOldRules() {
		List<RuleComplex> rules = DecisionTableRuleGenerator.existingRules;
		int removedCount = 0;
		for (Iterator<RuleComplex> it = rules.iterator(); it.hasNext();)
			if (kbm.getKnowledgeBase().remove(it.next()))
				removedCount++;
		return removedCount;
	}

	private void setKBTitle(String title) {
		DCMarkup dcm = knowledge.getDCMarkup();
		dcm.setContent(DCElement.TITLE, title);
	}

	private void parseHierarchy(TextParserResource decisionTree,
			TextParserResource config, TextParserResource diagnosisHierarchy,
			TextParserResource qContainerHierarchy, boolean update,
			boolean onlySyntaxCheck) {

		boolean complexPathConditions = unboxDefaultFalse(lookUpConfigKey(KnowledgebaseConfigParser.KEY_INDICATION_RULES));
		
		boolean uniqueQuestionNames = unboxDefaultFalse(lookUpConfigKey(KnowledgebaseConfigParser.KEY_UNIQUE_QUESTION_NAMES));
		boolean unknownVisible = unboxDefaultFalse(lookUpConfigKey(KnowledgebaseConfigParser.KEY_UNKNOWN_VISIBLE));
		boolean useNewDCParser = unboxDefaultTrue(lookUpConfigKey(KnowledgebaseConfigParser.KEY_NEW_DCPARSER));
		ParserManagement dtpManagement = new ParserManagement(config,
				decisionTree, knowledge, diagnosisHierarchy,
				qContainerHierarchy, update, onlySyntaxCheck,
				complexPathConditions, uniqueQuestionNames,useNewDCParser);

		if (!unknownVisible) {
			for (Question question : knowledge.getQuestions()) {
				question.getProperties().setProperty(Property.UNKNOWN_VISIBLE,
						false);
			}
		}

		qchReport = dtpManagement.getQConatinerReport();
		dhReport = dtpManagement.getDiagnosisReport();
		dtReport = dtpManagement.getDecisionTreeReport();
	}

	private boolean unboxDefaultFalse(Boolean boolean1) {
		if(boolean1 == null) return false;
		return boolean1.booleanValue();
	}
	private boolean unboxDefaultTrue(Boolean boolean1) {
		if(boolean1 == null) return true;
		return boolean1.booleanValue();
	}

	private Boolean lookUpConfigKey(String key) {
		Boolean returnValue = null;
		if (this.configParameters != null
				&& this.configParameters.containsKey(key)) {
			String value = configParameters.get(key);
			if (value.equals(KnowledgebaseConfigParser.TRUE)) {
				returnValue = Boolean.TRUE;
			}
			if (value.equalsIgnoreCase(KnowledgebaseConfigParser.FALSE)) {
				returnValue = Boolean.FALSE;
			}
		}
		return returnValue;
	}

	private Report parseDiagnosisScoreTable(TextParserResource xlsFile,
			Reader configFile, boolean update, boolean onlySyntaxCheck) {
		// create ConfigReader
		DecisionTableConfigReader configReader = new DecisionTableConfigReader();
		if (configFile != null)
			configReader = new DecisionTableConfigReader(configFile);
		Report r = configReader.getReport();
		if (r.getErrorCount() > 0)
			return r;

		SyntaxChecker sChecker = new DiagnosisScoreTableSyntaxChecker(
				configReader);
		ValueChecker vChecker = new DiagnosisScoreTableValueChecker(
				configReader, kbm);
		KnowledgeGenerator knowGen = new DiagnosisScoreTableRuleGenerator(kbm,
				update);
		if (onlySyntaxCheck) {
			knowGen.setSyntaxCheckOnly(true);
		}

		DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
				xlsFile, sChecker, vChecker, knowGen);
		xlsParser.checkContent();
		
		if (xlsParser.getReport().getErrorCount() == 0)
			xlsParser.insertKnowledge();

		return xlsParser.getReport();
	}


	private Report parseSetCoveringList(TextParserResource file) {

		// START INCLUDE PREPROCESSING
		URL url = file.getUrl();
		IncludeManager includer = null;
		if (url != null) {
			String path = url.getFile();
			path = path.substring(0, path.lastIndexOf("/") + 1);
			includer = new IncludeManager(file.getDataString(), path);
			String expandedText = includer.expand();
			file = new TextParserResource(new StringReader(expandedText));
		}
		// END INCLUDE PREPROCESSING
		
		Boolean inheritanceEndabled = lookUpConfigKey(KnowledgebaseConfigParser.KEY_SCLIST_INHERITANCE);
		
		//FindingListParser parser = new SetCoveringListParser(file, kbm,inheritanceEndabled);
		
		//Report p =  parser.parse(knowledge);
		String text="";
		if (file.hasText()){
			text=file.getText();
		} else {
			text = new String(Utils.readBytes(file.getReader()));
		}
		
		//String [] lists = XCLParserHelper.splitToSingleLists(text);
		
		Report p = new Report();
		
		//for (String string : lists) {
			Report singleReport = XCLParserHelper.getXCLModel(kbm.getKnowledgeBase(),new StringReader(text));
			p.addAll(singleReport);
		//}
		
		
		if(includer != null) {
			includer.adaptErrors(p.getAllMessages());
		}
		return p;
	}

	private Report parseHeuristicDecisionTable(TextParserResource xlsFile,
			Reader configFile, boolean update) {
		// ConfigReader erzeugen
		DecisionTableConfigReader configReader = new DecisionTableConfigReader();
		if (configFile != null)
			configReader = new DecisionTableConfigReader(configFile);
		Report r = configReader.getReport();
		if (r.getErrorCount() > 0)
			return r;

		SyntaxChecker sChecker = new HeuristicDecisionTableSyntaxChecker(
				configReader);
		ValueChecker vChecker = new HeuristicDecisionTableValueChecker(
				configReader, kbm);
		KnowledgeGenerator knowGen = new HeuristicDecisionTableRuleGenerator(
				kbm, update);

		DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
				xlsFile, sChecker, vChecker, knowGen);
		xlsParser.checkContent();
		if (xlsParser.getReport().getErrorCount() == 0)
			xlsParser.insertKnowledge();

		return xlsParser.getReport();
	}
	
	private Report parseNewHeuristicDecisionTable(TextParserResource xlsFile,
			Reader configFile, boolean update) {
		// ConfigReader erzeugen
		DecisionTableConfigReader configReader = new DecisionTableConfigReader();
		if (configFile != null)
			configReader = new DecisionTableConfigReader(configFile);
		Report r = configReader.getReport();
		if (r.getErrorCount() > 0)
			return r;

		SyntaxChecker sChecker = new NewHeuristicDecisionTableSyntaxChecker();
		ValueChecker vChecker = new NewHeuristicDecisionTableValueChecker(
				configReader, kbm);
		KnowledgeGenerator knowGen = new NewHeuristicDecisionTableRuleGenerator(
				kbm, update);

		NewDecisionTableParserManagement xlsParser = new NewDecisionTableParserManagement(
				xlsFile, sChecker, vChecker, knowGen);
	
		xlsParser.checkContent();

		if (xlsParser.getReport().getErrorCount() == 0)
			xlsParser.insertKnowledge();

		return xlsParser.getReport();
	}

	private Report parseIndicationTable(TextParserResource xlsFile,
			boolean update) {
		SyntaxChecker sChecker = new IndicationTableSyntaxChecker();
		ValueChecker vChecker = new IndicationTableValueChecker(kbm);
		KnowledgeGenerator knowGen = new IndicationTableRuleGenerator(kbm);

		DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
				xlsFile, sChecker, vChecker, knowGen);
		xlsParser.checkContent();
		if (xlsParser.getReport().getErrorCount() == 0)
			xlsParser.insertKnowledge();

		return xlsParser.getReport();
	}

	private Report parseSymptomAbstractionTable(TextParserResource xlsFile,
			boolean update) {
		SyntaxChecker sChecker = new SymptomAbstractionTableSyntaxChecker();
		ValueChecker vChecker = new SymptomAbstractionTableValueChecker(
				new DecisionTableConfigReader(), kbm);
		KnowledgeGenerator knowGen = new SymptomAbstractionTableRuleGenerator(
				kbm, update);

		DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
				xlsFile, sChecker, vChecker, knowGen);
		xlsParser.checkContent();
		if (xlsParser.getReport().getErrorCount() == 0)
			xlsParser.insertKnowledge();

		return xlsParser.getReport();
	}

	private Report parseAttributeTable(TextParserResource xlsFile,
			Reader attributeList, boolean update) {
		// read attribute-list from File
		AttributeConfigReader attrReader = new AttributeConfigReader(
				attributeList);
		Report r = attrReader.getReport();
		if (r.getErrorCount() > 0)
			return r;

		SyntaxChecker sChecker = new AttributeTableSyntaxChecker();
		ValueChecker vChecker = new AttributeTableValueChecker(attrReader, kbm);
		KnowledgeGenerator knowGen = new AttributeTableKnowledgeGenerator(kbm,
				attrReader, update);

		DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
				xlsFile, sChecker, vChecker, knowGen);
		xlsParser.checkContent();
		
		if (xlsParser.getReport().getErrorCount() == 0)
			xlsParser.insertKnowledge();

		return xlsParser.getReport();
	}

	private Report parseSimilarityTable(TextParserResource xlsFile,
			boolean update) {
		SyntaxChecker sChecker = new SimilarityTableSyntaxChecker();
		ValueChecker vChecker = new SimilarityTableValueChecker(kbm);
		KnowledgeGenerator knowGen = new SimilarityTableKnowledgeGenerator(kbm);

		DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
				xlsFile, sChecker, vChecker, knowGen);
		xlsParser.checkContent();
		if (xlsParser.getReport().getErrorCount() == 0)
			xlsParser.insertKnowledge();

		return xlsParser.getReport();
	}

	private Report parseReplacementTable(TextParserResource xlsFile) {
		SyntaxChecker sChecker = new ReplacementTableSyntaxChecker();
		ValueChecker vChecker = new ReplacementTableValueChecker(kbm);
		KnowledgeGenerator knowGen = new ReplacementTableKnowledgeGenerator(kbm);

		DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
				xlsFile, sChecker, vChecker, knowGen);
		xlsParser.checkContent();
		if (xlsParser.getReport().getErrorCount() == 0)
			xlsParser.insertKnowledge();

		return xlsParser.getReport();
	}

	private Report parseComplexRules(Reader complexRules, Reader configFile,
			boolean update, boolean onlySyntaxCheck) {
		boolean english = false;
		if(this.configParameters != null && this.configParameters.containsKey(KnowledgebaseConfigParser.KEY_LANGUAGE_EN)) {
			english = lookUpConfigKey(KnowledgebaseConfigParser.KEY_LANGUAGE_EN);
		}
		ComplexRuleParserManagement parser = new ComplexRuleParserManagement(
				complexRules, kbm, configFile, update, onlySyntaxCheck, english);
		return parser.getReport();
	}

	/**
	 * Vergleicht die Cases anhand der alphabetischen Reihenfolge ihrer Titel
	 */
	private final class CaseLexicalComparator implements Comparator<CaseObject> {
		public int compare(CaseObject o1, CaseObject o2) {
			return o1.getDCMarkup().getContent(DCElement.TITLE).compareTo(
					o2.getDCMarkup().getContent(DCElement.TITLE));
		}
	}

	/**
	 * updates a Collection of cases, equally titled cases get replaced by the
	 * newer ones
	 */
	public Collection<CaseObject> updateCases(Collection<CaseObject> oldCases,
			Collection<CaseObject> newCases) {
		if (oldCases == null) {
			if (newCases == null)
				return null;
			else
				return newCases;
		} else if (newCases == null) {
			return oldCases;
		}

		// TODO: maybe the date shold be compared when the titles are equal ->
		// really the newer one

		TreeSet<CaseObject> cases = new TreeSet<CaseObject>(
				new CaseLexicalComparator());

		// add the new cases
		for (CaseObject co : newCases) {
			System.out.println("adding new case: "
					+ co.getDCMarkup().getContent(DCElement.TITLE));
			cases.add(co);
		}

		// add the old cases with no equally named new case
		for (CaseObject co : oldCases) {
			System.out.println("adding old case: "
					+ co.getDCMarkup().getContent(DCElement.TITLE));
			cases.add(co);
		}

		// update complete
		return cases;
	}

	private Report parseCasesTable(Reader file, boolean update) {
		CasesTableParser parser = new CasesTableParser(kbm);
		caseManCases = parser.parseFile(file);

		if (update) {
			Collection previousDefaultCaseRep = knowledge
					.getDefaultCaseRepository();
			caseManCases = updateCases(previousDefaultCaseRep, caseManCases);
		}

		return parser.getReport();
	}

	private Report parseExternalCaseRepTable(Reader file, Reader caserep,
			String caseFileName, boolean update) {
		CasesTableParser parser = new CasesTableParser(kbm);
		extCaseRepCases = parser.parseFile(file);

		Report report = parser.getReport();

		if (update) {
			String targetFile = caseFileName;

			if (!targetFile.endsWith(".xml") && !targetFile.endsWith(".XML")) {
				targetFile += ".xml";
			}

			CaseObjectListCreator c = new CaseObjectListCreator();
			File casefile = new File(targetFile);
			if (casefile.isFile()) {
				List previousExtCases = c.createCaseObjectList(caserep,
						knowledge);
				extCaseRepCases = updateCases(previousExtCases, extCaseRepCases);
				report.note(new Message("Contents of external caserepository "
						+ targetFile + " will be " + "updated"));
			} else {
				report.warning(new Message("The external caserepository "
						+ targetFile + " to be "
						+ "updated does not exist, a new one will be written"));
			}
		}

		return report;
	}
}