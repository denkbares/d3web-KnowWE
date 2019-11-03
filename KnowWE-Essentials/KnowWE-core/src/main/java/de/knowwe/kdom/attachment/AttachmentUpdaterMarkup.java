package de.knowwe.kdom.attachment;

import de.knowwe.kdom.defaultMarkup.DefaultMarkup;

/**
 * This is a clone of the {@link AttachmentMarkup} only here for backwards compatibility.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 22.06.16
 */
@Deprecated
public class AttachmentUpdaterMarkup extends AttachmentMarkup {

	private static final DefaultMarkup MARKUP = AttachmentMarkup.MARKUP.copy();

	static {
		MARKUP.setDeprecated(MARKUP.getName());
		MARKUP.setName("AttachmentUpdater");
	}

	public AttachmentUpdaterMarkup() {
		super(MARKUP);
	}

}
