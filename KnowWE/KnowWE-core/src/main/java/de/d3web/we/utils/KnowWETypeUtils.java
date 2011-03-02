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

package de.d3web.we.utils;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Type;

/**
 * This class offers some methods for the KnowWETypeBrowser and the
 * KnowWETypeActivator
 * 
 * @author Johannes Dienst
 * 
 */
public class KnowWETypeUtils {

	/**
	 * Removes duplicates. Needed because the java.util.Set-approach wont work
	 * (Different Objects). TODO: Maybe implement a special Set for this
	 * 
	 * @param cleanMe
	 * @return
	 */
	public static List<Type> cleanList(List<Type> cleanMe) {
		List<Type> cleaned = new ArrayList<Type>();
		for (int i = 0; i < cleanMe.size(); i++) {
			String name = cleanMe.get(i).getName();
			cleaned.add(cleanMe.get(i));
			for (int j = i + 1; j < cleanMe.size(); j++) {
				if ((cleanMe.get(j).getName()).equals(name)) {
					cleanMe.remove(j--);
				}
			}
		}
		return cleaned;
	}

	// /**
	// * Get the father element of the current cell content section. Search as
	// long as the section
	// * is instance of AbstractXMLObjectType. Used to get the
	// <code>Table</code> section itself.
	// *
	// * @param child
	// * @return
	// */
	// @Deprecated
	// public static Section getAncestorOfType(Section child, String classname)
	// {
	// if( child == null )
	// return null;
	//		
	// try {
	// if( Class.forName( classname ).isAssignableFrom(
	// child.get().getClass() ) ) return child;
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// return null;
	// }
	//		 
	// return getAncestorOfType(child.getFather(), classname);
	// }

	/**
	 * Getting of all ChildrenTypes of a Type.
	 * 
	 * @param type
	 * @param allTypes
	 * @return
	 */
	public static KnowWETypeSet getAllChildrenTypesRecursive(Type type, KnowWETypeSet allTypes) {

		// Recursionstop
		if (allTypes.contains(type)) {
			return allTypes;
		}

		allTypes.add(type);

		// check all allowed children types from this type
		if (type.getAllowedChildrenTypes() != null) {

			// NOTE: .getAllowedChildrenTypes() now returns an unmodifiable list
			// => copy
			List<Type> unModList = type.getAllowedChildrenTypes();
			List<Type> moreChildren = new ArrayList<Type>();
			moreChildren.addAll(unModList);

			// Loop Protection
			if (hasTypeInList(moreChildren, type)) removeTypeFromList(moreChildren, type);

			for (Type childrentype : moreChildren) {

				// if children does not contain this type
				if (!allTypes.contains(childrentype)) {
					allTypes.add(childrentype);
					for (Type c : childrentype.getAllowedChildrenTypes()) {
						KnowWETypeSet t = getAllChildrenTypesRecursive(c, allTypes);
						allTypes.addAll(t.toList());
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
		List<Type> childrenTypes = root.getAllowedChildrenTypes();
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
	 * Get the father element of the given section specified by class.
	 * 
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static <OT extends Type> Section<OT> getAncestorOfType(Section<?> s, Class<OT> clazz) {

		if (s == null) return null;

		if (clazz.isAssignableFrom(s.get().getClass())) return (Section<OT>) s;

		return getAncestorOfType(s.getFather(), clazz);
	}
}
