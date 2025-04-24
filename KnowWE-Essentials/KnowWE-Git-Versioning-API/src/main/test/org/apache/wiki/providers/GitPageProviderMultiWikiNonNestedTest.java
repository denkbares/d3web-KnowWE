/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package org.apache.wiki.providers;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.wiki.TestEngine;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.multiwiki.ParameterizedNestedNonNestedMultiWikiTest;
import org.apache.wiki.pages.PageManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This tests the standard tests that all other standard PageProvider implementations must pass.
 * It does not test any git functionality though.
 */
public class GitPageProviderMultiWikiNonNestedTest extends FileSystemProviderMultiWikiNestedTest {

	// TODO: can we add git assertions to any test method from the super class?

	@Override
	@BeforeEach
	public void setUp() throws Exception {

		props.setProperty( PageManager.PROP_PAGEPROVIDER, "GitPageProviderMultiWiki" );
		props.setProperty( FileSystemProvider.PROP_PAGEDIR, "./target/jspwiki_test_pages/" );

		Properties propertiesNonNestedVersioning = getAdditionalProperties();

		props.putAll(propertiesNonNestedVersioning);

		m_engine = TestEngine.build(props);
		m_provider = new GitPageProviderMultiWiki();
		m_provider.initialize( m_engine, props );

		propsUTF8.putAll(props);
		propsUTF8.setProperty( Engine.PROP_ENCODING, StandardCharsets.UTF_8.name() );
		m_providerUTF8 = new GitPageProviderMultiWiki();
		m_providerUTF8.initialize( m_engine, propsUTF8 );

	}

	@Override
	protected  @NotNull Properties getAdditionalProperties() {
		Properties propertiesNonNestedVersioning = FileSystemProviderMultiWikiNestedTest.getTestProperties();
		propertiesNonNestedVersioning.put("jspwiki.applicationName", "TestEngineNonNestedGitPageProvider");
		ParameterizedNestedNonNestedMultiWikiTest.standardPropertiesMultiWiki(propertiesNonNestedVersioning);
		propertiesNonNestedVersioning.put("jspwiki.pageProvider", "GitPageProviderMultiWiki");
		return propertiesNonNestedVersioning;
	}

	@Test
	@Override
	public void testScandinavianLetters() throws Exception {
		super.testScandinavianLetters();
		Assertions.assertEquals(1,1);
	}

}
