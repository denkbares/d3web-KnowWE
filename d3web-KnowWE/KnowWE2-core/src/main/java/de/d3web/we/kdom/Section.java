package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.filter.SectionFilter;
import de.d3web.we.kdom.include.IncludedFromTypeHead;
import de.d3web.we.kdom.include.IncludedFromTypeTail;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.visitor.Visitable;
import de.d3web.we.kdom.visitor.Visitor;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.KnowWEModule;
import de.d3web.we.user.UserSettingsManager;
import de.d3web.we.utils.PairOfInts;

/**
 * @author Jochen
 * 
 * This class represents a node in the Knowledge-DOM of KnowWE. Basically it has
 * some text, one type and a list of children.
 * 
 * Further, it has a reference to its father and a positionOffset to its fathers
 * text.
 * 
 * Further information can be attached to a node (TypeInformation), to connect
 * the text-parts with external resources, e.g. knowledge bases, OWL,
 * User-feedback-DBs etc.
 * 
 */
public class Section implements Visitable, Comparable<Section> {

	private boolean isExpanded = false;

	private KnowWEDomRenderer renderer;
	
	/**
	 * @deprecated
	 */
	@Deprecated
	public static final String SECTION_TYPE_UNDEF = "UNDEF";

	/**
	 * The id of this node, unique in an article
	 */
	protected String id;

	/**
	 * If the parsed id for this section is already assigned to another section,
	 * this section gets a generated id and this boolean get the value true;
	 */
	protected boolean idConflict = false;

	/**
	 * Contains the text of this KDOM-node
	 */
	protected String originalText;

	/**
	 * The child-nodes of this KDOM-node. This forms the tree-structure of KDOM.
	 */
	protected List<Section> children = new ArrayList<Section>();

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

	/**
	 * the local topic TODO: still necessary in every node?
	 */
	protected String topic;

	PairOfInts startPosFromTmp;

	protected int absolutePositionStartInArticle = -1;
	
