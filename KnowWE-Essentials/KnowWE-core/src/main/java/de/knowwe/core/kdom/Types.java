package de.knowwe.core.kdom;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import de.knowwe.core.kdom.rendering.Renderer;

public class Types {

	/**
	 * Returns the last element of the type path that matches the specified class. If there is no
	 * such type in the specified path, null is returned.
	 *
	 * @param path      the path to be searched
	 * @param typeClass the class to be searched
	 * @return the matched type
	 * @created 28.10.2013
	 */
	public static <T> T getLastOfType(Type[] path, Class<T> typeClass) {
		for (int i = path.length - 1; i >= 0; i--) {
			Type type = path[i];
			if (typeClass.isInstance(type)) {
				return typeClass.cast(type);
			}
		}
		return null;
	}

	/**
	 * Returns the first element of the type path that matches the specified class. If there is no
	 * such type in the specified path, null is returned.
	 *
	 * @param path      the path to be searched
	 * @param typeClass the class to be searched
	 * @return the matched type
	 * @created 28.10.2013
	 */
	public static <T> T getFirstOfType(Type[] path, Class<T> typeClass) {
		for (Type type : path) {
			if (typeClass.isInstance(type)) {
				return typeClass.cast(type);
			}
		}
		return null;
	}

	/**
	 * Returns whether the specified type is a leaf type, i.e., true if it has no children types.
	 *
	 * @param type the type to be tested
	 * @return if the specified type is a leaf type
	 * @throws NullPointerException if the specified type is null
	 * @created 27.08.2013
	 */
	public static boolean isLeafType(Type type) {
		return type.getChildrenTypes().isEmpty();
	}

	/**
	 * Injects a given renderer to all successors of the specified type in the type hierarchy.
	 *
	 * @param root     the root of the hierarchy
	 * @param clazz    the class of the type to set the renderer for
	 * @param renderer the renderer to be set
	 * @created 15.04.2011
	 */
	public static void injectRendererToSuccessors(Type root, Class<? extends AbstractType> clazz, Renderer renderer) {
		Collection<Type> set = getAllChildrenTypesRecursive(root);
		injectRendererToType(set, clazz, renderer);
	}

	/**
	 * Injects a given renderer to all direct children of the specified type.
	 *
	 * @param root     the root type to take the children from
	 * @param clazz    the class of the type to set the renderer for
	 * @param renderer the renderer to be set
	 * @created 15.04.2011
	 */
	public static void injectRendererToChildren(Type root, Class<? extends AbstractType> clazz, Renderer renderer) {
		Collection<Type> set = root.getChildrenTypes();
		injectRendererToType(set, clazz, renderer);
	}

	/**
	 * Injects a given renderer to all the types of the collection matching the specified type.
	 *
	 * @param types    the types to match against
	 * @param clazz    the class of the type to set the renderer for
	 * @param renderer the renderer to be set
	 * @created 15.04.2011
	 */
	public static void injectRendererToType(Collection<Type> types, Class<? extends AbstractType> clazz, Renderer renderer) {
		for (Type type : types) {
			if (clazz.isInstance(type)) {
				clazz.cast(type).setRenderer(renderer);
			}
		}
	}


	/**
	 * Adds the given child type <tt>childTypeToAdd</tt> to the first successor of the given <tt>hierarchy</tt> with the
	 * type <tt>parentTypeClass</tt>.
	 *
	 * @param type       the type hierarchy in which we want to add the ne child type for one of its successors
	 * @param parentTypeClass the class of the type where the new child type should be added to
	 * @param childTypeToAdd  the new type to be added as a child
	 */
	public static void injectChildTypeToSuccessor(Type type, Class<? extends Type> parentTypeClass, Type childTypeToAdd) {
		Type successor = successor(type, parentTypeClass);
		Objects.requireNonNull(successor);
		successor.addChildType(childTypeToAdd);
	}

	public static boolean replaceType(Type typeHierarchy, Class<? extends Type> typeToReplace, Type newType) {
		return replaceType(typeHierarchy, typeToReplace, newType, false);
	}

