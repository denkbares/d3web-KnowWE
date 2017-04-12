/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Class for initializing a heart-beat on a tcp/ip output stream. It writes a
 * space character to the stream every few seconds and forces to push it down to
 * the client to keep the stream open (avoiding timeout of ajax requests)
 * 
 * @author volker_belli
 * @created 21.06.2011
 */
public class KeepAlive extends Thread {

	private final OutputStream out;

	/**
	 * Creates a new hear-beat for a specified stream. Note that "start" must be
	 * called to activate it.
	 * 
	 * @param out the stream to keep alive
	 */
	public KeepAlive(OutputStream out) {
		super("SetAnswer heartbeat");
		this.out = out;
	}

	@Override
	public void run() {
		try {
			//noinspection InfiniteLoopStatement
			while (true) {
				// heart beat every 10 seconds
				Thread.sleep(10 * 1000);
				synchronized (this) {
					out.write(32);
					out.flush();
				}
			}
		}
		catch (InterruptedException e) { // NOSONAR
			// we are finished
			// because request has been completed
		}
		catch (IOException e) { // NOSONAR
			// we are finished,
			// because client has closed the stream
		}
	}

	/**
	 * Stops the heart-beat previously started by {@link #start()}
	 * 
	 * @created 21.06.2011
	 */
	public synchronized void terminate() {
		interrupt();
	}
}
