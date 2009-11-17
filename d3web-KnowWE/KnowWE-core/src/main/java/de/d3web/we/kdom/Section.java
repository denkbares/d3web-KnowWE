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
import de.d3web.we.kdom.basic.EmbracedType;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.filter.SectionFilter;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.include.IncludeAddress;
import de.d3web.we.kdom.include.TextInclude;
import de.d3web.we.kdom.include.TextIncludeHead;
import de.d3web.we.kdom.include.TextIncludeTail;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.store.SectionStore;
import de.d3web.we.kdom.visitor.Visitable;
import de.d3web.we.kdom.visitor.Visitor;
import de.d3web.we.module.DefaultTextType;
import de.d3web.we.module.KnowWEModule;
import de.d3web.we.user.UserSettingsManager;
import de.d3web.we.utils.KnowWEUtils;
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
public class Section implements Visitable, Comparable<Section> {

	private boolean reused = false;
	
	private boolean hasReusedSuccessor = false;

	private PairOfInts startPosFromTmp;

	private IncludeAddress address;

	protected KnowWEArticle article;

	protected boolean isExpanded = false;

	protected KnowWEDomRenderer renderer;

	/**
	 * The id of this node, unique in an article
	 */
	protected String id;
	
	private String specificID;

	/**
	 * Contains the text of this KDOM-node
	 */
	protected String originalText;

	/**
	 * The child-nodes of this KDOM-node. This forms the tree-structure of KDOM.
	 */
	protected List<Section> children = new ArrayList<Section>();
	
	private LinkedList<Section> childrenParsingOrder = new LinkedList<Section>();

	/**
	 * The father section of this KDOM-node. Used for upwards navigation through
	 * the tree
	 */
	protected Section father;

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
	protected KnowWEObjectType objectType;

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

	/**
	 * 
	 * Constructor of a node Important: parses itself recursively by getting the
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
	 protected Section(String text, KnowWEObjectType objectType, Section father,
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
		
		// Update mechanism
		// try to get unchanged Sections from old article
		if (article.getLastVersionOfArticle() != null && !isExpanded
				&& !objectType.isNotRecyclable()
				&& !objectType.isLeafType()) {
			
			Map<String, Section> sectionsOfSameType = article.getLastVersionOfArticle()
					.findChildrenOfTypeMap(getPathFromArticleToThis());
			
			Section match = sectionsOfSameType.remove(getOriginalText());
			
			if (match != null && (!match.reused && !match.hasReusedSuccessor)) {
				
				match.reused = true;
				
				Section ancestor = match.getFather();
				while (ancestor != null) {
					ancestor.hasReusedSuccessor = true;
					ancestor = ancestor.getFather();
				}
				
				List<Section> oldChildren = match.getChildren();
				for (Section oldChild : oldChildren) {
					oldChild.setFather(this);
				}
				this.children = oldChildren;
				this.childrenParsingOrder = match.childrenParsingOrder;
			
				List<Section> newNodes = new ArrayList<Section>();
				getAllNodesParsingPreOrderWithoutIncludes(newNodes);
				for (Section node:newNodes) {
					
					if (!node.getTitle().equals(getTitle())) {
						continue;
					}
					
					if (node.getObjectType() instanceof Include) {
						article.getIncludeSections().add(node);
					}
					
					node.article = this.article;
					
					SectionStore oldStore = KnowWEUtils.getLastSectionStore(node.getWeb(), node.getTitle(), node.id);
					
					if (node != this) {
						if (node.specificID == null) {
							node.id = new SectionID(node.father, node.objectType).toString();
						} else {
							node.id = new SectionID(node.getArticle(), node.specificID).toString();
						}
					}
					
					node.reused = true;
					
					//System.out.print(oldStore.getAllObjects().isEmpty() ? "" : "#" + node.getId() + " put " + oldStore.getAllObjects() + "\n");
					KnowWEUtils.putSectionStore(node.getWeb(), node.getTitle(), node.id, oldStore);
				}
				
				//article.getUnchangedSubTrees().put(id, this);
				//System.out.println("Used old " + this.getObjectType().getName());
				return;
			}
		}

		//fetches the allowed children types of the local type
		List<KnowWEObjectType> types = new LinkedList<KnowWEObjectType>();

		if (objectType != null && objectType.getAllowedChildrenTypes() != null) {
			types.addAll(objectType.getAllowedChildrenTypes());
		}

		if (objectType != null
				&& !objectType.equals(TextIncludeHead.getInstance())
				&& !objectType.equals(TextIncludeTail.getInstance())
				&& !objectType.equals(Include.getInstance())
				&& !objectType.equals(PlainText.getInstance())) {
			types.add(TextIncludeHead.getInstance());
			types.add(TextIncludeTail.getInstance());
			types.add(Include.getInstance());
		}
		// hack... TODO: find nicer, more efficient way
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
		if (objectType instanceof KnowWEArticle) {
			System.out.print("");
		}
		sortChildrenParsingOrder();
		
		/**
		 * sort children sections in text-order
		 */
		Collections.sort(children, new TextOrderComparator());
		
