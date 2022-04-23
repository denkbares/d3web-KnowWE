/*
 * Copyright (C) 2022 denkbares GmbH, Germany
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

package de.knowwe.rdf2go;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.jetbrains.annotations.NotNull;

/**
 * Task with priority to allow usage of priority thread pool
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 23.04.22
 */
public class PriorityTask extends FutureTask<Object> implements Comparable<PriorityTask> {

	private final double priority;

	public PriorityTask(@NotNull Runnable runnable, double priority) {
		super(runnable, Void.class);
		this.priority = priority;
	}

	public PriorityTask(@NotNull Callable<Object> callable, double priority) {
		super(callable);
		this.priority = priority;
	}

	public double getPriority() {
		return priority;
	}

	@Override
	public int compareTo(@NotNull PriorityTask o) {
		return Double.compare(this.priority, o.priority);
	}
}
