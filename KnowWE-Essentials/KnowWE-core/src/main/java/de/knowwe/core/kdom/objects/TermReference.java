/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.kdom.objects;

import java.util.Set;
import java.util.stream.Collectors;

import com.denkbares.strings.Identifier;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.AttachmentManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TermCompiler.ReferenceValidationMode;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Interface for term references.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.03.2013
 */
public interface TermReference extends IncrementalTerm {

	default <C extends TermCompiler, T extends TermReference> ReferenceValidationMode getReferenceValidationMode(C compiler, Section<T> section) {
		if (KnowWEUtils.isAttachmentArticle(section.getArticle())) {
			ArticleManager articleManager = section.getArticleManager();
			if (articleManager instanceof DefaultArticleManager defaultArticleManager) {
				AttachmentManager attachmentManager = defaultArticleManager.getAttachmentManager();
				Set<Section<AttachmentCompileType>> compilingAttachmentSections = attachmentManager.getCompilingAttachmentSections(section.getArticle());
				Set<ReferenceValidationMode> modes = $(compilingAttachmentSections).map(s -> s.get()
						.getReferenceValidationMode(s)).collect(Collectors.toSet());
				if (modes.size() == 1) return modes.iterator().next();
			}
		}
		return compiler.getReferenceValidationMode();
	}

	default <C extends TermCompiler, T extends TermReference> boolean isDefinedTerm(C compiler, Section<T> section) {
		Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
		return compiler.getTerminologyManager().isDefinedTerm(termIdentifier);
	}
}
