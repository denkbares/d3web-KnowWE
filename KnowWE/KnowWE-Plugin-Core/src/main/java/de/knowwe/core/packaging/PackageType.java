package de.knowwe.core.packaging;

import java.util.Collection;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.ContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.StyleRenderer;

public class PackageType extends DefaultMarkupType {

	public PackageType() {
		super(new DefaultMarkup("Package"));
		this.setIgnorePackageCompile(true);
		this.addSubtreeHandler(Priority.PRECOMPILE_HIGH, new SetDefaultPackageHandler());
		this.setRenderer(new PackageTypeRenderer());
	}

	private static class SetDefaultPackageHandler extends SubtreeHandler<PackageType> {

		@Override
		public Collection<Message> create(Article article, Section<PackageType> section) {
			String defaultPackage = DefaultMarkupType.getContent(section).trim();
			if (!defaultPackage.isEmpty()) {
				Environment.getInstance().getPackageManager(
						article.getWeb()).addDefaultPackage(
						article, defaultPackage);
			}
			return Messages.noMessage();
		}

	}

	private static class PackageTypeRenderer extends DefaultMarkupRenderer {

		public PackageTypeRenderer() {
			super("KnowWEExtension/images/package_obj24.gif");
		}

		@Override
		protected void renderContents(Section<?> section, UserContext user, StringBuilder string) {
			Section<? extends ContentType> contentSection = DefaultMarkupType.getContentSection(section);
			StyleRenderer.PACKAGE.render(contentSection, user, string);
		}

	}

}
