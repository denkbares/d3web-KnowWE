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

package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.filter.SectionFilter;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.kdom.visitor.Visitable;
import de.d3web.we.kdom.visitor.Visitor;
import de.d3web.we.logging.Logging;
import de.d3web.we.user.UserSettingsManager;

/**
 * @author Jochen
 * 
 *         This class represents a node in the Knowledge-DOM of KnowWE.
 *         Basically it has some text, one type and a list of children.
 * 
 *         Further, it has a reference to its father and a positionOffset to its
 *         fathers text.
 * 
 *         Further information can be attached to a node (TypeInformation), to
 *         connect the text-parts with external resources, e.g. knowledge bases,
 *         OWL, User-feedback-DBs etc.
 * 
 */
public class Section<T extends Type> implements Visitable, Comparable<Section<? extends Type>> {

	private HashSet<String> reusedBy = null;

	@SuppressWarnings("rawtypes")
	private HashMap<String, HashSet<Class<? extends SubtreeHandler>>> compiledBy = null;

	private List<Integer> position = null;

	private List<Integer> lastPositions = null;

	protected boolean isOrHasReusedSuccessor = false;

	protected KnowWEArticle article;

	private HashSet<String> packageNames = null;

	protected boolean isExpanded = false;

	/**
	 * Specifies whether the orignialText was changed without changing the
	 * ancestor ones
	 */
	private boolean isDirty = false;

	/**
	 * The id of this node, unique in an article
	 */
	private String id;

	/**
	 * If the ID gets changed, e.g. by the update mechanism, the last version of
	 * the ID gets stored here.
	 */
	private String lastID;

	/**
	 * Contains the text of this KDOM-node
	 */
	protected String text;

	/**
	 * Specifies whether the children of this Section were set through the
	 * setChildren(List<Section> children) methods.
	 */
	protected boolean sharedChildren;

	/**
	 * The child-nodes of this KDOM-node. This forms the tree-structure of KDOM.
	 */
	protected List<Section<? extends Type>> children = new ArrayList<Section<? extends Type>>(5);
	/**
	 * The father section of this KDOM-node. Used for upwards navigation through
	 * the tree
	 */
	protected Section<? extends Type> father;

	/**
	 * the position when the text off this node starts related to the text of
	 * the father node. Thus: for first child always 0, for 2nd
	 * firstChild.length() etc.
	 */
	private int offSetFromFatherText = -1;

	/**
	 * Type of this node.
	 * 
	 * @see Type Each type has its own parser and renderer
	 */
	protected T type;

	public T get() {
		return type;
	}

	public static <T extends Type> Section<T> createSection(String text, T o, Section<? extends Type> father) {
		return new Section<T>(text, o, father);
	}

	/**
	 * 
	 * Constructor of a node
	 * <p/>
	 * Important: parses itself recursively by getting the allowed childrenTypes
	 * of the local type
	 * 
	 * @param text the part of (article-source) text of the node
	 * @param objectType type of the node
	 * @param father
	 * @param beginIndexFather
	 * @param article is the article this section is hooked in
	 */
	private Section(String text, T objectType, Section<?> father) {
		this.father = father;
		if (father != null) {
			this.father.addChild(this);
			this.article = father.getArticle();
		}
		this.text = text;
		this.type = objectType;
	}

	protected Section(KnowWEArticle article) {
		this.article = article;
	}

	/*
	 * verbalizes this node
	 */
	@Override
	@SuppressWarnings({
			"unchecked", "rawtypes" })
	public String toString() {
		return (type != null ? this.get().getClass().getSimpleName() + ": " : "")
				+ "'" + (type != null && type instanceof KnowWETerm<?>
						? ((KnowWETerm) type).getTermIdentifier(this)
						: this.getText()) + "'";
	}

