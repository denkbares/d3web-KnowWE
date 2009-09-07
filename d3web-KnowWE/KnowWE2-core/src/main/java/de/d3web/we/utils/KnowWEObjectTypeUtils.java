package de.d3web.we.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEObjectTypeComparator;

/**
 * This class provides some methods for the KnowWETypeBrowser
 * and the KnowWETypeActivator
 * 
 * @author Johannes
 *
 */
public class KnowWEObjectTypeUtils {

	/**
	 * Removes duplicates.
	 * 
	 * @param cleanMe
	 * @return
	 */
	public static List<KnowWEObjectType> cleanList(List<KnowWEObjectType> cleanMe) {
		List<KnowWEObjectType> cleaned = new ArrayList<KnowWEObjectType>();
		for(int i = 0;i < cleanMe.size();i++) {
			String name = cleanMe.get(i).getName();
			cleaned.add(cleanMe.get(i));
			for(int j = i+1;j < cleanMe.size();j++) {
				if((cleanMe.get(j).getName()).equals(name)) {
					cleanMe.remove(j--);
				}
			}
		}
		return cleaned;
	}
	
	/**
	 * Getting of all ChildrenTypes of a
	 * KnowWEObjectType.
	 * 
	 * @return
	 */
	public static List<KnowWEObjectType> getChildrenTypes(KnowWEObjectType type, List<KnowWEObjectType> children) {
		
		// Rekursionsabbruch
		if (hasTypeInList(children, type)) {
			return null;
		}
		
		List <KnowWEObjectType> children2 = new ArrayList<KnowWEObjectType>(children);
		
		// check all allowed children types from this type
		if(type.getAllowedChildrenTypes() != null) {
			List<? extends KnowWEObjectType> moreChildren = type.getAllowedChildrenTypes();
			
			// Loop Protection if type is in moreChildren
			if (hasTypeInList(moreChildren, type))
				moreChildren = removeTypeFromList(moreChildren, type);
			
			for (KnowWEObjectType childrentype : moreChildren) {
				
				// if children 2 does not contain this type
				if (!hasTypeInList(children2, childrentype)) {
					List<KnowWEObjectType> tester = getChildrenTypes(childrentype, children2);
					
					// tester contains children2-elements + new Elements
					if (tester != null){
						children2 = tester;					
					}
				}
			}
		}
		
		// perhaps type has been added yet with recursion
		if (!hasTypeInList(children2, type)) {
			children2.add(type);
		}
		
		// clean the list to remove duplicates
		children2 = cleanList(children2);
		
		return children2;
	}
	
	/**
	 * Removes a Type from a given List, because list.remove(type) is
	 * not functional (Different Objects)
	 * 
	 * @param types
	 * @param type
	 * @return
	 */
	private static List<? extends KnowWEObjectType> removeTypeFromList(List<? extends KnowWEObjectType> types, KnowWEObjectType type) {
		
		for (int i = 0; i < types.size(); i++) {
			if (types.get(i).getName().equals(type.getName())) {
				types.remove(i--);
			}
		}
		
		return types;
	}

	private static boolean hasTypeInList(List<? extends KnowWEObjectType> children, KnowWEObjectType type) {
		
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).getName().equals(type.getName())){
					return true;
				}				
			}
			return false;
		}

		return true;
	}

	/**
	 * Sorts a given KnowWEObjectTypeList.
	 * At the Moment lexicographical.
	 * 
	 * @param types
	 * @return
	 */
	public static List<KnowWEObjectType> sortTypeList(List<KnowWEObjectType> types) {
		KnowWEObjectTypeComparator c = new KnowWEObjectTypeComparator();
		Collections.sort(types, c);
		return types;
	}
	
	/**
	 * Checks if a given Section has a searched child.
	 * 
	 * @param type
	 * @param child
	 * @return
	 */
	public static boolean hasChildren(Section sec, String child) {
		for (Section s : sec.getChildren()) {
			if (s.getObjectType().getName().equals(child)) {
				return true;
			}
		}
		return false;
	}

//	/**
//	 * Searches the Children of a Section and only the children
//	 * of a Section for a given child
//	 * 
//	 * @param section
//	 * @param string
//	 */
//	public static Section findChildOfType(Section sec, String child) {
//		for (Section s : sec.getChildren())
//			if (s.getObjectType().getName().equals("XCLRelationWeight"))
//				return s;
//		return null;
//	}

}
