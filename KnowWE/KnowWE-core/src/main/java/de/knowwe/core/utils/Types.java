/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.rendering.Renderer;

/**
 * This class offers some methods for the KnowWETypeBrowser and the
 * KnowWETypeActivator
 * 
 * @author Johannes Dienst
 * 
 */
public class Types {

	/**
	 * Injects a given renderer to a subtype in the hierarchy
	 * 
	 * @created 15.04.2011
	 * @param root
	 * @param clazz
	 * @param renderer
	 */
	public static void injectRendererToSuccessorType(Type root, Class<? extends Type> clazz, Renderer renderer) {
		TreeSet<Type> set = new TreeSet<Type>();
		getAllChildrenTypesRecursive(root, set);
		for (Type t : set) {
			if (t.isAssignableFromType(clazz)) {
				((AbstractType) t).setRenderer(renderer);
			}
		}
	}

	/**
	 * Getting of all ChildrenTypes of a Type.
	 * 
	 * @param type
	 * @param allTypes
	 * @return
	 */
	public static TreeSet<Type> getAllChildrenTypesRecursive(Type type, TreeSet<Type> allTypes) {

		// Recursionstop
		if (allTypes.contains(type)) {
			return allTypes;
		}

		allTypes.add(type);

		// check all allowed children types from this type
		if (type.getChildrenTypes() != null) {

			// NOTE: .getAllowedChildrenTypes() now returns an unmodifiable list
			// => copy
			List<Type> unModList = type.getChildrenTypes();
			List<Type> moreChildren = new ArrayList<Type>();
			moreChildren.addAll(unModList);

			// Loop Protection
			if (hasTypeInList(moreChildren, type)) removeTypeFromList(moreChildren, type);

			for (Type childrentype : moreChildren) {

				// if children does not contain this type
				if (!allTypes.contains(childrentype)) {
					allTypes.add(childrentype);
					for (Type c : childrentype.getChildrenTypes()) {
						TreeSet<Type> t = getAllChildrenTypesRecursive(c, allTypes);
						allTypes.addAll(t);
					}
				}
			}
		}
		return allTypes;
	}

	/**
	 * Removes a Type from a given List, because list.remove(type) is not
	 * functional (Different Objects)
	 * 
	 * @param types
	 * @param type
	 * @return
	 */
	private static void removeTypeFromList(List<? extends Type> types, Type type) {
		for (int i = 0; i < types.size(); i++) {
			if (types.get(i).getName().equals(type.getName())) {
				types.remove(i--);
			}
		}
	}

	/**
	 * Test if List contains a type.
	 * 
	 * @param children
	 * @param type
	 * @return
	 */
	private static boolean hasTypeInList(List<? extends Type> children, Type type) {
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).getName().equals(type.getName())) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * retrieves a (the first occurring!) successor type in the type schema if
	 * existing
	 * 
	 * 
	 * @created 02.03.2011
	 * @param <OT>
	 * @param root
	 * @param clazz
	 * @return
	 */
	public static <OT extends Type> Type findSuccessorType(Type root, Class<OT> clazz) {
		List<Type> childrenTypes = root.getChildrenTypes();
		for (Type type : childrenTypes) {
			if (type.isAssignableFromType(clazz)) {
				return type;
			}
			Type t = findSuccessorType(type, clazz);
			if (t != null) return t;
		}

		return null;
	}

	/**
	 * Collects all Types.
	 * 
	 * @return
	 */
	public static TreeSet<Type> getAllTypes() {
		return Types.getAllChildrenTypesRecursive(RootType.getInstance(),
						new TreeSet<Type>());
	}

	/**
	 * @See KnowWETypeBrowserAction
	 * 
	 * @param clazz
	 * @return
	 */
	public static Type findType(Class<? extends Type> clazz) {
		for (Type t : getAllTypes()) {
			if (t.isType(clazz)) {
				return t;
			}
		}
		return null;
	}
}
