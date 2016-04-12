/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.ontology.sparql.validator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * 
 * @author Sebastian Furth
 * @created 20.12.2011
 */
public class ValidatorResult {

	private final Collection<Exception> exceptions = new HashSet<Exception>();
	private final String query;

	public ValidatorResult(String query) {
		if (query == null) {
			throw new NullPointerException("The query is null!");
		}
		if (query.isEmpty()) {
			throw new IllegalArgumentException("The query is empty!");
		}
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public void addException(Exception e) {
		if (e == null) {
			throw new NullPointerException("You can't add null as exception!");
		}
		exceptions.add(e);
	}

	public boolean isValid() {
		return exceptions.isEmpty();
	}

	public boolean hasErrors() {
		return !isValid();
	}

	public Collection<Exception> getErrors() {
		return Collections.unmodifiableCollection(exceptions);
	}

}
