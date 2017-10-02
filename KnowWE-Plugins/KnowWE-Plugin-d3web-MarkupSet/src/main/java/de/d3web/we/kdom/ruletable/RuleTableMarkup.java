package de.d3web.we.kdom.ruletable;

import java.util.ArrayList;
import java.util.List;

import com.denkbares.strings.Strings;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.NoAnswerException;
import de.d3web.core.inference.condition.UnknownAnswerException;
import de.d3web.core.session.Session;
import de.d3web.we.kdom.rules.action.RuleAction;
import de.d3web.we.kdom.rules.condition.ConditionContainer;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableIndexConstraint;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 14.09.17.
 */
public class RuleTableMarkup extends DefaultMarkupType

{

	private static DefaultMarkup MARKUP = null;


	static {
		MARKUP = new DefaultMarkup("RuleTable");
		Table content = new Table();
		MARKUP.addContentType(content);
		PackageManager.addPackageAnnotation(MARKUP);


		/*
		Header Row: cells 0 to n-1, 0
		 */
		CondHeaderCellEntry headerCellType = new CondHeaderCellEntry();
		TableIndexConstraint condHeaderCellLine = new TableIndexConstraint();
		condHeaderCellLine.setRowConstraints(0, 1); // first row
		condHeaderCellLine.setColumnConstraintsFromLast(Integer.MAX_VALUE, 1); // all but last col
		headerCellType.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				condHeaderCellLine));
		content.injectTableCellContentChildtype(headerCellType);




		/*
		Inner cell entries: cells 0 to n-1 , 0 to n-1
		 */
		ConditionCellEntry cellEntry = new ConditionCellEntry();
		TableIndexConstraint conditionCellsConstraint = new TableIndexConstraint();
		conditionCellsConstraint.setRowConstraints(1, Integer.MAX_VALUE); // all but first row
		conditionCellsConstraint.setColumnConstraintsFromLast(Integer.MAX_VALUE, 1); // all but last col
		cellEntry.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				conditionCellsConstraint));
		content.injectTableCellContentChildtype(cellEntry);


			/*
		Cell 0,n
		 */
		ActionHeaderCellEntry actionHeaderCellType = new ActionHeaderCellEntry();
		TableIndexConstraint tableIndexConstraint = new TableIndexConstraint();
		tableIndexConstraint.setRowConstraints(0, 1); // first row
		tableIndexConstraint.setColumnConstraintsFromLast(1, 0); // just the last column
		actionHeaderCellType.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				tableIndexConstraint));
		content.injectTableCellContentChildtype(actionHeaderCellType);


		/*
		Action column (last column): cells n, 1 to n
		 */
		ActionCellEntry actionCellType = new ActionCellEntry();
		TableIndexConstraint tableIndexConstraintAction = new TableIndexConstraint();
		tableIndexConstraintAction.setRowConstraints(1, Integer.MAX_VALUE); // start with first, then all
		tableIndexConstraintAction.setColumnConstraintsFromLast(1, 0); // just the last column
		actionCellType.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				tableIndexConstraintAction));
		content.injectTableCellContentChildtype(actionCellType);


	}

	public RuleTableMarkup() {
		super(MARKUP);
	}


	static class ConditionCellEntry extends AbstractType {

		public ConditionCellEntry() {
			ConditionContainer condition = new ConditionContainer();
			condition.setSectionFinder(AllTextFinderTrimmed.getInstance());
			this.addChildType(condition);
		}
	}


	static class CondHeaderCellEntry extends AbstractType {

	}

	static class ActionHeaderCellEntry extends AbstractType {

	}

	static class ActionCellEntry extends AbstractType {
		public ActionCellEntry() {
			RuleAction action = new RuleAction();
			this.setRenderer(new ActionHighlightingRenderer());
			this.addChildType(action);
		}
	}


	/**
	 * Highlights Rules according to state.
	 *
	 * @author Albrecht Striffler
	 */
	private static class ActionHighlightingRenderer implements Renderer {

		@Override
		public void render(Section<?> sec, UserContext user, RenderResult string) {

			D3webCompiler compiler = Compilers.getCompiler(sec, D3webCompiler.class);
			Session session = null;

			List<String> classes = new ArrayList<>();
			classes.add("d3webRule");

			string.appendHtml("<span id='" + sec.getID() + "' class='" + Strings.concat(" ", classes) + "'>");
			DelegateRenderer.getInstance().render(sec, user, string);
			string.appendHtml("</span>");
		}
	}

	public static String evaluateSessionCondition(Session session, Condition condition) {
		try {
			return condition.eval(session) ? "conditionTrue" : "conditionFalse";
		} catch (UnknownAnswerException e) {
			return "conditionUnknown";
		} catch (NoAnswerException ignore) {
			return "conditionUndefined";
		}
	}


}
