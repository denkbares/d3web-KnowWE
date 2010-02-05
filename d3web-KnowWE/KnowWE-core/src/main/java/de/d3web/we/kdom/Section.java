/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.we.core.KnowWEDomParseReport;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.basic.EmbracedType;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.basic.VerbatimType;
import de.d3web.we.kdom.filter.SectionFilter;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.include.IncludeAddress;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.visitor.Visitable;
import de.d3web.we.kdom.visitor.Visitor;
import de.d3web.we.module.DefaultTextType;
import de.d3web.we.module.KnowWEModule;
import de.d3web.we.user.UserSettingsManager;
import de.d3web.we.utils.PairOfInts;

/**
 * @author Jochen
 * 
 * This class represents a node in the Knowledge-DOM of KnowWE.
 * Basically it has some text, one type and a list of children.
 * 
 * Further, it has a reference to its father and a positionOffset to its
 * fathers text.
 * 
 * Further information can be attached to a node (TypeInformation), to
 * connect the text-parts with external resources, e.g. knowledge bases,
 * OWL, User-feedback-DBs etc.
 * 
 */
// TODO: vb: Section causes hundreds/thousands of compile warnings ==> use it consequent or remove Template declaration!
public class Section<T extends KnowWEObjectType> implements Visitable, Comparable<Section<? extends KnowWEObjectType>> {

//	private boolean reused = false;
	
	private Map<String, Boolean> reusedBy = new HashMap<String, Boolean>();
	
	protected boolean hasReusedSuccessor = false;

	private PairOfInts startPosFromTmp;

	private IncludeAddress address;

	protected KnowWEArticle article;

	protected boolean isExpanded = false;

	/**
	 * The id of this node, unique in an article
	 */
	protected String id;
	
	protected String specificID;
	
	protected boolean preAssignedID;

	/**
	 * Contains the text of this KDOM-node
	 */
	protected String originalText;

	/**
	 * The child-nodes of this KDOM-node. This forms the tree-structure of KDOM.
	 */
	protected List<Section<? extends KnowWEObjectType>> children = new LinkedList<Section<? extends KnowWEObjectType>>();	
	private LinkedList<Section<? extends KnowWEObjectType>> childrenParsingOrder = new LinkedList<Section<? extends KnowWEObjectType>>();

	/**
	 * The father section of this KDOM-node. Used for upwards navigation through
	 * the tree
	 */
	protected Section<? extends KnowWEObjectType> father;

	/**
	 * the position when the text off this node starts related to the text of
	 * the father node. Thus: for first child always 0, for 2nd
	 * firstChild.length() etc.
	 */
	protected int offSetFromFatherText;

	/**
	 * Type of this node.
	 * 
	 * @see KnowWEObjectType Each type has its own parser and renderer
	 */
	protected T objectType;
	
	protected T t;

	 public void add(T t) {
	        this.t = t;
	    }

	    public T get() {
	        return t;
	    }

	
	protected int absolutePositionStartInArticle = -1;

	/**
	 * only for KDOM-tree building algorithm - shouldnt be referenced later
	 * 
	 * @return
	 */
	public PairOfInts getPosition() {
		return startPosFromTmp;
	}

	public int getAbsolutePositionStartInArticle() {
		if (absolutePositionStartInArticle == -1) {
			calcAbsolutePositionStart();
		}
		return absolutePositionStartInArticle;
	}

	private void calcAbsolutePositionStart() {
		absolutePositionStartInArticle = offSetFromFatherText
				+ father.getAbsolutePositionStartInArticle();

	}

	public void setPosition(PairOfInts startPosFromTmp) {
		this.startPosFromTmp = startPosFromTmp;
	}
	
	public static <T extends KnowWEObjectType>Section<T> createTypedSection(String text, T o, Section father, int beginIndexOfFather, KnowWEArticle article, SectionID id, boolean isExpanded, IncludeAddress adress, T type) {
        return new Section<T>(text, o, father, beginIndexOfFather, article, id, isExpanded,adress);
    }

	
	