	/**
	 * Replaces the FIRST occurrence of the target class type with the new type
	 *
	 * @param typeHierarchy         the root of the hierarchy
	 * @param transferSectionFinder set this to true, if the SectionFinder of the replaced type should be transferred to
	 *                              the new type
	 * @created 02.07.2013
	 */
	public static boolean replaceType(Type typeHierarchy, Class<? extends Type> typeToReplace, Type newType, boolean transferSectionFinder) {
		Type replacedType = typeHierarchy.replaceChildType(typeToReplace, newType);
		if (replacedType != null) {
			handleTransfer(transferSectionFinder, replacedType, newType);
			return true;
		}
		else {
			for (Type childrenType : typeHierarchy.getChildrenTypes()) {
				if (replaceType(childrenType, typeToReplace, newType, transferSectionFinder)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void handleTransfer(boolean transferSectionFinder, Type replacedType, Type newType) {
		if (transferSectionFinder) {
			if (!(replacedType instanceof AbstractType && newType instanceof AbstractType)) {
				throw new IllegalArgumentException("Expecting " + AbstractType.class.getSimpleName() + "s if section finder is to be transferred");
			}
			((AbstractType) newType).setSectionFinder(((AbstractType) replacedType).getSectionFinder());
		}
	}

	/**
	 * Replaces all occurrence of the target class type with the new type
	 *
	 * @param typeHierarchy the root of the hierarchy
	 * @created 02.07.2013
	 */
	public static boolean replaceTypes(Type typeHierarchy, Class<? extends Type> typeToReplace, Type newType) {
		return replaceTypes(typeHierarchy, typeToReplace, () -> newType, false);
	}

	/**
	 * Replaces all occurrence of the target class type with the new type
	 *
	 * @param typeHierarchy         the root of the hierarchy
	 * @param transferSectionFinder set this to true, if the SectionFinder of the replaced type should be transferred to
	 *                              the new type
	 * @created 02.07.2013
	 */
	public static boolean replaceTypes(Type typeHierarchy, Class<? extends Type> typeToReplace, Supplier<Type> typeSupplier, boolean transferSectionFinder) {
		return replaceTypes(typeHierarchy, typeToReplace, typeSupplier, new HashSet<>(), transferSectionFinder);
	}

	private static boolean replaceTypes(Type typeHierarchy, Class<? extends Type> typeToReplace, Supplier<Type> typeSupplier, Set<Type> visited, boolean transferSectionFinder) {
		if (!visited.add(typeHierarchy)) return false;
		boolean replaced = false;
		for (Type childrenType : typeHierarchy.getChildrenTypes()) {
			replaced |= replaceTypes(childrenType, typeToReplace, typeSupplier, visited, transferSectionFinder);
		}
		Type newType = typeSupplier.get();
		Type replacedType = typeHierarchy.replaceChildType(typeToReplace, newType);
		if (replacedType != null) handleTransfer(transferSectionFinder, replacedType, newType);
		return replacedType != null || replaced;
	}

	/**
	 * Getting of all successor types of a {@link Type} in depth-first-order. The specified type
	 * will always become a member of the returned types.
	 *
	 * @param type the root of the type hierarchy to be fetched
	 * @return the list of all types
	 */
	public static Collection<Type> getAllChildrenTypesRecursive(Type type) {
		LinkedHashSet<Type> set = new LinkedHashSet<>();
		getAllChildrenTypesRecursive(type, set);
		return set;
	}

	private static void getAllChildrenTypesRecursive(Type type, Set<Type> allTypes) {
		// check for cyclic definitions
		if (allTypes.contains(type)) return;

		// add this item
		allTypes.add(type);

		// and add all children types from this type recursively
		for (Type childrentype : type.getChildrenTypes()) {
			getAllChildrenTypesRecursive(childrentype, allTypes);
		}
	}

	/**
	 * Retrieves the first successor {@link Type} of a depth-first-search being of the specified
	 * class (or being a subclass or implementation of the specified class) in the specified
	 * hierarchy. If no such type exists, null is returned
	 *
	 * @param type  the root of the type hierarchy to be fetched of the hierarchy
	 * @param clazz the class to be searched for
	 * @return the matched {@link Type} instance
	 * @created 02.03.2011
	 */
	public static <T extends Type> T successor(Type type, Class<T> clazz) {
		return successor(type, clazz, new HashSet<>());
	}

	private static <T extends Type> T successor(Type type, Class<T> clazz, Set<Type> visited) {
		if (!visited.add(type)) return null;
		if (type instanceof AbstractType && !canHaveSuccessorOfType((AbstractType) type, clazz)) return null;
		for (Type child : type.getChildrenTypes()) {
			if (clazz.isInstance(child)) {
				return clazz.cast(child);
			}
			T t = successor(child, clazz, visited);
			if (t != null) return t;
		}
		return null;
	}

	/**
	 * Retrieves all successor {@link Type}s of the specified class (or being a subclass or
	 * implementation of the specified class) in the specified hierarchy. If no such type exists, an
	 * empty collection is returned
	 *
	 * @param root  the root of the hierarchy
	 * @param clazz the class to be searched for
	 * @return the matched {@link Type} instance
	 * @created 02.03.2011
	 */
	public static <OT extends Type> Collection<OT> successors(Type root, Class<OT> clazz) {
		Collection<Type> types = getAllChildrenTypesRecursive(root);
		List<OT> result = new LinkedList<>();
		for (Type type : types) {
			if (clazz.isInstance(type)) {
				result.add(clazz.cast(type));
			}
		}
		return Collections.unmodifiableCollection(result);
	}

	/**
	 * Returns whether the given type potentially has a successor with the given class. If
	 * <tt>false</tt> is returned, we can be sure, that there is no successor with the given class.
	 * If it returns <tt>true</tt>, it is possible that there is a successor with the given class.
	 * If needed, use {@link Types#successor(Type, Class)} to be sure.
	 *
	 * @param clazz the type class we look for in the successors
	 * @return true if the type can have a successor with the given class, false if not
	 * @created 09.12.2013
	 */
	public static boolean canHaveSuccessorOfType(AbstractType type, Class<?> clazz) {
		// if the specified class is not an abstract class,
		// the hash-map is not initialized with, so return true to be save
		return !AbstractType.class.isAssignableFrom(clazz)
				|| type.getPotentialSuccessorTypes().contains(clazz);
	}

}
