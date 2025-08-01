package de.knowwe.core.kdom.parsing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.CachedIterable;
import com.denkbares.collections.ConcatenateIterable;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.filter.SectionFilter;

import static java.util.stream.Collectors.toList;

/**
 * Class providing utility methods for efficient and convenient access to sections and tree of typed sections. It allows
 * to search and iterate over special typed subclasses, x-path- (scope-)like access to ancestors and so on.
 */
@SuppressWarnings("Convert2Diamond")
public class Sections<T extends Type> implements Iterable<Section<T>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(Sections.class);
	private static final Sections<?> EMPTY = new Sections<>(Collections.emptyList());

	private final Iterable<Section<T>> sections;

	public static <T extends Type> Sections<T> $(Iterable<? extends Section<? extends T>> sections) {
		//noinspection rawtypes,unchecked
		return new Sections(sections);
	}

	@SafeVarargs
	public static <T extends Type> Sections<T> $(Iterable<? extends Section<? extends T>>... sections) {
		return $(Stream.of(sections)
				.flatMap(iter -> StreamSupport.stream(iter.spliterator(), false))
				.distinct());
	}

	public static <T extends Type> Sections<T> $(Iterator<? extends Section<? extends T>> sections) {
		//noinspection rawtypes,unchecked
		return new Sections(new CachedIterable<>(sections));
	}

	public static <T extends Type> Sections<T> $(ArticleManager manager) {
		//noinspection rawtypes,unchecked
		return new Sections(new CachedIterable<Section<RootType>>(manager.getArticles()
				.stream()
				.map(Article::getRootSection)
				.iterator()));
	}

	/**
	 * Create a new Sections object with all section the given compiler is compiling
	 */
	public static <T extends Type> Sections<T> $(PackageCompiler compiler) {
		//noinspection rawtypes,unchecked,deprecation
		return new Sections(compiler.getCompiledSections());
	}

	public static Sections<RootType> $(Article article) {
		return new Sections<>((article == null) ? null : article.getRootSection());
	}

	public static <T extends Type> Sections<T> $(Section<T> section) {
		return new Sections<>(section);
	}

	/**
	 * Creates a new Sections instance from the elements of the stream, skipping all null values in the stream.
	 *
	 * @param sections the sections to create the instance from
	 * @return a Sections instance with all non-null sections
	 */
	public static <T extends Type> Sections<T> $(Stream<? extends Section<? extends T>> sections) {
		return $(sections.filter(Objects::nonNull).iterator());
	}

	private Sections(Section<T> section) {
		this((section == null) ? Collections.emptyList() : Collections.singletonList(section));
	}

	private Sections(Iterable<Section<T>> sections) {
		this.sections = sections;
	}

	/**
	 * Returns an empty Sections instance, containing no sections.
	 *
	 * @return an empty instance
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Type> Sections<T> empty() {
		return (Sections<T>) EMPTY;
	}

	/**
	 * Maps the sections to the results of the mapper function. Returns an ordinary Java util {@link Stream} instance.
	 *
	 * @param mapper the function to map/convert the sections to the desired results
	 * @param <R>    the result type
	 * @return a java stream with the results of the mapper function
	 */
	@NotNull
	public <R> Stream<R> map(BiFunction<T, Section<T>, R> mapper) {
		return stream().map(s -> s.get(mapper));
	}

	/**
	 * Maps the sections to the results of the mapper function. Returns an ordinary Java util {@link Stream} instance.
	 *
	 * @param mapper the function to map/convert the sections to the desired results
	 * @param <R>    the result type
	 * @return a java stream with the results of the mapper function
	 */
	@NotNull
	public <R> Stream<R> map(Function<Section<T>, ? extends R> mapper) {
		return stream().map(mapper);
	}

	/**
	 * Maps the sections to the results of the mapper function. Returns a new Sections object.
	 *
	 * @param mapper the function to map/convert the sections to the desired results
	 * @param <R>    the result type
	 * @return a Sections instance with all the mapped functions
	 */
	public <R extends Type> Sections<R> mapTo(Function<Section<T>, Section<R>> mapper) {
		return $(stream().map(mapper));
	}

	/**
	 * Same as {@link Sections#filter(Class)}
	 */
	public <R extends Type> Sections<R> type(Class<R> typeClass) {
		return filter(typeClass);
	}

	/**
	 * Maps the sections to the results of the mapper function. Returns a new Sections object.
	 *
	 * @param mapper the function to map/convert the sections to the desired results
	 * @param <R>    the result type
	 * @return a Sections instance with all the mapped functions
	 */
	public <R extends Type> Sections<R> flatMapStreamTo(Function<Section<T>, Stream<Section<R>>> mapper) {
		return $(stream().flatMap(mapper));
	}

	/**
	 * Maps the sections to the results of the mapper function. Returns a new Sections object.
	 *
	 * @param mapper the function to map/convert the sections to the desired results
	 * @param <R>    the result type
	 * @return a Sections instance with all the mapped functions
	 */
	public <R extends Type> Sections<R> flatMapTo(Function<Section<T>, Sections<R>> mapper) {
		return $(stream().map(mapper).flatMap(Sections::stream));
	}

	/**
	 * Maps the available sections to the result of the mapper function, returning the first non-null value. If there is
	 * no section, or all sections are mapped to <tt>null</tt> is returned.
	 *
	 * @param mapper the function to map/convert the section to the result
	 * @param <R>    the result type
	 * @return the result returned by the mapper function for the first section
	 */
	@Nullable
	public <R> R mapFirst(BiFunction<T, Section<T>, R> mapper) {
		// filter null after applying, as the findFirst creates a NullPointerException otherwise
		return mapOptional(mapper).orElse(null);
	}

	/**
	 * Maps the available sections to the result of the mapper function, returning the first non-null value. If there is
	 * no section, or all sections are mapped to <tt>null</tt> is returned.
	 *
	 * @param mapper the function to map/convert the section to the result
	 * @param <R>    the result type
	 * @return the result returned by the mapper function for the first section
	 */
	@Nullable
	public <R> R mapFirst(Function<Section<T>, ? extends R> mapper) {
		// filter null after applying, as the findFirst creates a NullPointerException otherwise
		return mapOptional(mapper).orElse(null);
	}

	/**
	 * Maps the available sections to the result of the mapper function, returning the first non-null value. If there is
	 * no section, or all sections are mapped to <tt>null</tt>, an empty {@link Optional} is returned.
	 *
	 * @param mapper the function to map/convert the section to the result
	 * @param <R>    the result type
	 * @return the result returned by the mapper function for the first section
	 */
	@NotNull
	public <R> Optional<R> mapOptional(BiFunction<T, Section<T>, R> mapper) {
		// filter null after applying, as the findFirst creates a NullPointerException otherwise
		return map(mapper).filter(Objects::nonNull).findFirst();
	}

	/**
	 * Maps the available sections to the result of the mapper function, returning the first non-null value. If there is
	 * no section, or all sections are mapped to <tt>null</tt>, an empty {@link Optional} is returned.
	 *
	 * @param mapper the function to map/convert the section to the result
	 * @param <R>    the result type
	 * @return the result returned by the mapper function for the first section
	 */
	@NotNull
	public <R> Optional<R> mapOptional(Function<Section<T>, R> mapper) {
		// filter null after applying, as the findFirst creates a NullPointerException otherwise
		return map(mapper).filter(Objects::nonNull).findFirst();
	}

	/**
	 * Returns whether any elements of this Sections match the provided predicate.  May not evaluate the predicate on
	 * all elements if not necessary for determining the result.  If the there are not elements in this Sections, {@code
	 * false} is returned and the predicate is not evaluated.
	 *
	 * @param predicate predicate to apply to elements
	 * @return {@code true} if any elements match the provided predicate
	 */
	public boolean anyMatch(SectionFilter<T> predicate) {
		for (Section<T> section : this) {
			if (predicate.accept(section)) return true;
		}
		return false;
	}

	/**
	 * Returns whether all elements of this Sections match the provided predicate. May not evaluate the predicate on all
	 * elements if not necessary for determining the result.  If the there are not elements in this Sections, {@code
	 * true} is returned and the predicate is not evaluated.
	 *
	 * @param predicate predicate to apply to elements
	 * @return {@code true} if either all elements match the provided predicate or the sections are empty
	 */
	public boolean allMatch(SectionFilter<T> predicate) {
		for (Section<T> section : this) {
			if (!predicate.accept(section)) return false;
		}
		return true;
	}

	/**
	 * Returns whether no elements of this Sections match the provided predicate. May not evaluate the predicate on all
	 * elements if not necessary for determining the result.  If the there are not elements in this Sections, {@code
	 * true} is returned and the predicate is not evaluated.
	 *
	 * @param predicate predicate to apply to elements
	 * @return {@code true} if either no elements match the provided predicate or the sections are empty
	 */
	public boolean noneMatch(SectionFilter<T> predicate) {
		return !anyMatch(predicate);
	}

	@NotNull
	public static Sections<? extends Type> definitions(@NotNull TerminologyManager manager, @Nullable Identifier identifier) {
		return $(manager.getTermDefiningSections(identifier).stream().sorted().map(s -> Sections.cast(s, Type.class)));
	}

	@NotNull
	public static Sections<? extends Type> definitions(@NotNull TermCompiler compiler, @Nullable Identifier identifier) {
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		return definitions(terminologyManager, identifier);
	}

	@NotNull
	public static Sections<? extends Type> definitions(@NotNull TermCompiler compiler, @Nullable Section<? extends Term> term) {
		if (term == null) return Sections.empty();
		return definitions(compiler, term.get().getTermIdentifier(compiler, term));
	}

	@NotNull
	public static Sections<? extends Type> references(@NotNull TerminologyManager manager, @Nullable Identifier identifier) {
		return $(manager.getTermReferenceSections(identifier).stream().sorted().map(s -> Sections.cast(s, Type.class)));
	}

	/**
	 * Combines call for {@link #definitions(TermCompiler, Identifier)} and {@link #references(TermCompiler,
	 * Identifier)}
	 */
	@NotNull
	public static Sections<? extends Type> registrations(@NotNull TermCompiler compiler, @Nullable Section<? extends Term> term) {
		if (term == null) return Sections.empty();
		return registrations(compiler, term.get().getTermIdentifier(compiler, term));
	}

	/**
	 * Combines call for {@link #definitions(TermCompiler, Identifier)} and {@link #references(TermCompiler,
	 * Identifier)}
	 */
	@NotNull
	public static Sections<? extends Type> registrations(@NotNull TermCompiler compiler, @Nullable Identifier termIdentifier) {
		if (termIdentifier == null) return Sections.empty();
		//noinspection rawtypes,unchecked
		return new Sections(new ConcatenateIterable<Section<?>>(definitions(compiler, termIdentifier), references(compiler, termIdentifier)));
	}

	@NotNull
	public static Sections<? extends Type> references(@NotNull TermCompiler compiler, @Nullable Identifier identifier) {
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		return references(terminologyManager, identifier);
	}

	@NotNull
	public static Sections<? extends Type> references(@NotNull TermCompiler compiler, @Nullable Section<? extends Term> term) {
		if (term == null) return Sections.empty();
		return references(compiler, term.get().getTermIdentifier(compiler, term));
	}

	/**
	 * Creates a new sections instance that first iterates over the elements of this instance, then over the elements of
	 * the given instance.
	 *
	 * @param sections the sections to append/concat
	 * @return a new sections object with the elements of this and the given instance
	 */
	public Sections<T> concat(Sections<T> sections) {
		return new Sections<>(new ConcatenateIterable<>(this, sections));
	}

	/**
	 * Creates a new sections instance that first iterates over the elements of this instance, then over the elements of
	 * the given instance.
	 *
	 * @param sections the sections to append/concat
	 * @return a new sections object with the elements of this and the given instance
	 */
	public Sections<?> append(Sections<?> sections) {
		//noinspection rawtypes,unchecked
		return new Sections(new ConcatenateIterable<>(this.sections, sections));
	}

	/**
	 * Creates a new sections instance that first iterates over the elements of this instance, then over the elements of
	 * the given instance.
	 *
	 * @param section the section to append/concat
	 * @return a new sections object with the elements of this and the given instance
	 */
	public Sections<?> append(Section<?> section) {
		//noinspection rawtypes,unchecked
		return new Sections(new ConcatenateIterable<>(this.sections, List.of(section)));
	}

	/**
	 * Creates a new sections instance that first iterates over the elements of this instance, then over the elements of
	 * the given instance.
	 *
	 * @param sections the sections to append/concat
	 * @return a new sections object with the elements of this and the given instance
	 */
	public Sections<?> append(Iterable<Section<?>> sections) {
		//noinspection rawtypes,unchecked
		return new Sections(new ConcatenateIterable<>(this.sections, sections));
	}

	@NotNull
	@Override
	public Iterator<Section<T>> iterator() {
		return sections.iterator();
	}

	/**
	 * Returns the first section of this instance. If this instance is empty, null is returned.
	 *
	 * @return the first section
	 */
	@Nullable
	public Section<T> getFirst() {
		return getNth(0);
	}

	/**
	 * Returns the last section of this instance. If this instance is empty, null is returned.
	 *
	 * @return the last section
	 */
	@Nullable
	public Section<T> getLast() {
		if (sections instanceof List<Section<T>> list) {
			return list.isEmpty() ? null : list.get(list.size() - 1);
		}
		Section<T> last = null;
		for (Section<T> section : this) {
			last = section;
		}
		return last;
	}

	/**
	 * Returns a new Section instance with the first result of this instance. If this instance is empty, a new empty
	 * instance is returned, producing no further work.
	 *
	 * @return a Sections object with the first section of this instance
	 */
	@NotNull
	public Sections<T> first() {
		return nth(0);
	}

	/**
	 * Returns a new Section instance with the last result of this instance. If this instance is empty, a new empty
	 * instance is returned, producing no further work.
	 *
	 * @return a Sections object with the last section of this instance
	 */
	@NotNull
	public Sections<T> last() {
		Section<T> nth = getLast();
		return new Sections<>(nth == null ? Collections.emptyList() : Collections.singletonList(nth));
	}

	/**
	 * Returns true if this instance is empty and does not contain any sections.
	 *
	 * @return if the instance is empty
	 */
	public boolean isEmpty() {
		return !sections.iterator().hasNext();
	}

	/**
	 * Returns true if this instance contains at least one section, i.e. is not empty.
	 *
	 * @return true if we have at least one section
	 */
	public boolean isNotEmpty() {
		return !isEmpty();
	}

	/**
	 * Returns the section at the specified index contained in this instance. If this instance has less sections than
	 * the specified index requests, null is returned. If a negative index is specified, an {@link
	 * java.lang.IndexOutOfBoundsException} is thrown.
	 *
	 * @param index the index of the section to be returned.
	 * @return the nth section
	 */
	public Section<T> getNth(int index) {
		if (index < 0) throw new IndexOutOfBoundsException("Invalid index " + index);
		if (sections instanceof List<Section<T>> list) {
			return index < list.size() ? list.get(index) : null;
		}
		for (Section<T> section : this) {
			if (index == 0) return section;
			index--;
		}
		return null;
	}

	/**
	 * Returns a new Sections instance with the section at the specified index contained in this instance. If this
	 * instance has less sections than the specified index requests, an empty Sections object is returned, but not
	 * null!. This empty Sections object will produce no further work or results. If a negative index is specified, an
	 * {@link java.lang.IndexOutOfBoundsException} is thrown.
	 *
	 * @param index the index of the section to be returned.
	 * @return a Sections object with nth section or an empty Sections object, if this object has no nth entry.
	 */
	public Sections<T> nth(int index) {
		Section<T> nth = getNth(index);
		return new Sections<>(nth == null ? Collections.emptyList() : Collections.singletonList(nth));
	}

	/**
	 * Returns the number of sections that are in this sections instance.
	 */
	public int count() {
		if (this.sections instanceof Collection) {
			return ((Collection<Section<T>>) this.sections).size();
		}
		return (int) stream().count();
	}

	/**
	 * Returns a list of all contained sections of this instance.
	 *
	 * @return a list of all sections contained
	 */
	@NotNull
	public List<Section<T>> asList() {
		List<Section<T>> result = new ArrayList<>();
		for (Section<T> section : this) {
			result.add(section);
		}
		return result;
	}

	/**
	 * Returns a set of all contained sections of this instance. The order of the instances are preserved by this
	 * method.
	 *
	 * @return a (linked) set of all sections contained
	 */
	@NotNull
	public Set<Section<T>> asSet() {
		Set<Section<T>> result = new LinkedHashSet<>();
		for (Section<T> section : this) {
			result.add(section);
		}
		return result;
	}

	@Override
	@NotNull
	public Spliterator<Section<T>> spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(),
				Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
	}

	/**
	 * Returns all contained sections of this instance as a stream for further processing.
	 *
	 * @return a stream of all contained sections
	 */
	@NotNull
	public Stream<Section<T>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Returns a new Sections instance containing all successors of this object's sections that is an instance the
	 * specified type. If any section of this object's sections matches the specified class the section itself is
	 * contained. If any section of this instance is neither of the specified type, nor does have a successor of the
	 * specified type, the section is not considered in the resulting Sections objects.
	 * <p/>
	 * The successors will become truncated if they descent too far from the original sections of this object. If
	 * maxDepth is '0' only the original sections will be contained. A value of '1' allows only the original objects and
	 * their direct children, and so on. Any negative value will be ignored, so all successors of any depth are
	 * allowed.
	 * <p/>
	 * Please note that there is no de-duplication. If a sections is contained, that is already a successor of a defined
	 * section, the section and its successors may be contained duplicated in the resulting sections.
	 * <p/>
	 * If a successor is an instance of the specified class, but also have sub-successors of the specified class, both,
	 * the successor and the sub-successors are contained in the returned instance.
	 *
	 * @param maxDepth the maximum level to descent from the sections
	 * @param clazz    the class to be matched by the successors
	 * @param <R>      the class to be matched by the successors
	 * @return all successors for each section matching the type
	 */
	@NotNull
	public <R extends Type> Sections<R> successor(int maxDepth, Class<R> clazz) {
		return new Sections<R>(() -> {
			KDOMIterator depthFirst = KDOMIterator.depthFirst(
					sections, section -> Sections.canHaveSuccessor(section, clazz));
			depthFirst.setMaxDepth(maxDepth);
			return FilterTypeIterator.create(depthFirst, clazz);
		});
	}

	/**
	 * Returns a new Sections instance with definitions of the references (or definitions) in the current instance.
	 * Sections that don't have sub-types of {@link Term} will be skipped when collecting definitions.
	 *
	 * @param compiler the compiler where the definitions are registered
	 * @return the definitions for the current sections
	 */
	@NotNull
	public Sections<?> definitions(@NotNull TermCompiler compiler) {
		return new Sections<>(() -> filter(Term.class).stream()
				.map(s -> s.get().getTermIdentifier(compiler, s))
				.distinct()
				.flatMap(s -> Sections.definitions(compiler, s).stream())
				.iterator());
	}

	/**
	 * Returns a new Sections instance with definitions of the references (or definitions) in the current instance.
	 * Sections that don't have sub-types of {@link Term} will be skipped when collecting definitions.
	 *
	 * @param compiler the compiler where the definitions are registered
	 * @param clazz    the class of the type of the sections in the new sections object
	 * @param <R>      the class of the type of the sections in the new sections object
	 * @return the definitions for the current sections
	 */
	@NotNull
	public <R extends Term> Sections<R> definitions(@NotNull TermCompiler compiler, Class<R> clazz) {
		return new Sections<R>(() -> filter(Term.class).stream()
				.map(s -> s.get().getTermIdentifier(compiler, s))
				.distinct()
				.flatMap(s -> Sections.definitions(compiler, s).filter(clazz).stream())
				.iterator());
	}

	/**
	 * Returns a new Sections instance with references of the references or definitions in the current instance.
	 * Sections that don't have sub-types of {@link Term} will be skipped when collecting references.
	 *
	 * @param compiler the compiler where the references are registered
	 * @return the definitions for the current sections
	 */
	@NotNull
	public Sections<?> references(@NotNull TermCompiler compiler) {
		return new Sections<>(() -> filter(Term.class).stream()
				.map(s -> s.get().getTermIdentifier(compiler, s))
				.distinct()
				.flatMap(i -> Sections.references(compiler, i).stream())
				.iterator());
	}

	/**
	 * Returns a new Sections instance with references of the references or definitions in the current instance.
	 * Sections that don't have sub-types of {@link Term} will be skipped when collecting references.
	 *
	 * @param compiler the compiler where the references are registered
	 * @param clazz    the class of the type of the sections in the new sections object
	 * @param <R>      the class of the type of the sections in the new sections object
	 * @return the definitions for the current sections
	 */
	@NotNull
	public <R extends Term> Sections<R> references(@NotNull TermCompiler compiler, Class<R> clazz) {
		return new Sections<R>(() -> filter(Term.class).stream()
				.map(s -> s.get().getTermIdentifier(compiler, s))
				.distinct()
				.flatMap(s -> Sections.references(compiler, s).filter(clazz).stream())
				.iterator());
	}

	/**
	 * Returns a new Sections instance with all registrations (definitions and references) of the references or
	 * definitions in the current instance. Sections that don't have sub-types of {@link Term} will be skipped when
	 * collecting references.
	 *
	 * @param compiler the compiler with the registrations
	 * @param clazz    the class of the type of the sections in the new sections object
	 * @param <R>      the class of the type of the sections in the new sections object
	 * @return the definitions for the current sections
	 */
	public <R extends Term> Sections<R> registrations(@NotNull TermCompiler compiler, Class<R> clazz) {
		return new Sections<R>(() -> filter(Term.class).stream()
				.map(s -> s.get().getTermIdentifier(compiler, s))
				.distinct()
				.flatMap(s -> Stream.concat(Sections.definitions(compiler, s)
						.filter(clazz).stream(), Sections.references(compiler, s).filter(clazz).stream()))
				.iterator());
	}

	/**
	 * Returns a new Sections instance containing all successors of this object's sections that is an instance the
	 * specified type. If any section of this object's sections matches the specified class the section itself is
	 * contained. If any section of this instance is neither of the specified type, nor does have a successor of the
	 * specified type, the section is not considered in the resulting Sections objects.
	 * <p/>
	 * Please note that there is no de-duplication. If a sections is contained, that is already a successor of a defined
	 * section, the section and its successors may be contained duplicated in the resulting sections.
	 * <p/>
	 * If a successor is an instance of the specified class, but also have sub-successors of the specified class, both,
	 * the successor and the sub-successors are contained in the returned instance.
	 *
	 * @param clazz the class to be matched by the successors
	 * @param <R>   the class to be matched by the successors
	 * @return all successors for each section matching the type
	 */
	@NotNull
	public <R extends Type> Sections<R> successor(Class<R> clazz) {
		return successor(-1, clazz);
	}

	/**
	 * Returns a new Sections instance containing all successors of this instance's sections. The original sections are
	 * also still contained. The order of the resulting sections are defined by the depth-first-search from each section
	 * of this Sections instance.
	 * <p/>
	 * Please note that there is no de-duplication. If a sections is contained, that is already a successor of a defined
	 * section, the section and its successors will be contained duplicated in the resulting sections.
	 *
	 * @return all successors for each section matching the type
	 */
	@NotNull
	public Sections<Type> successor() {
		return successor(Type.class);
	}

	/**
	 * Returns the siblings of the first section in this sections object in reverse order, starting with the sibling
	 * directly prior to the given section.
	 *
	 * @return the sections before the first section in this sections object
	 */
	public Sections<Type> siblingsBefore() {
		List<? extends Section<?>> siblings = first().parent().children().asList();
		int i = siblings.indexOf(getFirst());
		if (i < 0) throw new IllegalArgumentException("Invalid KDOM, cannot find section in children of parent");
		//noinspection unchecked
		List<Section<Type>> list = (List<Section<Type>>) siblings.subList(0, i);
		Collections.reverse(list);
		return $(list);
	}

	/**
	 * Returns the siblings of the first section in this sections object, starting with the sibling
	 * directly after the first section in this sections object
	 *
	 * @return the sections after the first section in this sections object
	 */
	public Sections<Type> siblingsAfter() {
		List<? extends Section<?>> siblings = first().parent().children().asList();
		int i = siblings.indexOf(getFirst());
		if (i < 0) throw new IllegalArgumentException("Invalid KDOM, cannot find section in children of parent");
		//noinspection unchecked
		List<Section<Type>> list = (List<Section<Type>>) siblings.subList(i + 1, siblings.size());
		return $(list);
	}

	/**
	 * Casts the whole Sections to contain only elements of a particular type. Please note that the elements are not
	 * casted immediately, but sequentially as they will be accessed. Therefore this method does not throw a
	 * ClassCastException, but such an exception will be thrown during iterating the sections.
	 * <p/>
	 * This method is similar to {@link #filter(Class)}, but while {@link #filter(Class)} will remove all sections not
	 * being of the specified instance, this method will fail as soon as the first of these sections is accessed.
	 *
	 * @param clazz the class the contained sections shall be type of
	 * @param <R>   the type of the sections
	 * @return a new Sections object with all the original sections, but granted that all returned sections will be of
	 * the specified type
	 */
	@NotNull
	public <R extends Type> Sections<R> cast(Class<R> clazz) {
		return map(this, (section) -> Sections.cast(section, clazz));
	}

	/**
	 * Returns a new Sections containing only the sections of this sections object that are instances of the specified
	 * type.
	 * <p/>
	 * This method is similar to {@link #cast(Class)}, but while {@link #cast(Class)} will fail if a section is not of
	 * the specified instance, this method will remove the failing sections instead.
	 *
	 * @param clazz the class to be matched by the sections
	 * @param <R>   the class to be matched by the sections
	 * @return the sections matching the filter class
	 * @see #cast(Class)
	 */
	@NotNull
	public <R extends Type> Sections<R> filter(Class<R> clazz) {
		return new Sections<>(FilterTypeIterable.create(sections, clazz));
	}

	/**
	 * Returns a new Sections containing only the sections of this sections object that are instances of any of the
	 * specified types.
	 * <p/>
	 * This method is similar to {@link #cast(Class)}, but while {@link #cast(Class)} will fail if a section is not of
	 * the specified instance, this method will remove the failing sections instead.
	 *
	 * @param classes the classes to match any
	 * @param <R>     the common super-class of all classes
	 * @return the sections matching the filter class
	 * @see #cast(Class)
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	@NotNull
	public final <R extends Type> Sections<R> filter(Class<? extends R>... classes) {
		if (classes == null || classes.length == 0) return Sections.empty();
		return (Sections<R>) filter(section -> {
			T type = section.get();
			for (Class<? extends R> clazz : classes) {
				if (clazz.isInstance(type)) return true;
			}
			return false;
		});
	}

	/**
	 * Checks if the specified section is an instance of the specified type class (technically the section has a section
	 * {@link Type} which is of the specified type or is a class inherits or implements the specified type). The method
	 * returns true if (and only if) the method {@link #cast(Section, Class)} would be successful and the specified
	 * section is not null. If the specified section is null, false is returned.
	 *
	 * @param section   the section to be checked
	 * @param typeClass the class to check the section's type against
	 * @return if the section can be casted
	 * @throws NullPointerException is the specified class is null, but the section isn't
	 * @created 28.02.2012
	 */
	public static boolean hasType(Section<?> section, Class<?> typeClass) {
		// first check null, because Class.isInstance differs from
		// "instanceof"-operator for null objects
		//noinspection SimplifiableIfStatement
		if (section == null) return false;

		// check the type of the section
		return typeClass.isInstance(section.get());
	}

	/**
	 * Returns a new Sections instance containing the closest ancestor of each of this instance's sections that is an
	 * instance the specified type. In constrast to {@link #closest(Class)}, the ancestor must be a "real" ancestor, so
	 * the search for a matching type starts at the specified sections' parents. If any section of this instance does
	 * not have an ancestor of the specified type, the section is not considered in the resulting Sections objects.
	 *
	 * @param clazz the class to be matched by the ancestors
	 * @param <R>   the class to be matched by the ancestors
	 * @return the closest ancestor for each section matching the type
	 * @see #closest(Class)
	 */
	@NotNull
	public <R extends Type> Sections<R> ancestor(Class<R> clazz) {
		return mapNotNull(this, section -> Sections.ancestor(section, clazz));
	}

	/**
	 * Returns a new Sections instance containing the closest ancestor of each of this instance's sections that is an
	 * instance the specified type. In contrast to {@link #ancestor(Class)}, if any of the specified sections are
	 * instances of the specified type, the section is directly returned, without further searching. If any section of
	 * this instance does not have an ancestor of the specified type, the section is not considered in the resulting
	 * Sections objects.
	 *
	 * @param clazz the class to be matched by the ancestors
	 * @param <R>   the class to be matched by the ancestors
	 * @return the closest ancestor for each section matching the type
	 * @see #ancestor(Class)
	 */
	@NotNull
	public <R extends Type> Sections<R> closest(Class<R> clazz) {
		return mapNotNull(this, section ->
				clazz.isInstance(section.get()) ? cast(section, clazz) : Sections.ancestor(section, clazz));
	}

	/**
	 * Checks whether any of the current sections has an ancestor section with a type being an instance of the given
	 * class.
	 *
	 * @param clazz the class the ancestor is expected to have
	 * @return true if we find an ancestor with the given type, false otherwise
	 */
	public boolean hasAncestor(Class<? extends Type> clazz) {
		return !ancestor(clazz).isEmpty();
	}

	/**
	 * Checks whether any of the current sections has a successor section with a type being an instance of the given
	 * class.
	 *
	 * @param clazz the class the successor is expected to have
	 * @return true if we find a successor with the given type, false otherwise
	 */
	public boolean hasSuccessor(Class<? extends Type> clazz) {
		return !successor(clazz).isEmpty();
	}

	/**
	 * Returns a new Sections instance containing all children of each of this instance's sections. If any section have
	 * no children, the section is not considered in the resulting Sections objects.
	 *
	 * @return the children for each section
	 */
	@NotNull
	public Sections<Type> children() {
		return Sections.flatMap(this, Section::getChildren);
	}

	/**
	 * Returns a new Sections instance containing the parent of each of this instance's sections. If any section have no
	 * parent, the section is not considered in the resulting Sections objects.
	 *
	 * @return the parents for each section matching the type
	 */
	@NotNull
	public Sections<?> parent() {
		return mapNotNull(this, Section::getParent);
	}

	/**
	 * Returns a new Sections instance containing results of the mapping function for each section. If the mapping
	 * function returns 'null' the section is ignored for the resulting Sections instance).
	 *
	 * @param sections the sections to be filtered / mapped
	 * @param mapping  the mapping function to map each section
	 * @param <R>      the class to be matched by the ancestors
	 * @return the closest ancestor for each section matching the type
	 */
	private static <T extends Type, R extends Type> Sections<R> mapNotNull(Sections<T> sections, Function<Section<T>, Section<? extends R>> mapping) {
		return map(sections, mapping).filter(Objects::nonNull);
	}

	/**
	 * Returns a new Sections instance containing results of the mapping function for each section.
	 *
	 * @param sections the sections to be filtered / mapped
	 * @param mapping  the mapping function to map each section
	 * @param <R>      the class to be matched by the ancestors
	 * @return the closest ancestor for each section matching the type
	 */
	private static <T extends Type, R extends Type> Sections<R> map(Sections<T> sections, Function<Section<T>, Section<? extends R>> mapping) {
		return new Sections<>(() -> new SingleMaperator<>(sections.sections.iterator(), mapping));
	}

	private static final class SingleMaperator<T extends Type, R extends Type> implements Iterator<Section<R>> {
		private final Iterator<Section<T>> base;
		private final Function<Section<T>, Section<? extends R>> mapper;

		private SingleMaperator(Iterator<Section<T>> base, Function<Section<T>, Section<? extends R>> mapper) {
			this.base = base;
			this.mapper = mapper;
		}

		@Override
		public boolean hasNext() {
			return base.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Section<R> next() {
			return (Section<R>) mapper.apply(base.next());
		}
	}

	/**
	 * Returns a new Sections instance containing a number of sections for each section. If the mapping function returns
	 * 'null' the section is ignored for the resulting Sections instance.
	 *
	 * @param sections the sections to be filtered / mapped
	 * @param mapping  the mapping function to map each section
	 * @param <R>      the class to be matched by the ancestors
	 * @return the closest ancestor for each section matching the type
	 */
	private static <T extends Type, R extends Type> Sections<R> flatMap(Sections<T> sections, Function<Section<T>, Iterable<Section<? extends R>>> mapping) {
		return new Sections<>(() -> {
			Function<Section<T>, Iterator<Section<? extends R>>> fun =
					(section) -> mapping.apply(section).iterator();
			return new MultiMaperator<>(sections.sections.iterator(), fun);
		});
	}

	private static final class MultiMaperator<T extends Type, R extends Type> implements Iterator<Section<R>> {
		private final Iterator<Section<T>> base;
		private final Function<Section<T>, Iterator<Section<? extends R>>> mapper;

		private Iterator<Section<? extends R>> current = null;

		private MultiMaperator(Iterator<Section<T>> base, Function<Section<T>, Iterator<Section<? extends R>>> mapper) {
			this.base = base;
			this.mapper = mapper;
		}

		private void prepareCurrent() {
			// if a non-ended current is available do nothing
			if (current != null && current.hasNext()) return;

			// otherwise overwrite current by next non-empty one
			while (base.hasNext()) {
				current = mapper.apply(base.next());
				if (current != null && current.hasNext()) return;
			}

			// of no non-empty fount set to null to indicate end
			current = null;
		}

		@Override
		public boolean hasNext() {
			prepareCurrent();
			return current != null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Section<R> next() {
			if (!hasNext()) throw new NoSuchElementException();
			return (Section<R>) current.next();
		}
	}

	/**
	 * Returns whether the given section object is part of an article object that is still part of the main
	 * ArticleManager of the wiki. This means, that the section is for example still rendered and compiled.<p> Sections
	 * of outdated article objects (because of changes) are for example not live.
	 */
	public static boolean isLive(Section<?> section) {
		if (section == null) return false;
		ArticleManager manager = section.getArticleManager();
		return (manager != null) && manager.isLive(section);
	}

	/**
	 * Creates a list of class names of the types of the sections on the path from the given section to the root
	 * section.
	 *
	 * @created 28.11.2012
	 */
	public static List<String> typePathToRoot(Section<?> s) {
		List<String> result = new ArrayList<>();
		Section<?> father = s.getParent();
		while (father != null) {
			result.add(father.get().getClass().getSimpleName());
			father = father.getParent();
		}
		return result;
	}

	/**
	 * Returns all successors in pre order.
	 */
	public static List<Section<?>> successors(Section<?> section) {
		List<Section<?>> sections = new LinkedList<>();
		successors(section, sections);
		return sections;
	}

	private static void successors(Section<?> section, List<Section<?>> sections) {
		sections.add(section);
		for (Section<?> child : section.getChildren()) {
			Sections.successors(child, sections);
		}
	}

	public static Section<? extends Type> smallestSectionContaining(Section<?> section, int start, int end) {
		Section<? extends Type> s = null;
		int nodeStart = section.getOffsetInArticle();
		if (nodeStart <= start && nodeStart + section.getText().length() >= end) {
			s = section;
			for (Section<?> sec : section.getChildren()) {
				Section<? extends Type> sub = Sections.smallestSectionContaining(
						sec, start, end);
				if (sub != null) {
					s = sub;
				}
			}
		}
		return s;
	}

	public static Section<?> smallestSectionContaining(Section<?> section, String text) {
		Section<?> s = null;
		if (section.getText().contains(text)) {
			s = section;
			for (Section<?> sec : section.getChildren()) {
				Section<?> sub = Sections.smallestSectionContaining(sec, text);
				if (sub != null) {
					s = sub;
				}
			}
		}
		return s;
	}

	public static List<Section<?>> smallestSectionsContaining(Section<?> section, String text) {
		List<Section<?>> foundSections = new ArrayList<>();
		smallestSectionsContaining(section, text, foundSections);
		return foundSections;
	}

	private static void smallestSectionsContaining(Section<?> section, String text, Collection<Section<?>> foundSections) {
		List<Section<?>> temp = new ArrayList<>();
		if (section.getText().contains(text)) {
			for (Section<?> sec : section.getChildren()) {
				List<Section<?>> smallestSectionsContaining = Sections.smallestSectionsContaining(sec, text);
				temp.addAll(smallestSectionsContaining);
			}
			if (temp.isEmpty()) temp.add(section);
		}
		foundSections.addAll(temp);
	}

	/**
	 * This method returns the closest ancestor of the specified section matching the specified class as its type. The
	 * ancestor must be a "real" ancestor, so the search for a matching type starts at the specified sections parent.
	 * The specified class matches if the type object of a section if an instance of the specified class or an instance
	 * of any sub-class of the specified class or interface.
	 * <p/>
	 *
	 * @param section the section to get the ancestor section for
	 * @param clazz   the class of the ancestor to be matched
	 * @return the first ancestor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <T extends Type> Section<T> ancestor(Section<?> section, Class<T> clazz) {
		Section<? extends Type> parent = section.getParent();
		if (parent == null) return null;

		if (clazz.isInstance(parent.get())) {
			return Sections.cast(parent, clazz);
		}

		return Sections.ancestor(parent, clazz);
	}

	/**
	 * Finds the first child with the given type in the given Section.
	 */
	public static <T extends Type> Section<T> child(Section<?> section, Class<T> clazz) {
		for (Section<?> child : section.getChildren()) {
			if (clazz.isAssignableFrom(child.get().getClass())) {
				return cast(child, clazz);
			}
		}
		return null;
	}

	/**
	 * Finds all children with the given Type in the children of the given Section.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Type> List<Section<T>> children(Section<?> section, Class<T> clazz) {
		List<Section<T>> result = new ArrayList<>();
		if (canHaveSuccessor(section, clazz)) {
			for (Section<?> s : section.getChildren()) {
				if (clazz.isAssignableFrom(s.get().getClass())) {
					result.add((Section<T>) s);
				}
			}
		}
		return result;
	}

	public static boolean canHaveSuccessor(Section<?> section, Class<?>... classes) {
		Object type = section.get();
		if (type instanceof AbstractType aType) {
			for (Class<?> clazz : classes) {
				if (Types.canHaveSuccessorOfType(aType, clazz)) {
					return true;
				}
			}
			return false;
		}
		// should not happen, but just in case: we don't
		// know and say yes to be safe
		return true;
	}

	/**
	 * This method returns the first successor in depth-first-search of the specified section matching the specified
	 * class as its type. The class matches if the type object of a section is an instance of the specified class or an
	 * instance of any sub-class of the specified class or interface. If the specified section matches the specified
	 * class the specified section is returned.
	 * <p/>
	 *
	 * @param section the section to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the first successor sections of the specified class
	 * @created 09.12.2013
	 */
	@Nullable
	public static <T extends Type> Section<T> successor(Section<?> section, Class<T> clazz) {
		if (section == null) return null;
		if (clazz.isInstance(section.get())) {
			return cast(section, clazz);
		}

		if (canHaveSuccessor(section, clazz)) {
			for (Section<?> sec : section.getChildren()) {
				Section<T> s = Sections.successor(sec, clazz);
				if (s != null) return s;
			}
		}
		return null;
	}

	/**
	 * This method returns the if there is any successor of the specified section matching the specified class as its
	 * type. The class matches if the type object of a section is an instance of the specified class or an instance of
	 * any sub-class of the specified class or interface. If the specified section matches the specified class, true is
	 * returned as well.
	 * <p/>
	 *
	 * @param section the section to check the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return true if there is any successor section of the specified class
	 * @created 09.12.2013
	 */
	public static <T extends Type> boolean hasSuccessor(Section<?> section, Class<T> clazz) {
		return successor(section, clazz) != null;
	}

	/**
	 * This method returns all the successors of the specified article matching the specified class as their type. The
	 * class matches if the type object of a section if an instance of the specified class or an instance of any
	 * sub-class of the specified class or interface.
	 * <p/>
	 *
	 * @param article the article to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the list of all successor sections of the specified class
	 * @created 09.12.2013
	 */
	@NotNull
	public static <T extends Type> List<Section<T>> successors(Article article, Class<T> clazz) {
		return successors(article.getRootSection(), clazz);
	}

	/**
	 * This method returns all the successors of the specified section matching the specified class as their type. The
	 * class matches if the type object of a section if an instance of the specified class or an instance of any
	 * sub-class of the specified class or interface. If the specified section matches the specified class the specified
	 * section is contained in the returned list.
	 * <p/>
	 * If a section within the sub-tree matches the specified class, and some of its sub-sections will also match the
	 * class, both are contained in the returned list.
	 *
	 * @param section the section to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the list of all successor sections of the specified class
	 * @created 09.12.2013
	 */
	@NotNull
	public static <T extends Type> List<Section<T>> successors(Section<?> section, Class<T> clazz) {
		List<Section<T>> result = new ArrayList<>();
		successors(section, clazz, result);
		return result;
	}

	/**
	 * This method returns all the successors of the specified sections matching the specified class as their type. The
	 * class matches if the type object of a section if an instance of the specified class or an instance of any
	 * sub-class of the specified class or interface. If any of the specified sections matches the specified class the
	 * specified section contained in the returned list.
	 *
	 * @param sections the sections to get the successor sections for
	 * @param clazz    the class of the successors to be matched
	 * @return the list of all successor sections of the specified class
	 * @created 09.12.2013
	 */
	@NotNull
	public static <T extends Type> List<Section<T>> successors(Collection<Section<?>> sections, Class<T> clazz) {
		List<Section<T>> result = new ArrayList<>();
		for (Section<?> section : sections) {
			successors(section, clazz, result);
		}
		return result;
	}

	/**
	 * This method returns the first successor in depth-first-search of the specified article matching the specified
	 * class as its type. The class matches if the type object of a section is an instance of the specified class or an
	 * instance of any sub-class of the specified class or interface.
	 * <p/>
	 *
	 * @param article the article to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the first successor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <T extends Type> Section<T> successor(Article article, Class<T> clazz) {
		if (article == null) return null;
		return successor(article.getRootSection(), clazz);
	}

	/**
	 * Finds all Sections of the given type in the given article manager.
	 * <p/>
	 * WARNING: This could take a while for very large wikis!
	 *
	 * @param articleManager ArticleManager to be searched
	 * @param clazz          Types to be searched
	 * @created 08.01.2014
	 */
	@NotNull
	public static <T extends Type> Collection<Section<T>> successors(ArticleManager articleManager, Class<T> clazz) {
		return articleManager.getArticles().parallelStream()
				.flatMap(article -> successors(article, clazz).stream())
				.collect(toList());
	}

	/**
	 * This method returns the first successor in depth-first-search of the specified article matching the specified
	 * type instance. The class matches if the type object of a section equals specified instance. If the specified
	 * section matches the specified type instance the specified section is returned.
	 * <p/>
	 * <b>Note:</b><br> This method selects more specific sections than #successor(Article, Class)
	 * will do.
	 *
	 * @param article      the article to get the successor section for
	 * @param typeInstance the type instance of the successor to be matched
	 * @return the first successor section of the specified type instance
	 * @created 09.12.2013
	 */
	public static <T extends Type> Section<T> successor(Article article, T typeInstance) {
		return successor(article.getRootSection(), typeInstance);
	}

	/**
	 * This method returns the first successor in depth-first-search of the specified section matching the specified
	 * type instance. The class matches if the type object of a section equals specified instance. If the specified
	 * section matches the specified type instance the specified section is returned.
	 * <p/>
	 * <b>Note:</b><br> This method selects more specific sections than #successor(Section, Class)
	 * will do.
	 *
	 * @param section      the section to get the successor section for
	 * @param typeInstance the type instance of the successor to be matched
	 * @return the first successor section of the specified type instance
	 * @created 09.12.2013
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Type> Section<T> successor(Section<?> section, T typeInstance) {
		if (typeInstance.equals(section.get())) {
			return (Section<T>) section;
		}

		if (canHaveSuccessor(section, typeInstance.getClass())) {
			for (Section<?> sec : section.getChildren()) {
				Section<T> s = successor(sec, typeInstance);
				if (s != null) return s;
			}
		}
		return null;
	}

	/**
	 * This method returns a list of all successor in depth-first-search of the specified article matching the specified
	 * type instance. The class matches if the type object of a section equals specified instance. If the specified
	 * section matches the specified type instance the specified section is contained in the returned list.
	 * <p/>
	 * <b>Note:</b><br> This method selects more specific sections than #successors(Article, Class)
	 * will do.
	 *
	 * @param article      the article to get the successor sections for
	 * @param typeInstance the type instance of the successors to be matched
	 * @return the successor sections of the specified type instance
	 * @created 09.12.2013
	 */
	@NotNull
	public static <T extends Type> List<Section<T>> successors(Article article, T typeInstance) {
		return successors(article.getRootSection(), typeInstance);
	}

	/**
	 * This method returns a list of all successor in depth-first-search of the specified section matching the specified
	 * type instance. The class matches if the type object of a section equals specified instance. If the specified
	 * section matches the specified type instance the specified section is contained in the returned list.
	 * <p/>
	 * <b>Note:</b><br> This method selects more specific sections than #successors(Section, Class)
	 * will do.
	 *
	 * @param section      the section to get the successor sections for
	 * @param typeInstance the type instance of the successors to be matched
	 * @return the successor sections of the specified type instance
	 * @created 09.12.2013
	 */
	@NotNull
	public static <T extends Type> List<Section<T>> successors(Section<?> section, T typeInstance) {
		List<Section<T>> result = new LinkedList<>();
		successors(section, typeInstance, result);
		return result;
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below the given Section.
	 */
	public static <T extends Type> void successors(Section<?> section, Class<T> clazz, List<Section<T>> found) {
		if (clazz.isAssignableFrom(section.get().getClass())) {
			found.add(cast(section, clazz));
		}
		if (canHaveSuccessor(section, clazz)) {
			for (Section<?> child : section.getChildren()) {
				Sections.successors(child, clazz, found);
			}
		}
	}

	/**
	 * Finds all successors of the specified section-type in the KDOM below the given Section. Note that this method is
	 * more specific as calling <code>successors(Section, Class&lt;T&gt;, List&lt;...&gt;)</code>, because it only
	 * collects sections that have the specified type instance instead (or an equal instance) of the specified type
	 * class.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Type> void successors(Section<?> section, T typeInstance, List<Section<T>> found) {
		if (typeInstance.equals(section.get())) {
			found.add((Section<T>) section);
		}
		if (canHaveSuccessor(section, typeInstance.getClass())) {
			for (Section<?> child : section.getChildren()) {
				Sections.successors(child, typeInstance, found);
			}
		}
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM to the depth of
	 * <code>depth</code> below the argument Section.
	 */
	public static <T extends Type> void successors(Section<?> section, Class<T> clazz, int depth, List<Section<T>> found) {

		if (clazz.isAssignableFrom(section.get().getClass())) {
			found.add(cast(section, clazz));
		}
		if (depth == 0) {
			return;
		}
		if (canHaveSuccessor(section, clazz)) {
			for (Section<?> sec : section.getChildren()) {
				Sections.successors(sec, clazz, depth - 1, found);
			}
		}
	}

	/**
	 * @param id is the ID of the Section to be returned
	 * @return the Section for the given ID or null if no Section exists for this ID.
	 */
	public static Section<?> get(String id) {
		return Section.get(id);
	}

	/**
	 * Returns the section with the given id and casts it to the supplied class. For more information see get(id) and
	 * cast(section, class);
	 *
	 * @param id        is the ID of the Section to be returned
	 * @param typeClass the class to cast the generic section to
	 * @return the Section for the given ID or null if no Section exists for this ID.
	 */
	public static <T extends Type> Section<T> get(String id, Class<T> typeClass) {
		return cast(get(id), typeClass);
	}

	/**
	 * @param web            is the web in which the Section should be searched
	 * @param title          is the title of the article in which the Section should be searched
	 * @param positionInKDOM is the position of the Section in the Lists of children in the ancestorOneOf of the given
	 *                       wanted Section
	 * @return the Section on the given position in the KDOM, if it exists
	 * @created 11.12.2011
	 */
	public static Section<?> get(String web, String title, List<Integer> positionInKDOM) {
		return get(Environment.getInstance().getArticle(web, title), positionInKDOM);
	}

	/**
	 * @param article        is the article in which the Section should be searched
	 * @param positionInKDOM is the position of the Section in the Lists of children in the ancestorOneOf of the given
	 *                       wanted Section
	 * @return the Section on the given position in the KDOM, if it exists
	 * @created 11.12.2011
	 */
	public static Section<?> get(Article article, List<Integer> positionInKDOM) {
		Section<?> temp = article.getRootSection();
		for (Integer pos : positionInKDOM) {
			if (temp.getChildren().size() <= pos) return null;
			temp = temp.getChildren().get(pos);
		}
		temp.setPositionInKDOM(positionInKDOM);
		return temp;
	}

	/**
	 * This class contains some information about the replacement success or the errors occurred. It also allows to send
	 * the detected error in a standardized manner to the http result of some action user context.
	 *
	 * @author Volker Belli (denkbares GmbH)
	 * @created 17.12.2013
	 */
	public static class ReplaceResult {

		private final Collection<String> missingSectionIDs;
		private final Collection<String> forbiddenArticles;
		private final List<SectionInfo> sectionInfos;

		public ReplaceResult(List<SectionInfo> sectionInfos, Collection<String> missingSectionIDs, Collection<String> forbiddenArticles) {
			this.sectionInfos = sectionInfos;
			this.missingSectionIDs = missingSectionIDs;
			this.forbiddenArticles = forbiddenArticles;
		}

		/**
		 * Returns a map mapping the old section ids to the section ids replacing the old sections. The Map that
		 * provides for each changed Section a mapping from the old to the new id.
		 *
		 * @created 17.12.2013
		 */
		public Map<String, String> getSectionMapping() {
			return getOldToNewIdsMap(sectionInfos);
		}

		/**
		 * Sends the error occurred during the replacement to the user context's response. If there were no errors, the
		 * method has no effect on the response. Therefore this method can be called without checking for errors first.
		 *
		 * @param context the context to send the errors to
		 * @return if there have been any errors sent
		 * @created 17.12.2013
		 */
		public boolean sendErrors(UserActionContext context) throws IOException {
			return sendErrorMessages(context, missingSectionIDs, forbiddenArticles);
		}

		/**
		 * When replacing was not successful it returns all missing section IDs Can be used, if you don't want to send
		 * errors directly to response stream
		 *
		 * @return all missing section IDs, unmodifiable
		 */
		public Collection<String> getMissingSectionIDs() {
			return Collections.unmodifiableCollection(missingSectionIDs);
		}

		/**
		 * When replacing was not successful caused by missing user rights, it returns all article titles which hadn't
		 * edit rights for the user Can be used, if you don't want to send errors directly to response stream
		 *
		 * @return all forbidden article titles, unmodifiable
		 */
		public Collection<String> getForbiddenArticles() {
			return Collections.unmodifiableCollection(forbiddenArticles);
		}
	}

	/**
	 * Replaces a section with the specified text, but not in the KDOMs themselves. It collects the texts deep through
	 * the KDOM and appends the new text (instead of the original text) for the Sections with an ID in the sectionsMap.
	 * Finally the article is saved with this new content.
	 * <p/>
	 * If working on an action the resulting object may be used to send the errors during replacement back to the caller
	 * using {@link ReplaceResult#sendErrors(UserActionContext)}.
	 *
	 * @param context the user context to use for modifying the articles
	 * @param text    the new text for the specified section
	 * @return a result object containing some information about the replacement success or the errors occurred
	 */

	public static ReplaceResult replace(UserContext context, String sectionID, String text) {
		Map<String, String> map = new HashMap<>();
		map.put(sectionID, text);
		return replace(context, map);
	}

	/**
	 * Replaces Sections with the given texts, but not in the KDOMs themselves. It collects the texts deep through the
	 * KDOM and appends the new text (instead of the original text) for the Sections with an ID in the sectionsMap.
	 * Finally, the article is saved with this new content.
	 * <p/>
	 * If working on an action the resulting object may be used to send the errors during replacement back to the caller
	 * using {@link ReplaceResult#sendErrors(UserActionContext)}.
	 *
	 * @param context     the user context to use for modifying the articles
	 * @param sectionsMap containing pairs of the section id and the new text for this section
	 * @return a result object containing some information about the replacement success or the errors occurred
	 */
	public static ReplaceResult replace(UserContext context, Map<String, String> sectionsMap) {
		return replace(context, sectionsMap, null);
	}

	/**
	 * Replaces Sections with the given texts, but not in the KDOMs themselves. It collects the texts deep through the
	 * KDOM and appends the new text (instead of the original text) for the Sections with an ID in the sectionsMap.
	 * Finally the article is saved with this new content.
	 * <p/>
	 * If working on an action the resulting object may be used to send the errors during replacement back to the caller
	 * using {@link ReplaceResult#sendErrors(UserActionContext)}.
	 *
	 * @param context     the user context to use for modifying the articles
	 * @param sectionsMap containing pairs of the section id and the new text for this section
	 * @param changeNote  the change notes to be associated with the given changes
	 * @return a result object containing some information about the replacement success or the errors occurred
	 */
	public static ReplaceResult replace(UserContext context, Map<String, String> sectionsMap, @Nullable String changeNote) {

		List<SectionInfo> sectionInfos = getSectionInfos(sectionsMap);
		Map<Article, Collection<String>> idsByTitle = getIdsByArticle(sectionsMap.keySet());

		Collection<String> missingIDs = new LinkedList<>();
		Collection<String> forbiddenArticles = new LinkedList<>();

		KnowWEUtils.getArticleManager(context.getWeb()).open();
		try {
			for (Map.Entry<Article, Collection<String>> entry : idsByTitle.entrySet()) {
				Collection<String> idsForCurrentTitle = entry.getValue();
				boolean errorsForThisTitle = handleErrors(entry.getKey(), idsForCurrentTitle, context,
						missingIDs, forbiddenArticles);
				if (!errorsForThisTitle) {
					replaceForTitle(entry.getKey().getTitle(), getSectionsMapForCurrentTitle(idsForCurrentTitle,
							sectionsMap), context, changeNote);
				}
			}
		}
		finally {
			KnowWEUtils.getArticleManager(context.getWeb()).commit();
		}

		if (!missingIDs.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			for (String missingID : missingIDs) {
				builder.append("ID: ").append(missingID).append("\n");
				builder.append("New text: ").append(sectionsMap.get(missingID)).append("\n");
				builder.append("\n");
			}
			LOGGER.error("The following sections were not replaced, because they could not be found. " +
					"Maybe there were changes to their articles in the meantime?\n{}", builder);
		}
		return new ReplaceResult(sectionInfos, missingIDs, forbiddenArticles);
	}

	private static List<SectionInfo> getSectionInfos(Map<String, String> sectionsMap) {
		List<SectionInfo> sectionInfos = new ArrayList<>(sectionsMap.size());
		for (String id : sectionsMap.keySet()) {
			Section<?> section = Sections.get(id);
			SectionInfo sectionInfo = new SectionInfo();
			sectionInfos.add(sectionInfo);
			if (section != null) {
				sectionInfo.oldText = section.getText();
				sectionInfo.positionInKDOM = section.getPositionInKDOM();
				sectionInfo.offSet = section.getOffsetInArticle();
				sectionInfo.sectionExists = true;
				sectionInfo.title = section.getTitle();
				sectionInfo.web = section.getWeb();
				sectionInfo.newText = sectionsMap.get(id);
			}
			sectionInfo.oldId = id;
		}
		return sectionInfos;
	}

	private static Map<Article, Collection<String>> getIdsByArticle(Collection<String> allIds) {
		Map<Article, Collection<String>> idsByTitle = new HashMap<>();
		for (String id : allIds) {
			Section<?> section = Sections.get(id);
			Article article = section == null ? null : section.getArticle();
			Collection<String> ids = idsByTitle.computeIfAbsent(article, k -> new ArrayList<>());
			ids.add(id);
		}
		return idsByTitle;
	}

	private static boolean handleErrors(
			Article article,
			Collection<String> ids,
			UserContext context,
			Collection<String> missingIDs,
			Collection<String> forbiddenArticles) {

		if (article == null) {
			missingIDs.addAll(ids);
			return true;
		}
		if (!Environment.getInstance()
				.getWikiConnector()
				.userCanEditArticle(article.getTitle(), context)) {
			forbiddenArticles.add(article.getTitle());
			return true;
		}
		return false;
	}

	private static Map<String, String> getSectionsMapForCurrentTitle(
			Collection<String> ids,
			Map<String, String> sectionsMap) {

		Map<String, String> sectionsMapForCurrentTitle = new HashMap<>();
		for (String id : ids) {
			sectionsMapForCurrentTitle.put(id, sectionsMap.get(id));
		}
		return sectionsMapForCurrentTitle;
	}

	private static void replaceForTitle(String title,
										Map<String, String> sectionsMapForCurrentTitle,
										UserContext context, @Nullable String changeNote) {
		String newArticleText = getNewArticleText(title, sectionsMapForCurrentTitle, context);
		Environment.getInstance()
				.getWikiConnector()
				.writeArticleToWikiPersistence(title, newArticleText, context, changeNote);
	}

	private static String getNewArticleText(
			String title,
			Map<String, String> sectionsMapForCurrentTitle,
			UserContext context) {

		Article article = Environment.getInstance().getArticle(context.getWeb(), title);
		return collectTextAndReplace(article, sectionsMapForCurrentTitle);
	}

	private static void trimSuperfluousLineBreaks(StringBuilder newText) {
		int pos = newText.length() - 1;
		List<Integer> lineBreakPositions = new ArrayList<>();
		while (pos >= 0 && Strings.isBlank(newText.charAt(pos))) {
			if (newText.charAt(pos) == '\n') {
				lineBreakPositions.add(pos);
			}
			pos--;
		}
		int lineBreakCount = lineBreakPositions.size();
		if (lineBreakCount >= 1) {
			newText.setLength(lineBreakPositions.get(lineBreakCount - 1) + 1);
		}
	}

	public static String collectTextAndReplace(Article article, Map<String, String> nodesMap) {
		StringBuilder builder = new StringBuilder();
		collectTextAndReplace(article.getRootSection(), nodesMap, builder);
		trimSuperfluousLineBreaks(builder);
		return builder.toString();
	}

	private static void collectTextAndReplace(Section<?> sec, Map<String, String> nodesMap, StringBuilder newText) {

		String text = nodesMap.get(sec.getID());
		if (text != null) {
			newText.append(text);
			return;
		}

		List<Section<?>> children = sec.getChildren();
		if (children == null || children.isEmpty()) {
			newText.append(sec.getText());
			return;
		}
		for (Section<?> section : children) {
			collectTextAndReplace(section, nodesMap, newText);
		}
	}

	public static StringBuilder collectTextAndReplace(Section<?> sec, Map<String, String> sectionMap) {
		StringBuilder newText = new StringBuilder();
		collectTextAndReplace(sec, sectionMap, newText);
		return newText;
	}

	private static boolean sendErrorMessages(UserActionContext context,
											 Collection<String> missingIDs,
											 Collection<String> forbiddenArticles)
			throws IOException {

		if (!missingIDs.isEmpty()) {
			context.sendError(409, "The Sections '" + missingIDs
					+ "' could not be found, possibly because somebody else"
					+ " has edited them.");
			return true;
		}
		if (!forbiddenArticles.isEmpty()) {
			context.sendError(403,
					"You do not have the permission to edit the following pages: "
							+ forbiddenArticles + ".");
			return true;
		}
		return false;
	}

	private static Map<String, String> getOldToNewIdsMap(List<SectionInfo> sectionInfos) {
		Collections.sort(sectionInfos);
		Map<String, String> oldToNewIdsMap = new HashMap<>();
		int diff = 0;
		for (SectionInfo sectionInfo : sectionInfos) {
			if (sectionInfo.sectionExists) {

				Section<?> section = get(sectionInfo.web, sectionInfo.title,
						sectionInfo.positionInKDOM);
				if (section == null) continue;

				String text = section.getText();
				String newText = sectionInfo.newText;
				boolean sameText = text.equals(newText);
				int textOffset = section.getOffsetInArticle() + diff;
				int newTextOffSet = sectionInfo.offSet;
				boolean sameOffset = textOffset == newTextOffSet;

				if (sameText && sameOffset) {
					diff += section.getText().length() - sectionInfo.oldText.length();
					oldToNewIdsMap.put(sectionInfo.oldId, section.getID());
					continue;
				}
			}
			oldToNewIdsMap.put(sectionInfo.oldId, null);
		}
		return oldToNewIdsMap;
	}

	/**
	 * Casts the specified section to a generic section of the specified object type's class. Before the cast is done,
	 * it is checked if the section has the specified object type as its type. If not, a {@link ClassCastException} is
	 * thrown (as usual).
	 * <p/>
	 * This method is required because it: <ol> <li>avoids a "unchecked cast" warning when compiling the code <li>does a
	 * runtime type check whether the cast is valid (java itself is not capable to do) </ol>
	 *
	 * @param <T>       the class to cast the generic section to
	 * @param section   the section to be casted
	 * @param typeClass the class to cast the generic section to
	 * @return the casted section
	 * @throws ClassCastException if the type of the section is neither of the specified class, nor a subclass of the
	 *                            specified class.
	 * @created 28.02.2012
	 */
	@Contract("null, _ -> null; !null, _ -> !null")
	public static <T extends Type> Section<T> cast(Section<?> section, Class<T> typeClass) throws ClassCastException {
		// first check null, because Class.isInstance differs from
		// "instanceof"-operator for null objects
		if (section == null) return null;

		// check the type of the section
		if (!typeClass.isInstance(section.get())) {
			throw new ClassCastException();
		}
		// and securely cast
		//noinspection unchecked
		return (Section<T>) section;
	}

	/**
	 * Checks if the specified section is an instance of the exactly the specified type class (technically the section
	 * has a section {@link Type} which is identical to the specified type). If the specified section is null, false is
	 * returned.
	 *
	 * @param section   the section to be checked
	 * @param typeClass the class to check the section's type against
	 * @return if the section has exactly the specified type
	 * @throws NullPointerException is the specified class is null, but the section isn't
	 * @created 28.02.2012
	 */
	public static boolean hasExactType(Section<?> section, Class<?> typeClass) {
		// first check null, because Class.isInstance differs from
		// "instanceof"-operator for null objects
		//noinspection SimplifiableIfStatement
		if (section == null) return false;

		// check the type of the section
		return typeClass.equals(section.get().getClass());
	}

	/**
	 * Returns a new Sections containing only the sections of this sections object that are accepted by the specified
	 * filter.
	 *
	 * @param filter the filter to select the sections
	 * @return the sections matching the filter
	 */
	@NotNull
	public Sections<T> filter(SectionFilter<T> filter) {
		return new Sections<>(() -> SectionFilter.filter(sections.iterator(), filter));
	}

	/**
	 * Returns a new Sections containing only the sections of this sections object that have ancestors of the given
	 * type.
	 *
	 * @param clazz the type of ancestor the sections are required to have
	 * @return the sections with ancestors of the given type
	 */
	@NotNull
	public Sections<T> filterByAncestor(Class<? extends Type> clazz) {
		return new Sections<>(() -> SectionFilter.filter(sections.iterator(), section -> $(section).hasAncestor(clazz)));
	}

	/**
	 * Returns a new Sections containing only the sections of this sections object that have successors of the given
	 * type.
	 *
	 * @param clazz the type of successors the sections are required to have
	 * @return the sections with successors of the given type
	 */
	public Sections<T> filterBySuccessor(Class<? extends Type> clazz) {
		return new Sections<>(() -> SectionFilter.filter(sections.iterator(), section -> $(section).hasSuccessor(clazz)));
	}

	/**
	 * Returns the set of articles for a specified collection of sections. The Articles will remain the order of the
	 * first appearance within the specified section collection.
	 *
	 * @param sections the sections to get the articles for
	 * @return the articles of the sections
	 * @created 30.11.2013
	 */
	public static Set<Article> collectArticles(Collection<Section<?>> sections) {
		Function<Section<? extends Type>, Article> toArticle = Section::getArticle;
		return sections.stream().map(toArticle).collect(Collectors.toSet());
	}
}
