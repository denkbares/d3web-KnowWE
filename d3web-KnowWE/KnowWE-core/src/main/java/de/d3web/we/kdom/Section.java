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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.report.Message;
import de.d3web.we.core.KnowWEDomParseReport;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.basic.EmbracedType;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.basic.VerbatimType;
import de.d3web.we.kdom.filter.SectionFilter;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.include.IncludeAddress;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.Priority;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.kdom.visitor.Visitable;
import de.d3web.we.kdom.visitor.Visitor;
import de.d3web.we.user.UserSettingsManager;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
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
public class Section<T extends KnowWEObjectType> implements Visitable, Comparable<Section<KnowWEObjectType>> {

//	private boolean reused = false;

	private final Map<String, Boolean> reusedBy = new HashMap<String, Boolean>();

	protected boolean hasReusedSuccessor = false;

	private PairOfInts startPosFromTmp;

	private IncludeAddress address;

	protected KnowWEArticle article;

	protected boolean isExpanded = false;

	private Map<SubtreeHandler<? extends KnowWEObjectType>, Boolean> reviseAgain 
			= new HashMap<SubtreeHandler<? extends KnowWEObjectType>, Boolean>();
	
	/**
	 * Specifies whether the orignialText was changed
	 * without changing the ancestor ones
	 */
	private boolean isDirty = false;

	/**
	 * The id of this node, unique in an article
	 */
	protected String id;

	/**
	 * This is the part of the ID, that was specifically given for
	 * the ID of this Section to be used instead of the name of the
	 * ObjectType.
	 */
	protected String specificID;

	/**
	 * Contains the text of this KDOM-node
	 */
	protected String originalText;

	/**
	 * The child-nodes of this KDOM-node. This forms the tree-structure of KDOM.
	 */
	protected List<Section<? extends KnowWEObjectType>> children = new ArrayList<Section<? extends KnowWEObjectType>>();

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

//	protected T t;

//	 public void add(T t) {
//	        this.t = t;
//	    }

