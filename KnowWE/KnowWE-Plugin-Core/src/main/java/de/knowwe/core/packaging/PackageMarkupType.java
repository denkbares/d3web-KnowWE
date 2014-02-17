package de.knowwe.core.packaging;

import java.util.Collection;
import java.util.List;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageTerm;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageRegistrationScript;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageTermReferenceRegistrationHandler;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.sectionFinder.LineSectionFinder;

public class PackageMarkupType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Package");
		MARKUP.addContentType(new PackageTermLine());
	}

	public PackageMarkupType() {
		super(MARKUP);
		removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageTermReferenceRegistrationHandler.class);
		removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageRegistrationScript.class);
		setRenderer(new PackageMarkupRenderer());
	}

	private static class PackageTermLine extends AbstractType {

		public PackageTermLine() {
			this.setSectionFinder(new LineSectionFinder());
			PackageTerm packageTerm = new PackageTerm();
			packageTerm.addCompileScript(Priority.HIGHEST, new SetDefaultPackageHandler());
			packageTerm.addCompileScript(Priority.LOWEST, new RemoveDefaultPackageHandler());
			packageTerm.addCompileScript(Priority.HIGH, new PackageTermDefinitionRegistrationHandler());
			addChildType(packageTerm);
		}


	}

	private static class PackageMarkupRenderer extends DefaultMarkupRenderer {

		@Override
		protected void renderCompileWarning(Section<?> section, RenderResult string) {
			List<Section<PackageTerm>> packageTerms = Sections.findSuccessorsOfType(section, PackageTerm.class);
			for (Section<PackageTerm> packageTerm : packageTerms) {
				String defaultPackage = packageTerm.getText();
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
	}

	private static class PackageTermDefinitionRegistrationHandler extends PackageRegistrationScript<PackageTerm> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageTerm> section) {

			String defaultPackage = section.getText();
			compiler.getTerminologyManager().registerTermDefinition(compiler,
					section, Package.class, new Identifier(defaultPackage));
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageTerm> section) {
			String defaultPackage = section.getText();
			compiler.getTerminologyManager().unregisterTermDefinition(compiler,
					section, Package.class, new Identifier(defaultPackage));
		}
	}

	private static class SetDefaultPackageHandler extends PackageRegistrationScript<PackageTerm> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageTerm> section) {
			String defaultPackage = section.getText();
			if (!defaultPackage.isEmpty()) {
				KnowWEUtils.getPackageManager(section).addDefaultPackage(
						section.getArticle(), defaultPackage);
			}
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageTerm> section) {

		}
	}

	private static class RemoveDefaultPackageHandler extends PackageRegistrationScript<PackageTerm> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageTerm> section) {

		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageTerm> section) {
			String defaultPackage = section.getText();
			KnowWEUtils.getPackageManager(section).removeDefaultPackage(
					section.getArticle(), defaultPackage);
		}
	}
}
