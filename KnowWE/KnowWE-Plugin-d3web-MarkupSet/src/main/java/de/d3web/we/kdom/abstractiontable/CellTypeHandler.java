package de.d3web.we.kdom.abstractiontable;

import java.util.Collection;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.strings.Strings;
import de.d3web.strings.Identifier;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableUtils;

public class CellTypeHandler extends D3webSubtreeHandler<CellContent> {

	@Override
	public Collection<Message> create(Article article, Section<CellContent> section) {

		Section<?> content = section.getChildren().get(0);

		if (TableUtils.getRow(section) == 0) {
			return handleHeader(article, content);
		}
		else {
			return handleNormalCell(article, content);
		}
	}

	private Collection<Message> handleNormalCell(Article article, Section<?> content) {
		Section<TableCellContent> columnHeader = TableUtils.getColumnHeader(content);
		if (columnHeader == null) {
			return Messages.asList(Messages.error("Header is missing"));
		}
		Section<? extends Type> d3webReference = columnHeader.getChildren().get(0)
				.getChildren().get(0);
		if (Strings.isBlank(content.getText())) {
			return Messages.noMessage();
		}
		if (d3webReference != null) {
			Type type = d3webReference.get();
			if (type instanceof QuestionReference) {
				Section<QuestionReference> questionReference = Sections.cast(d3webReference,
						QuestionReference.class);
				Question question = questionReference.get().getTermObject(article,
						questionReference);
				if (question instanceof QuestionChoice) {
					content.setType(new AnswerReferenceCell(), article);
				}
				else if (question instanceof QuestionNum) {
					content.setType(new QuestionNumCell(), article);
				}
				else {
					return Messages.asList(Messages.error("The type "
							+ question.getClass().getSimpleName()
							+ " of question '" + question.getName() + "' is not supported"));
				}
			}
			else if (type instanceof SolutionReference) {
				int columns = TableUtils.getColumns(content);
				int column = TableUtils.getColumn(content);
				if (column == columns - 1) {
					content.setType(new SolutionScoreCell(), article);
				}
				else {
					content.setType(new SolutionStateCell(), article);
				}
			}
			else {
				return Messages.asList(Messages.error("Invalid header"));
			}
		}
		return Messages.noMessage();
	}

	private Collection<Message> handleHeader(Article article, Section<?> content) {
		TerminologyManager terminologyManager = KnowWEUtils.getTerminologyManager(article);
		String name = Strings.trimQuotes(content.getText());
		name = Strings.unquote(name);
		Identifier termIdentifier = new Identifier(name);
		Collection<Class<?>> termClasses = terminologyManager.getTermClasses(termIdentifier);
		if (termClasses.isEmpty()) {
			return Messages.asList(Messages.noSuchObjectError("Question or Solution", name));
		}
		for (Class<?> termClass : termClasses) {
			if (Question.class.isAssignableFrom(termClass)) {
				content.setType(new QuestionReference(), article);
				return Messages.noMessage();
			}
			else if (Solution.class.isAssignableFrom(termClass)) {
				content.setType(new SolutionReference(), article);
				return Messages.noMessage();
			}
		}
		return Messages.asList(Messages.error("'" + name
				+ "' is expected to be a Question or a Solution, but is a "
				+ termClasses.iterator().next().getSimpleName()));
	}
}
