/*
 * Copyright (C) 2013 denkbares GmbH
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.core.kdom.parsing;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import de.knowwe.core.kdom.Type;
import de.knowwe.kdom.filter.SectionFilter;

/**
 * Implements a depth-first iterator through the KDOM. It visits a specified root node first and
 * then the depth-first order of all the successor nodes. A SectionFilter may be specified to skip
 * some branches from iteration. You may also specify a maximum depth that shall be considered.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 22.08.14.
 * @see de.knowwe.kdom.filter.SectionFilter
 */
public class KDOMIterator implements Iterator<Section<? extends Type>> {
	/**
	 * The iterators for the elements to proceed. Finished iterators will be removed immediately. We
	 * will never add empty iterators. Therefore each iterator will have "next" elements.
	 */
	private final Stack<Iterator> iterators = new Stack<>();
	private final SectionFilter descentFilter;

	private int maxDepth = -1;

	/**
	 * Creates a depth-first iterator that visits the specified root node first and then the
	 * depth-first order of all the successor nodes.
	 *
	 * @param root the root node to start the iteration
	 */
	public static <T extends Type> KDOMIterator depthFirst(Section<T> root) {
		return depthFirst(root, SectionFilter.ALL_SECTIONS);
	}

	/**
	 * Creates a depth-first iterator that visits the specified root node and each of their
	 * successors in depth-first order.
	 *
	 * @param roots the root nodes to start the iteration
	 */
	public static <T extends Type> KDOMIterator depthFirst(Iterable<Section<T>> roots) {
		return depthFirst(roots.iterator(), SectionFilter.ALL_SECTIONS);
	}

	/**
	 * Creates a depth-first iterator that visits the specified root node and each of their
	 * successors in depth-first order.
	 *
	 * @param roots the root nodes to start the iteration
	 */
	public static <T extends Type> KDOMIterator depthFirst(Iterator<Section<T>> roots) {
		return depthFirst(roots, SectionFilter.ALL_SECTIONS);
	}

	/**
	 * Creates a depth-first iterator that visits the specified root node first and then the
	 * depth-first order of all the successor nodes. It truncates all the sub-branches from the
	 * iteration if the descentFilter does not accept the particular parent node.
	 * <p/>
	 * <b>Note:</b><br> The non-accepted node itself is NOT (!) excluded from the iteration.
	 *
	 * @param root the root node to start the iteration
	 * @param descentFilter a filter that truncates branches if the parent node is not accepted
	 * @see de.knowwe.kdom.filter.SectionFilter#accept(Section)
	 */
	public static <T extends Type> KDOMIterator depthFirst(Section<T> root, SectionFilter descentFilter) {
		return depthFirst(Arrays.asList(root).iterator(), descentFilter);
	}

	/**
	 * Creates a depth-first iterator that visits the specified root nodes and the
	 * depth-first order of all the successor nodes. It truncates all the sub-branches from the
	 * iteration if the descentFilter does not accept the particular parent node.
	 * <p/>
	 * <b>Note:</b><br> The non-accepted node itself is NOT (!) excluded from the iteration.
	 *
	 * @param roots the root nodes to start the iteration
	 * @param descentFilter a filter that truncates branches if the parent node is not accepted
	 * @see de.knowwe.kdom.filter.SectionFilter#accept(Section)
	 */
	public static <T extends Type> KDOMIterator depthFirst(Iterable<Section<T>> roots, SectionFilter descentFilter) {
		return depthFirst(roots.iterator(), descentFilter);
	}
	/**
	 * Creates a depth-first iterator that visits the specified root nodes and the
	 * depth-first order of all the successor nodes. It truncates all the sub-branches from the
	 * iteration if the descentFilter does not accept the particular parent node.
	 * <p/>
	 * <b>Note:</b><br> The non-accepted node itself is NOT (!) excluded from the iteration.
	 *
	 * @param roots the root nodes to start the iteration
	 * @param descentFilter a filter that truncates branches if the parent node is not accepted
	 * @see de.knowwe.kdom.filter.SectionFilter#accept(Section)
	 */
	public static <T extends Type> KDOMIterator depthFirst(Iterator<Section<T>> roots, SectionFilter descentFilter) {
		return new KDOMIterator(roots, descentFilter);
	}

	private KDOMIterator(Iterator roots, SectionFilter descentFilter) {
		this.descentFilter = descentFilter;
		iterators.push(roots);
	}

	@Override
	public boolean hasNext() {
		// having no empty iterators, we return true if stack is not empty
		return !iterators.isEmpty();
	}

	@Override
	public Section<?> next() {
		if (!hasNext()) throw new NoSuchElementException();
		Iterator iterator = iterators.peek();
		Section next = (Section) iterator.next();

		// initialize depth for this item is required
		int depth = 0;
		if (maxDepth >= 0) {
			if (iterator instanceof DepthIterator) {
				depth = ((DepthIterator) iterator).getDepth();
			}
		}

		// remove iterator if it has become empty
		if (!iterator.hasNext()) {
			iterators.pop();
		}

		// add children if not reached the max level (works also well for maxDepth being negative)
		// and if not denied by descentFilter
		if (depth != maxDepth && descentFilter.accept(next)) {
			// add children of the current element on top of the stack (will be processed next)
			// but only if there are children available
			@SuppressWarnings("unchecked")
			List<Section> children = next.getChildren();
			if (!children.isEmpty()) {
				Iterator<Section> childIterator = children.iterator();
				if (maxDepth >= 0) childIterator = new DepthIterator<>(depth+1, childIterator);
				iterators.push(childIterator);
			}
		}

		return next;
	}

	/**
	 * Specifies the maximum depth the iterator will descent from the original start node(s).
	 * A value of "0" does not descent at all, a value of "1" also descent to the direct children of
	 * the start nodes, while a negative value will have no limit at all. The default value is -1 for
	 * no limitation.
	 *
	 * @param maxDepth the maximum depth to descent
	 */
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	private static class DepthIterator<T> implements Iterator<T> {
		private final int depth;
		private final Iterator<T> base;

		private DepthIterator(int depth, Iterator<T> base) {
			this.depth = depth;
			this.base = base;
		}

		@Override
		public boolean hasNext() {
			return base.hasNext();
		}

		@Override
		public T next() {
			return base.next();
		}

		public int getDepth() {
			return depth;
		}
	}
}