	/**
	 * only for KDOM-tree building algorithm - shouldnt be referenced later
	 * 
	 * @return
	 */
	public PairOfInts getPosition() {
		// if( startPosFromTmp == null) {
		// return new PairOfInts(this.getOffSetFromFatherText(),
		// this.getOffSetFromFatherText()+this.originalText.length());
		// }
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
	 * Without topic, with id;
	 */
	public static Section createSection(KnowWEObjectType objectType,
			Section father, Section tmp, int beginIndex, int endIndex,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
			IDGenerator idgen, String id) {
		return createSection(objectType, father, tmp, beginIndex, endIndex,
				null, kbm, report, idgen, id);
	}

	/**
	 * Without topic, without id;
	 */
	public static Section createSection(KnowWEObjectType objectType,
			Section father, Section tmp, int beginIndex, int endIndex,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
			IDGenerator idgen) {
		return createSection(objectType, father, tmp, beginIndex, endIndex,
				null, kbm, report, idgen, null);
	}

	/**
	 * With topic, without id;
	 */
	public static Section createSection(KnowWEObjectType objectType,
			Section father, Section tmp, int beginIndex, int endIndex,
			String topic, KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
			IDGenerator idgen) {
		return createSection(objectType, father, tmp, beginIndex, endIndex,
				topic, kbm, report, idgen, null);

	}

	/**
	 * With topic, with id.
	 */
	public static Section createSection(KnowWEObjectType objectType,
			Section father, Section tmp, int beginIndex, int endIndex,
			String topic, KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
			IDGenerator idgen, String id) {
		// validate subsections
		boolean findingsOk = false;
		try {
			findingsOk = validateFinding(tmp.getOriginalText(), beginIndex,
					endIndex);
		} catch (InvalidSectionValuesException e) {

			Logger.getLogger(objectType.getName()).severe(
					"INVALID SECTIONIZING: Type: "
							+ objectType.getClass().getName() + " :"
							+ e.getMessage());
			return null;
		}

		Section s = new Section(tmp.getOriginalText().substring(beginIndex,
				endIndex), objectType, father, tmp.getOffSetFromFatherText()
				+ beginIndex, topic, kbm, report, idgen, id, false);
		s.setPosition(new PairOfInts(beginIndex, endIndex));
		return s;
	}

	/**
	 * With topic, with id.
	 */
	public static Section createSection(KnowWEObjectType objectType,
			Section father, Section tmp, int beginIndex, int endIndex,
			String topic, KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
			IDGenerator idgen, String id, boolean expanded) {
		// validate subsections
		boolean findingsOk = false;
		try {
			findingsOk = validateFinding(tmp.getOriginalText(), beginIndex,
					endIndex);
		} catch (InvalidSectionValuesException e) {

			Logger.getLogger(objectType.getName()).severe(
					"INVALID SECTIONIZING: Type: "
							+ objectType.getClass().getName() + " :"
							+ e.getMessage());
			return null;
		}

		Section s = new Section(tmp.getOriginalText().substring(beginIndex,
				endIndex), objectType, father, tmp.getOffSetFromFatherText()
				+ beginIndex, topic, kbm, report, idgen, id, expanded);
		s.setPosition(new PairOfInts(beginIndex, endIndex));
		return s;
	}

	// private Section(String text, KnowWEObjectType objectType, Section father,
	// int beginIndexFather, KnowledgeBaseManagement kbm,
	// KnowWEDomParseReport report, IDGenerator idgen, String id) {
	// this(text, objectType, father, beginIndexFather, null, kbm, report,
	// idgen, id);
	// }

	/**
	 * Validates the result of a type allocated some text part for itself As
	 * many types will be written with different parsing methods (ANTLR, REGEX,
	 * pur Java) there is some plausibility check is made here, preventing the
	 * KDOM generation process to crash, due to invalid text allocation
	 * positions
	 * 
	 * @param findings
	 * @param text
	 * @return
	 * @throws InvalidSectionValuesException
	 */
	private static boolean validateFinding(String fathertext, int start, int end)
			throws InvalidSectionValuesException {
		PairOfInts pairOfInts = new PairOfInts(start, end);
		int a = start;
		int b = end;
		if (a < 0) {
			throw new InvalidSectionValuesException(pairOfInts.toString());
		}
		if (b < a) {
			throw new InvalidSectionValuesException(pairOfInts.toString());
		}
		if (b > fathertext.length()) {
			throw new InvalidSectionValuesException(pairOfInts.toString());
		}

		return true;
	}

	/**
	 * Constructor for Sections with automatically generated ids.
	 */
	public static Section createExpandedSection(String text,
			KnowWEObjectType objectType, Section father, int beginIndexFather,
			String topic, KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
			IDGenerator idgen) {
		return new Section(text, objectType, father, beginIndexFather, topic,
				kbm, report, idgen, null, true);
	}

	/**
	 * Constructor for Sections with automatically generated ids.
	 */
	Section(String text, KnowWEObjectType objectType, Section father,
			int beginIndexFather, String topic, KnowledgeRepresentationManager kbm,
			KnowWEDomParseReport report, IDGenerator idgen) {
		this(text, objectType, father, beginIndexFather, topic, kbm, report,
				idgen, null, false);
	}

	/**
	 * DO USE THE FACTORY METHODS TO CREATE SECTIONS!
	 * 
	 * Constructor of a node Important: parses itself recursivly by getting the
	 * allowed childrenTypes of the local type
	 * 
	 * @param type
	 *            deprecated - use objectType
	 * @param text
	 *            the part of (article-source) text of the node
	 * @param objectType
	 *            type of the node
	 * @param father
	 * @param beginIndexFather
	 * @param topic
	 * @param kbm
	 *            the local knowledgebase (partly) filled, should contain local
	 *            terminology
	 * @param info
	 * @param report
	 */
	Section(String text, KnowWEObjectType objectType, Section father,
			int beginIndexFather, String topic, KnowledgeRepresentationManager kbm,
			KnowWEDomParseReport report, IDGenerator idgen, String id,
			boolean isExpanded) {

		if (topic == null && father!= null)
			topic = father.getTopic();
		this.isExpanded = isExpanded;
		this.topic = topic;
		
		this.father = father;
		if (father != null)
			father.addChild(this);
		this.originalText = text == null ? "null" : text;
		this.objectType = objectType;
		offSetFromFatherText = beginIndexFather;
		IDGeneratorOutput idOut = idgen.newID(id);
		this.id = idOut.getID();
		this.idConflict = idOut.isIdConflict();
		
//		// try to get unchanged Sections from old article
//		// still experimental!!!
//		KnowWEArticle thisArt = getArticle();
//		if (thisArt != null) {
//			
//			KnowWEArticle oldArt = KnowWEEnvironment.getInstance()
//					.getArticleManager(KnowWEEnvironment.DEFAULT_WEB)
//						.getArticle(thisArt.getTitle());
//			
//			if (oldArt != null) {
//				Section firstTry = oldArt.findSection(this.id);
//				if (firstTry != null && firstTry.getOriginalText().equals(this.getOriginalText())) {
//					
//					List<Section> oldChildren = firstTry.getChildren();
//					for (Section oldChild:oldChildren) {
//						oldChild.setFather(this);
//						
//					}
//					this.children = oldChildren;
//					List<Section> allChildren = this.getAllNodesPreOrder();
//					for (Section child:allChildren) {
//						child.getObjectType().reviseSubtree(child, kbm,
//								KnowWEEnvironment.DEFAULT_WEB, report);
//					}
//					System.out.println("####################");
//					System.out.println("Used old Children!!!");
//					return;
//				}
//			}
//		}

		/**
		 * fetches the allowed children types of the local type
		 */
		
		List<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();

		if (objectType != null && objectType.getAllowedChildrenTypes() != null) {
			types.addAll(objectType
					.getAllowedChildrenTypes());
		}

		if (objectType != null && !objectType.equals(IncludedFromTypeHead.getInstance())
				&& !objectType.equals(IncludedFromTypeTail.getInstance())
				&& !objectType.equals(PlainText.getInstance())) {
			types.add(IncludedFromTypeHead.getInstance());
			types.add(IncludedFromTypeTail.getInstance());

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
				&& !(objectType.equals(PlainText.getInstance())) && !isExpanded) {
			Sectionizer.getInstance().splitToSections(text, types, this,
					getTopic(), kbm, report, idgen);
		}



		/**
		 * sort children sections in text-order
		 */
		Collections.sort(children, new TextOrderComparator());

		/**
		 * a type can revise a complete subtree
		 */
		try {
			this.objectType.reviseSubtree(this, kbm,
					KnowWEEnvironment.DEFAULT_WEB, report);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	protected Section() {

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
	 * @param s
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

	/**
	 * Sets the text of this node. This IS an article source edit operation!
	 * TODO: Important - propagate changes through the whole tree OR ReIinit
	 * tree!
	 * 
	 * @param originalText
	 */
	public void setOriginalText(String newText) {
		this.originalText = newText;
		this.getArticle().setDirty(true);
	}

	/**
	 * return the list of child nodes
	 * 
	 * @return
	 */
	public List<Section> getChildren() {
		return children;
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
		for (Section current : children) {
			if (filter.accept(current))
				list.add(current);
		}
		return list;
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
	
	public void  setOffSetFromFatherText(int offSet) {
		this.offSetFromFatherText = offSet;
	}

	/**
	 * return the article-name, if its not defined it asks the father
	 * 
	 * @return
	 */
	public String getTopic() {
		if (topic != null)
			return topic;
		if (father != null)
			return father.getTopic();
		return null;
	}

	public KnowWEArticle getArticle() {
		if (this.getObjectType() instanceof KnowWEArticle) {
			return (KnowWEArticle) this.getObjectType();
		}
		if (father != null) {
			return father.getArticle();
		}
		return null;
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
		for (Section child : children) {
			if (child.getObjectType().getClass().equals(class1)) {
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
		for (Section child : children) {
			if (child.getObjectType().getClass().equals(class1)) {
				if (this.originalText.indexOf(text) > child
						.getOffSetFromFatherText()) {
					return true;
				}
			}
		}
		return false;
	}

	public void setObjectType(KnowWEObjectType objectType) {
		this.objectType = objectType;
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
		buffi.append(", ID: " + this.id);
		buffi.append(", length: " + this.getOriginalText().length() + " ("
				+ offSetFromFatherText + ")" + ", children: " + children.size());
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
		for (Section child : children) {
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
		for (Section child : children) {
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
			for (Section sec : children) {
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
			for (Section sec : children) {
				Section sub = sec.findSmallestNodeContaining(text);
				if (sub != null && (!(s.getObjectType() instanceof PlainText))) {
					s = sub;
				}
			}
		}
		return s;
	}

	/**
	 * Searches the Children of a Section and only the children
	 * of a Section for a given class
	 * 
	 * @param section
	 */
	public Section findChildOfType(Class class1) {
		
		for (Section s : this.getChildren())
			if (class1.isAssignableFrom(s.getObjectType().getClass()))
				return s;
		return null;
	}
	
	public Section findSuccessor(Class class1) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			return this;
		}
		for (Section sec : children) {
			Section s = sec.findSuccessor(class1);
			if (s != null)
				return s;
		}

		return null;
	}

	public void findChildrenOfType(Class<?> class1, List<Section> found) {

		if (class1.isAssignableFrom(this.getObjectType().getClass())) {
			found.add(this);
		}
		for (Section sec : children) {
			sec.findChildrenOfType(class1, found);
		}

	}

	public void collectTextsFromLeaves(StringBuilder buffi) {
		if (this.children != null && this.children.size() > 0) {
			for (Section s : children) {
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
				user, this.getTopic())) {
			return true;
		}
		if (father == null)
			return false;

		return father.hasQuickEditModeSet(user);
	}

	public List<Section> getAllNodesPreOrder() {
		ArrayList<Section> nodes = new ArrayList<Section>();
		nodes.add(this);
		if (this.getChildren() != null) {
			for (Section child : this.getChildren()) {
				nodes.addAll(child.getAllNodesPreOrder());
			}
		}

		return nodes;
	}

	public boolean isEmpty() {
		String text = getOriginalText();
		text = text.replaceAll("<includedFrom[^>]*>", "");
		text = text.replaceAll("</includedFrom>", "");
		text = text.replaceAll("\\s", "");
		return text.length() == 0 ? true : false;
	}

	public boolean isExpanded() {
		return isExpanded;
	}

	public void setFather(Section father) {
		this.father = father;
	}

}
