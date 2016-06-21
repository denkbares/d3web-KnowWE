/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.include;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiMarkupUtils;
import de.knowwe.jspwiki.types.DefinitionType;
import de.knowwe.jspwiki.types.HeaderType;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.HasChildrenOfTypeConstraint;
import de.knowwe.kdom.constraint.NoChildrenOfTypeConstraint;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * KDOM type that references an article or a headed section of an article.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 05.02.2014
 */
public class InnerWikiReference extends AbstractType {

	private static class Indent extends AbstractType {

		public Indent() {
			setSectionFinder(new RegexSectionFinder(
					"^([ \t\u00A0]+)[^\\s]", Pattern.MULTILINE, 1));
		}
	}

	private static class LinkName extends AbstractType {

		public static LinkName matchLinkName() {
			LinkName instance = new LinkName();
			instance.setSectionFinder(new RegexSectionFinder(
					"^\\s*\\[\\s*([^\\n\\r]+?)\\s*\\|", 0, 1));
			return instance;
		}

		public static LinkName matchStaticText() {
			LinkName instance = new LinkName();
			instance.setSectionFinder(new ConstraintSectionFinder(
					new RegexSectionFinder("^\\s*([^\\n\\r]+?)\\s*$", 0, 1),
					new HasChildrenOfTypeConstraint(ListMarks.class),
					new NoChildrenOfTypeConstraint(KeywordType.class)
			));
			return instance;
		}
	}

	private static class ArticleReference extends AbstractType {

		public ArticleReference() {
			setSectionFinder(new RegexSectionFinder("[^#\\s][^#@\\n\\r]+"));
		}
	}

	private static class HeaderReference extends AbstractType {

		public HeaderReference() {
			setSectionFinder(new RegexSectionFinder("#([^\\n\\r]+)", 0, 1));
		}
	}

	private static class NamedSectionReference extends AbstractType {

		public NamedSectionReference() {
			setSectionFinder(new RegexSectionFinder("@([^\\n\\r]+)", 0, 1));
		}
	}

	private static class ListMarks extends AbstractType {

		public ListMarks() {
			setSectionFinder(new ConstraintSectionFinder(
					new RegexSectionFinder("^[#*\\-]+"),
					SingleChildConstraint.getInstance()));
		}
	}

	private static final String TARGET_SECTION_ID_KEY = "includedSectionID";

	public InnerWikiReference() {
		// matches everything begins with a
		// non-whitespace, non-enumeration char,
		// is on one line and end with some printable character
		// (after the optional list marks)
		setSectionFinder(new RegexSectionFinder(
				"^[ \t\u00A0]*([#*\\-]+[^\\n\\r]*)?[^#*\\-\\s\\n\\r][^\\n\\r]*[^\\s\\n\\r]",
				Pattern.MULTILINE));
		setRenderer(new InterWikiReferenceRenderer());

		// grab list marks
		addChildType(new Indent());
		addChildType(new ListMarks());

		// find link name
		addChildType(LinkName.matchLinkName());
		addChildType(new KeywordType(Pattern.compile("\\s*[\\[\\]\\|]\\s*")));
		addChildType(LinkName.matchStaticText());

		// grap all link characters with whitespaces
		addChildType(new HeaderReference());
		addChildType(new NamedSectionReference());
		addChildType(new ArticleReference());
	}

	/**
	 * Get the section referenced by this include line, or null if there is no
	 * such section due to a malicious reference. Please note that
	 * {@link #updateReferences(Section)} must be called first to check the wiki
	 * for the reference and update the link if the wiki has been changed.
	 *
	 * @param section this include section to take the reference information
	 *                from
	 * @return the referenced Section
	 * @created 05.02.2014
	 */
	public Section<?> getReferencedSection(Section<InnerWikiReference> section) {
		String id = (String) KnowWEUtils.getStoredObject(section, TARGET_SECTION_ID_KEY);
		if (id == null) {
			// not initialized yet
			// or is has not been found previously
			// initialize it and return
			return updateReferences(section);
		}

		// check for stored section
		Section<?> target = Sections.get(id);
		if (target != null) return target;

		// if stored id is deprecated, refresh it
		return updateReferences(section);
	}

	/**
	 * return a list of all included sections. If the include line used header
	 * suppression, but including a specific header, the header section is also
	 * not part of the list.
	 *
	 * @param section the section defines the include
	 * @return the list of sections to be included
	 * @created 12.02.2014
	 */
	public List<Section<?>> getIncludedSections(Section<InnerWikiReference> section) {
		return getIncludedSections(section, false);
	}

