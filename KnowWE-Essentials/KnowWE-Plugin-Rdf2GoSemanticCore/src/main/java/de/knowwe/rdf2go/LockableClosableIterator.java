/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ontoware.aifbcommons.collection.ClosableIterator;

/**
 * Some models have extreme slow downs if during a SPARQL query new statements are added or removed. Concurrent
 * SPARQLs however are no problem. Therefore we use a lock that locks exclusively for writing but shared for
 * reading.<p>
 * This iterator provides the method to lock and unlock the model, so nobody can write while this iterator is used.
 * Be sure to always also unlock (in a try-finally block). If you do not unlock, nobody can write to the model again. Ever.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 25.04.2014
 */
public class LockableClosableIterator<E> implements ClosableIterator<E>, Lockable {

	private final ClosableIterator<E> iterator;
	private final ReentrantReadWriteLock.ReadLock readLock;

	public LockableClosableIterator(ReentrantReadWriteLock.ReadLock readLock, ClosableIterator<E> iterator) {
	this.readLock = readLock;
	this.iterator = iterator;
}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public E next() {
		return iterator.next();
	}

	public void lock() {
		readLock.lock();
	}

	public void unlock() {
		readLock.unlock();
	}

	@Override
	public void remove() {
		iterator.remove();
	}

	@Override
	public void close() {
		iterator.close();
	}
}
