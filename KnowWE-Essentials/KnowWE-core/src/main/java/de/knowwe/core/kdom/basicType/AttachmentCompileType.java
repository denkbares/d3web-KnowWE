/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.basicType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.util.Icon;

/**
 * Interface for section types that specify that an attachment on the same article is compiled
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 09.12.21
 */
public interface AttachmentCompileType extends Type {

	/**
	 * Provides the attachment that is specified to be compiled by the given section. If no attachment is compiled, null
	 * will be returned.
	 *
	 * @param section the section of the type for which to get the attachment
	 * @return the compiled attachment or null, if no attachment is compiled at the moment
	 */
	@Nullable
	WikiAttachment getCompiledAttachment(Section<? extends AttachmentCompileType> section) throws IOException;

	/**
	 * Provides the path to the attachment that is specified to be compiled by the given section, if available. If no
	 * attachment is specified to be compiled, null will be returned.
	 *
	 * @param section the section of the type for which to get the attachment
	 * @return the path to attachment we want to compile or null, if non should be compiled
	 */
	@Nullable
	String getCompiledAttachmentPath(Section<? extends AttachmentCompileType> section);

	/**
	 * Provides the paths of all attachments that are specified to be compiled by the given section. Most types compile
	 * one attachment at most, so the default returns the path of {@link #getCompiledAttachmentPath(Section)}. Types
	 * that compile several attachments (e.g. one per source of an import) return all of them here.
	 * <p>
	 * The paths may depend on other pages, e.g. on variable definitions. Such a type must only use information that
	 * is available as soon as the other page is registered (not compiled), and whoever provides that information has
	 * to call {@link de.knowwe.core.AttachmentManager#refreshCompiledAttachmentPaths()} after it changed.
	 *
	 * @param section the section of the type for which to get the attachment paths
	 * @return the paths of the attachments we want to compile, empty if none should be compiled
	 */
	@NotNull
	default Collection<String> getCompiledAttachmentPaths(Section<? extends AttachmentCompileType> section) {
		String path = getCompiledAttachmentPath(section);
		return path == null ? List.of() : List.of(path);
	}

	/**
	 * Provides the sections that additionally decide which compilers compile the attachment at the given path. If the
	 * result is not null, the content of the attachment is only compiled by compilers that compile both the given
	 * section and at least one of the returned sections. An empty result means that no compiler compiles the
	 * attachment. By default, there is no such condition (null) and the content is compiled like the given section.
	 * <p>
	 * This allows a section that compiles several attachments to assign each of them to different compilers, e.g. one
	 * attachment per variant.
	 *
	 * @param section the section of the type compiling the attachment
	 * @param path    the path of one of the attachments compiled by the section
	 * @return the sections of which one has to be compiled together with the given section, or null if there is no
	 * such condition
	 */
	@Nullable
	default Collection<? extends Section<?>> getCompileConditionSections(Section<? extends AttachmentCompileType> section, String path) {
		return null;
	}

	/**
	 * Check whether this section is compiling its attachment or not (may be inactive at the moment)
	 *
	 * @param section the section to check
	 * @return true if the attachment is compiled, false otherwise
	 */
	boolean isCompilingTheAttachment(Section<? extends AttachmentCompileType> section);

	/**
	 * Get the reference validation mode defined for this attachment compile type. Default is 'error'.
	 *
	 * @param section the section to check
	 * @return the mode
	 */
	default TermCompiler.ReferenceValidationMode getReferenceValidationMode(Section<? extends AttachmentCompileType> section) {
		return TermCompiler.ReferenceValidationMode.error;
	}

	/**
	 * Get the icon indicating that some content was imported by the given section, e.g. in the lists of sections.
	 *
	 * @param section the section importing the content
	 * @return the icon of the import
	 */
	default Icon getImportIcon(Section<? extends AttachmentCompileType> section) {
		return Icon.ATTACHMENT;
	}

	/**
	 * Get the articles of all compiled attachments of the given section, see
	 * {@link #getCompiledAttachmentPaths(Section)}. Use this method instead of
	 * {@link #getCompiledAttachmentArticle(Section)} wherever the content compiled by a section is collected: a section
	 * may compile several attachments, e.g. an import one per variant. Which compilers compile an article is decided
	 * by the packages of its content, as for any other article.
	 *
	 * @param section the section for which to get the attachment articles
	 * @return the articles of the compiled attachments, empty if no attachment is compiled (yet)
	 */
	@NotNull
	default List<Article> getCompiledAttachmentArticles(Section<? extends AttachmentCompileType> section) {
		if (!isCompilingTheAttachment(section)) return List.of();
		ArticleManager articleManager = section.getArticleManager();
		if (articleManager == null) return List.of();
		List<Article> articles = new ArrayList<>();
		for (String path : getCompiledAttachmentPaths(section)) {
			Article article = articleManager.getArticle(path);
			if (article != null) articles.add(article);
		}
		return articles;
	}

	/**
	 * Get the article of the compiled attachment of the given section, if it compiles exactly one. For a section
	 * compiling several attachments null is returned, use {@link #getCompiledAttachmentArticles(Section)} then.
	 *
	 * @param section the section for which to check the attachment article
	 * @return the article of the compiled attachment or null, if no attachment is compiled
	 */
	@Nullable
	default Article getCompiledAttachmentArticle(Section<? extends AttachmentCompileType> section) {
		if (!isCompilingTheAttachment(section)) {
			return null;
		}
		ArticleManager articleManager = section.getArticleManager();
		if (articleManager == null) return null;
		return articleManager.getArticle(getCompiledAttachmentPath(section));
	}
}
