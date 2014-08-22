package de.d3web.we.kdom.action.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.strings.Strings;
import de.d3web.we.kdom.condition.NonTerminalCondition;
import de.d3web.we.kdom.condition.helper.BracedCondition;
import de.d3web.we.kdom.condition.helper.BracedConditionContent;
import de.d3web.we.kdom.condition.helper.CompCondLineEndComment;
import de.d3web.we.kdom.condition.helper.ConjunctSectionFinder;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;

public class CompositeFormula extends AbstractType {

	private final TerminalExpression terminalExpression = new TerminalExpression();

	public CompositeFormula() {
		// this composite takes everything it gets => needs suitable wrapper
		// type as father
		this.setSectionFinder(new AllTextFinderTrimmed());
		// this.setCustomRenderer(new
		// de.d3web.we.kdom.renderer.KDOMDepthFontSizeRenderer());

		// a composite condition may either be a BracedCondition,...
		BracedCondition braced = new BracedCondition(); // contains the
														// brackets
		// and the
		// endline-comments
		this.addChildType(braced);
		BracedConditionContent bracedContent = new BracedConditionContent(); // without
		// brackets
		// and
		// comments
		braced.addChildType(bracedContent);
		braced.addChildType(new CompCondLineEndComment()); // explicit nodes for
		// the
		// endline-comments
		bracedContent.addChildType(this);

		// ...a Additive expression,...
		Addition conj = new Addition();
		this.addChildType(conj);
		conj.addChildType(this); // Additions again allow for a
		// CompositeExpression

		// ... a Subtractive expression,...
		Subtraction sub = new Subtraction();
		this.addChildType(sub);
		sub.addChildType(this); // Subtractions again allow for a
		// CompositeExpression

		// ... a multiplicative expression,...
		Multiplication mult = new Multiplication();
		this.addChildType(mult);
		mult.addChildType(this); // Multiplications again allow for a
		// CompositeExpression

		// ... a dividing expression,...
		Division div = new Division();
		this.addChildType(div);
		div.addChildType(this); // Divisions again allow for a
		// CompositeExpression

		// ... or finally a TerminalExpression which stops the recursive descent
		this.addChildType(terminalExpression);

	}

	/**
	 * Sets the set of terminalConditions for this CompositeCondition
	 * 
	 * Any terminal that is not accepted by one of these will be marked by an
	 * UnrecognizedTerminalCondition causing an error
	 * 
	 * @param types
	 */
	public void setAllowedTerminalConditions(List<Type> types) {
		terminalExpression.setAllowedTerminalConditions(types);
	}

	/**
	 * tells whether a CompositeCondition is a bracedexpression
	 * 
	 * @param c
	 * @return
	 */
	public boolean isBraced(Section<CompositeFormula> c) {
		return getBraced(c) != null;
	}

	/**
	 * returns the BracedCondition
	 * 
	 * @param c
	 * @return
	 */
	public Section<? extends NonTerminalCondition> getBraced(Section<CompositeFormula> c) {
		return Sections.child(c, BracedCondition.class);
	}

	/**
	 * returns the conjunts of a conjunction
	 * 
	 * @param c
	 * @return
	 */
	public List<Section<? extends CalcMethodType>> getMultiplicationElements(Section<CompositeFormula> c) {
		List<Section<? extends CalcMethodType>> result = new ArrayList<Section<? extends CalcMethodType>>();
		List<Section<Multiplication>> childrenOfType = Sections.children(c,
				Multiplication.class);
		result.addAll(childrenOfType);
		return result;
	}

	/**
	 * tells whether a CompositeCondition is a Conjunction
	 * 
	 * @param c
	 * @return
	 */
	public boolean isMultiplication(Section<CompositeFormula> c) {
		return getMultiplicationElements(c).size() > 0;
	}

	/**
	 * tells whether a CompositeCondition is a Conjunction
	 * 
	 * @param c
	 * @return
	 */
	public boolean isSubtraction(Section<CompositeFormula> c) {
		return getSubtractionElements(c).size() > 0;
	}

