/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.kdom.parsing;

import java.util.ArrayList;

import org.junit.Test;

import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;

import static org.junit.Assert.*;

/** Covers child-list edits, including the GrammarParser's addChild-before-setParent convention. */
public class SectionIndexTest {

	private static Section<RootType> node(Section<?> parent) {
		return Section.createSection("", new RootType(), parent);
	}

	@Test
	public void appendingAndReadingWideSiblingListsDoesNotSearchTheList() {
		Section<?> parent = node(null);
		parent.children = new ArrayList<Section<? extends Type>>() {
			@Override
			public int indexOf(Object object) {
				throw new AssertionError("Regular sibling-index lookup must not scan the list");
			}
		};
		for (int i = 0; i < 1_000; i++) assertEquals(i, node(parent).getIndexInParent());
		for (int i = 999; i >= 0; i--) assertEquals(i, parent.getChildren().get(i).getIndexInParent());
	}

	@Test
	public void insertingBeforeExistingChildrenUpdatesTheirIndices() {
		Section<?> parent = node(null);
		Section<?> first = node(parent);
		Section<?> second = node(parent);
		Section<?> inserted = node(null);
		parent.addChild(0, inserted);
		assertEquals(-1, inserted.getIndexInParent());
		inserted.setParent(parent);
		assertEquals(0, inserted.getIndexInParent());
		assertEquals(1, first.getIndexInParent());
		assertEquals(2, second.getIndexInParent());
		Section<?> middle = node(null);
		middle.setParent(parent);
		parent.addChild(2, middle);
		assertEquals(2, middle.getIndexInParent());
		assertEquals(3, second.getIndexInParent());
	}

	@Test
	public void removedChildrenDoNotRetainAnIndexWhenTheListIsRebuilt() {
		Section<?> parent = node(null);
		Section<?> removed = node(parent);
		parent.removeAllChildren();
		assertEquals(-1, removed.getIndexInParent());
		assertEquals(0, node(parent).getIndexInParent());
		assertEquals(-1, removed.getIndexInParent());
	}

	@Test
	public void reparentingValidatesTheIndexAgainstTheCurrentParent() {
		Section<?> oldParent = node(null);
		Section<?> child = node(oldParent);
		Section<?> newParent = node(null);
		node(newParent);
		newParent.addChild(child);
		child.setParent(newParent);
		assertEquals(1, child.getIndexInParent());
		child.setParent(oldParent);
		assertEquals(0, child.getIndexInParent());
		child.setParent(null);
		assertEquals(-1, child.getIndexInParent());
	}
}
