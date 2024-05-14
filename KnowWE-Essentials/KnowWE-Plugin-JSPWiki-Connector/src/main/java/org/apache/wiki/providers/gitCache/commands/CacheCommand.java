package org.apache.wiki.providers.gitCache.commands;

import java.io.File;
import java.util.Objects;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 13.12.19
 */
public class CacheCommand {

	public final Page page;

	CacheCommand(Page page) {
		this.page = page;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CacheCommand that)) return false;
		if (that instanceof MoveAttachment) {
			if (this instanceof AddAttachmentVersion || this instanceof DeleteAttachmentVersion) {
				return page.getName().equals(that.page.getName()) ||
						((Attachment) this.page).getParentName().equals(((MoveAttachment) that).newParent);
			}
		}
		if (that instanceof MovePage) {
			return page.getName().equals(that.page.getName()) ||
					page.getName().equals(((MovePage) that).to);
		}
		return page.getName().equals(that.page.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(page);
	}

	public static class AddPageVersion extends CacheCommand {
		public AddPageVersion(Page page) {
			super(page);
		}
	}

	public static class DeletePageVersion extends CacheCommand {
		public DeletePageVersion(Page page) {
			super(page);
		}
	}

	public static class AddAttachmentVersion extends CacheCommand {

		public AddAttachmentVersion(Attachment att) {
			super(att);
		}
	}

	public static class DeleteAttachmentVersion extends CacheCommand {
		public DeleteAttachmentVersion(Attachment att) {
			super(att);
		}
	}

	public static class MovePage extends CacheCommand {
		public final String to;

		public MovePage(Page from, String to) {
			super(from);
			this.to = to;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof CacheCommand that)) return false;
			return page.getName().equals(that.page.getName()) ||
					this.to.equals(that.page.getName());
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), to);
		}
	}

	public static class MoveAttachment extends CacheCommand {
		public final String newParent;
		public final File file;

		public MoveAttachment(Page oldParent, String newParent, File file) {
			super(oldParent);
			this.newParent = newParent;
			this.file = file;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof CacheCommand that)) return false;
			if (that instanceof AddAttachmentVersion || that instanceof DeleteAttachmentVersion) {
				Attachment att = (Attachment) that.page;
				return page.getName().equals(att.getParentName()) && file.getName().equals(att.getFileName());
			}
			else if (that instanceof MoveAttachment) {
				return page.getName().equals(that.page.getName()) && file.getName()
						.equals(((MoveAttachment) that).file.getName());
			}
			else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(page);
		}
	}
}
