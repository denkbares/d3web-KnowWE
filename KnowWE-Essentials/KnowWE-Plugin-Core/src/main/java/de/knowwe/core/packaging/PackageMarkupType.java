package de.knowwe.core.packaging;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageReferenceRegistrationScript;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageRegistrationScript;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class PackageMarkupType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Package");
		PackageTerm packageTerm = new PackageTerm();
		packageTerm.addCompileScript(Priority.HIGHEST, new SetDefaultPackageHandler());
		packageTerm.addCompileScript(Priority.LOWEST, new RemoveDefaultPackageHandler());
		MARKUP.addContentType(packageTerm);
		MARKUP.setTemplate("%%package \u00ABpackage-name\u00BB\n");
	}

	public PackageMarkupType() {
		super(MARKUP);
		removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageReferenceRegistrationScript.class);
		removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageRegistrationScript.class);
	}

	private static class SetDefaultPackageHandler extends PackageRegistrationScript<PackageTerm> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageTerm> section) {
			String defaultPackage = section.get().getTermName(section);
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
			String defaultPackage = section.get().getTermName(section);
			KnowWEUtils.getPackageManager(section).removeDefaultPackage(
					section.getArticle(), defaultPackage);
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