	/**
	 * return a list of all included sections. If the include line used header
	 * suppression, but including a specific header, the header section is also
	 * not part of the list.
	 *
	 * @param section the section defines the include
	 * @return the list of sections to be included
	 * @created 12.02.2014
	 */
	public List<Section<? extends de.knowwe.core.kdom.Type>> getIncludedSections(Section<InnerWikiReference> section, boolean forceSkipHeader) {
		Section<?> targetSection = getReferencedSection(section);
		if (targetSection == null) return Collections.emptyList();

		List<Section<? extends de.knowwe.core.kdom.Type>> result = new ArrayList<>();
		boolean isHeader = targetSection.get() instanceof HeaderType;
		if (!isHeader || (!forceSkipHeader && !isSuppressHeader(section))) {
			result.add(targetSection);
		}
		if (isHeader) {
			Section<HeaderType> header = Sections.cast(targetSection, HeaderType.class);
			result.addAll(JSPWikiMarkupUtils.getContent(header));
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Returns the number of header marks of the most significant heading of the
	 * included sections. It return 0 if no header is included.
	 *
	 * @param section the section to be examined
	 * @return the highest number of header marks of any included header
	 * @created 11.02.2014
	 */
	public int getMaxHeaderMarkCount(Section<InnerWikiReference> section) {
		List<Section<?>> included = getIncludedSections(section);
		int marks = 0;
		for (Section<HeaderType> header : Sections.successors(included, HeaderType.class)) {
			marks = Math.max(marks, header.get().getMarkerCount());
		}
		return marks;
	}

	private synchronized Section<?> findReferencedSection(Section<InnerWikiReference> section) {

		// prepare sub-sections
		Section<ArticleReference> articleReference = getArticleReference(section);
		Section<HeaderReference> headerReference = getHeaderReference(section);
		Section<NamedSectionReference> namedSectionReference = getNamedSectionReference(section);

		// prepare header and article names
		// and clear all messages and section store
		String targetArticleName = "";
		String targetHeaderName = "";
		String namedSectionName = "";
		if (articleReference != null) {
			targetArticleName = articleReference.getText();
		}
		if (headerReference != null) {
			targetHeaderName = headerReference.getText();
		}
		if (namedSectionReference != null) {
			namedSectionName = namedSectionReference.getText();
		}

		// we do not have an error if we use static text
		if (getLink(section) == null && getListMarks(section).length() > 0) {
			return null;
		}

		// warning if no article specified
		if (Strings.isBlank(targetArticleName)) {
			Messages.storeMessage(section, getClass(), Messages.error("No article name specified"));
			return null;
		}

		Article targetArticle = section.getArticleManager().getArticle(targetArticleName);

		// warning if article not found
		if (targetArticle == null) {
			Messages.storeMessage(articleReference, getClass(), Messages.error("Article '" + targetArticleName + "' not found!"));
			return null;
		}

		// find section to be rendered
		// if no specific section, use article's root section
		if (Strings.isBlank(targetHeaderName) && Strings.isBlank(namedSectionName)) {
			return targetArticle.getRootSection();
		}
		else if (!Strings.isBlank(targetHeaderName)) {
			// search for the header sections
			for (Section<HeaderType> header : Sections.successors(targetArticle, HeaderType.class)) {
				String text = header.get().getHeaderText(header);
				if (text.equalsIgnoreCase(targetHeaderName)) {
					return header;
				}
			}
			// and for the definition sections
			for (Section<DefinitionType> def : Sections.successors(targetArticle, DefinitionType.class)) {
				String text = def.get().getHeadText(def);
				if (text.equalsIgnoreCase(targetHeaderName)) {
					return def;
				}
			}
			Messages.storeMessage(headerReference, getClass(), Messages.error("Header '" + targetHeaderName + "' not found!"));
		}
		else if (!Strings.isBlank(namedSectionName)) {
			// search for the named section
			Sections<DefaultMarkupType> namedSections = $(targetArticle).successor(DefaultMarkupType.class)
					.filter(markupSection -> DefaultMarkupType.getAnnotation(markupSection, "name") != null);
			if (!namedSections.isEmpty()) {
				return namedSections.getFirst();
			}
			Messages.storeMessage(headerReference, getClass(), Messages.error("Name '" + namedSectionName + "' not found!"));
		}
		Messages.storeMessage(headerReference, getClass(), Messages.error("Reference for '" + section.getText() + "' not found!"));
		return null;
	}

	private Section<NamedSectionReference> getNamedSectionReference(Section<InnerWikiReference> section) {
		return $(section).successor(NamedSectionReference.class).getFirst();
	}

	private static Section<HeaderReference> getHeaderReference(Section<?> section) {
		return Sections.successor(section, HeaderReference.class);
	}

	private static Section<ArticleReference> getArticleReference(Section<?> section) {
		return Sections.successor(section, ArticleReference.class);
	}

	public synchronized Section<?> updateReferences(Section<InnerWikiReference> section) {
		Section<?> targetSection = findReferencedSection(section);
		// we store the section id
		// to easily recognize if the section has disappeared
		String targetID = targetSection == null ? null : targetSection.getID();
		KnowWEUtils.storeObject(section, TARGET_SECTION_ID_KEY, targetID);
		return targetSection;
	}

	private class InterWikiReferenceRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			Section<InnerWikiReference> innerRef = Sections.cast(section, InnerWikiReference.class);

			// add break before first on
			if (Sections.successor(innerRef.getParent(), InnerWikiReference.class) == innerRef) {
				result.append("\n");
			}

			boolean suppressed = innerRef.get().isSuppressHeader(innerRef);
			String cssName = suppressed ? "include-suppressedHeader" : "include-allowHeader";
			Section<ListMarks> marks = Sections.successor(innerRef, ListMarks.class);
			if (marks != null) {
				String mark = marks.getText();
				result.append(mark.replace('-', '*')).append(" ");
			}

			result.appendHtml("<span class='").append(cssName).appendHtml("'>");
			renderLine(innerRef, user, result);
			result.appendHtml("</span><br>");
		}

		private void renderLine(Section<InnerWikiReference> innerRef, UserContext user, RenderResult result) {
			// if a header is specified, but it has an error
			// then we know the article is ok, and therefore render differently
			Section<HeaderReference> headerReference = getHeaderReference(innerRef);
			if (headerReference != null) {
				if (Messages.hasMessages(headerReference, Type.ERROR, Type.WARNING)) {
					Section<ArticleReference> articleReference = getArticleReference(innerRef);
					result.append("[");
					result.append(articleReference.getText());
					result.append("]#");
					DelegateRenderer.getInstance().renderSubSection(headerReference, user, result);
					return;
				}
			}
			// render as static text if there is no link defined
			String link = getLink(innerRef);
			if (link == null) {
				result.append(getLinkName(innerRef));
				return;
			}
			// otherwise use normal wiki rendering
			result.append("[");
			result.append(getLinkName(innerRef));
			result.append("|");
			result.append(link);
			result.append("]");
		}
	}

