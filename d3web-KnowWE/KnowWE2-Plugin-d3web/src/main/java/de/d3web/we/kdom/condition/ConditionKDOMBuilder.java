package de.d3web.we.kdom.condition;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.KnOfficeParser.ConditionBuilder;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.Annotation.FindingAnswer;
import de.d3web.we.kdom.Annotation.FindingComparator;
import de.d3web.we.kdom.Annotation.FindingQuestion;
import de.d3web.we.kdom.rules.RuleCondLine;
/**
 * Klasse um den KDOM Tree mithilfe des ANTLR Parsers zu Erstellen
 * @author Markus Friedrich
 *
 */
public class ConditionKDOMBuilder implements ConditionBuilder {

	private Stack<Section> sections = new Stack<Section>();
	
	private String topic;
	
	private IDGenerator idgen;
	
	
	public ConditionKDOMBuilder(String topic, IDGenerator idgen) {
		this.topic = topic;
		this.idgen = idgen;
	}
	
	@Override
	public void all(int line, String linetext, String question, String type,
			List<String> answers) {
		if(linetext == null) return;

	}

	@Override
	public void andcond(String text) {
		if(text == null) return;
		if (sections.size() >= 2) {
			Section disjunct = Section.createExpandedSection(text, new Conjunct(), null, -1, topic, null, null, idgen);
			Section second = sections.pop();
			disjunct.addChild(sections.pop());
			
			Pattern andPattern = Pattern.compile("( +AND +)" + Pattern.quote(second.getOriginalText()));
			Matcher m = andPattern.matcher(text);
			String and = " AND ";
			int offset = 0;
			if (m.find()) {
				and = m.group(1);
				offset = m.start();
			}
			
			Section.createExpandedSection(and, new AndOperator(), disjunct, offset, topic, null, null, idgen);
			disjunct.addChild(second);
			sections.push(disjunct);
		}
	}
	
	@Override
	public void orcond(String text) {
		if(text == null) return;
		if (sections.size() >= 2) {
			Section cf = Section.createExpandedSection(text, new ComplexFinding(), null, -1, topic, null, null, idgen);
			Section second = sections.pop();
			cf.addChild(sections.pop());
			
			Pattern orPattern = Pattern.compile("( +OR +)" + Pattern.quote(second.getOriginalText()));
			Matcher m = orPattern.matcher(text);
			String or = " OR ";
			int offset = 0;
			if (m.find()) {
				or = m.group(1);
				offset = m.start();
			}
			
			Section.createExpandedSection(or, new OrOperator(), cf, offset, topic, null, null, idgen);
			cf.addChild(second);
			sections.push(cf);
		}
	}

	@Override
	public void complexcondition(String text) {
		if(sections.size() == 0) {
			return;
		}
		String content = sections.peek().getOriginalText();
		Pattern bracedPattern = Pattern.compile("( *\\( *)(" + Pattern.quote(content) + ")( *\\) *)");
		Matcher m = bracedPattern.matcher(text);
		if(m.find()) {
			Section bracedCond = Section.createExpandedSection(text, new ComplexFindingBraced(), null, m.start(2), topic, null, null, idgen);
			Section open = Section.createExpandedSection(m.group(1), new ConditionBracketOpen(), bracedCond, m.start(1), topic, null, null, idgen);
			//bracedCond.addChild(open);
			Section cond = sections.pop();
			bracedCond.addChild(cond);
			Section close = Section.createExpandedSection(m.group(3), new ConditionBracketClose(), bracedCond, m.start(3), topic, null, null, idgen);
			//bracedCond.addChild(close);
			sections.push(bracedCond);
		}
	}
	
	public Section peek() {
		if(sections.size() == 0) return null;
		return sections.peek();
	}

	@Override
	public void condition(int line, String linetext, String qname, String type,
			String op, String value) {
		if(linetext == null) return;		
		Section cond = Section.createExpandedSection(linetext, new Finding(), null, -1, topic, null, null, idgen);
		sections.add(cond);
		if(qname == null || op == null || value == null) return;
		
		Pattern condPattern = Pattern.compile("\\A(\"?" + Pattern.quote(qname) + "\"?)( +" + Pattern.quote(op) 
				+ " +)(\"?" + Pattern.quote(value) + "\"?)");
		Matcher m = condPattern.matcher(linetext);
		
		int offset1 = 1;
		int offset2 = 2;
		int offset3 = 3;
		
		if (m.find()) {
			offset1 = m.start(1);
			qname = m.group(1);
			
			offset2 = m.start(2);
			op = m.group(2);

			offset3 = m.start(3);
			value = m.group(3);
		}
		
		Section.createExpandedSection(qname, new FindingQuestion(), cond, offset1, topic, null, null, idgen);
		Section.createExpandedSection(op, new FindingComparator(), cond, offset2, topic, null, null, idgen);
		Section.createExpandedSection(value, new FindingAnswer(), cond, offset3, topic, null, null, idgen);
	}

