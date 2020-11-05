package de.knowwe.core.packaging;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageRule;
import de.knowwe.core.compile.packaging.PackageTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageRegistrationScript;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class PackageMarkupType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Package");
		PackageRule packageRule = new PackageRule();
		packageRule.addCompileScript(Priority.HIGHEST, new SetDefaultPackageHandler());
		packageRule.addCompileScript(Priority.LOWEST, new RemoveDefaultPackageHandler());
		MARKUP.addContentType(packageRule);
		MARKUP.setTemplate("%%package \u00ABpackage-name\u00BB\n");
	}

	public PackageMarkupType() {
		super(MARKUP);
		removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageRegistrationScript.class);
	}

	public static class SetDefaultPackageHandler extends PackageRegistrationScript<PackageRule> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageRule> section) {
			PackageManager packageManager = KnowWEUtils.getPackageManager(section);
			if (section.get().isOrdinaryPackage(section)) {
				packageManager.addDefaultPackage(section.getArticle(), section.get().getOrdinaryPackage(section));
			}
			else {
				packageManager.addDefaultPackageRule(section.getArticle(), section.get().getRule(section));

			}
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageRule> section) {

		}
	}

	public static class RemoveDefaultPackageHandler extends PackageRegistrationScript<PackageRule> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageRule> section) {

		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageRule> section) {
			PackageManager packageManager = KnowWEUtils.getPackageManager(section);
			if (section.get().isOrdinaryPackage(section)) {
				packageManager.removeDefaultPackage(section.getArticle(), section.get().getOrdinaryPackage(section));
			}
			else {
				packageManager.removeDefaultPackageRule(section.getArticle(), section.get().getRule(section));

			}
		}
	}

	private static class PackageTermReferenceRegistrationHandler extends PackageRegistrationScript<PackageTerm> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageTerm> section) {

			String defaultPackage = section.getText();
			compiler.getTerminologyManager().registerTermReference(compiler,
					section, Package.class, new Identifier(defaultPackage));
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageTerm> section) {
			String defaultPackage = section.getText();
			compiler.getTerminologyManager().unregisterTermReference(compiler,
					section, Package.class, new Identifier(defaultPackage));
		}
	}
}
