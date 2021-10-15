package de.d3web.we.kdom.ruletable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.condition.Condition;
import de.d3web.we.kdom.action.D3webRuleAction;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.kdom.rules.RuleAction;
import de.d3web.we.kdom.rules.RuleCompileScript;
import de.d3web.we.kdom.rules.utils.RuleCreationUtil;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.table.TableCell;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 14.09.17.
 */
@SuppressWarnings("rawtypes")
public class LineHandler implements D3webCompileScript<TableLine> {
	@Override
	public void compile(D3webCompiler compiler, Section<TableLine> section) throws CompilerMessage {
		if (TableUtils.isHeaderRow(section)) {
			Messages.clearMessages(compiler, section, getClass());
			return;
		}

		List<Section<TableCell>> cells = Sections.successors(section, TableCell.class);

		List<Condition> conditions = new ArrayList<>(cells.size());
		Map<PSAction, Section<D3webRuleAction>> actions = new HashMap<>();

		for (Section<TableCell> cell : cells) {
			Section<CompositeCondition> conditionSection = Sections.successor(cell, CompositeCondition.class);
			if (conditionSection != null) {
				Condition condition = KDOMConditionFactory.createCondition(compiler, conditionSection);
				if (condition != null) {
					conditions.add(condition);
				}
			}

			List<Section<D3webRuleAction>> actionSections = Sections.successors(cell, D3webRuleAction.class);
			for (Section<D3webRuleAction> actionSection : actionSections) {
				//noinspection unchecked
				PSAction action = actionSection.get().getAction(compiler, actionSection);
				if (action != null) {
					actions.put(action, actionSection);
				}
			}
		}

		if (actions.isEmpty() || conditions.isEmpty()) {
			int row = TableUtils.getRow(section);
			String message = "Rule for row " + row + " was not created (no "
					+ (actions.isEmpty() ? "action" : "conditions")
					+ " found)";
			throw CompilerMessage.error(message);
		}

		for (PSAction psAction : actions.keySet()) {
			Section<D3webRuleAction> actionSection = actions.get(psAction);
			//noinspection unchecked
			PSAction action = actionSection.get().getAction(compiler, actionSection);
			Class context = actionSection.get().getProblemSolverContext();
			RuleCompileScript.createRules(compiler, actionSection, RuleCreationUtil.combineConditionsToConjunction(conditions), null, Collections
					.singleton(new RuleAction(action, context)), RuleCompileScript.DEFAULT_RULE_STORE_KEY);
		}
		throw CompilerMessage.info();
	}
}
