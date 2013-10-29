package de.knowwe.core.kdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.knowwe.core.kdom.rendering.Renderer;

public class Types {

	/**
	 * Returns the last element of the type path that matches the specified
	 * class. If there is no such type in the specified path, null is returned.
	 * 
	 * @created 28.10.2013
	 * @param path the path to be searched
	 * @param typeClass the class to be searched
	 * @return the matched type
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
	 * Returns the first element of the type path that matches the specified
	 * class. If there is no such type in the specified path, null is returned.
	 * 
	 * @created 28.10.2013
	 * @param path the path to be searched
	 * @param typeClass the class to be searched
	 * @return the matched type
	 */
	public static <T> T getFirstOfType(Type[] path, Class<T> typeClass) {
		for (int i = 0; i < path.length; i++) {
			Type type = path[i];
			if (typeClass.isInstance(type)) {
				return typeClass.cast(type);
			}
		}
		return null;
	}

	/**
	 * Returns whether the specified type is a leaf type, i.e., true if it has
	 * no children types.
	 * 
	 * @created 27.08.2013
	 * @param type the type to be tested
	 * @return if the specified type is a leaf type
	 * @throws NullPointerException if the specified type is null
	 */
	public static boolean isLeafType(Type type) {
		return type.getChildrenTypes().isEmpty();
	}

	/**
	 * Returns whether the specified type is an instance of the specified
	 * {@link Type} instance.
	 * 
	 * @created 27.08.2013
	 * @param type the type to be tested
	 * @param clazz the class to check against
	 * @return if the type can be cast to be an instance from the specified
	 *         class
	 */
	public static boolean isAssignableFromType(Type type, Class<? extends Type> clazz) {
		return clazz.isAssignableFrom(type.getClass());
	}

	/**
	 * Injects a given renderer to all successors of the specified type in the
	 * type hierarchy.
	 * 
	 * @created 15.04.2011
	 * @param root the root of the hierarchy
	 * @param clazz the class of the type to set the renderer for
	 * @param renderer the renderer to be set
	 */
	public static void injectRendererToSuccessors(Type root, Class<? extends AbstractType> clazz, Renderer renderer) {
		Collection<Type> set = getAllChildrenTypesRecursive(root);
		injectRendererToType(set, clazz, renderer);
	}

	/**
	 * Injects a given renderer to all direct children of the specified type.
	 * 
	 * @created 15.04.2011
	 * @param root the root type to take the children from
	 * @param clazz the class of the type to set the renderer for
	 * @param renderer the renderer to be set
	 */
	public static void injectRendererToChildren(Type root, Class<? extends AbstractType> clazz, Renderer renderer) {
		Collection<Type> set = root.getChildrenTypes();
		injectRendererToType(set, clazz, renderer);
	}

	/**
	 * Injects a given renderer to all the types of the collection matching the
	 * specified type.
	 * 
	 * @created 15.04.2011
	 * @param set the types to match against
	 * @param clazz the class of the type to set the renderer for
	 * @param renderer the renderer to be set
	 */
	public static void injectRendererToType(Collection<Type> set, Class<? extends AbstractType> clazz, Renderer renderer) {
		for (Type t : set) {
			if (Types.isAssignableFromType(t, clazz)) {
				((AbstractType) t).setRenderer(renderer);
			}
		}
	}

	/**
	 * Replaces the FIRST occurrence of the target class type with the new type
	 * 
	 * @created 02.07.2013
	 * @param typeHierarchy the root of the hierarchy
	 * @param c
	 * @param newType
	 */
	public static boolean replaceType(Type typeHierarchy, Class<? extends Type> c, Type newType) {
		List<Type> childrenTypes = typeHierarchy.getChildrenTypes();
		int index = -1;
		for (Type child : childrenTypes) {
			if (Types.isAssignableFromType(child, c)) {
				index = childrenTypes.indexOf(child);
				// we only replace first occurrence
				break;
			}
			else {
				return replaceType(child, c, newType);
			}
		}
		if (index != -1) {
			// some overhead to actually replace type in children list
			List<Type> listCopy = new ArrayList<Type>();
			listCopy.addAll(childrenTypes);
			listCopy.add(index, newType);
			listCopy.remove(index + 1);
			((AbstractType) typeHierarchy).clearChildrenTypes();
			for (Type type : listCopy) {
				typeHierarchy.addChildType(type);
			}
			return true;
		}
		return false;
	}

	/**
	 * Getting of all successor types of a {@link Type} in depth-first-order.
	 * 
	 * @param type the root of the type hierarchy to be fetched
	 * @return the list of all types
	 */
	public static Collection<Type> getAllChildrenTypesRecursive(Type type) {
		LinkedHashSet<Type> set = new LinkedHashSet<Type>();
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
	 * Retrieves the first successor {@link Type} of a depth-first-search being
	 * of the specified class (or being a subclass or implementation of the
	 * specified class) in the specified hierarchy. If no such type exists, null
	 * is returned
	 * 
	 * @created 02.03.2011
	 * @param <OT>
	 * @param root the root of the hierarchy
	 * @param clazz the class to be searched for
	 * @return the matched {@link Type} instance
	 */
	public static <OT extends Type> Type findSuccessorType(Type root, Class<OT> clazz) {
		List<Type> childrenTypes = root.getChildrenTypes();
		for (Type type : childrenTypes) {
			if (Types.isAssignableFromType(type, clazz)) {
				return type;
			}
			Type t = findSuccessorType(type, clazz);
			if (t != null) return t;
		}
		return null;
	}
}
