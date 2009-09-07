package de.d3web.we.kdom.TiRex;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.tirex.core.OriginalMatchAndStrategy;
import de.d3web.tirex.core.QuestionAndAnswerWithRating;
import de.d3web.tirex.core.TiRexInterpreter;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.TiRex.TiRexQuestion.TiRexQuestionInfo;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webTerminologyHandler;

public class TiRexAnswerSectioner extends SectionFinder{

		public TiRexAnswerSectioner(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father,
				KnowledgeRepresentationManager tm, KnowWEDomParseReport rep, IDGenerator idg) {
			
			if(father.findSuccessor(this.getType().getClass()) != null) {
				return null;
			}
			
			KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
			if (handler instanceof D3webTerminologyHandler) {
				KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
						.getKBM(father.getTopic());
				TiRex.initTiRexSettings();
				String text = tmp.getOriginalText();
				List<Section> list = new ArrayList<Section>();
				if (kbm == null || kbm.getKnowledgeBase() == null
						|| text.length() < 2)
					return list;

				String matchText = null;
				String aid = null;
				String qid = null;
				String aText = null;
				String kbid = null;
				Double rating = null;
				String strategyName = null;
				QuestionAndAnswerWithRating best = null;
				List<Section> children = father.getChildren();
				for (Section section : children) {
					if (section.getObjectType() instanceof TiRexQuestion) {
						TiRexQuestionInfo info = ((TiRexQuestion) section
								.getObjectType()).getQuestionInfo(section);
						best = info.getMatch();
					}
				}

				if (best == null) {
					TreeSet<QuestionAndAnswerWithRating> qawrSet = TiRexInterpreter
							.getInstance().extractQuestionsAndAnswers(
									kbm.getKnowledgeBase(), text,
									TiRex.getDefaultQuestionStrategies(),
									TiRex.getDefaultAnswerStrategies());
					if (qawrSet != null && qawrSet.size() > 0) {
						best = qawrSet.first();
					}
				}

				if (best != null) {

					OriginalMatchAndStrategy match = best.getAnswer();

					if (match != null) {
						matchText = match.getMatch();
						IDObject ob = match.getIDObject();
						if (ob != null) {
							if (ob instanceof AnswerChoice) {
								Question q = ((AnswerChoice) ob).getQuestion();
								/* HOTFIX: Numerical RegExMatch: q is null because AnswerChoice-Object is just created independently */
								if(q == null) return list;   // TODO: fix TiRex to create valid output objects
								
								qid = q.getId();
							}

							aid = ob.getId();
							aText = null;
							if (ob instanceof AnswerChoice) {
								aText = ((AnswerChoice) ob).getText();
							}
							kbid = kbm.getKnowledgeBase().getId();
							rating = match.getRating();
							strategyName = match.getStrategy().getName();
						}
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
					TiRexAnswer a = ((TiRexAnswer)type);
					a.answerStore.put(sec, new TiRexAnswerInfo(aText, aid, kbid,
							strategyName, rating, qid));
				}
				return list;

			}
			return null;
		}

	
}
