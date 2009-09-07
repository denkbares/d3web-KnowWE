package de.d3web.we.kdom.TiRex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.textParser.decisionTable.DecisionTableRuleGenerator;
import de.d3web.tirex.core.TiRexSettings;
import de.d3web.tirex.core.extractionStrategies.CrossComparisonStrategy;
import de.d3web.tirex.core.extractionStrategies.DirectMatch;
import de.d3web.tirex.core.extractionStrategies.ExtractionStrategy;
import de.d3web.tirex.core.extractionStrategies.NumericalRegexMatch;
import de.d3web.tirex.core.extractionStrategies.PartOfIDObjectStrategy;
import de.d3web.tirex.core.extractionStrategies.StemmingMatch;
import de.d3web.tirex.core.extractionStrategies.SynonymWithEditDistanceMatch;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.DistributedRegistrationManager;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.TiRex.TiRexQuestion.TiRexQuestionInfo;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.kdom.typeInformation.XCLRelationInfo;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webTerminologyHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class TiRex extends AbstractXMLObjectType {

	public static final String TIREX_UPDATE_TAG = "updateTiRex";

	private static Collection<ExtractionStrategy> questionStrategies;

	private static Collection<ExtractionStrategy> answerStrategies;

	private static boolean settingsInitialized = false;

	public static Collection<ExtractionStrategy> getDefaultQuestionStrategies() {
		return questionStrategies;
	}

	public static Collection<ExtractionStrategy> getDefaultAnswerStrategies() {
		return answerStrategies;
	}

	public TiRex() {
		super("TiRex");

		questionStrategies = new ArrayList<ExtractionStrategy>();
		questionStrategies.add(CrossComparisonStrategy.getInstance());
		questionStrategies.add(DirectMatch.getInstance());
		questionStrategies.add(StemmingMatch.getInstance());
		questionStrategies.add(PartOfIDObjectStrategy.getInstance());
		questionStrategies.add(SynonymWithEditDistanceMatch.getInstance());

		answerStrategies = new ArrayList<ExtractionStrategy>();
		answerStrategies.addAll(questionStrategies);
		answerStrategies.add(NumericalRegexMatch.getInstance());
	}

	@Override
	protected void init() {
		childrenTypes.add(new TiRexBody());
	}

	public String performAction(String action, KnowWEParameterMap parameterMap) {
		return "";
	}

	public static void initTiRexSettings() {
		if (!settingsInitialized) {
			KnowWEWikiConnector loader = KnowWEEnvironment.getInstance()
					.getWikiConnector();
			String settings = null;
			if (loader.doesPageExist("TiRexSettings")) {
				settings = loader.getArticleSource("TiRexSettings");
			}

			String synonyms = null;
			if (loader.doesPageExist("TiRexSynonyms")) {
				synonyms = loader.getArticleSource("TiRexSynonyms");
			}

			String regExp = null;
			if (loader.doesPageExist("TiRexConvRegex")) {
				regExp = loader.getArticleSource("TiRexConvRegex");
			}

			TiRexSettings.createNewInstanceFromStrings(settings, synonyms,
					regExp);
		}
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return new KnowWEDomRenderer() {
			@Override
			public String render(Section sec, KnowWEUserContext user, String web,
					String topic) {

				String returnString = KnowWEEnvironment
						.maskHTML("<div style='border: 1px;border-style:solid; padding: 15px;' >")
						+ SpecialDelegateRenderer.getInstance().render(sec,
								user, web, topic)
						+ KnowWEEnvironment.maskHTML("</div>");
				// Add Button for TiRex to XCL.
				StringBuffer html = new StringBuffer();

				// not used yet
				boolean useTiRexToXCL = false;

				if (useTiRexToXCL) {
					html
							.append("<input type='button' value='"
									+ "XCL-Generieren"
									+ "'"
									+ " name='TiRexToXCL' class='button' onclick='doTiRexToXCL(\""
									+ topic + "\");'/>");

					// div for generating info
					html.append("<div id ='GeneratingTiRexToXCLInfo'>");
					html.append("</div>");
				}

				// Append masked Button to returnString
				returnString += KnowWEEnvironment.maskHTML(html.toString());

				return returnString;
			}
		};
	}

	@Override
	public void reviseSubtree(Section s, KnowledgeRepresentationManager tm, String web,
			KnowWEDomParseReport rep) {
		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
		if (handler instanceof D3webTerminologyHandler) {
			KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
					.getKBM(s.getTopic());
			List<Section> children = s.getChildren();
			String solution = s.getTopic();
			if (s.getObjectType() instanceof AbstractXMLObjectType) {
				Map<String, String> mapFor = ((AbstractXMLObjectType) s
						.getObjectType()).getMapFor(s);
				if (mapFor != null) {
					String str = mapFor.get("solution");
					if (str != null) {
						solution = str;
					}
				}
			}

			for (Section section1 : children) {

				if (section1.getObjectType() instanceof TiRexBody) {
					List<Section> subChildren = section1.getChildren();
					for (Section section : subChildren) {

						if (section.getObjectType() instanceof TiRexParagraph) {
							List<Section> tirexChunks = section.getChildren();

							for (Section chunk : tirexChunks) {
								List<Section> tirexQAs = chunk.getChildren();

								String qid = null;
								for (Section section2 : tirexQAs) {

									if (section2.getObjectType() instanceof TiRexQuestion) {
										TiRexQuestionInfo info = ((TiRexQuestion) section2
												.getObjectType())
												.getQuestionInfo(section2);
										qid = info.getQid();

									}

									if (section2.getObjectType() instanceof TiRexAnswer) {
										TiRexAnswerInfo info = ((TiRexAnswer) section2
												.getObjectType())
												.getAnswerInfo(section2);
										String aText = info.getAnswerText();
										if (qid == null)
											qid = info.getQid();

										String relID = insertXCLRelation(kbm,
												aText, qid, solution, chunk
														.getId());
										TiRexParagraph type = ((TiRexParagraph) section
												.getObjectType());
										type.storeRelation(section,
												new XCLRelationInfo(relID, kbm
														.getKnowledgeBase()
														.getId()));

									}
								}
							}
						}
					}
				}
			}
			DistributedRegistrationManager.getInstance().registerKnowledgeBase(
					kbm, s.getTopic(), web);
		}

	}

	private String insertXCLRelation(KnowledgeBaseManagement kbm, String aid,
			String qid, String solution, String kdomid) {
		KnowledgeBase kb = kbm.getKnowledgeBase();
		Question q = kbm.findQuestion(qid);
		Diagnosis s = kbm.findDiagnosis(solution);
		if (s == null) {
			s = kbm.createDiagnosis(solution, kb.getRootDiagnosis());
		}
		if (q != null && q instanceof QuestionChoice) {
			AnswerChoice a = kbm.findAnswerChoice((QuestionChoice) q, aid);
			if (a != null) {
				AbstractCondition theCondition = DecisionTableRuleGenerator
						.createCondition(q.getText(), a.getText(), kbm);

				// CREATE XCL-KNOWLEDGE
				return XCLModel.insertXCLRelation(kbm.getKnowledgeBase(),
						theCondition, s, kdomid);
			}
		}

		return null;

	}

}