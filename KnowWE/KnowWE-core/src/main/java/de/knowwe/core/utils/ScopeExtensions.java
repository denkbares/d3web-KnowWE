package de.knowwe.core.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.Scope.TypePath;

/**
 * Class that manages a fixed set of extensions and matches them against the
 * scope of sections to get the relevant subset of the extensions for that
 * section. This class may use caches to quickly access relevant subsets.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 30.11.2013
 */
public class ScopeExtensions {

	private final Extension[] extensions;
	private final Map<TypePath, List<Extension>> cache = new HashMap<Scope.TypePath, List<Extension>>();

	/**
	 * Creates a new {@link ScopeExtensions} instance for the specified
	 * extension point. The extensions matched by this extension point requires
	 * a "scope" attribute to function well. For more details on scopes consult
	 * the {@link Scope} class.
	 * 
	 * @param extendedPluginID the plugin define the extension point
	 * @param extendedPointID the extension point to be extended
	 */
	public ScopeExtensions(String extendedPluginID, String extendedPointID) {
		this(PluginManager.getInstance().getExtensions(extendedPluginID, extendedPointID));
	}

	/**
	 * Creates a new {@link ScopeExtensions} instance for the specified
	 * extensions. The specified extensions requires a "scope" attribute to
	 * function well. For more details on scopes consult the {@link Scope}
	 * class.
	 * 
	 * @param extensions the extensions to be handled by this instance
	 */
	public ScopeExtensions(Extension[] extensions) {
		this.extensions = extensions;
	}

	/**
	 * Returns the list of matching extensions of this object's extensions with
	 * their scope matching to the specified section.
	 * 
	 * @created 30.11.2013
	 * @param section the section to check the extensions against
	 * @return the matching extensions
	 */
	public List<Extension> getMatches(Section<?> section) {
		return getMatches(Scope.getTypePath(section));
	}

	/**
	 * Returns the list of matching extensions of this object's extensions with
	 * their scope matching to the specified type path.
	 * 
	 * @created 30.11.2013
	 * @param typePath the type path to check the extensions against
	 * @return the matching extensions
	 */
	public List<Extension> getMatches(Type[] typePath) {
		return getMatches(new TypePath(typePath));
	}

	/**
	 * Returns the list of matching extensions of this object's extensions with
	 * their scope matching to the specified type path.
	 * 
	 * @created 30.11.2013
	 * @param typePath the type path to check the extensions against
	 * @return the matching extensions
	 */
	public List<Extension> getMatches(TypePath typePath) {
		List<Extension> list = cache.get(typePath);
		if (list == null) {
			list = Collections.unmodifiableList(createMatches(typePath));
			cache.put(typePath, list);
		}
		return list;
	}

	/**
	 * Returns the best/first matching extension of this object's extensions
	 * with its scope matching to the specified section.
	 * 
	 * @created 30.11.2013
	 * @param section the section to check the extensions against
	 * @return the first extension matching the section
	 */
	public Extension getMatch(Section<?> section) {
		return getMatch(Scope.getTypePath(section));
	}

	/**
	 * Returns the best/first matching extension of this object's extensions
	 * with its scope matching to the specified type path.
	 * 
	 * @created 30.11.2013
	 * @param typePath the type path to check the extensions against
	 * @return the first extension matching the section
	 */
	public Extension getMatch(Type[] typePath) {
		return getMatch(new TypePath(typePath));
	}

	/**
	 * Returns the best/first matching extension of this object's extensions
	 * with its scope matching to the specified type path.
	 * 
	 * @created 30.11.2013
	 * @param typePath the type path to check the extensions against
	 * @return the first extension matching the type path
	 */
	public Extension getMatch(TypePath typePath) {
		List<Extension> matches = getMatches(typePath);
		if (matches.isEmpty()) return null;
		return matches.get(0);
	}

	private List<Extension> createMatches(TypePath typePath) {
		List<Extension> list = ScopeUtils.getMatchingExtensions(extensions, typePath);
		// use empty list singleton if possible to allow garbage collection
		if (list.isEmpty()) return Collections.emptyList();
		return list;
	}
}
