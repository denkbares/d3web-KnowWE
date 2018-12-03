package de.d3web.we.kdom.abstractiontable;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.values.Unknown;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.D3webTerm;
import de.d3web.we.object.ValueTooltipRenderer;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableUtils;

public class CellContentValue extends AbstractType implements D3webTerm<NamedObject>, RenamableTerm {

	public static final String CELL_CONTENT_TYPE_KEY = "CellContentTypeKey";
	public static final String RENDER_COLOR = "color:rgb(125, 80, 102)";

	public enum CellType {
		ANSWER_REFERENCE, QUESTION_NUM_VALUE, SOLUTION_SCORE, SOLUTION_STATE, QUESTION_REFERENCE, SOLUTION_REFERENCE, NONE
	}

	public CellContentValue() {
		this.setSectionFinder(AllTextFinder.getInstance());
		this.setRenderer(new CellRenderer());
		this.addCompileScript(new MessagesScript());
	}

	public void setType(D3webCompiler compiler, Section<CellContentValue> section, CellType tpye) {
		KnowWEUtils.storeObject(compiler, section, CELL_CONTENT_TYPE_KEY, tpye);
	}

	public CellType getType(TermCompiler compiler, Section<? extends Term> section) {
		CellType type = (CellType) KnowWEUtils.getStoredObject(compiler, section, CELL_CONTENT_TYPE_KEY);
		if (type == null) type = CellType.NONE;
		return type;
	}

	public Class<?> getTermObjectClass(D3webCompiler compiler, Section<? extends Term> section) {
		CellType type = getType(compiler, section);
		if (type == CellType.QUESTION_REFERENCE) {
			return Question.class;
		}
		else if (type == CellType.SOLUTION_REFERENCE) {
			return Solution.class;
		}
		else if (type == CellType.ANSWER_REFERENCE) {
			return Choice.class;
		}
		else {
			return null;
		}
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		if (compiler == null) return NamedObject.class;
		return getTermObjectClass(compiler, section);
	}

	@Override
	public Identifier getTermIdentifier(TermCompiler compiler, Section<? extends Term> section) {
		if (compiler == null) return new Identifier(getTermName(section));
		CellType type = getType(compiler, section);
		if (type == CellType.ANSWER_REFERENCE) {
			Section<CellContentValue> headerContent = TableUtils.getColumnHeader(section, CellContentValue.class);
			return new Identifier(getTermName(headerContent), getTermName(section));

		}
		else {
			return new Identifier(getTermName(section));
		}
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		String text = Strings.trim(section.getText());
		if (isNegatedWithExclamation(text)) {
			text = text.replaceAll("^!=?", "");
		}
		else {
			if (isNegatedWithNOT(text)) {
				text = text.replaceAll("^NOT\\s*[\\(\\[]", "");
				text = text.replaceAll("[\\)\\]]\\z", "");
			}
		}
		return Strings.trimQuotes(text);
	}

	public boolean isNegated(Section<? extends Term> section) {
		return isNegatedWithExclamation(section.getText()) || isNegatedWithNOT(section.getText());
	}

	private boolean isNegatedWithNOT(String text) {
		text = Strings.trim(text);
		int indexOfNOT = Strings.indexOf(text, 0, Strings.UNQUOTED, "NOT");
		return indexOfNOT == 0;
	}

	private boolean isNegatedWithExclamation(String text) {
		text = Strings.trim(text);
		int indexOfExclamation = Strings.indexOf(text, 0, Strings.UNQUOTED, "!=", "!");
		return indexOfExclamation == 0;
	}

