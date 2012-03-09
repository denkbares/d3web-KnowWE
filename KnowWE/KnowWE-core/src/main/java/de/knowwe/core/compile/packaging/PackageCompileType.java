package de.knowwe.core.compile.packaging;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.ConstraintModule;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

public abstract class PackageCompileType extends AbstractType implements PackageCompiler {

	public PackageCompileType() {
		this.setSectionFinder(new AllTextSectionFinder());
		this.setIgnorePackageCompile(true);
		this.addSubtreeHandler(Priority.PRECOMPILE_LOW, new PackageCompileHandler());
	}

	@Override
	public abstract List<String> getPackagesToCompile(Section<? extends PackageCompiler> s);

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
				packageMng.registerPackageCompileSection(article, s);
			}

			List<Section<?>> sectionsOfPackage = new LinkedList<Section<?>>();
			for (String packagesToCompile : s.get().getPackagesToCompile(s)) {
				List<Section<?>> tempSectionsOfPackage = packageMng.getSectionsOfPackage(packagesToCompile);
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

			KnowWEUtils.getGlobalTerminologyManager(article.getWeb()).registerTermDefinition(s,
					String.class, s.getTitle());

			return Messages.noMessage();
		}

		@Override
		public void destroy(KnowWEArticle article, Section<PackageCompileType> s) {
			KnowWEUtils.getTerminologyManager(article).unregisterTermDefinition(
					s, String.class, s.getTitle());
		}

		private class CompileHandlerConstraint extends ConstraintModule<PackageCompileType> {

			@Override
			public boolean violatedConstraints(KnowWEArticle article, Section<PackageCompileType> s) {
				return true;
			}

		}

	}

}
