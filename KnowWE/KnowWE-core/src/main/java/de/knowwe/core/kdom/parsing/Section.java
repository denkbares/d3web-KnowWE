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

package de.knowwe.core.kdom.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.filter.SectionFilter;
import de.knowwe.kdom.visitor.Visitable;
import de.knowwe.kdom.visitor.Visitor;

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
public final class Section<T extends Type> implements Visitable, Comparable<Section<? extends Type>> {

	public void setArticle(Article article) {
		this.article = article;
	}

	private HashSet<String> reusedBy = null;

	// @SuppressWarnings("rawtypes")
	// private HashMap<String, HashSet<Class<? extends SubtreeHandler>>>
	// compiledBy = null;
	private List<Integer> position = null;

	private List<Integer> lastPositions = null;

	protected boolean isOrHasReusedSuccessor = false;

	protected Article article;

	private HashSet<String> packageNames = null;

	protected boolean isExpanded = false;

	/**
	 * Store for this Section. Can be used to store infos or Objects for this
	 * Section.
	 */
	private final SectionStore sectionStore = new SectionStore();

	/**
	 * Specifies whether the orignialText was changed without changing the
	 * ancestor ones
	 */
	private boolean isDirty = false;

	/**
	 * The ID of this Section.
	 */
	private String id = null;

	/**
	 * Contains the text of this KDOM-node
	 */
	protected String text;

	/**
	 * The child-nodes of this KDOM-node. This forms the tree-structure of KDOM.
	 */
	protected List<Section<? extends Type>> children = new ArrayList<Section<? extends Type>>(5);
	/**
	 * The father section of this KDOM-node. Used for upwards navigation through
	 * the tree
	 */
	private Section<? extends Type> parent;

	/**
	 * the position the text of this node starts related to the text of the
	 * parent node. Thus: for first child always 0, for 2nd firstChild.length()
	 * etc.
	 */
	private int offsetInParent = -1;

