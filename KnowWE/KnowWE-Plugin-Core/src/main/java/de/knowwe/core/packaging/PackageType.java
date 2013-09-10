package de.knowwe.core.packaging;

import java.util.Collection;

import de.d3web.strings.Identifier;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageTermDefinition;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupTermReferenceRegisterHandler;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class PackageType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Package");
		MARKUP.addContentType(new PackageTermDefinition(false));
	}

	public PackageType() {
		super(MARKUP);
		addSubtreeHandler(Priority.PRECOMPILE_HIGH, new SetDefaultPackageHandler());
		addSubtreeHandler(Priority.HIGHEST, new RegisterPackageDefinitionHandler());
		removeSubtreeHandler(DefaultMarkupTermReferenceRegisterHandler.class);
		addChildType(new PackageMarkupType());
	}

	private static class RegisterPackageDefinitionHandler extends SubtreeHandler<PackageType> {

		public Collection<Message> create(Article article, Section<PackageType> section) {
			String defaultPackage = DefaultMarkupType.getContent(section).trim();
			KnowWEUtils.getTerminologyManager(article).registerTermDefinition(section,
					Package.class, new Identifier(defaultPackage));
			return Messages.noMessage();
		}
	}

	private static class SetDefaultPackageHandler extends SubtreeHandler<PackageType> {

		public SetDefaultPackageHandler() {
			super(true);
		}

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
}
