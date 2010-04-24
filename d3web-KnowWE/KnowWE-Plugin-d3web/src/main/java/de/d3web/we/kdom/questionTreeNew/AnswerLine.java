package de.d3web.we.kdom.questionTreeNew;

import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionDef;
import de.d3web.we.kdom.objects.QuestionTreeAnswerDef;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectCreationError;
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
							.getDashTreeFather(dashTreeElement);
					if (dashFather != null
							&& dashFather.findSuccessor(QuestionLine.class) != null) {
						return true;
					}
				}

				return false;
			}
		};

		QuestionTreeAnswerDef aid = new QuestionTreeAnswerDef();
		aid.setSectionFinder(AllTextFinderTrimmed.getInstance());
		aid.addReviseSubtreeHandler(new createAnswerHandler());
		this.childrenTypes.add(aid);
	}

	static class createAnswerHandler implements ReviseSubTreeHandler {

		@SuppressWarnings("unchecked")
		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {

			if(s.getObjectType() instanceof QuestionTreeAnswerDef) {

				KnowledgeBaseManagement mgn = D3webModule
					.getKnowledgeRepresentationHandler(article.getWeb())
					.getKBM(article, s);

				if (mgn==null) return null;

				//"safe unsafe cast"
				Section<QuestionTreeAnswerDef> answer = s;
				String name = answer.get().getID(answer);
				Section<? extends QuestionDef> questionID = answer.get().getQuestionSection(
						answer);

				//Section<QuestionID> questionID = ((QuestionTreeAnswerID)answer.getObjectType()).getQuestionSection(answer);
				Question q = questionID.get().getObject(questionID);

				if(q instanceof QuestionChoice) {
					Answer a = mgn.addChoiceAnswer((QuestionChoice)q, name);
					answer.get().storeObject(answer, a);
					return new NewObjectCreated(a.getClass().getSimpleName()+"  "+a.getName());
				}
				return new ObjectCreationError("no choice question - " + name,
						this.getClass());
			}
			return new ObjectCreationError(null, this.getClass());
		}
	}
}
