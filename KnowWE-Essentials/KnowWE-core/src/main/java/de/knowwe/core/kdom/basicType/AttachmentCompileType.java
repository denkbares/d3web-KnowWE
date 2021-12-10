/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.basicType;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.wikiConnector.WikiAttachment;

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
}