	/**
	 * looks for the child at a specific offset.
	 * 
	 * @param index
	 * @return
	 */
	public Section getChildSectionAtPosition(int index) {
		for (Section child : this.children) {
			if(child.getOffSetFromFatherText() <= index && index < child.getOffSetFromFatherText() + child.getOriginalText().length()) {
				return child;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * Constructor of a node <p/>
	 * Important: parses itself recursively by getting the
	 * allowed childrenTypes of the local type
	 * 
	 * @param text 
	 * 			the part of (article-source) text of the node
	 * @param objectType
	 *          type of the node
	 * @param father
	 * @param beginIndexFather
	 * @param article
	 *          is the article this section is hooked in
	 * @param address
	 */
	 private Section(String text, T objectType, Section father,
			int beginIndexFather, KnowWEArticle article, SectionID sectionID,
			boolean isExpanded, IncludeAddress address) {
		

		this.article = article;
		this.isExpanded = isExpanded;
		this.address = address;
		
		this.father = father;
		if (father != null)
			father.addChild(this);
		this.originalText = text == null ? "null" : text;
		this.objectType = objectType;
		offSetFromFatherText = beginIndexFather;
		
		if (sectionID == null) {
			this.preAssignedID = false;
			if (objectType instanceof KnowWEArticle) {
				this.id = new SectionID(getTitle()).toString();
			} else {
				this.id = new SectionID(father, objectType).toString();
			}
		} else {
			this.preAssignedID = true;
			this.id = sectionID.toString();
			this.specificID = sectionID.getSpecificID();
		}

		//fetches the allowed children types of the local type
		// TODO: Clean up here... maybe merge Include types with global types?
		List<KnowWEObjectType> types = new LinkedList<KnowWEObjectType>();

		if (objectType != null && objectType.getAllowedChildrenTypes() != null) {
			types.addAll(objectType.getAllowedChildrenTypes());
		}

		if (objectType != null
				&& !objectType.equals(Include.getInstance())
				&& !objectType.equals(PlainText.getInstance())
				&& !objectType.equals(VerbatimType.getInstance())) {
			types.add(Include.getInstance());
		}
		
		if (types.remove(DefaultTextType.getInstance())) {
			types.add(DefaultTextType.getInstance());
		}
		
		/**
		 * adding the registered global types to the children-list
		 * 
		 * TODO: Types should be able to restrict global types if they 
		 * dont want any (foreign) global types in the sub-KDOM-tree
		 */
		if (KnowWEEnvironment.GLOBAL_TYPES_ENABLED && !(objectType instanceof TerminalType)) {
			types.addAll(KnowWEEnvironment.getInstance().getGlobalTypes());
		}


		if (types.size() == 0 && objectType != null) {
			if (!objectType.equals(PlainText.getInstance())) {
				types.add(PlainText.getInstance());
			}
		}

		/**
		 * searches for children types and splits recursively
		 */
		if (!(this instanceof UndefinedSection)
				&& !objectType.equals(PlainText.getInstance()) 
				&& !objectType.equals(Include.getInstance())
				&& !isExpanded) {
			Sectionizer.getInstance().splitToSections(originalText, types, this,
					article);
		}
		
		childrenParsingOrder.addAll(children);
		
		/**
		 * sort children sections in text-order
		 */
		Collections.sort(children, new TextOrderComparator());
		
		if (objectType instanceof Include) {
			article.getIncludeSections().add(this);
		}
	}

	protected Section(KnowWEArticle article) {
		this.article = article;
	}

	/**
	 * don't allow Sections without a KnowWEArticle and ids
	 */
	private Section() {

	}
	
	/*
	 * verbalizes this node
	 */
	@Override
	public String toString() {
		return (objectType != null && objectType instanceof Include && article != null ? 
				article.getTitle() : this.getObjectType().getClass().getName() + " l:"
				+ this.getOriginalText().length()) + " - "
				+ this.getOriginalText();
	}

	/**
	 * ascends in the tree up to the module level and returns module of this
	 * subtree
	 * 
	 * @return module of the subtree this node is in
	 */
	public String getModuleName() {
		if (this.objectType instanceof KnowWEModule) {
			return objectType.getName();
		} else {
			if (father == null)
				return "modulename not found";
			return father.getModuleName();
		}

	}

	/**
	 * Adds a child to this node. Use for KDOM creation and editing only!
	 * 
	 */
	public void addChild(Section s) {
		if (s.getOffSetFromFatherText() == -1) {
			// WEAK! TODO: Find other way..
			if (s.father != null) {
				s.offSetFromFatherText = s.father.getOriginalText().indexOf(
						s.getOriginalText());
			}
		}
		this.children.add(s);
	}

	/**
	 * @return the text of this Section/Node
	 */
	public String getOriginalText() {
		return originalText;
	}
	
	public IncludeAddress getIncludeAddress() {
		return this.address;
	}
	
	/**
	 * Sets the text of this node. This IS an article source edit operation!
	 * TODO: Important - propagate changes through the whole tree OR ReIinit
	 * tree!
	 * 
	 * @param originalText
	 */
	public void setOriginalText(String newText) {
		this.originalText = newText;
		this.article.setDirty(true);
	}

	/**
	 * @return the list of child nodes
	 */
	public List<Section<? extends KnowWEObjectType>> getChildren() {
		if (objectType instanceof Include) {
			return KnowWEEnvironment.getInstance().getIncludeManager(getWeb()).getChildrenForSection(this);
		} 
		else {
			return children;
		}
		
	}

	/**
	 * return the list of child nodes matching a filter
	 * 
	 * @return
	 * @param filter
	 *            the filter to be matched
	 */
	public List<Section> getChildren(SectionFilter filter) {
		ArrayList<Section> list = new ArrayList<Section>();
		for (Section current : getChildren()) {
			if (filter.accept(current))
				list.add(current);
		}
		return list;
	}
	
	public void getAllNodesPreOrder(List<Section<? extends KnowWEObjectType>> nodes) {
		nodes.add(this);
		if (this.getChildren() != null) {
			for (Section<? extends KnowWEObjectType> child : this.getChildren()) {
				child.getAllNodesPreOrder(nodes);
			}
		}
	}
	
	public void getAllNodesParsingPostOrder(List<Section> nodes) {
		for (Section node:this.getChildrenParsingOrder()) {
			if (node.isExpanded) {
				node.getAllNodesPreOrder(nodes);
			} else {
				node.getAllNodesParsingPostOrder(nodes);
			}
		}
		nodes.add(this);
	}
	
	public void getAllNodesParsingPreOrderWithoutIncludes(List<Section> nodes) {
		nodes.add(this);
		if (!(objectType instanceof Include)) {
			for (Section node:this.getChildrenParsingOrder()) {
				if (node.isExpanded) {
					node.getAllNodesPreOrder(nodes);
				} else {
					node.getAllNodesParsingPreOrderWithoutIncludes(nodes);
				}
			}
		}
	}
	
	public void getAllNodesParsingPreOrder(List<Section> nodes) {
		nodes.add(this);
		for (Section node:this.getChildrenParsingOrder()) {
			if (node.isExpanded) {
				node.getAllNodesPreOrder(nodes);
			} else {
				node.getAllNodesParsingPreOrder(nodes);
			}
		}
	}
	
	/**
	 * @return the list of child nodes in parsing order
	 */
	public List<Section<? extends KnowWEObjectType>> getChildrenParsingOrder() {
		if (objectType instanceof Include) {
			return getChildren();
		} 
		else {
			sortChildrenParsingOrder();
			return childrenParsingOrder;
		}
	}
	
	/**
	 * Sorts children to parsing order regarding Includes. Includes per definitions
	 * get parsed last, but the Section they are including may normally get parsed 
	 * earlier. Since the correct order is important for the ReviseSubTreeHandler,
	 * the Includes get sorted to the position in the List where the Section they are
	 * including normally is positioned.
	 */
	private void sortChildrenParsingOrder() {
		if (childrenParsingOrder.size() < 2) {
			// already sorted
			return;
		}
		// for every ObjectType a list with all Includes that include a Section with this ObjectType
		Map<Class<? extends KnowWEObjectType>, List<Section<? extends KnowWEObjectType>>> includes = new HashMap<Class<? extends KnowWEObjectType>, List<Section<? extends KnowWEObjectType>>>();
		// all ObjectTypes that are possible in the children list
		Set<Class<? extends KnowWEObjectType>> types = new HashSet<Class<? extends KnowWEObjectType>>();
		for (KnowWEObjectType type:getObjectType().getAllowedChildrenTypes()) {
			types.add(type.getClass());
		}
		for (Section<? extends KnowWEObjectType> sec:childrenParsingOrder) {
			// store the Includes to the map
			if (sec.getObjectType() instanceof Include 
					&& types.contains(sec.getChildren().get(0).getObjectType().getClass())) {
				Class<? extends KnowWEObjectType> includedType = sec.getChildren().get(0).getObjectType().getClass();
				List<Section<? extends KnowWEObjectType>> includesOfType = includes.get(includedType);
				if (includesOfType == null) {
					includesOfType = new ArrayList<Section<? extends KnowWEObjectType>>();
					includes.put(includedType, includesOfType);
				}
				includesOfType.add(sec);
				
			}
		}
		if (includes.isEmpty() || childrenParsingOrder.isEmpty() 
				|| includes.size() == childrenParsingOrder.size()) {
			// nothing to sort here
			return;
		}
		for (List<Section<? extends KnowWEObjectType>> incList : includes.values()) {
			// remove the Includes from the children list
			childrenParsingOrder.removeAll(incList);
		}
		// and sort them back in
		// for each ObjectType move the pivot to the position behind the last Section
		// with this Type and then insert Includes that include a Section with the same
		// ObjectType (if given) 
		int i = 0; // pivot
		for(KnowWEObjectType type:getObjectType().getAllowedChildrenTypes()) {
			while (i < childrenParsingOrder.size() 
					&& childrenParsingOrder.get(i).getObjectType().isAssignableFromType(type.getClass())) {
				i++;
			}
			List<Section<? extends KnowWEObjectType>> includesOfType = includes.get(type.getClass());
			if (includesOfType != null) {
				childrenParsingOrder.addAll(i, includesOfType);
				i += includesOfType.size();
			}
		}		
	}

	/**
	 * returns father node
	 * 
	 * @return
	 */
	public Section<? extends KnowWEObjectType> getFather() {
		return father;
	}
	
	/**
	 * returns the type of this node
	 * 
	 * @return
	 */
	public T getObjectType() {
		return objectType;
	}

	/**
	 * returns offSet relatively to father text
	 * 
	 * @return
	 */
	public int getOffSetFromFatherText() {
		return offSetFromFatherText;
	}

	public void setOffSetFromFatherText(int offSet) {
		this.offSetFromFatherText = offSet;
	}

	/**
	 * return the article-name, if its not defined it asks the father
	 * 
	 * @return
	 */
	public String getTitle() {
		return this.article.getTitle();
	}

	public String getWeb() {
		return this.article.getWeb();
	}

	public KnowWEDomParseReport getReport() {
		return this.article.getReport();
	}

	public KnowWEArticle getArticle() {
		return this.article;
	}

	/**
	 * @return the depth of this Section inside the KDOM
	 */
	public int getDepth() {
		if (getObjectType() instanceof KnowWEArticle) {
			return 0;
		} else {
			return father.getDepth() + 1;
		}
	}

	/**
	 * checks whether this node has a son of type class1 beeing right from the
	 * given substring.
	 * 
	 * @param class1
	 * @param text
	 * @return
	 */
	public boolean hasRightSonOfType(Class class1, String text) {
		if(this.getObjectType() instanceof EmbracedType) {
			if(this.getFather().hasRightSonOfType(class1, text)) {
				return true;
			}
		}
		for (Section child : getChildren()) {
			if (child.getObjectType().isAssignableFromType(class1)) {
				if (this.originalText.indexOf(text) < child
						.getOffSetFromFatherText()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * checks whether this node has a son of type class1 beeing left from the
	 * given substring.
	 * 
	 * @param class1
	 * @param text
	 * @return
	 */
	public boolean hasLeftSonOfType(Class class1, String text) {
		if(this.getObjectType() instanceof EmbracedType) {
			if(this.getFather().hasLeftSonOfType(class1, text)) {
				return true;
			}
		}
		for (Section child : getChildren()) {
			if (child.getObjectType().isAssignableFromType(class1)) {
				if (this.originalText.indexOf(text) > child
						.getOffSetFromFatherText()) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * part of the visitor pattern
	 * 
	 * @see de.d3web.we.kdom.visitor.Visitable#accept(de.d3web.we.kdom.visitor.Visitor)
	 */
	@Override
	public void accept(Visitor v) {
		v.visit(this);

	}

	/**
	 * Verbalizes this node
	 * 
	 * @return
	 */
	public String verbalize() {
		StringBuffer buffi = new StringBuffer();
		String simpleName = this.getObjectType().getClass().getSimpleName();
		if(simpleName.equals(AnonymousType.class.getSimpleName())) {
			simpleName = simpleName += "("+this.getObjectType().getName()+")";
		}
		buffi.append(simpleName);
		//TODO: Show more of the IDs...
		buffi.append(", ID: " + getShortId());
		buffi.append(", length: " + this.getOriginalText().length() + " ("
				+ offSetFromFatherText + ")" + ", children: " + getChildren().size());
		buffi.append(", \"" + replaceNewlines(getShortText(50)));
		buffi.append("\"");
		return buffi.toString();
	}
	
	

	private String replaceNewlines(String shortText) {
		return shortText.replaceAll("\\n", "\\\\n");
	}

	private String getShortText(int i) {
		if (this.getOriginalText().length() < i)
			return this.getOriginalText();
		return this.getOriginalText().substring(0, i) + "...";
	}

	public String getId() {
		return id;
	}
	
	/**
	 * <b>IMPORTANT:</b> This is NOT the actual ID, this may NOT be unique and this should only be used 
	 * in situations where a short version of the ID is needed e.g. to make it easier to
	 * read for humans in debugging, logging and similar stuff.
	 */
	public String getShortId() {
		String temp = id;
		if (temp.contains(SectionID.SEPARATOR)) {
			temp = temp.substring(0, temp.indexOf(SectionID.SEPARATOR) + 1) + "..." 
				+ temp.substring(temp.lastIndexOf(SectionID.SEPARATOR));
		}
		return temp;
	}
	
	@Override
	public int compareTo(Section o) {
		return Integer.valueOf(this.getOffSetFromFatherText())
				.compareTo(Integer.valueOf(o.getOffSetFromFatherText()));
	}

	/**
	 * use findChild
	 * 
	 * @see findChild
	 * 
	 * @param nodeID
	 * @return
	 */
	@Deprecated
	public Section getNode(String nodeID) {
		if (this.id.equals(nodeID))
			return this;
		for (Section child : getChildren()) {
			Section s = child.getNode(nodeID);
			if (s != null)
				return s;
		}
		return null;
	}

	public void removeChild(Section s) {
		this.children.remove(s);

	}

	/**
	 * Scanning subtree for Section with given id
	 * 
	 * @param id2
	 * @return
	 */
	public Section findChild(String id2) {
		if (this.id.equals(id2))
			return this;
		for (Section child : getChildren()) {
			Section s = child.findChild(id2);
			if (s != null)
				return s;
		}
		return null;
	}

	public Section findSmallestNodeContaining(int start, int end) {
		Section s = null;
		int nodeStart = this.getAbsolutePositionStartInArticle();
		if (nodeStart <= start && nodeStart + originalText.length() >= end
				&& (!(this.getObjectType() instanceof PlainText))) {
			s = this;
			for (Section sec : getChildren()) {
				Section sub = sec.findSmallestNodeContaining(start, end);
				if (sub != null && (!(s.getObjectType() instanceof PlainText))) {
					s = sub;
				}
			}
		}
		return s;
	}

	public Section findSmallestNodeContaining(String text) {
		Section s = null;
		if (this.getOriginalText().contains(text)
				&& (!(this.getObjectType() instanceof PlainText))) {
			s = this;
			for (Section sec : getChildren()) {
				Section sub = sec.findSmallestNodeContaining(text);
				if (sub != null && (!(s.getObjectType() instanceof PlainText))) {
					s = sub;
				}
			}
		}
		return s;
	}

	/**
	 * Searches the Children of a Section and only the children of a Section for
	 * a given class
	 * 
	 * @param section
	 */
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> Section<? extends OT> findChildOfType(Class<OT> class1) {
		for (Section<?> s : this.getChildren()) {
			if (class1.isAssignableFrom(s.getObjectType().getClass())) {
				return (Section<? extends OT>) s;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> Section<? extends OT> findChildOfType(OT objectTypeInstance) {
		return (Section<? extends OT>) findChildOfType(objectTypeInstance.getClass());
	}

	/**
	 * Searches the Children of a Section and only the children of a Section for
	 * a given class
	 * 
	 * @param section
	 */
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> List<Section<OT>> findChildrenOfType(Class<OT> clazz) {
		List<Section<OT>> result = new ArrayList<Section<OT>>();
		for (Section<?> s : this.getChildren())
			if (clazz.isAssignableFrom(s.getObjectType().getClass()))
				result.add((Section<OT>) s);
		return result;
	}
	
	/**
	 * Searches the Children of a Section and only the children of a Section for
	 * a given class
	 * 
	 * @param section
	 */
	public <OT extends KnowWEObjectType> List<Section<OT>> findChildrenOfType(OT t) {
		List<Section<OT>> result = new ArrayList<Section<OT>>();
		for (Section s : this.getChildren())
			if (t.getClass().isAssignableFrom(s.getObjectType().getClass()))
				result.add(s);
		return result;
	}
	
	public <T extends KnowWEObjectType>Section<T> findSuccessor(T t) {
		Class<?> class1 = t.getClass();
		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			return (Section<T>) this;
		}
		for (Section sec : getChildren()) {
			Section<T> s = sec.findSuccessor(t);
			if (s != null)
				return s;
		}

		return null;
	}
	
	@Deprecated
	public Section findSuccessor(Class<?> class1) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			return this;
		}
		for (Section sec : getChildren()) {
			Section s = sec.findSuccessor(class1);
			if (s != null)
				return s;
		}

		return null;
	}
	
	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section.
	 */
	@Deprecated
	public void findSuccessorsOfType(Class<?> class1, List<Section> found) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			found.add(this);
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfType(class1, found);
		}
	}
	
	public <T extends KnowWEObjectType>void findSuccessorsOfType(T t, List<Section<T>> found) {
		Class<?> class1 = t.getClass();
		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			found.add((Section<T>)this);
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfType(t, found);
		}
	}
	
	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section and stores them in a Map, using their originalText as key.
	 */
	@Deprecated
	public void findSuccessorsOfType(Class<?> class1, Map<String, Section> found) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			Section tmp = found.get(this.getOriginalText());
			// only replace the finding by this Section, if this Section is not reused
			// but the Section already in the map is reused
			if (tmp == null || (tmp.isReusedBy(getTitle()) && !this.isReusedBy(getTitle()))) {
				found.put(this.getOriginalText(), this);
			}
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfType(class1, found);
		}

	}
	
	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section and stores them in a Map, using their originalText as key.
	 */
	public <T extends KnowWEObjectType>void findSuccessorsOfType(T t, Map<String, Section<T>> found) {

		if (t.getClass().isAssignableFrom(this.getObjectType().getClass())) {
			Section tmp = found.get(this.getOriginalText());
			// only replace the finding by this Section, if this Section is not reused
			// but the Section already in the map is reused
			if (tmp == null || (tmp.isReusedBy(getTitle()) && !this.isReusedBy(getTitle()))) {
				found.put(this.getOriginalText(), (Section<T>)this);
			}
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfType(t, found);
		}

	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM to the depth
	 * of <code>depth</code> below this Section.
	 */
	@Deprecated
	public void findSuccessorsOfType(Class<?> class1, int depth,
			List<Section> found) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			found.add(this);
		}
		if (depth == 0) {
			return;
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfType(class1, depth--, found);
		}

	}
	
	/**
	 * Finds all successors of type <code>class1</code> in the KDOM to the depth
	 * of <code>depth</code> below this Section.
	 */
	public <T extends KnowWEObjectType> void findSuccessorsOfType(T t, int depth,
			List<Section<T>> found) {

		if (t.getClass().isAssignableFrom(this.getObjectType().getClass())) {
			found.add((Section<T>)this);
		}
		if (depth == 0) {
			return;
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfType(t, depth--, found);
		}

	}

	/**
	 * Finds all successors of type <tt>class1</tt> in the KDOM at the end of the
	 * given path of ancestors. If your <tt>path</tt> starts with the ObjectType 
	 * of this Section, set <tt>index</tt> to <tt>0</tt>. Else set the <tt>index</tt>
	 * to the index of the ObjectType of this Section in the path.
	 * </p>
	 * Stores found successors in a Map of Sections, using their originalTexts as key.
	 */

	public void findSuccessorsOfTypeAtTheEndOfPath(
			List<Class<? extends KnowWEObjectType>> path,
			int index,
			Map<String, Section> found) {

		if (index < path.size() - 1 && path.get(index).isAssignableFrom(this.getObjectType().getClass())) {
			for (Section sec : getChildren()) {
				sec.findSuccessorsOfTypeAtTheEndOfPath(path, index + 1, found);
			}
		} else if (index == path.size() - 1 && path.get(index).isAssignableFrom(this.getObjectType().getClass())) {
			found.put(this.getOriginalText(), this);
		}

	}
	

	
	/**
	 * Finds all successors of type <tt>class1</tt> in the KDOM at the end of the
	 * given path of ancestors. If your <tt>path</tt> starts with the ObjectType 
	 * of this Section, set <tt>index</tt> to <tt>0</tt>. Else set the <tt>index</tt>
	 * to the index of the ObjectType of this Section in the path.
	 * </p>
	 * Stores found successors in a List of Sections
	 * 
	 */
	public void findSuccessorsOfTypeAtTheEndOfPath(
			List<Class<? extends KnowWEObjectType>> path,
			int index,
			List<Section> found) {

		if (index < path.size() - 1 && path.get(index).isAssignableFrom(this.getObjectType().getClass())) {
			for (Section sec : getChildren()) {
				sec.findSuccessorsOfTypeAtTheEndOfPath(path, index + 1, found);
			}
		} else if (index == path.size() - 1 && path.get(index).isAssignableFrom(this.getObjectType().getClass())) {
			found.add(this);
		}

	}

	/**
	 * 
	 * @return a List of ObjectTypes beginning at the KnowWWEArticle and ending
	 *         at this Section. Returns <tt>null</tt> if no path is found.
	 */
	public List<Class<? extends KnowWEObjectType>> getPathFromArticleToThis() {
		LinkedList<Class<? extends KnowWEObjectType>> path = new LinkedList<Class<? extends KnowWEObjectType>>();
		
		path.add(getObjectType().getClass());
		Section father = getFather();
		while (father != null) {
			path.addFirst(father.getObjectType().getClass());
			father = father.getFather();
		}

		if (path.getFirst().isAssignableFrom(KnowWEArticle.class)) {
			return path;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @return a List of ObjectTypes beginning at the given Section and ending
	 *         at this Section. Returns <tt>null</tt> if no path is found.
	 */
	public List<Class<? extends KnowWEObjectType>> getPathFromGivenSectionToThis(Section sec) {
		LinkedList<Class<? extends KnowWEObjectType>> path = new LinkedList<Class<? extends KnowWEObjectType>>();

		Section father = getFather();
		while (father != null && father != sec) {
			path.addFirst(father.getObjectType().getClass());
			father = father.getFather();
		}
		path.addFirst(father.getObjectType().getClass());
		
		if (path.getFirst().isAssignableFrom(sec.getClass())) {
			return path;
		} else {
			return null;
		}
	}

	public void collectTextsFromLeaves(StringBuilder buffi) {
		if (this.getChildren() != null && this.getChildren().size() > 0) {
			for (Section s : getChildren()) {
				s.collectTextsFromLeaves(buffi);
			}
		} else {
			buffi.append(this.originalText);
		}

	}

	public boolean hasQuickEditModeSet(String user) {
		if (UserSettingsManager.getInstance().hasQuickEditFlagSet(getId(),
				user, this.getTitle())) {
			return true;
		}
		if (father == null)
			return false;

		return father.hasQuickEditModeSet(user);
	}

	public boolean isEmpty() {
		String text = getOriginalText();
		text = text.replaceAll("\\s", "");
		return text.length() == 0;
	}

	public boolean isExpanded() {
		return isExpanded;
	}
	
	public boolean isReusedBy(String title) {
		Boolean reused = reusedBy.get(title);
		if (reused == null) {
			reused = false;
		}
		return reused;
	}
	
	public void setReusedBy(String title, boolean reused) {
		reusedBy.put(title, reused);
	}
	
	public void setReusedStateRecursively(String title, boolean reused) {
		setReusedBy(title, reused);
		if (!(objectType instanceof Include)) {
			for (Section child:getChildren()) {
				child.setReusedStateRecursively(title, reused);
			}
		}
	}
	
	public void setReusedSuccessorStateRecursively(boolean reused) {
		this.hasReusedSuccessor = reused;
		for (Section child:getChildren()) {
			child.setReusedSuccessorStateRecursively(reused);
		}
	}

	public boolean equalsOrIsChildrenOf(Section sec) {
		if (sec == this) {
			return true;
		} else {
			if (father == null) {
				return false;
			} else {
				return father.equalsOrIsChildrenOf(sec);
			}
		}
	}

	public void setFather(Section father) {
		this.father = father;
	}
	
	public boolean setType(T newType) {
		if(objectType.getClass() != (newType.getClass()) && objectType.getClass().isAssignableFrom(newType.getClass())) {
			this.objectType = newType;
			newType.reviseSubtree(getArticle(), this);
			return true;
		}
		return false;
	}
	
	private class TextOrderComparator implements Comparator<Section> {

		@Override
		public int compare(Section arg0, Section arg1) {
			if(arg0.getOffSetFromFatherText() > arg1.getOffSetFromFatherText()) return 1;
			if(arg0.getOffSetFromFatherText() < arg1.getOffSetFromFatherText()) return -1;
			return 0;
			
			
		}
		
	}
	
}
