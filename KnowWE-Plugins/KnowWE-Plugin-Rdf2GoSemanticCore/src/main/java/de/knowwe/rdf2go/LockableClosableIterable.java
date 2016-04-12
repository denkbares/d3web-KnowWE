package de.knowwe.rdf2go;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;

/**
 * Some models have extreme slow downs if during a SPARQL query new statements are added or removed. Concurrent
 * SPARQLs however are no problem. Therefore we use a lock that locks exclusively for writing but shared for
 * reading.<p>
 * This iterator provides the method to lock and unlock the model, so nobody can write while this iterator is used.
 * Be sure to always also unlock (in a try-finally block). If you do not unlock, nobody can write to the model again. Ever.
 *
 * @author Albrecht Striffler (denkbares GmbH) on 11.05.2014.
 */
public class LockableClosableIterable<E> implements ClosableIterable<E>, Lockable {

	private final ReentrantReadWriteLock.ReadLock readLock;
	private final ClosableIterable<E> iterable;

	public LockableClosableIterable(ReentrantReadWriteLock.ReadLock readLock, ClosableIterable<E> iterable) {
		this.readLock = readLock;
		this.iterable = iterable;
	}

	@Override
	public ClosableIterator<E> iterator() {
		return new LockableClosableIterator<E>(readLock, iterable.iterator());
	}

	@Override
	public void lock() {
		readLock.lock();
	}

	@Override
	public void unlock() {
		readLock.unlock();
	}
}