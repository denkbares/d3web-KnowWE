package de.knowwe.core.kdom.objects;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

public class TermInfoSet implements Collection<TermInfo> {

	private class DefaultTermInfo implements TermInfo {

		private final Identifier identifier;
		private final Collection<TermCompiler> termCompilers = new LinkedHashSet<>();

		public DefaultTermInfo(Identifier identifier) {
			this.identifier = identifier;
		}

		@Override
		public Identifier getIdentifier() {
			return identifier;
		}

		@Override
		public boolean isCaseSensitive() {
			return this.identifier.isCaseSensitive();
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
			return identifier.equals(otherIdentifier);
		}

		public void addManager(TermCompiler compiler) {
			termCompilers.add(compiler);
		}

		@Override
		@NotNull
		public Collection<TermCompiler> getTermCompilers() {
			return Collections.unmodifiableCollection(termCompilers);
		}

		@NotNull
		@Override
		public Iterator<TermCompiler> iterator() {
			return termCompilers.iterator();
		}

		@Override
		public int hashCode() {
			return identifier.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			TermInfo other = (TermInfo) obj;
			return (identifier.equals(other.getIdentifier()));
		}

		@Override
		public String toString() {
			return identifier.toExternalForm();
		}
	}

	private final Map<Identifier, DefaultTermInfo> result = new HashMap<>();
	private final Class<?>[] allowedTermClasses;

	public TermInfoSet(Class<?>... allowedTermClasses) {
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
	private Identifier getKey(Identifier identifier) {
		return identifier;
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
		return result.containsKey(termInfo.getIdentifier());
	}

	@NotNull
	@Override
	public Iterator<TermInfo> iterator() {
		return Collections.<TermInfo>unmodifiableCollection(result.values()).iterator();
	}

	@NotNull
	@Override
	public Object[] toArray() {
		return result.values().toArray();
	}

	@NotNull
	@Override
	public <T> T[] toArray(@NotNull T[] a) {
		return result.values().toArray(a);
	}

	@Override
	public boolean add(TermInfo termInfo) {
		throw new UnsupportedOperationException("not implemented yet.");
	}

	public void initTerm(String web, Identifier identifier) {
		Collection<TermCompiler> terminologyManagers = Compilers.getCompilers(KnowWEUtils.getArticleManager(web), TermCompiler.class);
		for (TermCompiler terminologyManager : terminologyManagers) {
			addTermManagerIfMatches(identifier, terminologyManager);
		}
	}

	private void addTermManagerIfMatches(Identifier termIdentifier, TermCompiler termCompiler) {
		Collection<Identifier> identifiers;
		if (termIdentifier.isCaseSensitive()) {
			identifiers = Collections.singletonList(termIdentifier);
		}
		else if (termCompiler.getTerminologyManager().isUndefinedTerm(termIdentifier)) {
			// getAllTermsEqualIgnoreCase does not return undefined terms
			// so we just use the current identifier
			identifiers = Collections.singletonList(termIdentifier);
		}
		else {
			identifiers = termCompiler.getTerminologyManager().getAllTermsEqualIgnoreCase(termIdentifier);
		}
		for (Identifier identifier : identifiers) {
			// check if class is matched
			if (!isMatchingIdentifier(identifier, termCompiler)) continue;

			// add term manager
			getTermInfoValid(identifier).addManager(termCompiler);
		}
	}

	public void initAllTerms(UserContext userContext, boolean defaultOnly) {
		Collection<TermCompiler> compilers = Compilers.getCompilers(userContext, userContext.getArticleManager(), TermCompiler.class);
		for (TermCompiler compiler : compilers) {
			if (defaultOnly) {
				if (!Compilers.isDefaultCompiler(userContext, compiler)) {
					continue;
				}
			}
			addAllMatchingTermInfos(compiler);
		}
	}

	private void addAllMatchingTermInfos(TermCompiler termManager) {
		for (Identifier identifier : termManager.getTerminologyManager().getAllDefinedTerms()) {
			if (!isMatchingIdentifier(identifier, termManager)) continue;

			// add term manager
			getTermInfoValid(identifier).addManager(termManager);
		}
	}

	private boolean isMatchingIdentifier(Identifier identifier, TermCompiler termManager) {

		// check if class is matched
		for (Class<?> clazz : allowedTermClasses) {
			if (termManager.getTerminologyManager().hasTermOfClass(identifier, clazz)) {
				return true;
			}
		}
		// if the term has no definitions, there aren't any term classes either
		// we still might want the references
		return termManager.getTerminologyManager().isUndefinedTerm(identifier);
	}

	public TermInfo getTermInfo(Identifier identifier) {
		return result.get(getKey(identifier));
	}

	private DefaultTermInfo getTermInfoValid(Identifier identifier) {
		return result.computeIfAbsent(identifier, k -> new DefaultTermInfo(identifier));
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Identifier) return remove((Identifier) o) != null;
		if (o instanceof TermInfo) return remove((TermInfo) o) != null;
		return false;
	}

	public TermInfo remove(Identifier identifier) {
		return result.remove(identifier);
	}

	public TermInfo remove(TermInfo termInfo) {
		return remove(termInfo.getIdentifier());
	}

	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		for (Object object : c) {
			if (!contains(object)) return false;
		}
		return true;
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends TermInfo> c) {
		boolean changed = false;
		for (TermInfo termInfo : c) {
			changed |= add(termInfo);
		}
		return changed;
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		boolean changed = false;
		for (Object termInfo : c) {
			changed |= remove(termInfo);
		}
		return changed;
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		return this.removeIf(e -> !c.contains(e));
	}

	@Override
	public void clear() {
		result.clear();
	}
}
