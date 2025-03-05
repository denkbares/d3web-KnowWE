/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.knowwe.event;

import com.denkbares.events.Event;

/**
 * Thrown if the entire wiki content has been replaced (on the file system by external manipulation).
 * In that case any extension needs to clear its caches.
 */
public class WikiContentReplacedEvent implements Event {
}