	@Override
	public void condition(int line, String linetext, String qname, String type,
			double left, double right, boolean in) {
		
		Section cond = Section.createExpandedSection(linetext, new RuleCondLine(), null, -1, topic, null, null, idgen);
		sections.add(cond);
		
		int offset1 = 1;
		
		int offset2 = 2;
		String inString = " IN ";
		
		int offset3 = 3;
		String bracketOpen = "[";
		
		int offset4 = 4;
		String bracketClose = "]";
		
		int offset5 = 5;
		String leftBorder = trimZeros(Double.toString(left)); 
		
		int offset6 = 6;
		String rightBorder = trimZeros(Double.toString(right));
		
		Pattern condPattern = Pattern.compile("\\A( *\"" + Pattern.quote(qname) + "\")( +IN +)?(\\[ *)(" 
				+ leftBorder + " +)(" + rightBorder + " *)(\\] *)");
		Matcher m = condPattern.matcher(linetext);
		
		if (m.find()) {
			offset1 = m.start(1);
			qname = m.group(1);
			
			if (m.group(2) != null) {
				offset2 = m.start(2);
				inString = m.group(2);
			}
			
			offset3 = m.start(3);
			bracketOpen = m.group(3);
			
			offset4 = m.start(4);
			leftBorder = m.group(4);
			
			offset5 = m.start(5);
			rightBorder = m.group(5);
			
			offset6 = m.start(6);
			bracketClose = m.group(6);
			
		}
		
		Section.createExpandedSection(qname, new FindingQuestion(), cond, offset1, topic, null, null, idgen);
		
		//TODO: Bedeutung von type? Überflüssig?
		//if (type!=null) Section.createExpandedSection(type, null, cond, fatherindex++, topic, null, null, idgen);
		
		if (in) Section.createExpandedSection(inString, new IN(), cond, offset2, topic, null, null, idgen);
		Section.createExpandedSection(bracketOpen, new IntervallBracketOpen(), cond, offset3, topic, null, null, idgen);
		Section.createExpandedSection(leftBorder,new IntervallLeftBorderValue(), cond, offset4, topic, null, null, idgen);
		Section.createExpandedSection(rightBorder, new IntervallRightBorderValue(), cond, offset5, topic, null, null, idgen);
		Section.createExpandedSection(bracketClose, new IntervallBracketClose(), cond, offset6, topic, null, null, idgen);
	}

	@Override
	public void in(int line, String linetext, String question, String type,
			List<String> answers) {
		if(linetext == null) return;

	}

	@Override
	public void knowncondition(int line, String linetext, String name,
			String type, boolean unknown) {
		if(name == null) return;
		Section s = Section.createExpandedSection(linetext, new CondKnown(), null, -1, topic, null, null, idgen);
		Section knownbr = Section.createExpandedSection(linetext.substring(0, linetext.indexOf("[")+1), new CondKnownSyntax(), s, 1, topic, null, null, idgen);
		Section q = Section.createExpandedSection(name, new FindingQuestion(), s, 2, topic, null, null, idgen);
		Section brCl = Section.createExpandedSection("]", new CondKnownSyntax(), s, 3, topic, null, null, idgen);
		sections.push(s);
	

	}

	@Override
	public void minmax(int line, String linetext, int min, int max,
			int anzahlcond) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notcond(String text) {
		if(text == null) return;

		if (sections.size() >= 1) {
			String not = "NOT ";
			Matcher m = Pattern.compile(" *(NOT|NICHT) *").matcher(text);
			if (m.find()) {
				not = m.group();
			}
			Section notCond = Section.createExpandedSection(text, new NegatedFinding(), null, -1, topic, null, null, idgen);
			Section.createExpandedSection(not, new NOT(), notCond, 0, topic, null, null, idgen);
			notCond.addChild(sections.pop());
			sections.push(notCond);
		}
	}

	private String trimZeros(String s) {
		s = s.replaceAll("\\.0", "");
		return s;
	}



}
