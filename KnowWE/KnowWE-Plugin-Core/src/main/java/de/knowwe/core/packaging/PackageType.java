package de.knowwe.core.packaging;

import java.util.Collection;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageTermReference;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class PackageType extends DefaultMarkupType {
	
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Package");
		MARKUP.addContentType(new PackageTermReference());
	}
	
	public PackageType() {
		super(MARKUP);
		this.setIgnorePackageCompile(true);
		this.addSubtreeHandler(Priority.PRECOMPILE_HIGH, new SetDefaultPackageHandler());
		this.childrenTypes.add(new PackageMarkupType());
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

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm>
			section, String oldValue, String replacement) {
		Section<PackageMarkupType> packageDefinition =
				Sections.findSuccessor(section,
						PackageMarkupType.class);
		return packageDefinition.getText() + " " + replacement + "\n";
	}

}