		article.getChangedSections().put(id, this);
		
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
		return this.getObjectType().getClass().getName() + " l:"
				+ this.getOriginalText().length() + " - "
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
	public List<Section> getChildren() {
		if (objectType instanceof Include) {
			return KnowWEEnvironment.getInstance().getIncludeManager(getWeb()).getChildrenForSection(this);
		} else {
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
	
	public void getAllNodesPreOrder(List<Section> nodes) {
		nodes.add(this);
		if (this.getChildren() != null) {
			for (Section child : this.getChildren()) {
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
	public List<Section> getChildrenParsingOrder() {
		if (objectType instanceof Include) {
			return getChildren();
		} else {
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
		Map<KnowWEObjectType, List<Section>> includes = new HashMap<KnowWEObjectType, List<Section>>();
		// all ObjectTypes that are possible in the children list
		Set<KnowWEObjectType> types = new HashSet<KnowWEObjectType>(getObjectType().getAllowedChildrenTypes());
		
		for (Section sec:childrenParsingOrder) {
			// store the Includes to the map
			if (sec.getObjectType() instanceof Include && types.contains(sec.getChildren().get(0).getObjectType())) {
				KnowWEObjectType includedType = sec.getChildren().get(0).getObjectType();
				List<Section> includesOfType = includes.get(includedType);
				if (includesOfType == null) {
					includesOfType = new ArrayList<Section>();
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
		for (List<Section> incList:includes.values()) {
			// remove the Includes from the children list
			childrenParsingOrder.removeAll(incList);
		}
		// and sort them back in
		// for each ObjectType move the pivot to the position behind the last Section
		// with this Type and then insert Includes that include a Section with the same
		// ObjectType (if given) 
		for(KnowWEObjectType type:getObjectType().getAllowedChildrenTypes()) {
			int i = 0; // pivot
			while (i <= childrenParsingOrder.size() 
					&& childrenParsingOrder.get(i).getObjectType().isAssignableFromType(type.getClass())) {
				i++;
			}
			List<Section> includesOfType = includes.get(type);
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
	public Section getFather() {
		return father;
	}
	
	/**
	 * returns the type of this node
	 * 
	 * @return
	 */
	public KnowWEObjectType getObjectType() {
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
		buffi.append(this.getObjectType().getClass().getSimpleName());
		//TODO: Show more of the IDs...
		buffi.append(", ID: " + (id.contains("/") ? ".." + id.substring(id.lastIndexOf("/")) : id));
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
	public Section findChildOfType(Class<?> class1) {

		for (Section s : this.getChildren())
			if (class1.isAssignableFrom(s.getObjectType().getClass()))
				return s;
		return null;
	}

	/**
	 * Searches the Children of a Section and only the children of a Section for
	 * a given class
	 * 
	 * @param section
	 */
	public List<Section> findChildrenOfType(Class<?> class1) {
		List<Section> result = new ArrayList<Section>();
		for (Section s : this.getChildren())
			if (class1.isAssignableFrom(s.getObjectType().getClass()))
				result.add(s);
		return result;
	}
	
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
	public void findSuccessorsOfType(Class<?> class1, List<Section> found) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			found.add(this);
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfType(class1, found);
		}
	}
	
	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section and stores them in a Map, using their originalText as key.
	 */
	public void findSuccessorsOfType(Class<?> class1, Map<String, Section> found) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			Section tmp = found.get(this.getOriginalText());
			if (tmp == null || (tmp.reused && !this.reused)) {
				found.put(this.getOriginalText(), this);
			}
		}
		for (Section sec : getChildren()) {
			sec.findSuccessorsOfType(class1, found);
		}

	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM to the depth
	 * of <code>depth</code> below this Section.
	 */
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
	public LinkedList<Class<? extends KnowWEObjectType>> getPathFromArticleToThis() {
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
	public LinkedList<Class<? extends KnowWEObjectType>> getPathFromGivenSectionToThis(Section sec) {
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

	public KnowWEDomRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(KnowWEDomRenderer renderer) {
		this.renderer = renderer;
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
		text = text.replaceAll(TextInclude.PATTERN_BOTH, "");
		text = text.replaceAll("\\s", "");
		return text.length() == 0;
	}

	public boolean isExpanded() {
		return isExpanded;
	}
	
	public boolean isReused() {
		return reused;
	}
	
	public void resetStateRecursively() {
		reused = false;
		hasReusedSuccessor = false;
		for (Section child:getChildren()) {
			child.resetStateRecursively();
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
	
	public boolean setType(KnowWEObjectType newType) {
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
