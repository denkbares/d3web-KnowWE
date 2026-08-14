/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.renderer;

import java.util.List;

import org.junit.Test;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests which columns of a section list are offered for individual filtering.
 */
public class ListSectionsRendererTest {

	private static ListSectionsRenderer<Type> newRenderer() {
		return new ListSectionsRenderer<>(List.<Section<Type>>of(), null);
	}

	private static List<String> columnNames(ListSectionsRenderer<Type> renderer) {
		return List.copyOf(renderer.getFilterableColumns().keySet());
	}

	@Test
	public void skipsColumnsWithoutOwnHeader() {
		ListSectionsRenderer<Type> renderer = newRenderer()
				.column("error", section -> "") // e.g. the error column, rendered without a header
				.header("Name").column("name", section -> "")
				.header("Prompt").column("prompt", section -> "");

		assertThat(columnNames(renderer), is(List.of("Name", "Prompt")));
	}

	@Test
	public void skipsHeadersSpanningMultipleColumns() {
		ListSectionsRenderer<Type> renderer = newRenderer()
				.header("Progress")
				.column("progress", section -> "")
				.column("progress", section -> "")
				.header("Name").column("name", section -> "");

		assertThat(columnNames(renderer), is(List.of("Name")));
	}

	@Test
	public void skipsColumnsExcludedFromSearch() {
		ListSectionsRenderer<Type> renderer = newRenderer()
				.header("Page", "some explanation", false).column("page", section -> "")
				.header("Name", "some explanation", true).column("name", section -> "");

		assertThat(columnNames(renderer), is(List.of("Name")));
	}

	@Test
	public void skipsSeparators() {
		ListSectionsRenderer<Type> renderer = newRenderer()
				.header("Name").column("name", section -> "")
				.separator(true)
				.header("Variants").column("variants", section -> "");

		assertThat(columnNames(renderer), is(List.of("Name", "Variants")));
	}

	@Test
	public void makesDuplicateHeadersUnique() {
		ListSectionsRenderer<Type> renderer = newRenderer()
				.header("Progress").column("first", section -> "")
				.header("Progress").column("second", section -> "")
				.header("Progress").column("third", section -> "");

		assertThat(columnNames(renderer), is(List.of("Progress", "Progress (2)", "Progress (3)")));
	}

	@Test
	public void mapsColumnNamesToTheirOwnContentAccessor() {
		ListSectionsRenderer<Type> renderer = newRenderer()
				.header("Name").column("name", section -> "the name")
				.header("Prompt").column("prompt", section -> "the prompt");

		assertThat(renderer.getFilterableColumns().get("Name").apply(null), is("the name"));
		assertThat(renderer.getFilterableColumns().get("Prompt").apply(null), is("the prompt"));
	}

	@Test
	public void reducesHtmlColumnsToTheirPlainText() {
		ListSectionsRenderer<Type> renderer = newRenderer()
				.header("Type").html(section -> "<span class='tooltipster' title='Malfunction'>MA</span>")
				.header("Count").number(0, section -> 42);

		assertThat(renderer.getFilterableColumns().get("Type").apply(null), is("MA"));
		assertThat(renderer.getFilterableColumns().get("Count").apply(null), is("42"));
	}

	@Test
	public void keepsPlainColumnsUnchanged() {
		// text of a plain column must not be altered, it is compared to the value the user selected
		ListSectionsRenderer<Type> renderer = newRenderer()
				.header("Name").column("name", section -> "a < b & c");

		assertThat(renderer.getFilterableColumns().get("Name").apply(null), is("a < b & c"));
	}
}
