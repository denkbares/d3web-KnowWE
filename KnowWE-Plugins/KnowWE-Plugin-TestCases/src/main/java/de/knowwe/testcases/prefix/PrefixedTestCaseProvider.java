package de.knowwe.testcases.prefix;

import java.util.List;
import java.util.Set;

import de.d3web.testcase.model.TestCase;
import de.d3web.testcase.prefix.PrefixedTestCase;
import de.knowwe.core.kdom.parsing.Section;
import org.jetbrains.annotations.NotNull;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.ProviderTriple;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseUtils;

public abstract class PrefixedTestCaseProvider implements TestCaseProvider {

	public static final String PREFIX_ANNOTATION_NAME = "prefix";

	private String[] prefixPackages;
	private String prefixName;
	private ArticleManager articleManager;
	private TestCase testCase;

	public PrefixedTestCaseProvider(Section<? extends DefaultMarkupType> prefixDefiningSection) {
		if (prefixDefiningSection == null) return;
		String prefix = DefaultMarkupType.getAnnotation(prefixDefiningSection,
				PREFIX_ANNOTATION_NAME);
		if (prefix == null) return;
		Set<String> packagesSet = prefixDefiningSection.getPackageNames();
		String[] packages = packagesSet.toArray(new String[0]);
		setPrefixTestCase(KnowWEUtils.getArticleManager(prefixDefiningSection), prefix, packages);
	}

	@Override
	public TestCase getTestCase() {
		if (this.testCase == null) {
			TestCase prefixTestCase = getPrefixTestCase();
			TestCase actualTestCase = getActualTestCase();
			if (prefixTestCase == null) return actualTestCase;
			this.testCase = new PrefixedTestCase(prefixTestCase, actualTestCase);
		}
		return this.testCase;
	}

	public abstract TestCase getActualTestCase();

	public void setPrefixTestCase(@NotNull ArticleManager articleManager, String prefixName, String... prefixPackages) {
		this.articleManager = articleManager;
		this.prefixName = prefixName;
		this.prefixPackages = prefixPackages;
	}

	public TestCase getPrefixTestCase() {
		TestCaseProvider providerOfPrefix = getProviderOfPrefix(articleManager, prefixName, prefixPackages);
		if (providerOfPrefix != null && !isLooping(providerOfPrefix)) {
			return providerOfPrefix.getTestCase();
		}
		return null;
	}

	private boolean isLooping(TestCaseProvider providerOfPrefix) {
		if (isSameProvider(providerOfPrefix)) return true;
		while (providerOfPrefix instanceof PrefixedTestCaseProvider) {
			PrefixedTestCaseProvider prefixProvider = (PrefixedTestCaseProvider) providerOfPrefix;
			providerOfPrefix = getProviderOfPrefix(prefixProvider.articleManager,
					prefixProvider.prefixName, prefixProvider.prefixPackages);
			if (providerOfPrefix != null && isSameProvider(providerOfPrefix)) return true;
		}
		return false;
	}

	private boolean isSameProvider(TestCaseProvider providerOfPrefix) {
		return providerOfPrefix.getName().equals(this.getName());
	}

	private TestCaseProvider getProviderOfPrefix(ArticleManager articleManager, String prefixName, String... prefixPackage) {
		if (articleManager != null && prefixName != null && prefixPackage != null) {
			List<ProviderTriple> testCaseProviderTriples = TestCaseUtils.getTestCaseProviders(
					articleManager, prefixPackage);
			for (ProviderTriple triple : testCaseProviderTriples) {
				if (triple.getA().getName().equals(prefixName)) return triple.getA();
			}
		}
		return null;
	}
}