	    public T get() {
	        return objectType;
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

	public static <T extends KnowWEObjectType>Section<T> createTypedSection(String text, T o, Section<? extends KnowWEObjectType> father, int beginIndexOfFather, KnowWEArticle article, SectionID id, boolean isExpanded, IncludeAddress adress, T type) {
        return new Section<T>(text, o, father, beginIndexOfFather, article, id, isExpanded,adress);
    }



	/**
	 * looks for the child at a specific offset.
	 *
	 * @param index
	 * @return
	 */
	public Section<? extends KnowWEObjectType> getChildSectionAtPosition(int index) {
		for (Section<?> child : this.children) {
			if (child.getOffSetFromFatherText() <= index
					&& index < child.getOffSetFromFatherText()
							+ child.getOriginalText().length()) {
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
	@SuppressWarnings("unchecked")
	private Section(String text, T objectType, Section<? extends KnowWEObjectType> father,
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
			if (objectType instanceof KnowWEArticle) {
				this.id = new SectionID(getTitle()).toString();
			} else {
				this.id = new SectionID(father, objectType).toString();
			}
		} else {
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
				&& !objectType.getClass().equals(Include.class)
				&& !objectType.getClass().equals(PlainText.class)
				&& !objectType.getClass().equals(VerbatimType.class)) {
			types.add(Include.getInstance());
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
			if (!objectType.getClass().equals(PlainText.class)) {
				types.add(PlainText.getInstance());
			}
		}

		/**
		 * searches for children types and splits recursively
		 */
		if (!(this instanceof UndefinedSection)
				&& !objectType.getClass().equals(PlainText.class)
				&& !objectType.getClass().equals(Include.class)
				&& !isExpanded) {
			Sectionizer.getInstance().splitToSections(originalText, types, this,
					article);
		}

//		childrenParsingOrder.addAll(children);

		/**
		 * sort children sections in text-order
		 */
		Collections.sort(children, new TextOrderComparator());

		if (objectType instanceof Include) {
			article.getIncludeSections().add((Section<Include>) this);
		}
	}

	protected Section(KnowWEArticle article) {
		this.article = article;
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
	 * Adds a child to this node. Use for KDOM creation and editing only!
	 *
	 */
	public void addChild(Section<?> s) {
		this.addChild(this.children.size(), s);
	}

	/**
	 * Adds a child to this node. Use for KDOM creation and editing only!
	 *
	 */
	public void addChild(int index, Section<?> s) {
		if (s.getOffSetFromFatherText() == -1) {
			// WEAK! TODO: Find other way..
			if (s.father != null) {
				s.offSetFromFatherText = s.father.getOriginalText().indexOf(
						s.getOriginalText());
			}
		}
		this.children.add(index, s);
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
		this.isDirty = true;
		// setting the isDirty flag to true for all ancestor sections
		// since they are also dirty (concatenation of leafs doesn't
		// represent original text of the section, offsets don't fit...)
		Section<? extends KnowWEObjectType> ancestor = this.getFather();
		while (ancestor != null) {
			ancestor.setDirty(true);
			ancestor = ancestor.getFather();
		}
	}

	/**
	 * Getter method for boolean variable isDirty.
	 * @return The section's actual value of isDirty
	 */
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * Setter method for boolean variable is Dirty.
	 * @param invalidated New value for section's variable isDirty
	 */
	public void setDirty(boolean invalidated) {
		this.isDirty = invalidated;
	}

	/**
	 * Searches the successor-node with nodeID in the successors of this node,
	 * sets the text of the successor-node and makes it a leaf by deleting all its children.
	 * This IS an article source edit operation!
	 * TODO: Important - propagate changes through the whole tree OR ReIinit
	 * tree!
	 * @author Franz Schwab
	 * @param nodeID
	 * @param replacingText
	 */
	public void setOriginalTextSetLeaf(String nodeID, String replacingText) {
		if (this.getId().equals(nodeID)) {
			this.setOriginalText(replacingText);
			this.removeAllChildren();
			return;
		}
		List<Section<?>> children = this.getChildren();
		if (children == null || children.isEmpty() || this.getObjectType().getClass() == Include.class) {
			return;
		}
		for (Section<?> section : children) {
			section.setOriginalTextSetLeaf(nodeID, replacingText);
		}
	}

	public void removeAllChildren() {
		this.children = new LinkedList<Section<? extends KnowWEObjectType>>();
		//this.childrenParsingOrder = new LinkedList<Section<? extends KnowWEObjectType>>();
	}

	/**
	 * @return the list of child nodes
	 */
	@SuppressWarnings("unchecked")
	public List<Section<? extends KnowWEObjectType>> getChildren() {
		if (objectType instanceof Include) {
			return KnowWEEnvironment.getInstance().getIncludeManager(getWeb())
					.getChildrenForSection((Section<Include>) this);
		}
		else {
			return children;
		}

	}

	public List<Section<? extends KnowWEObjectType>> getChildrenExceptExactType(Class<?>[] classes) {
		List<Class<?>> classesList = Arrays.asList(classes);
		List<Section<? extends KnowWEObjectType>> list = new LinkedList<Section<? extends KnowWEObjectType>>(this.children);
		Iterator<Section<? extends KnowWEObjectType>> i = list.iterator();
		while (i.hasNext()) {
			Section<? extends KnowWEObjectType> sec = i.next();
			if (classesList.contains(sec.get().getClass())) {
				i.remove();
			}
		}
		return list;
	}

	/**
	 * return the list of child nodes matching a filter
	 *
	 * @return
	 * @param filter
	 *            the filter to be matched
	 */
	public List<Section<? extends KnowWEObjectType>> getChildren(SectionFilter filter) {
		ArrayList<Section<? extends KnowWEObjectType>> list = new ArrayList<Section<? extends KnowWEObjectType>>();
		for (Section<? extends KnowWEObjectType> current : getChildren()) {
			if (filter.accept(current))
				list.add(current);
		}
		return list;
	}
	
	/**
	 * Returns all Nodes down to the given depth. Includes are not considered as
	 * adding to depth. Therefore, subtrees below an Include get collected one
	 * step deeper.
	 */
	public void getAllNodesPreOrderToDepth(List<Section<? extends KnowWEObjectType>> nodes, 
			int depth) {
		nodes.add(this);
		if (this.getChildren() != null && depth > 0) {
			for (Section<? extends KnowWEObjectType> child : this.getChildren()) {
				child.getAllNodesPreOrderToDepth(nodes, 
						child.getObjectType() instanceof Include ? depth : --depth);
			}
		}
	}

	public void getAllNodesPreOrder(List<Section<? extends KnowWEObjectType>> nodes) {
		nodes.add(this);
		if (this.getChildren() != null) {
			for (Section<? extends KnowWEObjectType> child : this.getChildren()) {
				child.getAllNodesPreOrder(nodes);
			}
		}
	}
	
	public void getAllNodesPostOrder(List<Section<? extends KnowWEObjectType>> nodes) {	
		if (this.getChildren() != null) {
			for (Section<? extends KnowWEObjectType> child : this.getChildren()) {
				child.getAllNodesPostOrder(nodes);
			}
		}
		nodes.add(this);
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
	@Deprecated
	public boolean hasRightSonOfType(Class<? extends KnowWEObjectType> class1, String text) {
		if(this.getObjectType() instanceof EmbracedType) {
			if(this.getFather().hasRightSonOfType(class1, text)) {
				return true;
			}
		}
		for (Section<? extends KnowWEObjectType> child : getChildren()) {
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
	@Deprecated
	public boolean hasLeftSonOfType(Class<? extends KnowWEObjectType> class1, String text) {
		if(this.getObjectType() instanceof EmbracedType) {
			if(this.getFather().hasLeftSonOfType(class1, text)) {
				return true;
			}
		}
		for (Section<? extends KnowWEObjectType> child : getChildren()) {
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
	 * <b>IMPORTANT:</b> This is NOT the actual ID, this may NOT be unique and
	 * this should ONLY be used in situations where a short version of the ID
	 * is needed e.g. to make it easier to read for humans in debugging, logging
	 * and similar stuff.
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
	public int compareTo(Section<KnowWEObjectType> o) {
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
	public Section<? extends KnowWEObjectType> getNode(String nodeID) {
		if (this.id.equals(nodeID))
			return this;
		for (Section<? extends KnowWEObjectType> child : getChildren()) {
			Section<? extends KnowWEObjectType> s = child.getNode(nodeID);
			if (s != null)
				return s;
		}
		return null;
	}

	public void removeChild(Section<? extends KnowWEObjectType> s) {
		this.children.remove(s);

	}

	public void removeChildren(List<Section<? extends KnowWEObjectType>> removeList) {
		for(Section<? extends KnowWEObjectType> s : removeList) {
			this.removeChild(s);
		}
	}

	/**
	 * Scanning subtree for Section with given id
	 *
	 * @param id2
	 * @return
	 */
	public Section<? extends KnowWEObjectType> findChild(String id2) {
		if (this.id.equals(id2))
			return this;
		for (Section<? extends KnowWEObjectType> child : getChildren()) {
			Section<? extends KnowWEObjectType> s = child.findChild(id2);
			if (s != null)
				return s;
		}
		return null;
	}

	public Section<? extends KnowWEObjectType> findSmallestNodeContaining(int start, int end) {
		Section<? extends KnowWEObjectType> s = null;
		int nodeStart = this.getAbsolutePositionStartInArticle();
		if (nodeStart <= start && nodeStart + originalText.length() >= end
				&& (!(this.getObjectType() instanceof PlainText))) {
			s = this;
			for (Section<? extends KnowWEObjectType> sec : getChildren()) {
				Section<? extends KnowWEObjectType> sub = sec.findSmallestNodeContaining(start, end);
				if (sub != null && (!(s.getObjectType() instanceof PlainText))) {
					s = sub;
				}
			}
		}
		return s;
	}

	public Section<?> findSmallestNodeContaining(String text) {
		Section<?> s = null;
		if (this.getOriginalText().contains(text)
				&& (!(this.getObjectType() instanceof PlainText))) {
			s = this;
			for (Section<?> sec : getChildren()) {
				Section<?> sub = sec.findSmallestNodeContaining(text);
				if (sub != null && (!(s.getObjectType() instanceof PlainText))) {
					s = sub;
				}
			}
		}
		return s;
	}

	/**
	 * Searches the ancestor for this section for a given class
	 * @param <OT>
	 * @param clazz
	 * @return
	 */
	public <OT extends KnowWEObjectType> Section<OT> findAncestor(Class<OT> clazz) {
		return KnowWEObjectTypeUtils.getAncestorOfType(this, clazz);
	}

	/**
	 * Searches the ancestor for this section for a given collection of classes
	 * @param <OT>
	 * @param clazz
	 * @return
	 *
	 */
	public Section<? extends KnowWEObjectType> findAncestor(Collection<Class<? extends KnowWEObjectType>> classes) {
		for (Class<? extends KnowWEObjectType> class1 : classes) {
			Section<? extends KnowWEObjectType> s = KnowWEObjectTypeUtils.getAncestorOfType(this, class1);
			if(s != null) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Searches the ancestor for this section for a given class.
	 * Note: Here, a section can't be its own ancestor.
	 * Furthermore, if an ancestor is just a subtype of the given class, it will be ignored.
	 * For other purposes, use the following method:
	 * @see #findAncestor(Class)
	 * @param <OT>
	 * @param clazz
	 * @return
	 * @author Franz Schwab
	 */
	public <OT extends KnowWEObjectType> Section<OT> findAncestorOfExactType(Class<OT> clazz) {
		LinkedList<Class<? extends KnowWEObjectType>> l = new LinkedList<Class<? extends KnowWEObjectType>>();
		l.add(clazz);
		@SuppressWarnings("unchecked")
		Section<OT> returnValue = (Section<OT>) findAncestorOfExactType(l);
		return returnValue;
	}

	/**
	 * Searches the ancestor for this section for a given collection of classes.
	 * The ancestor with the lowest distance to this section will be returned.
	 * @see #findAncestorOfExactType(Class)
	 * For other purposes, use the following method:
	 * @see #findAncestor(Collection)
	 * @param <OT>
	 * @param clazz
	 * @return
	 * @author Franz Schwab
	 */
	public Section<? extends KnowWEObjectType> findAncestorOfExactType(Collection<Class<? extends KnowWEObjectType>> classes) {
		Section<? extends KnowWEObjectType> f = this.getFather();
		while((f != null) && !(classes.contains(f.getObjectType().getClass()))) {
			f = f.getFather();
		}
		return  f;
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

	@Deprecated
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> Section<? extends OT> findChildOfType(OT t) {
		return (Section<? extends OT>) findChildOfType(t.getClass());
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
	@Deprecated
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> List<Section<OT>> findChildrenOfType(OT t) {
		List<Section<OT>> result = new ArrayList<Section<OT>>();
		for (Section<? extends KnowWEObjectType> s : this.getChildren())
			if (t.getClass().isAssignableFrom(s.getObjectType().getClass()))
				result.add((Section<OT>) s);
		return result;
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> Section<OT> findSuccessorForType(OT t) {
		Class<?> class1 = t.getClass();
		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			return (Section<OT>) this;
		}
		for (Section<? extends KnowWEObjectType> sec : getChildren()) {
			Section<OT> s = sec.findSuccessorForType(t);
			if (s != null)
				return s;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public<OT extends KnowWEObjectType> Section<OT> findSuccessor(Class<OT> class1) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			return (Section<OT>)this;
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
	@SuppressWarnings("unchecked")
	public<OT extends KnowWEObjectType> void findSuccessorsOfType(Class<OT> class1, List<Section<OT>> found) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			found.add((Section<OT>)this);
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfType(class1, found);
		}
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section.
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public void findSuccessorsOfTypeUntyped(Class class1, List<Section> found) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			found.add((Section) this);
		}
		for (Section<? extends KnowWEObjectType> sec : getChildren()) {
			sec.findSuccessorsOfTypeUntyped(class1, found);
		}
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> void findSuccessorsOfType(OT t, List<Section<OT>> found) {
		Class<?> class1 = t.getClass();
		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			found.add((Section<OT>) this);
		}
		for (Section<? extends KnowWEObjectType> sec : getChildren()) {
			sec.findSuccessorsOfType(t, found);
		}
	}


	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section and stores them in a Map, using their originalText as key.
	 */
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> void findSuccessorsOfTypeAsMap(Class<OT> class1, Map<String, Section<OT>> found) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			Section tmp = found.get(this.getOriginalText());
			// only replace the finding by this Section, if this Section is not reused
			// but the Section already in the map is reused
			if (tmp == null || (tmp.isReusedBy(getTitle()) && !this.isReusedBy(getTitle()))) {
				found.put((this).getOriginalText(), (Section<OT>)this);
			}
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfTypeAsMap(class1, found);
		}

	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section and stores them in a Map, using their originalText as key.
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> void findSuccessorsOfType(OT t, Map<String, Section<OT>> found) {

		if (t.getClass().isAssignableFrom(this.getObjectType().getClass())) {
			Section<? extends KnowWEObjectType> tmp = found.get(this.getOriginalText());
			// only replace the finding by this Section, if this Section is not reused
			// but the Section already in the map is reused
			if (tmp == null || (tmp.isReusedBy(getTitle()) && !this.isReusedBy(getTitle()))) {
				found.put(this.getOriginalText(), (Section<OT>)this);
			}
		}
		for (Section<? extends KnowWEObjectType> sec : getChildren()) {
			sec.findSuccessorsOfType(t, found);
		}

	}




	/**
	 * Finds all successors of type <code>class1</code> in the KDOM to the depth
	 * of <code>depth</code> below this Section.
	 */
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> void findSuccessorsOfType(Class<OT> class1, int depth,
			List<Section<OT>> found) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			found.add((Section<OT>)this);
		}
		if (depth == 0) {
			return;
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfType(class1, depth - 1, found);
		}

	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM to the depth
	 * of <code>depth</code> below this Section.
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public <OT extends KnowWEObjectType> void findSuccessorsOfType(OT e, int depth,
			List<Section<OT>> found) {

		if (e.getClass().isAssignableFrom(this.getObjectType().getClass())) {
			found.add((Section<OT>)this);
		}
		if (depth == 0) {
			return;
		}
		for (Section<? extends KnowWEObjectType> sec : getChildren()) {
			sec.findSuccessorsOfType(e, --depth, found);
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
			Map<String, Section<? extends KnowWEObjectType>> found) {

		if (index < path.size() - 1 && path.get(index).isAssignableFrom(this.getObjectType().getClass())) {
			for (Section<? extends KnowWEObjectType> sec : getChildren()) {
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
			List<Section<? extends KnowWEObjectType>> found) {

		if (index < path.size() - 1 && path.get(index).isAssignableFrom(this.getObjectType().getClass())) {
			for (Section<? extends KnowWEObjectType> sec : getChildren()) {
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
		Section<? extends KnowWEObjectType> father = getFather();
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
	public List<Class<? extends KnowWEObjectType>> getPathFromGivenSectionToThis(Section<? extends KnowWEObjectType> sec) {
		LinkedList<Class<? extends KnowWEObjectType>> path = new LinkedList<Class<? extends KnowWEObjectType>>();

		Section<? extends KnowWEObjectType> father = getFather();
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
		collectTextsFromLeaves(buffi, true);
	}

	/**
	 * @param buffi
	 * @param followIncludes if false, the text from includes will not be included. this is necessary if you want just
	 * the text of a wikipage having generated.
	 */
	public void collectTextsFromLeaves(StringBuilder buffi, boolean followIncludes) {
		if (this.getChildren() != null && this.getChildren().size() > 0
				&& (followIncludes || !(this.getObjectType().getClass() == Include.class))) {
			for (Section<?> s : this.getChildren()) {
				if(s != null) {
					s.collectTextsFromLeaves(buffi, followIncludes);
				}
			}
		} else {
			if (this.getObjectType().getClass() == Include.class) {
				// System.out.println( "include tag complete text: " + section.getOriginalText());
			}
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
			for (Section<? extends KnowWEObjectType> child:getChildren()) {
				child.setReusedStateRecursively(title, reused);
			}
		}
	}

	public void setReusedSuccessorStateRecursively(boolean reused) {
		this.hasReusedSuccessor = reused;
		for (Section<? extends KnowWEObjectType> child:getChildren()) {
			child.setReusedSuccessorStateRecursively(reused);
		}
	}

	public boolean equalsOrIsChildrenOf(Section<? extends KnowWEObjectType> sec) {
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

	public void setFather(Section<? extends KnowWEObjectType> father) {
		this.father = father;
	}

	/**
	 * Method that looks (recursively down) for this section whether some error
	 * has been stored in that subtree
	 *
	 * @return
	 */
	public boolean hasErrorInSubtree() {
		Collection<KDOMError> s = KDOMReportMessage.getErrors(article, this);
		if (s != null && s.size() > 0) return true;
		for (Section<?> child : children) {
			boolean err = child.hasErrorInSubtree();
			if (err) return true;
		}

		return false;
	}
	
	public boolean isReviseAgain(SubtreeHandler<? extends KnowWEObjectType> sub) {
		Boolean revised = this.reviseAgain.get(sub);
		if (revised == null) {
			revised = false;
		}
		return revised;
	}

	public void setReviseAgain(SubtreeHandler<? extends KnowWEObjectType> sub, boolean notYetRevised) {
		this.reviseAgain.put(sub, notYetRevised);
	}
	
	protected void setReviseAgain(boolean notYetRevised) {
		List<SubtreeHandler<? extends KnowWEObjectType>> handlers 
				= new ArrayList<SubtreeHandler<? extends KnowWEObjectType>>();
		for (List<SubtreeHandler<? extends KnowWEObjectType>> list:objectType.getSubtreeHandlers().values()) {
			handlers.addAll(list);
		}
		for (SubtreeHandler<? extends KnowWEObjectType> sth:handlers) {
			setReviseAgain(sth, notYetRevised);
		}
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.d3web.we.kdom.KnowWEObjectType#reviseSubtree(de.d3web.we.kdom.KnowWErticle
	 * , de.d3web.we.kdom.Section)
	 */
	// Templates aren't working for us here, since getSubtreeHandlers() can not have
	// any knowledge of the template T specified by the Section -> SuppressWarning...
	@SuppressWarnings("unchecked")
	public final void runSubtreeHandlers(KnowWEArticle article, Priority p) {
		List<SubtreeHandler<? extends KnowWEObjectType>> handlerList 
				= objectType.getSubtreeHandlers().get(p);
		if (handlerList != null) {
			for (SubtreeHandler handler : objectType.getSubtreeHandlers().get(p)) {
					runSubtreeHandler(article, handler);
			}
		}

	}
	
	@SuppressWarnings("unchecked")
	public final void runSubtreeHandler(KnowWEArticle article, SubtreeHandler handler) {
		try {
			KDOMReportMessage.storeMessages(article, this, handler.getClass(), handler.reviseSubtree(article, this));
		}
		catch (Throwable e) {
			String text = "unexpected internal error in subtree handler '" + handler + "'";
			Message msg = new Message(text + ": " + e);
			AbstractKnowWEObjectType.storeMessages(article, this, this.getClass(), Arrays.asList(msg));
			// TODO: vb: store the error also in the article. (see below for
			// more details)
			//
			// Idea 1:
			// Any unexpected error (and therefore catched here) of the
			// ReviseSubtreeHandlers
			// shall be remarked at the "article" object. When rendering the
			// article page,
			// the error should be placed at the top level. A KnowWEPlugin
			// to list all
			// erroneous articles as links with its errors shall be
			// introduced. A call to this
			// plugin should be placed in the LeftMenu-page.
			//
			// Idea 2:
			// The errors are not marked at the article, but as a special
			// message
			// type is added to this section. When rendering the page, the
			// error
			// message should be placed right before the section and the
			// content
			// of the section as original text in pre-formatted style (no
			// renderer
			// used!):
			// {{{ <div class=error>EXCEPTION WITH MESSAGE</div> ORIGINAL
			// TEXT }}}
		}
		// This flag is set for the case that another article than the article, 
		// this section is directly hooked in revises this Section
		this.setReusedBy(article.getTitle(), true);
	}
	
	@SuppressWarnings("unchecked")
	public boolean setType(KnowWEObjectType newType) {
		if(objectType.getClass() != (newType.getClass()) && objectType.getClass().isAssignableFrom(newType.getClass())) {
			this.objectType = (T) newType;
			this.reviseAgain = new HashMap<SubtreeHandler<? extends KnowWEObjectType>, Boolean>();
			for (Priority p:objectType.getSubtreeHandlers().descendingKeySet()) {
				runSubtreeHandlers(getArticle(), p);
			}
			
			return true;
		}
		return false;
	}

	private class TextOrderComparator implements Comparator<Section<? extends KnowWEObjectType>> {

		@Override
		public int compare(Section<? extends KnowWEObjectType> arg0, Section<? extends KnowWEObjectType> arg1) {
			if(arg0.getOffSetFromFatherText() > arg1.getOffSetFromFatherText()) return 1;
			if(arg0.getOffSetFromFatherText() < arg1.getOffSetFromFatherText()) return -1;
			return 0;


		}

	}
}
