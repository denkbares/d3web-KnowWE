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
import de.knowwe.core.compile.Compiler;
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
import de.knowwe.jspwiki.types.HeaderType;

/**
 * KDOM type that references an article or a headed section of an article.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 05.02.2014
 */
public class InnerWikiReference extends AbstractType {

	private static class LinkName extends AbstractType {

		public LinkName() {
			setSectionFinder(new RegexSectionFinder("\\[\\s*([^\\n\\r]+?)\\s*\\|", 0, 1));
		}
	}

	private static class ArticleReference extends AbstractType {

		public ArticleReference() {
			setSectionFinder(new RegexSectionFinder("[^#\\n\\r]+"));
		}
	}

	private static class HeaderReference extends AbstractType {

		public HeaderReference() {
			setSectionFinder(new RegexSectionFinder("#([^\\n\\r]+)", 0, 1));
		}
	}

	private static final String TARGET_SECTION_ID_KEY = "includedSectionID";

	public InnerWikiReference() {
		// matches everything begins with a
		// non-whitespace, non-enumeration char,
		// is on one line and end with some printable character
		setSectionFinder(new RegexSectionFinder("[^#*\\s\\n\\r][^\\n\\r]*[^\\s\\n\\r]"));
		setRenderer(new InterWikiReferenceRenderer());
		// find link name
		addChildType(new LinkName());
		// grap all link characters with whitespaces
		addChildType(new KeywordType(Pattern.compile("\\s*[\\[\\]\\|]\\s*")));
		addChildType(new HeaderReference());
		addChildType(new ArticleReference());
	}

	/**
	 * Get the section referenced by this include line, or null if there is no
	 * such section due to a malicious reference. Please note that
	 * {@link #updateReferences(Section)} must be called first to check the wiki
	 * for the reference and update the link if the wiki has been changed.
	 * 
	 * @created 05.02.2014
	 * @param user the user context to search the section for
	 * @param section this include section to take the reference information
	 *        from
	 * @return the referenced Section
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
		Section<?> target = Sections.getSection(id);
		if (target != null) return target;

		// if stored id is deprecated, refresh it
		return updateReferences(section);
	}

	public List<Section<?>> getIncludedSections(Section<InnerWikiReference> section) {
		Section<?> targetSection = getReferencedSection(section);
		if (targetSection == null) return Collections.emptyList();

		List<Section<?>> result = new ArrayList<Section<?>>();
		result.add(targetSection);
		if (targetSection.get() instanceof HeaderType) {
			Section<HeaderType> header = Sections.cast(targetSection, HeaderType.class);
			result.addAll(JSPWikiMarkupUtils.getContent(header));
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Returns the number of header marks of the most significant heading of the
	 * included sections. It return 0 if no header is included.
	 * 
	 * @created 11.02.2014
	 * @param sections the list of sections to be examined
	 * @return the highest number of header marks of any included header
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

		Class<?> source = getClass();
		Compiler compiler = null;

		// prepare sub-sections
		Section<ArticleReference> articleReference = getArticleReference(section);
		Section<HeaderReference> headerReference = getHeaderReference(section);

		// prepare header and article names
		// and clear all messages and section store
		String targetArticleName = "";
		String targetHeaderName = "";
		if (articleReference != null) {
			targetArticleName = articleReference.getText();
			Messages.clearMessages(compiler, articleReference, source);
		}
		if (headerReference != null) {
			targetHeaderName = headerReference.getText();
			Messages.clearMessages(compiler, headerReference, source);
		}
		Messages.clearMessages(compiler, section, source);

		// warning if not article specified
		if (Strings.isBlank(targetArticleName)) {
			Messages.storeMessage(compiler, section, source,
					Messages.error("No article name specified"));
			return null;
		}

		Article targetArticle = section.getArticleManager().getArticle(targetArticleName);

		// warning if article not found
		if (targetArticle == null) {
			Messages.storeMessage(compiler, articleReference, source,
					Messages.error("Article '" + targetArticleName + "' not found!"));
			return null;
		}

		// find section to be rendered
		// if no specific section, use article's root section
		if (Strings.isBlank(targetHeaderName)) {
			return targetArticle.getRootSection();
		}

		// otherwise search for the section
		for (Section<HeaderType> header : Sections.successors(targetArticle, HeaderType.class)) {
			String text = header.get().getHeaderText(header);
			if (text.equalsIgnoreCase(targetHeaderName)) {
				return header;
			}
		}
		Messages.storeMessage(compiler, headerReference, source,
				Messages.error("Header '" + targetHeaderName + "' not found!"));
		return null;
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
			// if a header is specified, but it has an error
			// then we know the article is ok, and therefore render differently
			Section<HeaderReference> headerReference = getHeaderReference(section);
			if (headerReference != null) {
				if (Messages.hasMessages(headerReference, Type.ERROR, Type.WARNING)) {
					Section<ArticleReference> articleReference = getArticleReference(section);
					result.append("[");
					result.append(articleReference.getText());
					result.append("]#");
					DelegateRenderer.getInstance().renderSubSection(headerReference, user, result);
					return;
				}
			}
			// otherwise use normal wiki rendering
			result.append("[");
			result.append(getLinkName(section));
			result.append("|");
			result.append(getLink(section));
			result.append("]");
		}
	}

	public String getLinkName(Section<?> section) {
		Section<LinkName> nameSection = Sections.successor(section, LinkName.class);
		if (nameSection != null) return nameSection.getText();
		return getLink(section);
	}

	public String getLink(Section<?> section) {
		Section<ArticleReference> articleReference = getArticleReference(section);
		Section<HeaderReference> headerReference = getHeaderReference(section);
		return headerReference == null
				? articleReference.getText()
				: articleReference.getText() + "#" + headerReference.getText();
	}

	/**
	 * Returns the level of the preceding enumeration marks or bullet marks ('#'
	 * or '*') or "" if no such marks were specified.
	 * 
	 * @created 11.02.2014
	 * @param reference the inner wiki reference to get the preceding marks for
	 * @return the marks preceding the include link
	 */
	public String getListMarks(Section<InnerWikiReference> reference) {
		String text = reference.getParent().getText();
		int end = reference.getOffsetInParent();
		int start = text.lastIndexOf("\n", end) + 1;
		return Strings.trim(text.substring(start, end));
	}
}
