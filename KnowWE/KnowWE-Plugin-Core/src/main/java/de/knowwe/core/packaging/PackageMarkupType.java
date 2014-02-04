package de.knowwe.core.packaging;

import java.util.Collection;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageRegistrationScript;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageTermReferenceRegistrationHandler;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class PackageMarkupType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Package");
		MARKUP.addContentType(new PackageTerm());
	}

	public PackageMarkupType() {
		super(MARKUP);
		addCompileScript(Priority.HIGHEST, new SetDefaultPackageHandler());
		addCompileScript(Priority.LOWEST, new RemoveDefaultPackageHandler());
		addCompileScript(Priority.HIGH, new PackageTermDefinitionRegistrationHandler());
		removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageTermReferenceRegistrationHandler.class);
		removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageRegistrationScript.class);
		setRenderer(new PackageMarkupRenderer());
		addChildType(new PackageType());
	}

	private static class PackageMarkupRenderer extends DefaultMarkupRenderer {

		@Override
		protected void renderCompileWarning(Section<?> section, RenderResult string) {

			String defaultPackage = DefaultMarkupType.getContent(section).trim();
			Collection<Section<? extends PackageCompileType>> compileSections = KnowWEUtils.getPackageManager(
					section).getCompileSections(defaultPackage);

			// add warning if section is not compiled
			if (compileSections.isEmpty()) {
				String warningString = "The package '" + defaultPackage
						+ "' is not used to compile any knowledge.";
				renderMessagesOfType(Type.WARNING,
						Messages.asList(Messages.warning(warningString)),
						string);
			}
		}
	}

	private static class PackageTermDefinitionRegistrationHandler extends PackageRegistrationScript<PackageMarkupType> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageMarkupType> section) {

			String defaultPackage = DefaultMarkupType.getContent(section).trim();
			compiler.getTerminologyManager().registerTermDefinition(compiler,
					section, Package.class, new Identifier(defaultPackage));
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageMarkupType> section) {
			String defaultPackage = DefaultMarkupType.getContent(section).trim();
			compiler.getTerminologyManager().unregisterTermDefinition(compiler,
					section, Package.class, new Identifier(defaultPackage));
		}
	}

	private static class SetDefaultPackageHandler extends PackageRegistrationScript<PackageMarkupType> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageMarkupType> section) {
			String defaultPackage = DefaultMarkupType.getContent(section).trim();
			if (!defaultPackage.isEmpty()) {
				KnowWEUtils.getPackageManager(section).addDefaultPackage(
						section.getArticle(), defaultPackage);
			}
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageMarkupType> section) {

		}
	}

	private static class RemoveDefaultPackageHandler extends PackageRegistrationScript<PackageMarkupType> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageMarkupType> section) {

		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageMarkupType> section) {
			String defaultPackage = DefaultMarkupType.getContent(section).trim();
			KnowWEUtils.getPackageManager(section).removeDefaultPackage(
					section.getArticle(), defaultPackage);
		}
	}
}
