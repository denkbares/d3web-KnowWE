package de.knowwe.timeline.serialization;

import java.util.Comparator;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import de.d3web.core.knowledge.terminology.Question;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public class ValueStorage {

	private SortedMap<Date, SortedMap<Question, String>> values;

	public ValueStorage() {
		this.values = new TreeMap<Date, SortedMap<Question, String>>();
	}
	
	public boolean addValue(Date date, Question question, String value) {
		SortedMap<Question, String> x = values.get(date);
		if (x == null) {
			x = new TreeMap<Question, String>(new Comparator<Question>() {

				@Override
				public int compare(Question o1, Question o2) {
					return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
				}
			});
			values.put(date, x);
		}
		x.put(question, value);
		return true;
	}
	
	
	public SortedMap<Question, String> getValues(Date date) {
		return values.get(date);
	}
}