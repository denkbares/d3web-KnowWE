/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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
package de.knowwe.include;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.TreeMap;

import org.junit.Test;

import com.denkbares.knowwe.textdiff.TextDiff;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.include.InterWikiTrackingService.State;
import de.knowwe.include.InterWikiTrackingService.TrackingStatus;

import static org.junit.Assert.*;

/**
 * Tests the acknowledgement handling of the InterWikiImport tracking mode (ILMERED-2498): after a
 * change in the source wiki only the changes since the last acknowledgement have to be reviewed.
 */
public class InterWikiTrackingServiceTest {

	private static final Instant T1 = Instant.parse("2026-09-01T10:00:00Z");
	private static final Instant ACCEPTED = Instant.parse("2026-09-02T10:00:00Z");
	private static final Instant T2 = Instant.parse("2026-09-03T10:00:00Z");
	private static final Instant T3 = Instant.parse("2026-09-04T10:00:00Z");

	private static final String REFERENCE = "line a\nline b\nline c";
	private static final String REFERENCE_WITH_TKZ = "line a\nline b\nTKZ 4711\nline c";
	private static final String LOCAL = "line a\nline b (local)\nline c";

	private static final InterWikiTrackingService.AcceptedReferenceLookup NO_LOOKUP = acceptedAt -> {
		throw new AssertionError("lookup must not be called");
	};

	@Test
	public void missingReference() throws Exception {
		TrackingStatus status = InterWikiTrackingService.computeTrackingStatus(null, LOCAL, ACCEPTED, null, NO_LOOKUP);
		assertEquals(State.MISSING_REFERENCE, status.state());
		assertFalse(status.warningActive());
		assertNull(status.sourceChanges());
	}

	@Test
	public void equalAfterTrimNeedsNoLookup() throws Exception {
		TrackingStatus status = InterWikiTrackingService.computeTrackingStatus(REFERENCE, "\n" + REFERENCE + "\n\n", ACCEPTED, T2, NO_LOOKUP);
		assertEquals(State.EQUAL, status.state());
		assertFalse(status.warningActive());
		assertNull(status.diff());
		assertNull(status.sourceChanges());
	}

	@Test
	public void neverAcknowledged() throws Exception {
		TrackingStatus status = InterWikiTrackingService.computeTrackingStatus(REFERENCE, LOCAL, null, T1, NO_LOOKUP);
		assertEquals(State.UNACCEPTED_DIFF, status.state());
		assertTrue(status.warningActive());
		assertNotNull(status.diff());
		assertNull(status.sourceChanges());
	}

	@Test
	public void acknowledgedAndReferenceUnchanged() throws Exception {
		TrackingStatus status = InterWikiTrackingService.computeTrackingStatus(REFERENCE, LOCAL, ACCEPTED, T1, at -> REFERENCE);
		assertEquals(State.ACCEPTED_DIFF, status.state());
		assertFalse(status.warningActive());
		assertNotNull(status.diff());
		assertNull(status.sourceChanges());
	}

	@Test
	public void referenceChangedAfterAcknowledgementShowsOnlySourceChanges() throws Exception {
		TrackingStatus status = InterWikiTrackingService.computeTrackingStatus(REFERENCE_WITH_TKZ, LOCAL, ACCEPTED, T2, at -> REFERENCE);
		assertEquals(State.UNACCEPTED_DIFF, status.state());
		assertTrue(status.warningActive());
		TextDiff sourceChanges = status.sourceChanges();
		assertNotNull(sourceChanges);
		assertEquals(new TextDiff.Stats(1, 0), sourceChanges.stats());
		// the complete differences to the local content are still available
		assertNotNull(status.diff());
		assertEquals(new TextDiff.Stats(1, 2), status.diff().stats());
	}

	@Test
	public void restoredIdenticalReferenceAfterAcknowledgementKeepsAcknowledgement() throws Exception {
		// newer timestamp, but same content (e.g. re-stored attachment) must not re-activate the warning
		TrackingStatus status = InterWikiTrackingService.computeTrackingStatus(REFERENCE, LOCAL, ACCEPTED, T2, at -> REFERENCE + "\n");
		assertEquals(State.ACCEPTED_DIFF, status.state());
		assertFalse(status.warningActive());
	}

