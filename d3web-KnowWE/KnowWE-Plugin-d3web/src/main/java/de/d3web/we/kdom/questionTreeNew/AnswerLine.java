package de.d3web.we.kdom.questionTreeNew;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionID;
import de.d3web.we.kdom.objects.QuestionTreeAnswerID;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.NewObjectCreated;
import de.d3web.we.kdom.report.ObjectCreationError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;

public class AnswerLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new ConditionalAllTextFinder() {

			@Override
			protected boolean condition(String text, Section father) {

				Section dashTreeElement = father.getFather();
				if (dashTreeElement.getObjectType() instanceof DashTreeElement) {
					Section<? extends DashTreeElement> dashFather = DashTreeElement
							.getDashTreeFather((Section<DashTreeElement>) dashTreeElement);
					if (dashFather != null
							&& dashFather.findSuccessor(new QuestionLine()) != null) {
						return true;
					}
				}

				return false;
			}
		};

		QuestionTreeAnswerID aid = new QuestionTreeAnswerID();
		aid.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR6));
		aid.setSectionFinder(AllTextFinderTrimmed.getInstance());
		aid.addReviseSubtreeHandler(new createAnswerHandler());
		this.childrenTypes.add(aid);
	}

	static class createAnswerHandler implements ReviseSubTreeHandler {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
			
			
			
			if(s.getObjectType() instanceof QuestionTreeAnswerID) {
				//"safe unsafe cast"
				Section<QuestionTreeAnswerID> answer = (Section<QuestionTreeAnswerID>) s;
				String name = answer.get().getID(answer);
				Section<QuestionID> questionID = answer.get().getQuestionSection(answer);
				
				//Section<QuestionID> questionID = ((QuestionTreeAnswerID)answer.getObjectType()).getQuestionSection(answer);
				Question q = questionID.get().getObject(questionID);
				
				
				KnowledgeBaseManagement mgn = D3webModule
				.getKnowledgeRepresentationHandler(article.getWeb())
				.getKBM(article, s);
				
				if(q instanceof QuestionChoice) {
					Answer a = mgn.addChoiceAnswer((QuestionChoice)q, name);
					answer.get().storeAnswer(answer, a);
					return new NewObjectCreated(a.getClass().getSimpleName()+"  "+a.getText());
				}
				return new ObjectCreationError(name, this.getClass());
			}
			return new ObjectCreationError(null, this.getClass());
		}
	}
}
