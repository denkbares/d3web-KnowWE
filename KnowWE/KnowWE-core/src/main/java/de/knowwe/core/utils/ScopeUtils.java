package de.knowwe.core.utils;

import java.util.ArrayList;
import java.util.List;

import de.d3web.plugin.Extension;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

public class ScopeUtils {

	public static Extension[] getMatchingExtensions(Extension[] extensions, Section<?> section) {
		return getMatchingExtensions(extensions, Scope.getTypePath(section));
	}

	public static Extension[] getMatchingExtensions(Extension[] extensions, Type[] typePath) {
		List<Extension> matches = new ArrayList<Extension>(extensions.length);
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
		return matches.toArray(new Extension[matches.size()]);
	}
}
