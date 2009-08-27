package de.d3web.KnOfficeParser.rule;

import java.util.List;
/**
 * Inteface für Builder des Complexrules Parser
 * @author Markus Friedrich
 *
 */
public interface RuleBuilder {

	void indicationrule(int line, String linetext, List<String> names,
			List<String> types, boolean except, boolean instant, boolean not);

	void suppressrule(int line, String linetext, String qname, String type,
			List<String> anames, boolean except);

	void numValue(int line, String linetext, boolean except, String op);

	void questionOrDiagnosis(int line, String linetext, String s, String type);

	void choiceOrDiagValue(int line, String linetext, String op, String value,
			boolean except);

	void formula(int line, String linetext, String value);

	void formulaAdd();

	void formulaSub();

	void formulaMult();

	void formulaDiv();

}
