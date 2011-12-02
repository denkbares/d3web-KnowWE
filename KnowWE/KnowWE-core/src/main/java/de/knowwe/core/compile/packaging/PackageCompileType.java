package de.knowwe.core.compile.packaging;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.ConstraintModule;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.objects.KnowWETerm;
import de.knowwe.core.kdom.objects.StringDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.utils.KnowWEUtils;

public abstract class PackageCompileType extends StringDefinition implements PackageReference {

	public PackageCompileType() {
		this.clearSubtreeHandlers();
		this.setTermScope(Scope.GLOBAL);
		this.sectionFinder = new AllTextSectionFinder();
		this.setIgnorePackageCompile(true);
		this.addSubtreeHandler(Priority.PRECOMPILE_LOW, new PackageCompileHandler());
	}

	@Override
	public String getTermIdentifier(Section<? extends KnowWETerm<String>> s) {
		return s.getTitle();
	}

	@Override
	public final List<String> getPackagesToReferTo(Section<? extends PackageReference> s) {
		return getPackagesToCompile(s);
	}

	/**
	 * Needs to return a List of package names. These are the packages compiled
	 * later...
	 * 
	 * @created 02.10.2010
	 * @param s should always be the Section calling this method.
	 * @return a List of package names.
	 */
	public abstract List<String> getPackagesToCompile(Section<? extends PackageReference> s);

	private static class PackageCompileHandler extends SubtreeHandler<PackageCompileType> {

		public PackageCompileHandler() {
			super(true);
			this.unregisterAllConstraintModules();
			this.registerConstraintModule(0, new CompileHandlerConstraint());
		}

		@Override
		public Collection<Message> create(KnowWEArticle article, Section<PackageCompileType> s) {

			KnowWEPackageManager packageMng = KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb());

			if (article.isFullParse() || !s.isReusedBy(article.getTitle())) {
				packageMng.registerPackageReference(article, s);
			}

			List<Section<?>> sectionsOfPackage = new LinkedList<Section<?>>();
			for (String referedPackages : s.get().getPackagesToCompile(s)) {
				List<Section<?>> tempSectionsOfPackage = packageMng.getSectionsOfPackage(referedPackages);
				for (Section<?> sectionOfPackage : tempSectionsOfPackage) {
					if (!sectionOfPackage.getTitle().equals(article.getTitle())) {
						sectionsOfPackage.add(sectionOfPackage);
					}
				}
			}

			for (Section<?> sectionOfPackage : sectionsOfPackage) {
				if (KnowWEEnvironment.getInstance().getWikiConnector().doesPageExist(
						sectionOfPackage.getTitle())) {
					article.getReviseIterator().addRootSectionToRevise(sectionOfPackage);
				}
				else {
					KnowWEEnvironment.getInstance().getArticleManager(sectionOfPackage.getWeb()).addArticleToUpdate(
							sectionOfPackage.getTitle());
				}
			}

			s.get().storeTermObject(article, s, s.get().getTermIdentifier(s));
			KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
					article, s);

			return null;
		}

		@Override
		public void destroy(KnowWEArticle article, Section<PackageCompileType> s) {
			if (!s.isReusedBy(article.getTitle())) {
				article.setFullParse(this.getClass());
			}
			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
					article, s);
		}

		private class CompileHandlerConstraint extends ConstraintModule<PackageCompileType> {

			@Override
			public boolean violatedConstraints(KnowWEArticle article, Section<PackageCompileType> s) {
				return true;
			}

		}

	}

}