	/**
	 * If the compared Sections are from different articles, a value less than 0
	 * will be returned, if the title of this Section is lexicographically less
	 * than the title of the arguments Section, greater than 0 if the title of
	 * arguments Section is lexicographically greater.<br/>
	 * If the Sections are from the same article, a value less than 0 will be
	 * returned, if the Section is textually located above the argument Section,
	 * a value greater than 0, if below.<br/>
	 * If a Sections is compared with itself, 0 will be returned.
	 */
	@Override
	public int compareTo(Section<? extends Type> o) {
		if (this == o) return 0;
		int comp = getTitle().compareTo(o.getTitle());
		if (comp == 0) {
			List<Integer> thisPos = getPositionInKDOM();
			List<Integer> otherPos = o.getPositionInKDOM();
			ListIterator<Integer> thisIter = thisPos.listIterator(thisPos.size());
			ListIterator<Integer> otherIter = otherPos.listIterator(otherPos.size());

			while (comp == 0 && thisIter.hasPrevious() && otherIter.hasPrevious()) {
				comp = thisIter.previous().compareTo(otherIter.previous());
			}
			if (comp == 0) {
				comp = otherPos.size() - thisPos.size();
			}
		}
		return comp;
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

	@SuppressWarnings("unchecked")
	public Section<InjectType> injectChildren(InjectType injectType) {

		Section<InjectType> injectSection = Section.createSection(
				text, injectType, this);

		if (!this.get().getAllowedChildrenTypes().isEmpty()
				|| this.getChildren().size() > 1
				|| (this.getChildren().size() == 1 && !(this.getChildren().get(0).get() instanceof InjectType))) {
			KDOMReportMessage.storeSingleError(null, this, this.getClass(),
					new SimpleMessageError(
							"Internal error: Tried to inject sections in non-leaf section."));
			return null;
		}

		if (this.getChildren().size() == 1 && !this.getChildren().get(0).equalsAsKDOMSubtree(
						injectSection)) {
			KDOMReportMessage.storeSingleError(null, this, this.getClass(),
					new SimpleMessageError(
							"Internal error: Multiple diverging section injections."));
			return (Section<InjectType>) this.getChildren().get(0);
		}

		KDOMReportMessage.clearMessages(null, this, this.getClass());
		this.addChild(injectSection);

		return injectSection;
	}

	public boolean removeInjectedChildren() {
		if (this.getChildren().size() == 1 && this.getChildren().get(0).get() instanceof InjectType) {
			Section<?> injectSection = this.getChildren().remove(0);
			KnowWEEnvironment.getInstance().getArticleManager(getWeb()).addAllArticlesToUpdate(
					injectSection.getReusedBySet());
			injectSection.clearReusedBySet();
			return true;
		}
		return false;
	}

	/**
	 * Compares the KDOM subtree of this Section with the KDOM subtree of given
	 * Section. Types, originalText and structure of the tree are checked.
	 * 
	 * @created 04.02.2011
	 * @param sec is the Section to compare with this Section
	 * @return true, if both KDOM subtrees equal in terms of type, originalText
	 *         and structure. False else.
	 */
	public boolean equalsAsKDOMSubtree(Section<?> sec) {
		if (this.type.equals(sec.type)
					&& this.text.equals(sec.text)
					&& this.getChildren().size() == sec.getChildren().size()) {
			Iterator<Section<?>> thisIter = this.getChildren().iterator();
			Iterator<Section<?>> secIter = sec.getChildren().iterator();
			while (thisIter.hasNext()) {
				if (!thisIter.next().equalsAsKDOMSubtree(secIter.next())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean equalsOrIsSuccessorOf(Section<?> sec) {
		if (sec == this) {
			return true;
		}
		if (article != sec.article || father == null) {
			return false;
		}
		return father.equalsOrIsSuccessorOf(sec);
	}

	public boolean equalsOrIsAncestorOf(Section<?> sec) {
		return sec.equalsOrIsSuccessorOf(this);
	}

	/**
	 * Adds a child to this node. Use for KDOM creation and editing only!
	 */
	public void addChild(Section<?> s) {
		this.addChild(this.children.size(), s);
	}

	/**
	 * Adds a child to this node. Use for KDOM creation and editing only!
	 */
	public void addChild(int index, Section<?> s) {
		this.children.add(index, s);
	}

	/**
	 * @return the text of this Section/Node
	 */
	public String getOriginalText() {
		return text;
	}

	public String getText() {
		return text;
	}

	// public IncludeAddress getIncludeAddress() {
	// return this.address;
	// }

	/**
	 * Sets the text of this node. This IS an article source edit operation!
	 * TODO: Important - propagate changes through the whole tree OR ReIinit
	 * tree!
	 * 
	 * @param text
	 */
	public void setOriginalText(String newText) {
		this.text = newText;
		this.isDirty = true;
		// setting the isDirty flag to true for all ancestor sections
		// since they are also dirty (concatenation of leafs doesn't
		// represent original text of the section, offsets don't fit...)
		Section<? extends Type> ancestor = this.getFather();
		while (ancestor != null) {
			ancestor.setDirty(true);
			ancestor = ancestor.getFather();
		}
	}

	/**
	 * Getter method for boolean variable isDirty.
	 * 
	 * @return The section's actual value of isDirty
	 */
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * Setter method for boolean variable is Dirty.
	 * 
	 * @param invalidated New value for section's variable isDirty
	 */
	public void setDirty(boolean invalidated) {
		this.isDirty = invalidated;
	}

	/**
	 * Searches the successor-node with nodeID in the successors of this node,
	 * sets the text of the successor-node and makes it a leaf by deleting all
	 * its children. This IS an article source edit operation! TODO: Important -
	 * propagate changes through the whole tree OR ReIinit tree!
	 * 
	 * @author Franz Schwab
	 * @param nodeID
	 * @param replacingText
	 */
	public void setOriginalTextSetLeaf(String nodeID, String replacingText) {
		if (this.getID().equals(nodeID)) {
			this.setOriginalText(replacingText);
			this.removeAllChildren();
			return;
		}
		List<Section<?>> children = this.getChildren();
		if (children.isEmpty() || sharedChildren) {
			return;
		}
		for (Section<?> section : children) {
			section.setOriginalTextSetLeaf(nodeID, replacingText);
		}
	}

	// /**
	// * looks for the child at a specific offset.
	// *
	// * @param index
	// * @return
	// */
	// public Section<? extends Type> getChildSectionAtPosition(int index) {
	// for (Section<?> child : getChildren()) {
	// if (child.getOffSetFromFatherText() <= index
	// && index < child.getOffSetFromFatherText()
	// + child.getOriginalText().length()) {
	// return child;
	// }
	// }
	// return null;
	// }

	protected int absolutePositionStartInArticle = -1;

	public int getAbsolutePositionStartInArticle() {
		if (absolutePositionStartInArticle == -1) {
			calcAbsolutePositionStart();
		}
		return absolutePositionStartInArticle;
	}

	private void calcAbsolutePositionStart() {
		absolutePositionStartInArticle = getOffSetFromFatherText()
				+ father.getAbsolutePositionStartInArticle();

	}

	public void removeAllChildren() {
		this.children.clear();
	}

	/**
	 * Use for KDOM creation and editing only!
	 * 
	 * @created 26.08.2010
	 * @param children
	 */
	public void setChildren(List<Section<? extends Type>> children) {
		if (children == null) {
			throw new NullPointerException("Children of a Section cannot be null.");
		}
		for (Section<?> child : children) {
			child.setFather(this);
		}
		this.children = children;
	}

	// /**
	// * Use for KDOM creation and editing only!
	// *
	// * @created 26.08.2010
	// * @param children
	// */
	// public void setLastChildren(List<Section<? extends Type>>
	// children) {
	// this.possiblySharedChildren = true;
	// this.lastChildren = children;
	// }

	/**
	 * @return the list of child nodes
	 */
	public List<Section<? extends Type>> getChildren() {
		// if (objectType instanceof Include) {
		// return KnowWEEnvironment.getInstance().getIncludeManager(getWeb())
		// .getChildrenForSection((Section<Include>) this);
		// }
		// else
		return Collections.unmodifiableList(children);

	}

	/**
	 * return the list of child nodes matching a filter
	 * 
	 * @return
	 * @param filter the filter to be matched
	 */
	public List<Section<? extends Type>> getChildren(SectionFilter filter) {
		ArrayList<Section<? extends Type>> list = new ArrayList<Section<? extends Type>>();
		for (Section<? extends Type> current : getChildren()) {
			if (filter.accept(current)) list.add(current);
		}
		return list;
	}

	/**
	 * returns father node
	 * 
	 * @return
	 */
	public Section<? extends Type> getFather() {
		return father;
	}

	/**
	 * returns offSet relatively to father text
	 * 
	 * @return
	 */
	public int getOffSetFromFatherText() {
		if (offSetFromFatherText == -1) {
			int temp = 0;
			for (Section<?> child : father.getChildren()) {
				if (child == this) break;
				temp += child.getOriginalText().length();
			}
			offSetFromFatherText = temp;
		}
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

	public boolean addPackageName(String packageName) {
		if (getPackageNames().contains(packageName)) {
			return false;
		}
		else {
			if (packageNames == null) {
				packageNames = new HashSet<String>(4);
			}
			packageNames.add(packageName);
			return true;
		}
	}

	public boolean removePackageName(String packageName) {
		boolean removed = packageNames == null ? false : packageNames.remove(packageName);
		if (packageNames != null && packageNames.isEmpty()) packageNames = null;
		return removed;
	}

	public Set<String> getPackageNames() {
		if (packageNames == null) {
			Set<String> fatherSet;
			if (father == null) {
				fatherSet = new HashSet<String>(0);
			}
			else {
				fatherSet = father.getPackageNames();
			}
			return Collections.unmodifiableSet(fatherSet);
		}
		else {
			Set<String> tempNamespaces = new HashSet<String>(4);
			if (father != null) tempNamespaces.addAll(father.getPackageNames());
			tempNamespaces.addAll(packageNames);
			return Collections.unmodifiableSet(tempNamespaces);
		}
	}

	public String getWeb() {
		return this.article.getWeb();
	}

	public KnowWEArticle getArticle() {
		return this.article;
	}

	/**
	 * @return the depth of this Section inside the KDOM
	 */
	public int getDepth() {
		if (get() instanceof KnowWEArticle) {
			return 0;
		}
		else {
			return father.getDepth() + 1;
		}
	}

	/**
	 * Verbalizes this node
	 * 
	 * @return
	 */
	public String verbalize() {
		StringBuffer buffi = new StringBuffer();
		String simpleName = this.get().getClass().getSimpleName();
		if (simpleName.contains("nonymous")) {
			simpleName = simpleName += "(" + this.get().getName() + ")";
		}
		buffi.append(simpleName);
		buffi.append(", ID: " + getShortId());
		buffi.append(", length: " + this.getOriginalText().length() + " ("
				+ offSetFromFatherText + ")" + ", children: " + getChildren().size());
		String ot = this.getOriginalText().length() < 50 ? text : text.substring(0,
				50) + "...";
		ot = ot.replaceAll("\\n", "\\\\n");
		buffi.append(", \"" + ot);
		buffi.append("\"");
		return buffi.toString();
	}

	protected void setID(String id) {
		this.lastID = this.id;
		this.id = id;
	}

	public String getID() {
		if (id == null) {
			if (type instanceof KnowWEArticle) {
				this.id = new SectionID(getTitle()).toString();
			}
			else {
				this.id = new SectionID(father, type).toString();
			}
		}
		return id;
	}

	public String getLastID() {
		return lastID == null ? getID() : lastID;
	}

	/**
	 * <b>IMPORTANT:</b> This is NOT the actual ID, this may NOT be unique and
	 * this should ONLY be used in situations where a short version of the ID is
	 * needed e.g. to make it easier to read for humans in debugging, logging
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

	public void removeChild(Section<? extends Type> s) {
		this.children.remove(s);

	}

	public void removeChildren(List<Section<? extends Type>> removeList) {
		for (Section<? extends Type> s : removeList) {
			this.removeChild(s);
		}
	}

	/**
	 * 
	 * @return a List of ObjectTypes beginning at the KnowWWEArticle and ending
	 *         at this Section. Returns <tt>null</tt> if no path is found.
	 */
	public List<Class<? extends Type>> getPathFromArticleToThis() {
		LinkedList<Class<? extends Type>> path = new LinkedList<Class<? extends Type>>();

		path.add(get().getClass());
		Section<? extends Type> father = getFather();
		while (father != null) {
			path.addFirst(father.get().getClass());
			father = father.getFather();
		}

		if (path.getFirst().isAssignableFrom(KnowWEArticle.class)) {
			return path;
		}
		else {
			return null;
		}
	}

	/**
	 * 
	 * @return a List of ObjectTypes beginning at the given Section and ending
	 *         at this Section. Returns <tt>null</tt> if no path is found.
	 */
	public List<Class<? extends Type>> getPathFromGivenSectionToThis(Section<? extends Type> sec) {
		LinkedList<Class<? extends Type>> path = new LinkedList<Class<? extends Type>>();

		Section<? extends Type> father = getFather();
		while (father != null && father != sec) {
			path.addFirst(father.get().getClass());
			father = father.getFather();
		}
		path.addFirst(father.get().getClass());

		if (path.getFirst().isAssignableFrom(sec.getClass())) {
			return path;
		}
		else {
			return null;
		}
	}

	public void collectTextsFromLeaves(StringBuilder buffi) {
		collectTextsFromLeaves(buffi, true);
	}

	/**
	 * @param buffi
	 * @param followSharedChildren if false, the text from for example includes
	 *        will not be included. this is necessary if you want just the text
	 *        of a wikipage having generated.
	 */
	public void collectTextsFromLeaves(StringBuilder buffi, boolean followSharedChildren) {
		if (!this.getChildren().isEmpty()
				&& (followSharedChildren ? true : sharedChildren)) {
			for (Section<?> s : this.getChildren()) {
				if (s != null) {
					s.collectTextsFromLeaves(buffi, followSharedChildren);
				}
			}
		}
		else {
			buffi.append(this.text);
		}
	}

	public void setFather(Section<? extends Type> father) {
		this.father = father;
	}

	/**
	 * Method that looks (recursively down) for this section whether some errors
	 * has been stored in that subtree for the given article.
	 * 
	 * @return
	 */
	public boolean hasErrorInSubtree(KnowWEArticle article) {
		Collection<KDOMError> s = KDOMReportMessage.getErrors(article, this);
		if (s != null && s.size() > 0) {
			return true;
		}
		for (Section<?> child : children) {
			boolean err = child.hasErrorInSubtree(article);
			if (err) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return whether this Section has children that were set with the
	 * setChildren(List<Section> children) methods, for example by the
	 * IncludeManager.
	 * 
	 * @created 26.08.2010
	 * @return
	 */
	public boolean hasSharedChildren() {
		return this.sharedChildren;
	}

	public void setHasSharedChildren(boolean shared) {
		this.sharedChildren = shared;
	}

	public boolean hasQuickEditModeSet(String user) {
		if (UserSettingsManager.getInstance().hasQuickEditFlagSet(getID(),
				user, this.getTitle())) {
			return true;
		}
		if (father == null) return false;

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
		return reusedBy == null ? false : reusedBy.contains(title);
	}

	public Set<String> getReusedBySet() {
		return reusedBy == null
				? Collections.unmodifiableSet(new HashSet<String>(0))
				: Collections.unmodifiableSet(reusedBy);
	}

	public boolean isOrHasSuccessorReusedBy(String title) {
		if (isReusedBy(title)) {
			return true;
		}
		else {
			for (Section<?> child : this.getChildren()) {
				if (child.isOrHasSuccessorReusedBy(title)) return true;
			}
			return false;
		}
	}

	public boolean isOrHasSuccessorNotReusedBy(String title) {
		if (!isReusedBy(title)) {
			return true;
		}
		else {
			for (Section<?> child : this.getChildren()) {
				if (child.isOrHasSuccessorNotReusedBy(title)) return true;
			}
			return false;
		}
	}

	/**
	 * Checks whether this Section or a successor is not reused. Sections and
	 * successors with a Type contained in the Set of classes will be ignored.
	 * 
	 * @created 10.07.2010
	 * @param title is the article, for which to check.
	 * @param filteredTypes if this Section has one of the filtered types, false
	 *        is returned.
	 * @return a boolean with the result of the check
	 */
	public boolean isOrHasChangedSuccessor(String title, Collection<Class<? extends Type>> filteredTypes) {
		if (isChanged(title, filteredTypes)) {
			return true;
		}
		else {
			for (Section<?> child : this.getChildren()) {
				if (child.isOrHasChangedSuccessor(title, filteredTypes)) return true;
			}
			return false;
		}
	}

	/**
	 * Checks, whether this Section has changed since the last version of the
	 * article.
	 * 
	 * @created 08.12.2010
	 * @param title is the article, for which to check.
	 * @param filteredTypes if this Section has one of the filtered types, false
	 *        is returned.
	 * @return a boolean with the result of the check
	 */
	public boolean isChanged(String title, Collection<Class<? extends Type>> filteredTypes) {
		if (filteredTypes != null) {
			for (Class<?> c : filteredTypes) {
				if (c.isAssignableFrom(type.getClass())) {
					return false;
				}
			}
		}
		if (!isReusedBy(title) || (type.isOrderSensitive() && isPositionChangedFor(title))) {
			return true;
		}
		return false;
	}

	public void setReusedBy(String title, boolean reused) {
		if (reused) {
			if (reusedBy == null) reusedBy = new HashSet<String>(4);
			reusedBy.add(title);
		}
		else {
			if (reusedBy != null) reusedBy.remove(title);
		}
	}

	/**
	 * Affects all Sections this Section is connected to (also included
	 * Sections).
	 */
	public void setReusedByRecursively(String title, boolean reused) {
		setReusedBy(title, reused);
		for (Section<? extends Type> child : getChildren()) {
			child.setReusedByRecursively(title, reused);
		}
	}

	/**
	 * Sets all compiled states to false. This method does not affect includes.
	 */
	// public void clearCompiledRecursively() {
	// this.compiledBy = null;
	// for (Section<? extends Type> child : getChildren()) {
	// if (child.getTitle().equals(getTitle())) {
	// child.clearCompiledRecursively();
	// }
	// }
	// }

	public void clearReusedBySet() {
		this.reusedBy = null;
	}

	public void clearReusedBySetRecursively() {
		clearReusedBySet();
		for (Section<?> child : getChildren()) {
			child.clearReusedBySetRecursively();
		}
	}

	/**
	 * This method has the purpose to clear the reused states of all Sections
	 * that could not be reused by the incremental update. Call this method on
	 * the root Section of the old article with the new article as the argument.
	 * 
	 * @created 13.09.2010
	 * @param article
	 */
	public void clearReusedOfOldSectionsRecursively(KnowWEArticle
			article) {
		// skip included Sections
		if (article.getTitle().equals(getTitle())) {
			// only old Sections can have old child Sections
			if (this.article != article) {
				for (Section<?> child : children) {
					child.clearReusedOfOldSectionsRecursively(article);
				}
				reusedBy = null;
			}
		}
	}

	/**
	 * Set reusedSuccessor state to false... only used for incremental KDOM
	 * update.
	 */
	public void clearReusedSuccessorRecursively() {
		this.isOrHasReusedSuccessor = false;
		for (Section<? extends Type> child : getChildren()) {
			if (child.getTitle().equals(getTitle())) {
				child.clearReusedSuccessorRecursively();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public boolean isCompiledBy(String title, SubtreeHandler handler) {
		if (compiledBy == null) return false;
		HashSet<Class<? extends SubtreeHandler>> compiledByHandlerSet =
				compiledBy.get(title);
		if (compiledByHandlerSet == null) return false;
		return compiledByHandlerSet.contains(handler.getClass());
	}

	public boolean isCompiledBy(String title) {
		if (compiledBy == null) return false;
		return compiledBy.containsKey(title);
	}

	@SuppressWarnings("rawtypes")
	public Map<String, HashSet<Class<? extends SubtreeHandler>>> getCompiledByMap() {
		return compiledBy == null
				? Collections.unmodifiableMap(
						new HashMap<String, HashSet<Class<? extends SubtreeHandler>>>(
								0))
				: Collections.unmodifiableMap(compiledBy);
	}

	@SuppressWarnings("rawtypes")
	public void setCompiledBy(String title, SubtreeHandler handler, boolean compiled) {
		if (compiled) {
			if (compiledBy == null) {
				compiledBy = new HashMap<String, HashSet<Class<? extends
						SubtreeHandler>>>(4);
			}
			HashSet<Class<? extends SubtreeHandler>> reusedByArticleSet =
					compiledBy.get(title);
			if (reusedByArticleSet == null) {
				reusedByArticleSet = new HashSet<Class<? extends SubtreeHandler>>(4);
				compiledBy.put(title, reusedByArticleSet);
			}
			reusedByArticleSet.add(handler.getClass());
		}
		else {
			if (compiledBy != null) {
				HashSet<Class<? extends SubtreeHandler>> compiledByHandlerSet =
						compiledBy.get(title);
				if (compiledByHandlerSet != null) {
					compiledByHandlerSet.remove(handler.getClass());
					if (compiledByHandlerSet.isEmpty()) compiledBy.remove(title);
				}
			}
		}
	}

	public void setNotCompiledBy(String title) {
		if (compiledBy != null) compiledBy.remove(title);
	}

	/**
	 * Affects all Sections this Section is connected to (also included
	 * Sections).
	 */
	public void setNotCompiledByRecursively(String title) {
		setNotCompiledBy(title);
		for (Section<? extends Type> child : getChildren()) {
			child.setNotCompiledByRecursively(title);
		}
	}

	public boolean isPositionChangedFor(String title) {
		if (lastPositions == null) return false;
		return !lastPositions.equals(getPositionInKDOM());
	}

	protected void clearPositionInKDOM() {
		position = null;
	}

	public List<Integer> getPositionInKDOM() {
		if (position == null) {
			position = calcPositionInKDOM();
		}
		return Collections.unmodifiableList(position);
	}

	protected void setLastPositionInKDOM(List<Integer> lastPositions) {
		this.lastPositions = lastPositions;
	}

	public List<Integer> getLastPositionInKDOM() {
		return lastPositions == null ? null : Collections.unmodifiableList(lastPositions);
	}

	public List<Integer> calcPositionTil(Section<?> end) {
		List<Integer> positions = new ArrayList<Integer>();
		Section<?> temp = this;
		Section<?> tempFather = temp.getFather();
		while (temp != end && tempFather != null) {
			positions.add(tempFather.getChildren().indexOf(temp));
			temp = tempFather;
			tempFather = temp.getFather();
		}
		return positions;
	}

	public List<Integer> calcPositionInKDOM() {
		return calcPositionTil(getArticle().getSection());
	}

	private boolean isMatchingPackageName(KnowWEArticle article, SubtreeHandler<?> h) {

		// ignore: compile always but only for the article of this section
		if (h.isIgnoringPackageCompile() || type.isIgnoringPackageCompile()) {
			return article.getTitle().equals(getTitle());
		}
		// auto: compile always
		else if (KnowWEPackageManager.isAutocompileArticleEnabled()) {
			return true;
		}
		else {
			Set<String> referencedPackages = KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb()).getReferencedPackages(article.getTitle());

			if (referencedPackages.contains(article.getTitle())
					|| referencedPackages.contains(KnowWEPackageManager.THIS)) return true;

			for (String name : getPackageNames()) {
				if (referencedPackages.contains(name)) return true;
			}
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Templates aren't working for us here, since getSubtreeHandlers() can not
	 * have any knowledge of the template T specified by the Section =>
	 * SuppressWarning...
	 * 
	 * @see SubtreeHandler#create(KnowWEArticle, Section)
	 */
	public final void letSubtreeHandlersCreate(KnowWEArticle article, Priority p) {
		List<SubtreeHandler<? extends Type>> handlerList = type.getSubtreeHandlers().get(
				p);
		if (handlerList != null) {
			for (@SuppressWarnings("rawtypes")
			SubtreeHandler handler : handlerList) {
				if (handler != null) letSubtreeHandlerCreate(article, handler);
			}
		}

	}

	@SuppressWarnings({
			"unchecked", "rawtypes" })
	public final void letSubtreeHandlerCreate(KnowWEArticle article, SubtreeHandler handler) {
		if (handler.needsToCreate(article, this)
				&& isMatchingPackageName(article, handler)) {
			try {
				// long time = System.currentTimeMillis();
				Collection<KDOMReportMessage> msgs = handler.create(
						article, this);
				// a message should know its origin:
				if (msgs != null) {
					for (KDOMReportMessage m : msgs) {
						if (m != null) {
							m.setSection(this);
						}
					}
				}
				KDOMReportMessage.storeMessages(article, this, handler.getClass(), msgs);
				setCompiledBy(article.getTitle(), handler, true);
			}
			catch (Exception e) {
				e.printStackTrace();
				String text = "Unexpected internal error in subtree handler '" + handler
						+ "' : " + e.toString();
				SimpleMessageError msg = new SimpleMessageError(text);

				Logging.getInstance().severe(text);
				KDOMReportMessage.storeSingleError(getArticle(), this, getClass(), msg);

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Templates aren't working for us here, since getSubtreeHandlers() can not
	 * have any knowledge of the template T specified by the Section =>
	 * SuppressWarning...
	 * 
	 * @see SubtreeHandler#destroy(KnowWEArticle, Section)
	 */
	public final void letSubtreeHandlersDestroy(KnowWEArticle article, Priority p) {
		List<SubtreeHandler<? extends Type>> handlerList =
				type.getSubtreeHandlers().get(p);
		if (handlerList != null) {
			for (@SuppressWarnings("rawtypes")
			SubtreeHandler handler : handlerList) {
				if (handler != null) letSubtreeHandlerDestroy(article, handler);
			}
		}

	}

	@SuppressWarnings({
			"unchecked", "rawtypes" })
	public final void letSubtreeHandlerDestroy(KnowWEArticle article, SubtreeHandler handler) {
		if (handler.needsToDestroy(article, this)) {
			try {
				handler.destroy(article, this);
				setCompiledBy(article.getTitle(), handler, false);
			}
			catch (Exception e) {
				e.printStackTrace();
				Logging.getInstance().severe(
						"Unexpected internal error in subtree handler '" + handler
								+ "' : " + e.toString());

			}
		}
	}

	/**
	 * @see setType(Type newType, boolean create, KnowWEArticle article)
	 * 
	 *      create is true as default
	 * 
	 *      article is article of this section
	 * 
	 * @param newType
	 */
	public void setType(Type newType) {
		setType(newType, true, this.getArticle());
	}

	/**
	 * @see setType(Type newType, boolean create, KnowWEArticle article)
	 * 
	 * 
	 *      article is article of this section
	 * 
	 * @param newType
	 */
	public void setType(Type newType, boolean create) {
		setType(newType, create, this.getArticle());
	}

	/**
	 * @see setType(Type newType, boolean create, KnowWEArticle article)
	 * 
	 *      create is true as default
	 * 
	 * @param newType
	 */

	public void setType(Type newType, KnowWEArticle a) {
		setType(newType, true, a);
	}

	/**
	 * overrides type
	 * 
	 * 
	 * @created 03.03.2011
	 * @param newType the new type to be set
	 * @param create whether the handlers of the new type should be executed
	 *        right afterwards
	 * @param article the compilation context for removing old information
	 *        (error messages)
	 */
	@SuppressWarnings("unchecked")
	public void setType(Type newType, boolean create, KnowWEArticle article) {

		// remove error messages from old type
		TreeMap<Priority, List<SubtreeHandler<? extends Type>>> subtreeHandlers = this.get().getSubtreeHandlers();
		for (Priority p : subtreeHandlers.keySet()) {
			List<SubtreeHandler<? extends Type>> list = subtreeHandlers.get(p);
			for (SubtreeHandler<? extends Type> subtreeHandler : list) {
				KDOMReportMessage.clearMessages(article, this,
						subtreeHandler.getClass());
			}
		}

		this.type = (T) newType;

		if (create) {
			for (Priority p : type.getSubtreeHandlers().descendingKeySet()) {
				letSubtreeHandlersCreate(getArticle(), p);
			}
		}

	}
}
