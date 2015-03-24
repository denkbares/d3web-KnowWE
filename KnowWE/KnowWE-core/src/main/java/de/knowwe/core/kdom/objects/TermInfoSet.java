package de.knowwe.core.kdom.objects;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.d3web.strings.Identifier;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.utils.KnowWEUtils;

public class TermInfoSet implements Collection<TermInfo> {

	private class DefaultTermInfo implements TermInfo {

		private final Identifier identifier;
		private final String key;
		private final Collection<TerminologyManager> managers = new LinkedList<TerminologyManager>();

		public DefaultTermInfo(Identifier identifier) {
			this.identifier = identifier;
			this.key = TermInfoSet.this.getKey(identifier);
		}

		@Override
		public Identifier getIdentifier() {
			return identifier;
		}

		@Override
		public boolean isCaseSensitive() {
			return caseSensitive;
		}

		/**
		 * Returns if the specified identifier will match this {@link TermInfo},
		 * according to the {@link Identifier} of this set and whether this set
		 * is case sensitive or not.
		 *
		 * @param otherIdentifier the identifier to be checked if it matches
		 *                        this set
		 * @return if the identifier matches this set
		 * @created 25.08.2013
		 */
		@Override
		public boolean matches(Identifier otherIdentifier) {
			return key.equals(TermInfoSet.this.getKey(otherIdentifier));
		}

		/**
		 * Returns the key the to identify this {@link TermInfo} object. Is
		 * considers the term's identifier and the case sensitivity flag.
		 *
		 * @return the key to be used by that term info
		 * @created 26.08.2013
		 */
		@Override
		public String getKey() {
			return key;
		}

		public void addManager(TerminologyManager manager) {
			managers.add(manager);
		}

		@Override
		public Collection<TerminologyManager> getManagers() {
			return Collections.unmodifiableCollection(managers);
		}

		@Override
		public Iterator<TerminologyManager> iterator() {
			return managers.iterator();
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			TermInfo other = (TermInfo) obj;
			return (key.equals(other.getKey()));
		}
	}

	private final Map<String, DefaultTermInfo> result = new HashMap<String, DefaultTermInfo>();
	private final boolean caseSensitive;
	private final Class<?>[] allowedTermClasses;

	public TermInfoSet(boolean caseSensitive, Class<?>... allowedTermClasses) {
		this.caseSensitive = caseSensitive;
		if (allowedTermClasses == null || allowedTermClasses.length == 0) {
			allowedTermClasses = new Class[] { Object.class };
		}
		this.allowedTermClasses = allowedTermClasses;
	}

	/**
	 * Returns the key the to identify a {@link TermInfo} object for a specific
	 * identifier and a case sensitivity flag.
	 *
	 * @param identifier the term identifier
	 * @return the key to be used by that term info
	 * @created 26.08.2013
	 */
	private String getKey(Identifier identifier) {
		return caseSensitive
				? identifier.toExternalForm()
				: identifier.toExternalForm().toLowerCase();
	}

	@Override
	public int size() {
		return result.size();
	}

	@Override
	public boolean isEmpty() {
		return result.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Identifier) return contains((Identifier) o);
		if (o instanceof TermInfo) return contains((TermInfo) o);
		return false;
	}

	public boolean contains(Identifier identifier) {
		return result.containsKey(getKey(identifier));
	}

	public boolean contains(TermInfo termInfo) {
		return result.containsKey(termInfo.getKey());
	}

	@Override
	public Iterator<TermInfo> iterator() {
		return Collections.<TermInfo>unmodifiableCollection(result.values()).iterator();
	}

	@Override
	public Object[] toArray() {
		return result.values().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return result.values().toArray(a);
	}

	@Override
	public boolean add(TermInfo termInfo) {
		throw new UnsupportedOperationException("not implemented yet.");
	}

	public void initTerm(String web, Identifier identifier) {
		Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(KnowWEUtils.getArticleManager(web));
		for (TerminologyManager terminologyManager : terminologyManagers) {
			addTermManagerIfMatches(identifier, terminologyManager);
		}

	}

	private void addTermManagerIfMatches(Identifier termIdentifier, TerminologyManager termManager) {
		Collection<Identifier> identifiers;
		if (caseSensitive) {
			identifiers = Arrays.asList(termIdentifier);
		}
		else if (termManager.isUndefinedTerm(termIdentifier)) {
			// getAllTermsEqualIgnoreCase does not return undefined terms
			// so we just use the current identifier
			identifiers = Arrays.asList(termIdentifier);
		}
		else {
			identifiers = termManager.getAllTermsEqualIgnoreCase(termIdentifier);
		}
		for (Identifier identifier : identifiers) {
			// check if class is matched
			if (!isMatchingIdentifier(identifier, termManager)) continue;

			// add term manager
			getTermInfoValid(identifier).addManager(termManager);
		}
	}

	public void initAllTerms(String web) {
		ArticleManager articleManager = Environment.getInstance().getArticleManager(web);
		Collection<TermCompiler> compilers = Compilers.getCompilers(articleManager, TermCompiler.class);
		for (TermCompiler compiler : compilers) {
			addAllMatchingTermInfos(compiler.getTerminologyManager());
		}
	}

	private void addAllMatchingTermInfos(TerminologyManager termManager) {
		for (Identifier identifier : termManager.getAllDefinedTerms()) {
			if (!isMatchingIdentifier(identifier, termManager)) continue;

			// add term manager
			getTermInfoValid(identifier).addManager(termManager);
		}
	}

	private boolean isMatchingIdentifier(Identifier identifier, TerminologyManager termManager) {

		// check if class is matched
		for (Class<?> clazz : allowedTermClasses) {
			if (termManager.hasTermOfClass(identifier, clazz)) {
				return true;
			}
		}
		// if the term has no definitions, there aren't any term classes either
		// we still might want the references
		if (termManager.isUndefinedTerm(identifier)) return true;

		return false;
	}

	public TermInfo getTermInfo(Identifier identifier) {
		return result.get(getKey(identifier));
	}

	private DefaultTermInfo getTermInfoValid(Identifier identifier) {
		String key = getKey(identifier);
		DefaultTermInfo termInfo = result.get(key);
		if (termInfo == null) {
			termInfo = new DefaultTermInfo(identifier);
			result.put(key, termInfo);
		}
		return termInfo;
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Identifier) return remove((Identifier) o);
		if (o instanceof TermInfo) return remove((TermInfo) o);
		return false;
	}

	public boolean remove(Identifier identifier) {
		return result.remove(getKey(identifier)) != null;
	}

	public boolean remove(TermInfo termInfo) {
		return result.remove(termInfo.getKey()) != null;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object object : c) {
			if (!contains(object)) return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends TermInfo> c) {
		boolean changed = false;
		for (TermInfo termInfo : c) {
			changed |= add(termInfo);
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object termInfo : c) {
			changed |= remove(termInfo);
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		List<String> keysToRemove = new LinkedList<String>();
		for (DefaultTermInfo termInfo : result.values()) {
			if (!c.contains(termInfo)
					&& !c.contains(termInfo.getKey())
					&& !c.contains(termInfo.getIdentifier())) {
				keysToRemove.add(termInfo.getKey());
			}
		}
		boolean changed = false;
		for (String key : keysToRemove) {
			changed |= (result.remove(key) != null);
		}
		return changed;
	}

	@Override
	public void clear() {
		result.clear();
	}
}