	/**
	 * returns the conjunts of a conjunction
	 * 
	 * @param c
	 * @return
	 */
	public List<Section<? extends CalcMethodType>> getSubtractionElements(Section<CompositeFormula> c) {
		List<Section<? extends CalcMethodType>> result = new ArrayList<Section<? extends CalcMethodType>>();
		List<Section<Subtraction>> childrenOfType = Sections.children(c,
				Subtraction.class);
		result.addAll(childrenOfType);
		return result;
	}

	/**
	 * tells whether a CompositeCondition is a Conjunction
	 * 
	 * @param c
	 * @return
	 */
	public boolean isAddition(Section<CompositeFormula> c) {
		return getAdditionElements(c).size() > 0;
	}

	/**
	 * returns the conjunts of a conjunction
	 * 
	 * @param c
	 * @return
	 */
	public List<Section<? extends CalcMethodType>> getAdditionElements(Section<CompositeFormula> c) {
		List<Section<? extends CalcMethodType>> result = new ArrayList<Section<? extends CalcMethodType>>();
		List<Section<Addition>> childrenOfType = Sections.children(c, Addition.class);
		result.addAll(childrenOfType);
		return result;
	}

	/**
	 * tells whether a CompositeCondition is a Conjunction
	 * 
	 * @param c
	 * @return
	 */
	public boolean isDivision(Section<CompositeFormula> c) {
		return getDivisionElements(c).size() > 0;
	}

	/**
	 * returns the conjunts of a conjunction
	 * 
	 * @param c
	 * @return
	 */
	public List<Section<? extends CalcMethodType>> getDivisionElements(Section<CompositeFormula> c) {
		List<Section<? extends CalcMethodType>> result = new ArrayList<Section<? extends CalcMethodType>>();
		List<Section<Division>> childrenOfType = Sections.children(c, Division.class);
		result.addAll(childrenOfType);
		return result;
	}

	/**
	 * tells whether this CompositeCondition is a TerminalCondition
	 * 
	 * @param c
	 * @return
	 */
	public boolean isTerminal(Section<CompositeFormula> c) {
		return getTerminal(c) != null;
	}

	/**
	 * returns the TerminalCondition of a (terminal-)CompositeCondition
	 * 
	 * @param c
	 * @return
	 */
	public Section<? extends TerminalExpression> getTerminal(Section<CompositeFormula> c) {
		// List<Section<? extends TerminalExpression>> result = new
		// ArrayList<Section<? extends TerminalExpression>>();
		Section<? extends TerminalExpression> terminal = Sections.child(c,
				TerminalExpression.class);
		return terminal;
	}

	/**
	 * 
	 * @created 03.08.2010
	 * @param trimmed
	 * @return
	 */
	static boolean hasLineBreakAfterComment(String text) {
		int start = Strings.lastIndexOfUnquoted(text, "//");
		if (start != -1) {
			Pattern pattern = Pattern.compile("\\r?\\n");
			Matcher matcher = pattern.matcher(text);
			int lineBreak = -1;
			while (matcher.find()) {
				// attempts to find the last line break
				lineBreak = matcher.start();
			}
			if (lineBreak > start) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @author Jochen
	 * 
	 *         Type for a conjunct element in the CompositeCondition
	 * 
	 *         example: 'a AND b' here 'a' and 'b' are nodes of type conjunct
	 * 
	 */
	abstract class CalcMethodType extends NonTerminalCondition implements de.knowwe.core.kdom.ExclusiveType {

		public CalcMethodType(String sign) {

			this.setSectionFinder(ConjunctSectionFinder.createConjunctFinder(new String[] { sign }));
		}

	}

	class Division extends CalcMethodType {

		public Division() {
			super("/");
		}
	}

	class Multiplication extends CalcMethodType {

		public Multiplication() {
			super("*");
		}
	}

	class Subtraction extends CalcMethodType {

		public Subtraction() {
			super("-");
		}
	}

	class Addition extends CalcMethodType {

		public Addition() {
			super("+");
		}
	}

}
