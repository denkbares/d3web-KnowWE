package de.knowwe.rdf2go;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Cleanup all thread locals for the current thread, so use with patient.
 * Should only be used, when a thread has ended and is only needed if the
 * thread is part of a thread pool. Threads without thread pools cleanup their
 * thread locals in finalize.
 */

public class ThreadLocalCleaner {

	/**
	 * Cleans all thread local entries of the current thread, so be sure you don't
	 * need them anymore, e.g. when the thread has ended in some way
	 */
	public static void cleanThreadLocals() {
		try {

			// Get a reference to the thread locals table of the current thread
			Thread thread = Thread.currentThread();
			Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
			threadLocalsField.setAccessible(true);
			Object threadLocalTable = threadLocalsField.get(thread);

			// Get a reference to the array holding the thread local variables inside the
			// ThreadLocalMap of the current thread
			@SuppressWarnings("rawtypes") Class threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
			Field tableField = threadLocalMapClass.getDeclaredField("table");
			tableField.setAccessible(true);
			Object table = tableField.get(threadLocalTable);

			// The key to the ThreadLocalMap is a WeakReference object. The referent field of this object
			// is a reference to the actual ThreadLocal variable
			Field referentField = Reference.class.getDeclaredField("referent");
			referentField.setAccessible(true);

			for (int i = 0; i < Array.getLength(table); i++) {
				// Each entry in the table array of ThreadLocalMap is an Entry object
				// representing the thread local reference and its value
				Object entry = Array.get(table, i);
				if (entry != null) {
					// Get a reference to the thread local object and remove it from the table
					@SuppressWarnings("rawtypes") ThreadLocal threadLocal = (ThreadLocal)referentField.get(entry);
					if(threadLocal != null)
						threadLocal.remove();
				}
			}
		} catch(Exception e) {
			// We will tolerate an exception here and just log it
			throw new IllegalStateException(e);
		}
	}
}
