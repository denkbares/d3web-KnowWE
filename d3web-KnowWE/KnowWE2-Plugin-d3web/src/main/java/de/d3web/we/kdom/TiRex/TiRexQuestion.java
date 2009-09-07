package de.d3web.we.kdom.TiRex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.openrdf.model.URI;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.tirex.core.OriginalMatchAndStrategy;
import de.d3web.tirex.core.QuestionAndAnswerWithRating;
import de.d3web.tirex.core.TiRexInterpreter;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;
import de.d3web.we.terminology.D3webTerminologyHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TiRexQuestion extends DefaultAbstractKnowWEObjectType {

	Map<Section, TiRexQuestionInfo> questionStore = new HashMap<Section, TiRexQuestionInfo>();

	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return new KnowWEDomRenderer() {
			@Override
			public String render(Section sec, KnowWEUserContext user, String web,
					String topic) {
				TiRexQuestionInfo info = ((TiRexQuestion) sec.getObjectType())
						.getQuestionInfo(sec);
				String questionid = info.questinID;
				String qText = info.questionText;
				double rating = info.rating;
				String strat = info.strategy;
				String kbid = info.kbid;

				String title = "Question: " + qText + " id: " + questionid
						+ "rating:" + rating + " strat: " + strat + " kbid:"
						+ kbid;

	//			return "<i>"+DefaultDelegateRenderer.getInstance().render(sec, user, web, topic)+"</i>";
				
				return spanColorTitle(SpecialDelegateRenderer.getInstance()
						.render(sec, user, web, topic), "lightgray", title);
			}
		};
	}

	@Override
	public Collection<Section> getAllSectionsOfType() {
		return questionStore.keySet();
	}

	public TiRexQuestionInfo getQuestionInfo(Section s) {
		return questionStore.get(s);
	}

	@Override
	public List<? extends KnowWEObjectType> getAllowedChildrenTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SectionFinder getSectioner() {

		return new TiRexQuestionSectioner(this);

	}

	class TiRexQuestionSectioner extends SectionFinder {

		public TiRexQuestionSectioner(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father,
				KnowledgeRepresentationManager tm, KnowWEDomParseReport rep,
				IDGenerator idg) {
			KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
			if (handler instanceof D3webTerminologyHandler) {
				KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
						.getKBM(father.getTopic());
				TiRex.initTiRexSettings();
				String text = tmp.getOriginalText();
				List<Section> list = new ArrayList<Section>();
				if (kbm == null || kbm.getKnowledgeBase() == null)
					return list;

				TreeSet<QuestionAndAnswerWithRating> qawrSet = TiRexInterpreter
						.getInstance().extractQuestionsAndAnswers(
								kbm.getKnowledgeBase(), text,
								TiRex.getDefaultQuestionStrategies(),
								TiRex.getDefaultAnswerStrategies());

				String matchText = null;
				String qid = null;
				String qText = null;
				String kbid = null;
				Double rating = null;
				String strategyName = null;
				QuestionAndAnswerWithRating best = null;
				if (qawrSet != null && qawrSet.size() > 0) {
					best = qawrSet.first();
					OriginalMatchAndStrategy match = best.getQuestion();

					if (match != null) {
						matchText = match.getMatch();
						IDObject ob = match.getIDObject();

						qid = ob.getId();
						qText = null;
						if (ob instanceof Question) {
							qText = ((Question) ob).getText();
						}
						kbid = kbm.getKnowledgeBase().getId();
						rating = match.getRating();
						strategyName = match.getStrategy().getName();
					} else {
						return list;
					}
				} else {
					return list;
				}
				if (text.contains(matchText)) {
					int start = text.indexOf(matchText);

					Section sec = Section.createSection(this.getType(), father,
							tmp, start, start + matchText.length(), tm, rep,
							idg);
					list.add(sec);
					questionStore.put(sec, new TiRexQuestionInfo(qText, qid,
							kbid, strategyName, rating, best));
					KBMContext con = new KBMContext();
					con.setKBM(kbm);
					ContextManager.getInstance().attachContext(father, con);
				}
				return list;

			}
			return null;
		}

	}

	class TiRexQuestionInfo {
		private String questionText;
		private String questinID;
		private String kbid;
		private String strategy;
		private double rating;
		private QuestionAndAnswerWithRating match;

		public QuestionAndAnswerWithRating getMatch() {
			return match;
		}

		public String getQid() {
			return questinID;
		}

		public String getQuestionText() {
			return questionText;
		}

		public TiRexQuestionInfo(String questionText, String questionID,
				String kbid, String strategy, double rating,
				QuestionAndAnswerWithRating match) {
			this.questinID = questionID;
			this.questionText = questionText;
			this.kbid = kbid;
			this.strategy = strategy;
			this.rating = rating;
			this.match = match;

		}
	}

	@Override
	public IntermediateOwlObject getOwl(Section section) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		TiRexQuestionInfo info = getQuestionInfo(section);
		String question = info.getQuestionText();
		URI questionuri = UpperOntology2.getInstance().createlocalURI(question);
		io.addLiteral(questionuri);
		return io;
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

}
