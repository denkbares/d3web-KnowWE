package de.d3web.we.kdom.abstractiontable;

import java.util.Collection;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.SolutionReference;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableUtils;

public class CellTypeHandler extends D3webCompileScript<CellContent> {

	@Override
	public void compile(D3webCompiler compiler, Section<CellContent> section) {

		Section<?> content = section.getChildren().get(0);

		if (TableUtils.getRow(section) == 0) {
			handleHeader(compiler, content);
		}
		else {
			handleNormalCell(compiler, content);
		}
	}

	private void handleNormalCell(D3webCompiler compiler, Section<?> content) {
		Section<TableCellContent> columnHeader = TableUtils.getColumnHeader(content);
		if (columnHeader == null) {
			Messages.storeMessage(compiler, content, this.getClass(),
					Messages.error("Header is missing"));
			return;
		}
		Section<? extends Type> d3webReference = columnHeader.getChildren().get(0)
				.getChildren().get(0);
		if (Strings.isBlank(content.getText())) {
			Messages.clearMessages(compiler, content, this.getClass());
			return;
		}
		if (d3webReference != null) {
			Type type = d3webReference.get();
			if (type instanceof QuestionReference) {
				Section<QuestionReference> questionReference = Sections.cast(d3webReference,
						QuestionReference.class);
				Question question = questionReference.get().getTermObject(compiler,
						questionReference);
				if (question instanceof QuestionChoice) {
					content.setType(new AnswerReferenceCell());
					Compilers.compile(compiler, content);
				}
				else if (question instanceof QuestionNum) {
					content.setType(new QuestionNumCell());
					Compilers.compile(compiler, content);
				}
				else {
					Messages.storeMessage(compiler, content, getClass(), Messages.error("The type "
							+ question.getClass().getSimpleName()
							+ " of question '" + question.getName() + "' is not supported"));
					return;
				}
			}
			else if (type instanceof SolutionReference) {
				int columns = TableUtils.getColumns(content);
				int column = TableUtils.getColumn(content);
				if (column == columns - 1) {
					content.setType(new SolutionScoreCell());
					Compilers.compile(compiler, content);
				}
				else {
					content.setType(new SolutionStateCell());
					Compilers.compile(compiler, content);
				}
			}
			else {
				Messages.storeMessage(compiler, content, getClass(),
						Messages.error("Invalid header"));
				return;
			}
		}
		Messages.clearMessages(compiler, content, this.getClass());
	}

	private void handleHeader(D3webCompiler compiler, Section<?> content) {
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		String name = Strings.trimQuotes(content.getText());
		name = Strings.unquote(name);
		Identifier termIdentifier = new Identifier(name);
		Collection<Class<?>> termClasses = terminologyManager.getTermClasses(termIdentifier);
		if (termClasses.isEmpty()) {
			Messages.storeMessage(compiler, content, getClass(),
					Messages.noSuchObjectError("Question or Solution", name));
			return;
		}
		for (Class<?> termClass : termClasses) {
			if (Question.class.isAssignableFrom(termClass)) {
				content.setType(new QuestionReference());
				Compilers.compile(compiler, content);
				Messages.clearMessages(compiler, content, this.getClass());
				return;
			}
			else if (Solution.class.isAssignableFrom(termClass)) {
				content.setType(new SolutionReference());
				Compilers.compile(compiler, content);
				Messages.clearMessages(compiler, content, this.getClass());
				return;
			}
		}
		Messages.storeMessage(compiler, content, getClass(), Messages.error("'" + name
				+ "' is expected to be a Question or a Solution, but is a "
				+ termClasses.iterator().next().getSimpleName()));
	}

}
