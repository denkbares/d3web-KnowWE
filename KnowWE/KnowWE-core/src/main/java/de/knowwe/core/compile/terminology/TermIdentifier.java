/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.compile.terminology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.knowwe.core.utils.StringFragment;
import de.knowwe.core.utils.Strings;

/**
 * Wrapper class to identify and match the terms inside the TerminologyHandler.
 * 
 * @author Albrecht Striffler
 * @created 08.01.2011
 */
public class TermIdentifier implements Comparable<TermIdentifier> {

	private static final String SEPARATOR = "#";

	private final String[] pathElements;

	private final String externalForm;

	private final String externalFormLowerCase;

	public TermIdentifier(String... pathElements) {
		this.pathElements = pathElements;
		this.externalForm = toExternalForm();
		this.externalFormLowerCase = externalForm.toLowerCase();
	}

	@Override
	public int hashCode() {
		return externalFormLowerCase.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TermIdentifier other = (TermIdentifier) obj;
		return externalFormLowerCase.equals(other.externalFormLowerCase);
	}

	/**
	 * Returns the external form of this {@link TermIdentifier}.
	 */
	@Override
	public String toString() {
		return this.externalForm;
	}

	@Override
	public int compareTo(TermIdentifier o) {
		return externalFormLowerCase.compareTo(o.externalFormLowerCase);
	}

	/**
	 * Returns whether this identifier starts with the given identifier path.
	 * 
	 * @created 23.04.2012
	 * @param identifier the identifier to check
	 */
	public boolean startsWith(TermIdentifier identifier) {
		if (identifier.pathElements.length > this.pathElements.length) return false;
		for (int i = 0; i < identifier.pathElements.length; i++) {
			if (!identifier.pathElements[i].equalsIgnoreCase(this.pathElements[i])) return false;
		}
		return true;
	}

	/**
	 * Returns the last element of the path given to create this term
	 * identifier.
	 * 
	 * @created 23.04.2012
	 */
	public String getLastPathElement() {
		if (pathElements.length == 0) return "";
		return pathElements[pathElements.length - 1];
	}

	/**
	 * Returns a copy of the path elements of this {@link TermIdentifier}.
	 * 
	 * @created 25.04.2012
	 */
	public String[] getPathElements() {
		return Arrays.copyOf(this.pathElements, this.pathElements.length);
	}

	/**
	 * Returns a new {@link TermIdentifier} consisting of the identifier
	 * elements of the given {@link TermIdentifier} appended to the identifier
	 * elements of this {@link TermIdentifier}.
	 * 
	 * @created 23.04.2012
	 * @param termIdentifier the {@link TermIdentifier} to append
	 */
	public TermIdentifier append(TermIdentifier termIdentifier) {
		int newLength = this.pathElements.length + termIdentifier.pathElements.length;
		ArrayList<String> newIdentifierElements = new ArrayList<String>(newLength);
		newIdentifierElements.addAll(Arrays.asList(this.pathElements));
		newIdentifierElements.addAll(Arrays.asList(termIdentifier.pathElements));
		return new TermIdentifier(newIdentifierElements.toArray(new String[newLength]));
	}

	public String toExternalForm() {
		StringBuilder externalForm = new StringBuilder();

		boolean first = true;
		for (String element : pathElements) {
			if (first) first = false;
			else externalForm.append(SEPARATOR);
			if (needsQuotes(element)) {
				externalForm.append(Strings.quote(element));
			}
			else {
				externalForm.append(element);
			}
		}

		return externalForm.toString();
	}

	private boolean needsQuotes(String element) {
		return element.contains("\"") || element.contains(SEPARATOR) || element.contains("\\");
	}

	public static TermIdentifier fromExternalForm(String externalForm) {
		List<StringFragment> pathElementFragments = Strings.splitUnquoted(externalForm, SEPARATOR,
				true);
		ArrayList<String> pathElements = new ArrayList<String>(pathElementFragments.size());
		for (StringFragment pathElementFragment : pathElementFragments) {
			pathElements.add(Strings.unquote(pathElementFragment.getContent()));
		}
		return new TermIdentifier(pathElements.toArray(new String[pathElements.size()]));
	}

}
