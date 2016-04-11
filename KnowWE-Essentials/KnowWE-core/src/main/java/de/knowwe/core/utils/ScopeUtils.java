/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.core.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.d3web.plugin.Extension;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.Scope.TypePath;

public class ScopeUtils {

	/**
	 * Returns the all matching extension of a specified array of extensions. A match is an extension with its scope
	 * matching to the specified section.
	 * <p/>
	 * If you call this method often for a specific Extension[] and recurring type paths, you should use the {@link
	 * ScopeExtensions} class which can handle a set of scoped extensions more efficient.
	 *
	 * @param extensions the extensions to be checked
	 * @param section    the section to check the extensions against
	 * @return the matching extensions
	 * @created 30.11.2013
	 */
	public static List<Extension> getMatchingExtensions(Extension[] extensions, Section<?> section) {
		return getMatchingExtensions(extensions, Scope.getTypePath(section));
	}

	/**
	 * Returns the all matching extension of a specified array of extensions. A match is an extension with its scope
	 * matching to the specified type path.
	 * <p/>
	 * If you call this method often for a specific Extension[] and recurring type paths, you should use the {@link
	 * ScopeExtensions} class which can handle a set of scoped extensions more efficient.
	 *
	 * @param extensions the extensions to be checked
	 * @param typePath   the type path to check the extensions against
	 * @return the matching extensions
	 * @created 30.11.2013
	 */
	public static List<Extension> getMatchingExtensions(Extension[] extensions, Type[] typePath) {
		return getMatchingExtensions(extensions, new TypePath(typePath));
	}

	/**
	 * Returns the all matching extension of a specified array of extensions. A match is an extension with its scope
	 * matching to the specified type path.
	 * <p/>
	 * If you call this method often for a specific Extension[] and recurring type paths, you should use the {@link
	 * ScopeExtensions} class which can handle a set of scoped extensions more efficient.
	 *
	 * @param extensions the extensions to be checked
	 * @param typePath   the type path to check the extensions against
	 * @return the matching extensions
	 * @created 30.11.2013
	 */
	public static List<Extension> getMatchingExtensions(Extension[] extensions, TypePath typePath) {
		List<Extension> matches = new LinkedList<Extension>();
		for (Extension extension : extensions) {
			// if we match any of the existing scopes
			// we add the extension and proceed to the next extension
			for (String scopeString : extension.getParameters("scope")) {
				Scope scope = Scope.getScope(scopeString);
				if (scope.matches(typePath)) {
					// if any has matched, add it
					matches.add(extension);
					// and proceed to next extension
					break;
				}
			}
		}
		Iterator<Extension> iterator = matches.iterator();
		while (iterator.hasNext()) {
			Extension match = iterator.next();
			for (String scopeString : match.getParameters("exclude")) {
				Scope scope = Scope.getScope(scopeString);
				if (scope.matches(typePath)) {
					// if any has matched, remove it
					iterator.remove();
					// and proceed to next extension
					break;
				}
			}

		}
		return matches;
	}

	/**
	 * Returns the best/first matching extension of a specified array of extensions. A match is an extension with its
	 * scope matching to the specified section.
	 * <p/>
	 * If you call this method often for a specific Extension[] and recurring type paths, you should use the {@link
	 * ScopeExtensions} class which can handle a set of scoped extensions more efficient.
	 *
	 * @param extensions the extensions to be checked
	 * @param section    the section to check the extensions against
	 * @return the first extension matching the section
	 * @created 30.11.2013
	 */
	public static Extension getMatchingExtension(Extension[] extensions, Section<?> section) {
		return getMatchingExtension(extensions, new TypePath(section));
	}

	/**
	 * Returns the best/first matching extension of a specified array of extensions. A match is an extension with its
	 * scope matching to the specified type path.
	 * <p/>
	 * If you call this method often for a specific Extension[] and recurring type paths, you should use the {@link
	 * ScopeExtensions} class which can handle a set of scoped extensions more efficient.
	 *
	 * @param extensions the extensions to be checked
	 * @param typePath   the type path to check the extensions against
	 * @return the first extension matching the type path
	 * @created 30.11.2013
	 */
	public static Extension getMatchingExtension(Extension[] extensions, Type[] typePath) {
		return getMatchingExtension(extensions, new TypePath(typePath));
	}

	/**
	 * Returns the best/first matching extension of a specified array of extensions. A match is an extension with its
	 * scope matching to the specified type path.
	 * <p/>
	 * If you call this method often for a specific Extension[] and recurring type paths, you should use the {@link
	 * ScopeExtensions} class which can handle a set of scoped extensions more efficient.
	 *
	 * @param extensions the extensions to be checked
	 * @param typePath   the type path to check the extensions against
	 * @return the first extension matching the type path
	 * @created 30.11.2013
	 */
	public static Extension getMatchingExtension(Extension[] extensions, TypePath typePath) {
		for (Extension extension : extensions) {
			// if we match any of the existing scopes
			// we add the extension and proceed to the next extension
			for (String scopeString : extension.getParameters("scope")) {
				Scope scope = Scope.getScope(scopeString);
				if (scope.matches(typePath)) {
					// if any has matched, return it
					return extension;
				}
			}
		}
		return null;
	}
}
