package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;

/**
 * Interface for type containing/wrapping object IDs
 *
 * @author Jochen
 *
 * @param <T>
 */
public interface ObjectIDContainer<T> {

	public String getID(Section<? extends ObjectIDContainer<T>> s);
}
