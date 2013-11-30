package de.knowwe.core.utils;

import java.util.LinkedList;
import java.util.List;

import de.d3web.plugin.Extension;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.Scope.TypePath;

public class ScopeUtils {

	/**
	 * Returns the all matching extension of a specified array of extensions. A
	 * match is an extension with its scope matching to the specified section.
	 * <p>
	 * If you call this method often for a specific Extension[] and recurring
	 * type paths, you should use the {@link ScopeExtensions} class which can
	 * handle a set of scoped extensions more efficient.
	 * 
	 * @created 30.11.2013
	 * @param extensions the extensions to be checked
	 * @param section the section to check the extensions against
	 * @return the matching extensions
	 */
	public static List<Extension> getMatchingExtensions(Extension[] extensions, Section<?> section) {
		return getMatchingExtensions(extensions, Scope.getTypePath(section));
	}

	/**
	 * Returns the all matching extension of a specified array of extensions. A
	 * match is an extension with its scope matching to the specified type path.
	 * <p>
	 * If you call this method often for a specific Extension[] and recurring
	 * type paths, you should use the {@link ScopeExtensions} class which can
	 * handle a set of scoped extensions more efficient.
	 * 
	 * @created 30.11.2013
	 * @param extensions the extensions to be checked
	 * @param typePath the type path to check the extensions against
	 * @return the matching extensions
	 */
	public static List<Extension> getMatchingExtensions(Extension[] extensions, Type[] typePath) {
		return getMatchingExtensions(extensions, new TypePath(typePath));
	}

	/**
	 * Returns the all matching extension of a specified array of extensions. A
	 * match is an extension with its scope matching to the specified type path.
	 * <p>
	 * If you call this method often for a specific Extension[] and recurring
	 * type paths, you should use the {@link ScopeExtensions} class which can
	 * handle a set of scoped extensions more efficient.
	 * 
	 * @created 30.11.2013
	 * @param extensions the extensions to be checked
	 * @param typePath the type path to check the extensions against
	 * @return the matching extensions
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
		return matches;
	}

	/**
	 * Returns the best/first matching extension of a specified array of
	 * extensions. A match is an extension with its scope matching to the
	 * specified section.
	 * <p>
	 * If you call this method often for a specific Extension[] and recurring
	 * type paths, you should use the {@link ScopeExtensions} class which can
	 * handle a set of scoped extensions more efficient.
	 * 
	 * @created 30.11.2013
	 * @param extensions the extensions to be checked
	 * @param section the section to check the extensions against
	 * @return the first extension matching the section
	 */
	public static Extension getMatchingExtension(Extension[] extensions, Section<?> section) {
		return getMatchingExtension(extensions, new TypePath(section));
	}

	/**
	 * Returns the best/first matching extension of a specified array of
	 * extensions. A match is an extension with its scope matching to the
	 * specified type path.
	 * <p>
	 * If you call this method often for a specific Extension[] and recurring
	 * type paths, you should use the {@link ScopeExtensions} class which can
	 * handle a set of scoped extensions more efficient.
	 * 
	 * @created 30.11.2013
	 * @param extensions the extensions to be checked
	 * @param typePath the type path to check the extensions against
	 * @return the first extension matching the type path
	 */
	public static Extension getMatchingExtension(Extension[] extensions, Type[] typePath) {
		return getMatchingExtension(extensions, new TypePath(typePath));
	}

	/**
	 * Returns the best/first matching extension of a specified array of
	 * extensions. A match is an extension with its scope matching to the
	 * specified type path.
	 * <p>
	 * If you call this method often for a specific Extension[] and recurring
	 * type paths, you should use the {@link ScopeExtensions} class which can
	 * handle a set of scoped extensions more efficient.
	 * 
	 * @created 30.11.2013
	 * @param extensions the extensions to be checked
	 * @param typePath the type path to check the extensions against
	 * @return the first extension matching the type path
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