	/**
	 * the position the text of this node starts related to the article's text.
	 */
	private int offsetInArticle = -1;

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
		this.parent = father;
		if (father != null) {
			this.parent.addChild(this);
			this.article = father.getArticle();
		}
		this.text = text;
		this.type = objectType;
	}

	/*
	 * verbalizes this node
	 */
	@Override
	public String toString() {
		String typeString = type != null ? this.get().getClass().getSimpleName() + ": " : "";
		String content;
		if (type != null && type instanceof Term) {
			Section<Term> simpleTerm = Sections.cast(this, Term.class);
			content = simpleTerm.get().getTermIdentifier(simpleTerm).toString();
		}
		else {
			content = this.getText();
		}
		return typeString + "'" + content + "'";
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
		if (o == null) return -1;
		int comp = getTitle().compareTo(o.getTitle());
		if (comp == 0) {
			List<Integer> thisPos = getPositionInKDOM();
			List<Integer> otherPos = o.getPositionInKDOM();
			Iterator<Integer> thisIter = thisPos.iterator();
			Iterator<Integer> otherIter = otherPos.iterator();

			while (comp == 0 && thisIter.hasNext() && otherIter.hasNext()) {
				comp = thisIter.next().compareTo(otherIter.next());
			}
			if (comp == 0) {
				comp = thisPos.size() - otherPos.size();
			}
		}
		return comp;
	}

	/**
	 * part of the visitor pattern
	 * 
	 * @see de.knowwe.kdom.visitor.Visitable#accept(de.knowwe.kdom.visitor.Visitor)
	 */
	@Override
	public void accept(Visitor v) {
		v.visit(this);

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
		if (article != sec.article || parent == null) {
			return false;
		}
		return parent.equalsOrIsSuccessorOf(sec);
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
	 * @created 08.07.2011
	 * @return the {@link SectionStore} for this {@link Section};
	 */
	public SectionStore getSectionStore() {
		return sectionStore;
	}

	/**
	 * @return the text of this Section/Node
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text of this node. This IS an article source edit operation!
	 * TODO: Important - propagate changes through the whole tree OR ReIinit
	 * tree!
	 * 
	 * @param text
	 */
	public void setText(String newText) {
		this.text = newText;
		this.isDirty = true;
		// setting the isDirty flag to true for all ancestor sections
		// since they are also dirty (concatenation of leafs doesn't
		// represent original text of the section, offsets don't fit...)
		Section<? extends Type> ancestor = this.getParent();
		while (ancestor != null) {
			ancestor.setDirty(true);
			ancestor = ancestor.getParent();
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
	 * Returns the character index position in the articles text where this
	 * section starts.
	 * 
	 * @created 25.11.2013
	 * @return the start position of this section in the article text
	 */
	public int getOffsetInArticle() {
		if (offsetInArticle == -1) {
			int parentOffset = parent != null ? parent.getOffsetInArticle() : 0;
			offsetInArticle = getOffsetInParent() + parentOffset;
		}
		return offsetInArticle;
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
			child.setParent(this);
		}
		this.children = children;
	}

	/**
	 * @return the list of child nodes
	 */
	public List<Section<? extends Type>> getChildren() {
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
	 * Returns the parent section of this section.
	 * 
	 * @return the parent section
	 */
	public Section<? extends Type> getParent() {
		return parent;
	}

	/**
	 * Returns the character index position in the parent's section text where
	 * this section starts.
	 * 
	 * @created 25.11.2013
	 * @return the start position of this section in the parent's section text
	 */
	public int getOffsetInParent() {
		if (offsetInParent == -1) {
			int temp = 0;
			if (parent != null) {
				for (Section<?> child : parent.getChildren()) {
					if (child == this) break;
					temp += child.getText().length();
				}
			}
			offsetInParent = temp;
		}
		return offsetInParent;
	}

	/**
	 * @return the title of the article this section belongs to.
	 */
	public String getTitle() {
		return article == null ? null : article.getTitle();
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
		if (parent == null) {
			if (packageNames == null) return Collections.emptySet();
			else return Collections.unmodifiableSet(packageNames);
		}
		else {
			Set<String> fatherPackageNames = parent.getPackageNames();
			if (packageNames == null) return fatherPackageNames;
			else {
				if (fatherPackageNames.isEmpty()) {
					return Collections.unmodifiableSet(packageNames);
				}
				else {
					Set<String> tempNamespaces = new HashSet<String>(fatherPackageNames.size()
							+ packageNames.size());
					tempNamespaces.addAll(packageNames);
					tempNamespaces.addAll(fatherPackageNames);
					return Collections.unmodifiableSet(tempNamespaces);
				}
			}
		}
	}

	public String getWeb() {
		return this.article.getWeb();
	}

	public Article getArticle() {
		return this.article;
	}

	/**
	 * @return the depth of this Section inside the KDOM
	 */
	public int getDepth() {
		if (get() instanceof RootType) {
			return 1;
		}
		else {
			return parent.getDepth() + 1;
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
		if (simpleName.contains("anonymous")) {
			simpleName = simpleName += "(" + this.get().getName() + ")";
		}
		buffi.append(simpleName);
		buffi.append(", ID: " + getID());
		buffi.append(", length: " + this.getText().length() + " ("
				+ getOffsetInParent() + ")" + ", children: " + getChildren().size());
		String ot = this.getText().length() < 50 ? text : text.substring(0,
				50) + "...";
		ot = ot.replaceAll("\\n", "\\\\n");
		buffi.append(", \"" + ot);
		buffi.append("\"");
		return buffi.toString();
	}

	public String getID() {
		if (id == null) {
			id = Sections.generateAndRegisterSectionID(this);
		}
		return id;
	}

	// @Override
	// public boolean equals(Object obj) {
	// if (obj == null) return false;
	// if (obj instanceof Section) {
	// Section<?> other = (Section<?>) obj;
	// if (other.getSignatureString().equals(this.getSignatureString())) {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// @Override
	// public int hashCode() {
	// String signatureString = getSignatureString();
	// return signatureString.hashCode();
	// }

	private String signatureString = null;

	protected String getSignatureString() {
		if (signatureString != null) return signatureString;
		List<Integer> positionInKDOM = this.getPositionInKDOM();
		String positionInKDOMString = positionInKDOM == null ? "" : positionInKDOM.toString();

		String signatureString = getWeb() + getTitle() + positionInKDOMString + this.getText();
		this.signatureString = signatureString;
		return signatureString;
	}

	protected boolean hasID() {
		return this.id != null;
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
		if (!this.getChildren().isEmpty()) {
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

	public void setParent(Section<?> parent) {
		this.parent = parent;
	}

	/**
	 * Method that looks (recursively down) for this section whether some
	 * message of the specified type has been stored in that subtree.
	 */
	public boolean hasErrorInSubtree() {
		return hasMessageInSubtree(Message.Type.ERROR);
	}

	/**
	 * Method that looks (recursively down) for this section whether some errors
	 * has been stored in that subtree.
	 */
	public boolean hasMessageInSubtree(Message.Type type) {
		Map<Compiler, Collection<Message>> errors = Messages.getMessagesMap(
				this, type);
		if (!errors.isEmpty()) return true;
		for (Section<?> child : children) {
			boolean err = child.hasErrorInSubtree();
			if (err) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method that looks (recursively down) for this section whether some
	 * messages of the specified type has been stored in that subtree for the
	 * given article.
	 */
	public boolean hasErrorInSubtree(Compiler compiler) {
		return hasMessageInSubtree(compiler, Message.Type.ERROR);
	}

	/**
	 * Method that looks (recursively down) for this section whether some errors
	 * has been stored in that subtree for the given article.
	 */
	public boolean hasMessageInSubtree(Compiler compiler, Message.Type type) {
		Collection<Message> errors = Messages.getMessages(compiler, this, type);
		if (!errors.isEmpty()) return true;
		for (Section<?> child : children) {
			boolean err = child.hasErrorInSubtree(compiler);
			if (err) {
				return true;
			}
		}

		return false;
	}

	public boolean isEmpty() {
		String text = getText();
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

	//
	// /**
	// * Checks whether this Section or a successor is not reused. Sections and
	// * successors with a Type contained in the Set of classes will be ignored.
	// *
	// * @created 10.07.2010
	// * @param title is the article, for which to check.
	// * @param filteredTypes if this Section has one of the filtered types,
	// false
	// * is returned.
	// * @return a boolean with the result of the check
	// */
	// public boolean isOrHasChangedSuccessor(String title, Collection<Class<?
	// extends Type>> filteredTypes) {
	// if (isChanged(title, filteredTypes)) {
	// return true;
	// }
	// else {
	// for (Section<?> child : this.getChildren()) {
	// if (child.isOrHasChangedSuccessor(title, filteredTypes)) return true;
	// }
	// return false;
	// }
	// }

	// /**
	// * Checks, whether this Section has changed since the last version of the
	// * article.
	// *
	// * @created 08.12.2010
	// * @param title is the article, for which to check.
	// * @param filteredTypes if this Section has one of the filtered types,
	// false
	// * is returned.
	// * @return a boolean with the result of the check
	// */
	// public boolean isChanged(String title, Collection<Class<? extends Type>>
	// filteredTypes) {
	// if (filteredTypes != null) {
	// for (Class<?> c : filteredTypes) {
	// if (c.isAssignableFrom(type.getClass())) {
	// return false;
	// }
	// }
	// }
	// if (!isReusedBy(title) || (type.isOrderSensitive() &&
	// hasPositionChanged())) {
	// return true;
	// }
	// return false;
	// }
	//
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
	public void clearReusedOfOldSectionsRecursively(Article
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

	//
	// @SuppressWarnings("rawtypes")
	// public boolean isCompiledBy(String title, SubtreeHandler handler) {
	// if (compiledBy == null) return false;
	// HashSet<Class<? extends SubtreeHandler>> compiledByHandlerSet =
	// compiledBy.get(title);
	// if (compiledByHandlerSet == null) return false;
	// return compiledByHandlerSet.contains(handler.getClass());
	// }
	//
	// public boolean isCompiledBy(String title) {
	// if (compiledBy == null) return false;
	// return compiledBy.containsKey(title);
	// }
	//
	// @SuppressWarnings("rawtypes")
	// public Map<String, HashSet<Class<? extends SubtreeHandler>>>
	// getCompiledByMap() {
	// return compiledBy == null
	// ? Collections.unmodifiableMap(
	// new HashMap<String, HashSet<Class<? extends SubtreeHandler>>>(
	// 0))
	// : Collections.unmodifiableMap(compiledBy);
	// }
	//
	// @SuppressWarnings("rawtypes")
	// public void setCompiledBy(String title, SubtreeHandler handler, boolean
	// compiled) {
	// if (compiled) {
	// if (compiledBy == null) {
	// compiledBy = new HashMap<String, HashSet<Class<? extends
	// SubtreeHandler>>>(4);
	// }
	// HashSet<Class<? extends SubtreeHandler>> reusedByArticleSet =
	// compiledBy.get(title);
	// if (reusedByArticleSet == null) {
	// reusedByArticleSet = new HashSet<Class<? extends SubtreeHandler>>(4);
	// compiledBy.put(title, reusedByArticleSet);
	// }
	// reusedByArticleSet.add(handler.getClass());
	// }
	// else {
	// if (compiledBy != null) {
	// HashSet<Class<? extends SubtreeHandler>> compiledByHandlerSet =
	// compiledBy.get(title);
	// if (compiledByHandlerSet != null) {
	// compiledByHandlerSet.remove(handler.getClass());
	// if (compiledByHandlerSet.isEmpty()) compiledBy.remove(title);
	// }
	// }
	// }
	// }
	//
	// public void setNotCompiledBy(String title) {
	// if (compiledBy != null) compiledBy.remove(title);
	// }
	//
	// /**
	// * Affects all Sections this Section is connected to (also included
	// * Sections).
	// */
	// public void setNotCompiledByRecursively(String title) {
	// setNotCompiledBy(title);
	// for (Section<? extends Type> child : getChildren()) {
	// child.setNotCompiledByRecursively(title);
	// }
	// }

	// public boolean hasPositionChanged() {
	// if (lastPositions == null) return false;
	// return !lastPositions.equals(getPositionInKDOM());
	// }

	protected void setPositionInKDOM(List<Integer> positionInKDOM) {
		position = positionInKDOM;
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
		return lastPositions == null ? null :
				Collections.unmodifiableList(lastPositions);
	}

	public List<Integer> calcPositionInKDOM() {

		// this can only occur in the exceptional case
		// when Sections are created independently of articles
		if (getArticle() == null || getArticle().getRootSection() == null) {
			LinkedList<Integer> positions = new LinkedList<Integer>();
			positions.add(new Integer(0));
			return positions;
		}

		return calcPositionTil(getArticle().getRootSection());
	}

	public List<Integer> calcPositionTil(Section<?> end) {
		LinkedList<Integer> positions = new LinkedList<Integer>();
		Section<?> temp = this;
		Section<?> tempFather = temp.getParent();
		while (temp != end && tempFather != null) {
			List<Section<? extends Type>> childrenList = tempFather.getChildren();
			int indexOf = getIndex(temp, childrenList);
			positions.addFirst(indexOf);
			temp = tempFather;
			tempFather = temp.getParent();
		}
		return positions;
	}

	/**
	 * Calculates the index of a contained object without using the
	 * equals-method.
	 * 
	 * @created 10.07.2012
	 * @param temp
	 * @param childrenList
	 * @return
	 */
	private int getIndex(Section<?> temp, List<Section<? extends Type>> childrenList) {
		int index = 0;
		for (Section<? extends Type> section : childrenList) {
			// this == comparator is on purpose on this place as this method is
			// used in the equals method
			if (section == temp) {
				return index;
			}
			index++;
		}
		return -1;
	}

	// private boolean isMatchingPackageName(Article article, SubtreeHandler<?>
	// h) {
	//
	// // ignore: compile always but only for the article of this section
	// if (!h.isPackageCompile() || !type.isPackageCompile()) {
	// return article.getTitle().equals(getTitle());
	// }
	// // auto: compile always
	// else if (PackageManager.isAutocompileArticleEnabled()) {
	// return true;
	// }
	// else {
	// Set<String> referencedPackages =
	// Environment.getInstance().getPackageManager(
	// article.getWeb()).getCompiledPackages(article.getTitle());
	//
	// if (referencedPackages.contains(PackageManager.THIS)) return true;
	//
	// for (String name : getPackageNames()) {
	// if (referencedPackages.contains(name)) return true;
	// }
	// return false;
	// }
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * Templates aren't working for us here, since getSubtreeHandlers() can
	// not
	// * have any knowledge of the template T specified by the Section =>
	// * SuppressWarning...
	// *
	// * @see SubtreeHandler#create(Article, Section)
	// */
	// public final void letSubtreeHandlersCreate(Article article, Priority p) {
	// List<SubtreeHandler<? extends Type>> handlerList =
	// type.getSubtreeHandlers().get(
	// p);
	// if (handlerList != null) {
	// for (@SuppressWarnings("rawtypes")
	// SubtreeHandler handler : handlerList) {
	// if (handler != null) letSubtreeHandlerCreate(article, handler);
	// }
	// }
	//
	// }

	// @SuppressWarnings({
	// "unchecked", "rawtypes" })
	// public final void letSubtreeHandlerCreate(Article article, SubtreeHandler
	// handler) {
	// if (handler.needsToCreate(article, this)
	// && isMatchingPackageName(article, handler)) {
	// try {
	// // long time = System.currentTimeMillis();
	// Collection<Message> msgs = handler.create(article, this);
	// Messages.storeMessages(article, this, handler.getClass(), msgs);
	// setCompiledBy(article.getTitle(), handler, true);
	// }
	// catch (Exception e) {
	// e.printStackTrace();
	// String text = "Unexpected internal error in subtree handler '"
	// + handler.getClass().getName()
	// + "' while creating for '" + get().getClass().getSimpleName()
	// + "' section '"
	// + getID() + "' in article '"
	// + getTitle() + "': " + e.toString();
	// Message msg = Messages.error(text);
	//
	// Logging.getInstance().severe(text);
	// Messages.storeMessage(getArticle(), this, getClass(), msg);
	//
	// }
	// }
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * Templates aren't working for us here, since getSubtreeHandlers() can
	// not
	// * have any knowledge of the template T specified by the Section =>
	// * SuppressWarning...
	// *
	// * @see SubtreeHandler#destroy(Article, Section)
	// */
	// public final void letSubtreeHandlersDestroy(Article article, Priority p)
	// {
	// List<SubtreeHandler<? extends Type>> handlerList =
	// type.getSubtreeHandlers().get(p);
	// if (handlerList != null) {
	// for (@SuppressWarnings("rawtypes")
	// SubtreeHandler handler : handlerList) {
	// if (handler != null) letSubtreeHandlerDestroy(article, handler);
	// }
	// }
	//
	// }
	//
	// @SuppressWarnings({
	// "unchecked", "rawtypes" })
	// public final void letSubtreeHandlerDestroy(Article article,
	// SubtreeHandler handler) {
	// if (handler.needsToDestroy(article, this)) {
	// try {
	// handler.destroy(article, this);
	// setCompiledBy(article.getTitle(), handler, false);
	// }
	// catch (Exception e) {
	// e.printStackTrace();
	// Logging.getInstance().severe(
	// "Unexpected internal error in subtree handler '" + handler
	// + "' while destroying for section '" + getID() + "' in article '"
	// + getTitle() + "': " + e.toString());
	//
	// }
	// }
	// }

	/**
	 * Overrides type.
	 * 
	 * 
	 * @created 03.03.2011
	 * @deprecated we should get rid of this method, because it can cause
	 *             problems with methods depending on a static type tree (like
	 *             the methods in {@link Sections} and ScriptManager)
	 * @param newType the new type to be set
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public void setType(Type newType) {
		for (Type type : this.type.getParentTypes()) {
			// we need to update the successor type, otherwise the methods
			// looking for successors no longer work correctly.
			((AbstractType) type).addSuccessorType(newType.getClass());
		}
		this.type = (T) newType;

	}

	public ArticleManager getArticleManager() {
		return article.getArticleManager();
	}
}
