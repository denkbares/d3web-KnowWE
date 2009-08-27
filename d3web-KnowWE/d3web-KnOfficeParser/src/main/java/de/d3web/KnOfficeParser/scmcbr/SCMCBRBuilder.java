package de.d3web.KnOfficeParser.scmcbr;

import java.util.List;

/**
 * Builderinterface für den SCMCR Parser
 * @author Markus Friedrich
 *
 */
public interface SCMCBRBuilder {

	void solution(int line, String text, String name);

	void question(int line, String text, String name);

	void setAmount(int line, String text, String name, Double value);

	void threshold(int line, String text, String name, Double value1,
			Double value2);

	void questionclass(int line, String text, String name);

	void answer(int line, String text, String name, String weight, String operator);

	void in(int line, String text, List<Double> values1, List<Double> values2, boolean startincluded,
			boolean endincluded);

	void into(int line, String text, List<Double> values1, List<Double> values2,
			boolean startincluded, boolean endincluded);

	void and(int line, String text, List<String> names, String weight,
			boolean or);

	void not(int line, String text, String name, String weight);

}