	/**
	 * Returns if the header of the section shall be hidden. If this method
	 * return "true", the eventually referenced header section will
	 * automatically be removed from the list returned by
	 *
	 * @param section the section to check the header suppression for
	 * @return if the header shall not be shown
	 * @created 12.02.2014
	 */
	public boolean isSuppressHeader(Section<InnerWikiReference> section) {
		return getListMarks(section).endsWith("-");
	}

	/**
	 * Returns if the numbering of the header of the section shall be hidden.
	 *
	 * @param section the section to check the numbering suppression for
	 * @return if the header's numbering shall not be shown
	 * @created 12.02.2014
	 */
	public boolean isSuppressNumbering(Section<InnerWikiReference> section) {
		return getListMarks(section).endsWith("*");
	}

	public String getLinkName(Section<InnerWikiReference> section) {
		// if a name is specified use the name
		Section<LinkName> nameSection = Sections.successor(section, LinkName.class);
		if (nameSection != null) return nameSection.getText();
		// if a specific header is included, use the name of the header
		Section<HeaderReference> headerReference = getHeaderReference(section);
		if (headerReference != null) return headerReference.getText();
		// otherwise use article link
		Section<ArticleReference> articleReference = getArticleReference(section);
		if (articleReference != null) return articleReference.getText();
		return section.getText();
	}

	public String getLink(Section<InnerWikiReference> section) {
		Section<ArticleReference> articleReference = getArticleReference(section);
		Section<HeaderReference> headerReference = getHeaderReference(section);
		if (articleReference == null && headerReference == null) return null;
		String article = (articleReference == null)
				? section.getTitle()
				: articleReference.getText();
		return headerReference == null
				? article
				: article + "#" + headerReference.getText();
	}

	/**
	 * Returns the level of the preceding enumeration marks or bullet marks ('#'
	 * or '*') or "" if no such marks were specified.
	 *
	 * @param reference the inner wiki reference to get the preceding marks for
	 * @return the marks preceding the include link
	 * @created 11.02.2014
	 */
	public String getListMarks(Section<InnerWikiReference> reference) {
		Section<ListMarks> marks = Sections.successor(reference, ListMarks.class);
		if (marks == null) return "";
		return marks.getText();
	}

}
