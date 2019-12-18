package org.apache.wiki.providers;

import java.io.File;

import org.apache.wiki.WikiPage;
import org.apache.wiki.attachment.Attachment;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 13.12.19
 */
public class CacheCommand {

	public static class AddPageVersion extends CacheCommand {
		final WikiPage page;

		AddPageVersion(WikiPage page) {
			this.page = page;
		}
	}

	public static class DeletePageVersion extends CacheCommand {
		final WikiPage page;

		DeletePageVersion(WikiPage page) {
			this.page = page;
		}
	}

	public static class AddAttachmentVersion extends CacheCommand {
		final Attachment att;

		AddAttachmentVersion(Attachment att) {
			this.att = att;
		}
	}

	public static class DeleteAttachmentVersion extends CacheCommand {
		final Attachment att;

		DeleteAttachmentVersion(Attachment att) {
			this.att = att;
		}
	}

	public static class MovePage extends CacheCommand {
		final WikiPage from;
		final String to;

		MovePage(WikiPage from, String to) {
			this.from = from;
			this.to = to;
		}
	}

	public static class MoveAttachment extends CacheCommand {
		final String oldParent;
		final String newParent;
		final File file;

		MoveAttachment(String oldParent, String newParent, File file) {
			this.oldParent = oldParent;
			this.newParent = newParent;
			this.file = file;
		}
	}
}
