package de.knowwe.timeline;

import java.util.Collection;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.Value;
import de.d3web.testcase.model.Finding;
import de.d3web.testcase.model.TestCase;


/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public class TestCaseDataProvider implements IDataProvider {

	private TestCase testCase;
	private KnowledgeBase kb;

	public TestCaseDataProvider(TestCase testCase, KnowledgeBase kb) {
		this.testCase = testCase;
		this.kb = kb;
	}
	
	@Override
	public SortedMap<Date, Value> getValues(Question question) {
		SortedMap<Date, Value> values = new TreeMap<Date, Value>();
		for (Date date : testCase.chronology()) {
			for (Finding f : testCase.getFindings(date, kb)) {
				if (f.getTerminologyObject() == question) {
					values.put(date, f.getValue());
				}
			}
		}
		return values;
	}

	@Override
	public Question searchQuestion(String questionName) {
		return kb.getManager().searchQuestion(questionName);
	}
	
	@Override
	public Collection<Date> getAllDates() {
		return testCase.chronology();
	}
}