	@Test
	public void unknownAcknowledgedReferenceFallsBackToTimestamp() throws Exception {
		TrackingStatus newer = InterWikiTrackingService.computeTrackingStatus(REFERENCE_WITH_TKZ, LOCAL, ACCEPTED, T2, at -> null);
		assertEquals(State.UNACCEPTED_DIFF, newer.state());
		assertNull(newer.sourceChanges());

		TrackingStatus older = InterWikiTrackingService.computeTrackingStatus(REFERENCE, LOCAL, ACCEPTED, T1, at -> null);
		assertEquals(State.ACCEPTED_DIFF, older.state());
	}

	@Test
	public void findReferenceTextAt() throws Exception {
		assertNull(InterWikiTrackingService.findReferenceTextAt(null, ACCEPTED));

		// current version is not newer than the acknowledgement
		VersionedAttachment unchanged = new VersionedAttachment().add(T1, REFERENCE);
		assertEquals(REFERENCE, InterWikiTrackingService.findReferenceTextAt(unchanged, ACCEPTED));
		assertEquals(REFERENCE, InterWikiTrackingService.findReferenceTextAt(unchanged, T1));

		// newer versions after the acknowledgement are skipped
		VersionedAttachment changed = new VersionedAttachment().add(T1, REFERENCE).add(T2, REFERENCE_WITH_TKZ).add(T3, LOCAL);
		assertEquals(REFERENCE, InterWikiTrackingService.findReferenceTextAt(changed, ACCEPTED));
		assertEquals(REFERENCE_WITH_TKZ, InterWikiTrackingService.findReferenceTextAt(changed, T2.plusSeconds(1)));

		// deleted versions are skipped
		VersionedAttachment gap = new VersionedAttachment().add(T1, REFERENCE).add(T1, "deleted").add(T2, REFERENCE_WITH_TKZ);
		gap.versions.remove(2);
		assertEquals(REFERENCE, InterWikiTrackingService.findReferenceTextAt(gap, ACCEPTED));

		// acknowledged version no longer available (e.g. stored without versioning)
		VersionedAttachment unversioned = new VersionedAttachment().add(T2, REFERENCE_WITH_TKZ);
		assertNull(InterWikiTrackingService.findReferenceTextAt(unversioned, ACCEPTED));
	}

	private static final class VersionedAttachment implements WikiAttachment {

		private record Version(Instant date, String text) {
		}

		private final TreeMap<Integer, Version> versions = new TreeMap<>();
		private int latest = 0;

		VersionedAttachment add(Instant date, String text) {
			versions.put(++latest, new Version(date, text));
			return this;
		}

		private Version version(int version) {
			Version result = versions.get(version);
			if (result == null) throw new IllegalArgumentException("no version " + version);
			return result;
		}

		@Override
		public String getFileName() {
			return "WikiImport-Page.txt";
		}

		@Override
		public String getParentName() {
			return "Page";
		}

		@Override
		public String getPath() {
			return getParentName() + "/" + getFileName();
		}

		@Override
		public Date getDate() {
			return getDate(latest);
		}

		@Override
		public Date getDate(int version) {
			return Date.from(version(version).date());
		}

		@Override
		public long getSize() {
			return getSize(latest);
		}

		@Override
		public long getSize(int version) {
			return version(version).text().getBytes(StandardCharsets.UTF_8).length;
		}

		@Override
		public InputStream getInputStream() {
			return getInputStream(latest);
		}

		@Override
		public InputStream getInputStream(int version) {
			return new ByteArrayInputStream(version(version).text().getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public int getVersion() {
			return latest;
		}

		@Override
		public void delete(int version) {
			versions.remove(version);
		}

		@Override
		public int[] getAvailableVersions() {
			return versions.keySet().stream().mapToInt(Integer::intValue).toArray();
		}
	}
}
