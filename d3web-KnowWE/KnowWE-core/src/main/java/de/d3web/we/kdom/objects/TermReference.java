package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;

/**
 * Interface for type containing/wrapping term names
 * 
 * @author Jochen
 * 
 * @param <T>
 */
public interface TermReference<T> {

	public String getTermName(Section<? extends TermReference<T>> s);
}
