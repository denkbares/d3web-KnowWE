package org.apache.wiki.providers;

import java.io.File;
import java.util.Objects;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.attachment.Attachment;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 13.12.19
 */
class CacheCommand {

	final Page page;

	CacheCommand(Page page) {
		this.page = page;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof CacheCommand)) return false;
		CacheCommand that = (CacheCommand) o;
		if(that instanceof MoveAttachment) {
			if (this instanceof AddAttachmentVersion || this instanceof DeleteAttachmentVersion) {
				return page.getName().equals(that.page.getName()) ||
						((Attachment)this.page).getParentName().equals(((MoveAttachment) that).newParent);
			}
		}
		if(that instanceof MovePage){
				return page.getName().equals(that.page.getName()) ||
						page.getName().equals(((MovePage) that).to);
		}
		return page.getName().equals(that.page.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(page);
	}

	static class AddPageVersion extends CacheCommand {
		AddPageVersion(Page page) {
			super(page);
		}
	}

	static class DeletePageVersion extends CacheCommand {
		DeletePageVersion(Page page) {
			super(page);
		}
	}

	static class AddAttachmentVersion extends CacheCommand {

		AddAttachmentVersion(Attachment att) {
			super(att);
		}
	}

	static class DeleteAttachmentVersion extends CacheCommand {
		DeleteAttachmentVersion(Attachment att) {
			super(att);
		}
	}

	static class MovePage extends CacheCommand {
		final String to;

		MovePage(Page from, String to) {
			super(from);
			this.to = to;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof CacheCommand)) return false;
			CacheCommand that = (CacheCommand) o;
			return page.getName().equals(that.page.getName()) ||
					this.to.equals(that.page.getName());

		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), to);
		}
	}

	static class MoveAttachment extends CacheCommand {
		final String newParent;
		final File file;

		MoveAttachment(WikiPage oldParent, String newParent, File file) {
			super(oldParent);
			this.newParent = newParent;
			this.file = file;
		}
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof CacheCommand)) return false;
			CacheCommand that = (CacheCommand) o;
			if(that instanceof AddAttachmentVersion || that instanceof DeleteAttachmentVersion){
				Attachment att = (Attachment) that.page;
				return page.getName().equals(att.getParentName()) && file.getName().equals(att.getFileName());
			} else if (that instanceof MoveAttachment){
				return page.getName().equals(that.page.getName()) && file.getName().equals(((MoveAttachment) that).file.getName());
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