	@Override
	public NamedObject getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<NamedObject>> content) {
		CellType type = getType(compiler, content);
		if (type == CellType.ANSWER_REFERENCE) {
			if (isKnown(content) || isUnknown(content)) return null;
			Identifier termIdentifier = getTermIdentifier(compiler, content);
			NamedObject termObject = D3webUtils.getTermObject(compiler, termIdentifier);
			if (termObject == null) {
				Section<CellContentValue> columnHeader = TableUtils.getColumnHeader(content, CellContentValue.class);
				NamedObject question = getTermObject(compiler, columnHeader);
				if (!(question instanceof QuestionChoice)) return null;
				termObject = KnowledgeBaseUtils.findChoice((QuestionChoice) question, getTermName(content), false);
			}
			return termObject;

		}
		else {
			return D3webUtils.getTermObject(compiler, getTermIdentifier(compiler, content));
		}
	}

	private class MessagesScript implements D3webCompileScript<CellContentValue> {

		@Override
		public void compile(D3webCompiler compiler, Section<CellContentValue> section) throws CompilerMessage {

			if (TableUtils.getRow(section) == 0) {
				handleHeader(compiler, section);
			}
			else {
				setCellType(compiler, section);
			}
			if (isUnknown(section) || isKnown(section)) {
				return;
			}
			TerminologyManager termManager = compiler.getTerminologyManager();
			Class<?> termObjectClass = getTermObjectClass(compiler, section);
			if (termObjectClass != null) {
				Identifier termIdentifier = getTermIdentifier(compiler, section);
				termManager.registerTermReference(compiler, section, termObjectClass, termIdentifier);
				if (termManager.isDefinedTerm(termIdentifier)) {
					throw new CompilerMessage();
				}
				else if (termObjectClass.equals(Choice.class)) {
					Section<CellContentValue> columnHeader = TableUtils.getColumnHeader(section, CellContentValue.class);
					NamedObject termObject = getTermObject(compiler, columnHeader);
					if (termObject instanceof QuestionYN) {
						String choiceName = section.get().getTermName(section);
						Choice choice = KnowledgeBaseUtils.findChoice((QuestionYN) termObject, choiceName, false);
						if (choice != null) throw new CompilerMessage();
					}
				}
				throw new CompilerMessage(Messages.noSuchObjectError(termObjectClass.getSimpleName(), getTermName(section)));
			}
		}

		private void setCellType(D3webCompiler compiler, Section<CellContentValue> section) throws CompilerMessage {
			Section<TableCellContent> columnHeader = TableUtils.getColumnHeader(section);
			if (columnHeader == null) {
				throw CompilerMessage.error("Header is missing");
			}
			Section<CellContentValue> headerContent = Sections.successor(columnHeader, CellContentValue.class);
			if (Strings.isBlank(section.getText())) {
				Messages.clearMessages(compiler, section, this.getClass());
				return;
			}
			if (headerContent != null) {
				CellType type = headerContent.get().getType(compiler, headerContent);
				if (type == CellType.QUESTION_REFERENCE) {
					NamedObject namedObject = getTermObject(compiler, headerContent);
					if (namedObject instanceof QuestionChoice) {
						setType(compiler, section, CellType.ANSWER_REFERENCE);
					}
					else if (namedObject instanceof QuestionNum) {
						setType(compiler, section, CellType.QUESTION_NUM_VALUE);
					}
					else {
						throw CompilerMessage.error("The type "
								+ namedObject.getClass().getSimpleName()
								+ " of question '" + namedObject.getName()
								+ "' is not supported");
					}
				}
				else if (type == CellType.SOLUTION_REFERENCE) {
					int columns = TableUtils.getColumns(section);
					int column = TableUtils.getColumn(section);
					if (column == columns - 1) {
						setType(compiler, section, CellType.SOLUTION_SCORE);
					}
					else {
						setType(compiler, section, CellType.SOLUTION_STATE);
					}
				}
				else {
					throw CompilerMessage.error("Invalid header");
				}
			}
			Messages.clearMessages(compiler, section, this.getClass());
		}

		private void handleHeader(D3webCompiler compiler, Section<CellContentValue> section) throws CompilerMessage {
			NamedObject namedObject = getTermObject(compiler, section);
			if (namedObject == null) {
				throw new CompilerMessage(Messages.noSuchObjectError("Question or Solution", section.getText()));
			}
			if (namedObject instanceof Question) {
				setType(compiler, section, CellType.QUESTION_REFERENCE);
			}
			else if (namedObject instanceof Solution) {
				setType(compiler, section, CellType.SOLUTION_REFERENCE);
			}
			else {
				throw CompilerMessage.error("'" + section.getText()
						+ "' is expected to be a Question or a Solution, but is a "
						+ namedObject.getClass().getSimpleName());
			}
			Messages.clearMessages(compiler, section, this.getClass());
		}
	}

	public boolean isUnknown(Section<? extends Term> section) {
		return Unknown.getInstance().getValue().toString().equalsIgnoreCase(section.get(Term::getTermName));
	}

	public boolean isKnown(Section<? extends Term> section) {
		return "known".equalsIgnoreCase(section.get(Term::getTermName));
	}

	private class CellRenderer implements Renderer {
		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			Renderer renderer = PlainText.getInstance().getRenderer();
			D3webCompiler compiler = D3webUtils.getCompiler(section);
			CellType type = CellContentValue.this.getType(compiler, Sections.cast(section, CellContentValue.class));
			if (compiler != null) {
				if (type == CellType.QUESTION_REFERENCE) {
					renderer = new ValueTooltipRenderer(StyleRenderer.Question);
				}
				else if (type == CellType.SOLUTION_REFERENCE) {
					renderer = StyleRenderer.SOLUTION;
				}
				else if (type == CellType.ANSWER_REFERENCE) {
					renderer = StyleRenderer.CHOICE;
				}
				else if (type == CellType.QUESTION_NUM_VALUE) {
					renderer = new StyleRenderer(RENDER_COLOR) {

						@Override
						protected void renderContent(Section<?> section, UserContext user, RenderResult string) {
							string.appendJSPWikiMarkup(Strings.encodeHtml(section.getText().replace("~", "")));
						}

					};
				}
				else if (type == CellType.SOLUTION_SCORE || type == CellType.SOLUTION_STATE) {
					renderer = new StyleRenderer(RENDER_COLOR, StyleRenderer.MaskMode.htmlEntities);
				}
			}
			renderer.render(section, user, result);
		}
	}
}
