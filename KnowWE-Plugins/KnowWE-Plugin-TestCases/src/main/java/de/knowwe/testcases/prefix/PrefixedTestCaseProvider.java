package de.knowwe.testcases.prefix;

import java.util.List;
import java.util.Set;

import de.d3web.testcase.model.TestCase;
import de.d3web.testcase.prefix.PrefixedTestCase;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.ProviderTriple;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseUtils;

public abstract class PrefixedTestCaseProvider implements TestCaseProvider {

	public static final String PREFIX_ANNOTATION_NAME = "prefix";

	private String[] prefixPackages;
	private String prefixName;
	private String web;
	private TestCase testCase;

	public PrefixedTestCaseProvider(Section<? extends DefaultMarkupType> prefixDefiningSection) {
		if (prefixDefiningSection == null) return;
		String prefix = DefaultMarkupType.getAnnotation(prefixDefiningSection,
				PREFIX_ANNOTATION_NAME);
		if (prefix == null) return;
		Set<String> packagesSet = prefixDefiningSection.getPackageNames();
		String[] packages = packagesSet.toArray(new String[packagesSet.size()]);
		setPrefixTestCase(prefixDefiningSection.getWeb(), prefix, packages);
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

	public void setPrefixTestCase(String web, String prefixName, String... prefixPackages) {
		this.web = web;
		this.prefixName = prefixName;
		this.prefixPackages = prefixPackages;
	}

	public TestCase getPrefixTestCase() {
		TestCaseProvider providerOfPrefix = getProviderOfPrefix(web, prefixName, prefixPackages);
		if (providerOfPrefix != null && !isLooping(providerOfPrefix)) {
			return providerOfPrefix.getTestCase();
		}
		return null;
	}

	private boolean isLooping(TestCaseProvider providerOfPrefix) {
		if (isSameProvider(providerOfPrefix)) return true;
		while (providerOfPrefix instanceof PrefixedTestCaseProvider) {
			PrefixedTestCaseProvider prefixProvider = (PrefixedTestCaseProvider) providerOfPrefix;
			providerOfPrefix = getProviderOfPrefix(prefixProvider.web,
					prefixProvider.prefixName, prefixProvider.prefixPackages);
			if (providerOfPrefix != null && isSameProvider(providerOfPrefix)) return true;
		}
		return false;
	}

	private boolean isSameProvider(TestCaseProvider providerOfPrefix) {
		return providerOfPrefix.getName().equals(this.getName());
	}

	private TestCaseProvider getProviderOfPrefix(String web, String prefixName, String... prefixPackage) {
		if (web != null && prefixName != null && prefixPackage != null) {
			List<ProviderTriple> testCaseProviderTriples = TestCaseUtils.getTestCaseProviders(
					web, prefixPackage);
			for (ProviderTriple triple : testCaseProviderTriples) {
				if (triple.getA().getName().equals(prefixName)) return triple.getA();
			}
		}
		return null;
	}
}
