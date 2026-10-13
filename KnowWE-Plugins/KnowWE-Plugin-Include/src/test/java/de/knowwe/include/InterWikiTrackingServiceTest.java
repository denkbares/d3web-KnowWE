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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Test;

import com.denkbares.knowwe.textdiff.TextDiff;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.include.InterWikiTrackingService.SkipReason;
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
	public void changesAlreadyContainedNeedNoReview() throws Exception {
		// the source added the TKZ, the local content (with its deviation) already contains it
		TrackingStatus status = InterWikiTrackingService.computeTrackingStatus(REFERENCE_WITH_TKZ,
				"line a\nline b (local)\nTKZ 4711\nline c", ACCEPTED, T2, at -> REFERENCE);
		assertEquals(State.CONTAINED_DIFF, status.state());
		assertFalse(status.warningActive());
		assertNotNull(status.sourceChanges());
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

	@Test
	public void findVersionAt() {
		VersionedAttachment attachment = new VersionedAttachment().add(T1, REFERENCE).add(T2, REFERENCE_WITH_TKZ).add(T3, LOCAL);
		assertEquals(3, InterWikiTrackingService.findVersionAt(attachment, T3));
		assertEquals(2, InterWikiTrackingService.findVersionAt(attachment, T2.plusSeconds(1)));
		assertEquals(1, InterWikiTrackingService.findVersionAt(attachment, ACCEPTED));
		assertEquals(-1, InterWikiTrackingService.findVersionAt(attachment, T1.minusSeconds(1)));
	}

	@Test
	public void previousVersions() {
		assertTrue(InterWikiTrackingService.getPreviousVersions(null, 10).isEmpty());
		assertTrue(InterWikiTrackingService.getPreviousVersions(new VersionedAttachment().add(T1, REFERENCE), 10).isEmpty());

		VersionedAttachment attachment = new VersionedAttachment().add(T1, REFERENCE).add(T2, "deleted").add(T3, REFERENCE_WITH_TKZ).add(T3, LOCAL);
		attachment.versions.remove(2);
		// newest first, without the current and the deleted version
		assertEquals(List.of(new InterWikiTrackingService.ReferenceVersion(3, T3), new InterWikiTrackingService.ReferenceVersion(1, T1)),
				InterWikiTrackingService.getPreviousVersions(attachment, 10));
		assertEquals(List.of(new InterWikiTrackingService.ReferenceVersion(3, T3)),
				InterWikiTrackingService.getPreviousVersions(attachment, 1));
	}

	@Test
	public void applySourceChanges() {
		String accepted = "Ader 1: rot\nAder 2: blau\nAder 3: gruen\nAder 4: gelb";
		String current = "Ader 1: rot\nAder 2: dunkelblau\nAder 3: gruen\nAder 4: gelb\nAder 5: schwarz";

		// local deviation elsewhere is kept, the changes are applied
		assertEquals("Ader 1: rot (lokal)\nAder 2: dunkelblau\nAder 3: gruen\nAder 4: gelb\nAder 5: schwarz",
				InterWikiTrackingService.applySourceChanges(accepted, current,
						"Ader 1: rot (lokal)\nAder 2: blau\nAder 3: gruen\nAder 4: gelb"));

		// local lines inserted before the changes shift them, they are still applied at the right place
		assertEquals("Hinweis\nAder 1: rot\nAder 2: dunkelblau\nAder 3: gruen\nAder 4: gelb\nAder 5: schwarz",
				InterWikiTrackingService.applySourceChanges(accepted, current,
						"Hinweis\nAder 1: rot\nAder 2: blau\nAder 3: gruen\nAder 4: gelb"));

		// the local text deviates at the changed line itself: conflict
		assertNull(InterWikiTrackingService.applySourceChanges(accepted, current,
				"Ader 1: rot\nAder 2: blau (lokal)\nAder 3: gruen\nAder 4: gelb"));

		// local deviation directly next to a change is no conflict
		assertEquals("Ader 1: rot\nAder 2: dunkelblau\nAder 3: gruen (lokal)\nAder 4: gelb\nAder 5: schwarz",
				InterWikiTrackingService.applySourceChanges(accepted, current,
						"Ader 1: rot\nAder 2: blau\nAder 3: gruen (lokal)\nAder 4: gelb"));

		// both inserted at the end: both are kept, the lines of the source after the local ones
		assertEquals("Ader 1: rot\nAder 2: dunkelblau\nAder 3: gruen\nAder 4: gelb\nAder 5: weiss\nAder 5: schwarz",
				InterWikiTrackingService.applySourceChanges(accepted, current,
						"Ader 1: rot\nAder 2: blau\nAder 3: gruen\nAder 4: gelb\nAder 5: weiss"));

		// no changes in the source: the local text is kept
		assertEquals("lokal", InterWikiTrackingService.applySourceChanges(accepted, accepted + "\n", "lokal\n"));
	}

	@Test
	public void diffOptions() throws Exception {
		VersionedAttachment attachment = new VersionedAttachment().add(T1, "a\nb\nc").add(T2, "a\nB\nc").add(T3, "a\nB\nc\nd");
		String reference = "a\nB\nc\nd";

		// the change of version 2 is already contained in the local content: version 1 shows the same as version 2
		List<InterWikiTrackingService.DiffOption> applied = InterWikiTrackingService.getDiffOptions(
				attachment, reference, "a (lokal)\nB\nc", 2, 10);
		assertEquals(List.of(2, InterWikiTrackingService.ALL_CHANGES), applied.stream().map(InterWikiTrackingService.DiffOption::version).toList());
		assertTrue(applied.get(0).acknowledged());
		assertEquals("a (lokal)\nB\nc\nd", applied.get(0).appliedText());
		assertEquals(reference, applied.get(1).appliedText());

		// the change of version 2 was deliberately not applied: version 1 shows more
		List<InterWikiTrackingService.DiffOption> notApplied = InterWikiTrackingService.getDiffOptions(
				attachment, reference, "a\nb\nc", 2, 10);
		assertEquals(List.of(2, 1, InterWikiTrackingService.ALL_CHANGES), notApplied.stream().map(InterWikiTrackingService.DiffOption::version).toList());
		assertEquals("a\nb\nc\nd", notApplied.get(0).appliedText());
		assertEquals("a\nB\nc\nd", notApplied.get(1).appliedText());

		// local deviation at the line changed by version 2: conflict for version 1
		List<InterWikiTrackingService.DiffOption> conflict = InterWikiTrackingService.getDiffOptions(
				attachment, reference, "a\nb (lokal)\nc", 2, 10);
		assertEquals(List.of(2, 1, InterWikiTrackingService.ALL_CHANGES), conflict.stream().map(InterWikiTrackingService.DiffOption::version).toList());
		assertFalse(conflict.get(0).conflict());
		assertTrue(conflict.get(1).conflict());

		// limited number of entries, the acknowledged version is still contained
		List<InterWikiTrackingService.DiffOption> limited = InterWikiTrackingService.getDiffOptions(
				attachment, reference, "a\nb\nc", 1, 2);
		assertEquals(List.of(1, InterWikiTrackingService.ALL_CHANGES), limited.stream().map(InterWikiTrackingService.DiffOption::version).toList());
		assertTrue(limited.get(0).acknowledged());
	}

	@Test
	public void overrideSourceChanges() {
		String base = "rot\nblau\ngruen";
		String source = "rot\nweiss\ngruen";
		// the source wins on the conflicting line
		assertEquals("rot\nweiss\ngruen", InterWikiTrackingService.overrideSourceChanges(base, source, "rot\nblau (lokal)\ngruen"));
		// local deviations elsewhere are kept, also directly next to the conflict
		assertEquals("rot (lokal)\nweiss\ngruen", InterWikiTrackingService.overrideSourceChanges(base, source, "rot (lokal)\nblau (lokal)\ngruen"));
		assertEquals("rot\nweiss\ngruen (lokal)\nschwarz", InterWikiTrackingService.overrideSourceChanges(base, source, "rot\nblau (lokal)\ngruen (lokal)\nschwarz"));
		// without a conflict like applying the changes
		assertEquals("rot (lokal)\nweiss\ngruen", InterWikiTrackingService.overrideSourceChanges(base, source, "rot (lokal)\nblau\ngruen"));
		// different number of lines in the conflict: the source lines replace the local ones
		assertEquals("rot\nweiss\nschwarz\ngruen", InterWikiTrackingService.overrideSourceChanges(base, "rot\nweiss\nschwarz\ngruen", "rot\nblau (lokal)\ngruen"));
	}

	/**
	 * The examples of the wiki page "IWI Diff Overview" of the local test wiki: lines 1-10 acknowledged,
	 * then the local content and the source changed.
	 */
	@Test
	public void reviewExamples() {
		String base = lines(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		// D1: local deviation elsewhere, applying does exactly the change of the source
		String local1 = base.replace("Zeile 1\n", "Zeile 1 (lokal)\n");
		String source1 = base.replace("Zeile 6", "Zeile 6 geaendert");
		assertEquals(local1.replace("Zeile 6", "Zeile 6 geaendert"), InterWikiTrackingService.applySourceChanges(base, source1, local1));
		assertFalse(InterWikiTrackingService.containsSourceChanges(base, source1, local1));

		// D2: end missing locally, the source deletes lines 7-8 and changes line 9: nothing to apply, but
		// still to be acknowledged
		String local2 = lines(1, 2, 3, 4);
		String source2 = lines(1, 2, 3, 4, 5, 6).replace("Zeile 6", "Zeile 6\nZeile 9 geaendert") + "\nZeile 10";
		assertEquals(local2, InterWikiTrackingService.applySourceChanges(base, source2, local2));
		assertFalse(InterWikiTrackingService.containsSourceChanges(base, source2, local2));

		// D3: middle block 4-7 missing locally, the source only deletes lines 5-6 there: nothing to apply, but
		// still to be acknowledged, the deletion was not made locally
		String local3 = lines(1, 2, 3, 8, 9, 10);
		String source3 = lines(1, 2, 3, 4, 7, 8, 9, 10);
		assertEquals(local3, InterWikiTrackingService.applySourceChanges(base, source3, local3));
		assertFalse(InterWikiTrackingService.containsSourceChanges(base, source3, local3));

		// D4: middle block 4-7 missing locally, the source changes line 5 and inserts a line after line 6
		String source4 = base.replace("Zeile 5", "Zeile 5 geaendert").replace("Zeile 6", "Zeile 6\nZeile 6a neu");
		assertEquals(local3, InterWikiTrackingService.applySourceChanges(base, source4, local3));
		assertFalse(InterWikiTrackingService.containsSourceChanges(base, source4, local3));

		// D5: conflict at line 5, the source wins there, the local deviation in line 9 is kept
		String local5 = base.replace("Zeile 5", "Zeile 5 (lokal)").replace("Zeile 9", "Zeile 9 (lokal)");
		String source5 = base.replace("Zeile 5", "Zeile 5 geaendert");
		assertNull(InterWikiTrackingService.applySourceChanges(base, source5, local5));
		assertEquals(base.replace("Zeile 5", "Zeile 5 geaendert").replace("Zeile 9", "Zeile 9 (lokal)"),
				InterWikiTrackingService.overrideSourceChanges(base, source5, local5));

		// D6: middle block 4-7 missing locally, the source changes line 2 and deletes line 6: only the change
		// of line 2 is applied, without conflict
		String source6 = lines(1, 2, 3, 4, 5, 7, 8, 9, 10).replace("Zeile 2", "Zeile 2 geaendert");
		assertEquals(local3.replace("Zeile 2", "Zeile 2 geaendert"), InterWikiTrackingService.applySourceChanges(base, source6, local3));
		assertFalse(InterWikiTrackingService.containsSourceChanges(base, source6, local3));

		// the changes of the source were also made locally (with a deviation elsewhere): nothing to review
		String local7 = local1.replace("Zeile 6", "Zeile 6 geaendert").replace("Zeile 8\n", "");
		String source7 = source1.replace("Zeile 8\n", "");
		assertTrue(InterWikiTrackingService.containsSourceChanges(base, source7, local7));

		// D7: after line 5, the local content and the source inserted different lines: no conflict, both are
		// kept, the line of the source after the local one, also when overriding a conflict elsewhere
		String local9 = base.replace("Zeile 5", "Zeile 5\nZeile 5a (lokal)");
		String source9 = base.replace("Zeile 5", "Zeile 5\nZeile 5a (Quelle)");
		assertEquals(base.replace("Zeile 5", "Zeile 5\nZeile 5a (lokal)\nZeile 5a (Quelle)"),
				InterWikiTrackingService.applySourceChanges(base, source9, local9));
		assertEquals(base.replace("Zeile 5", "Zeile 5\nZeile 5a (lokal)\nZeile 5a (Quelle)").replace("Zeile 9", "Zeile 9 geaendert"),
				InterWikiTrackingService.overrideSourceChanges(base, source9.replace("Zeile 9", "Zeile 9 geaendert"),
						local9.replace("Zeile 9", "Zeile 9 (lokal)")));

		// which diffs are shown: the changes of the source, and the changes to apply to the local content
		// only if they look different
		assertFalse(isLocalChangesDiffNeeded(base, source1, base)); // unchanged locally
		assertFalse(isLocalChangesDiffNeeded(base, source1, local1)); // D1, the deviation is not next to the change
		assertTrue(isLocalChangesDiffNeeded(base, source1, base.replace("Zeile 4", "Zeile 4 (lokal)"))); // next to it
		assertTrue(isLocalChangesDiffNeeded(base, source5, local5)); // D5, conflict
		assertTrue(isLocalChangesDiffNeeded(base, source6, local3)); // D6, lines missing locally
		assertTrue(isLocalChangesDiffNeeded(base, source9, local9)); // D7, local line next to the change

		// why nothing is applied
		assertEquals(Set.of(SkipReason.NOT_EXISTING_LOCALLY), InterWikiTrackingService.getSkipReasons(base, source2, local2));
		assertEquals(Set.of(SkipReason.NOT_EXISTING_LOCALLY), InterWikiTrackingService.getSkipReasons(base, source3, local3));
		assertEquals(Set.of(SkipReason.NOT_EXISTING_LOCALLY), InterWikiTrackingService.getSkipReasons(base, source4, local3));
		assertEquals(Set.of(SkipReason.CONTAINED), InterWikiTrackingService.getSkipReasons(base, source7, local7));
		// line 6 changed on both sides, line 9 changed by the source but deleted locally
		String source8 = source1.replace("Zeile 9", "Zeile 9 geaendert");
		String local8 = source1.replace("Zeile 9\n", "");
		assertEquals(local8, InterWikiTrackingService.applySourceChanges(base, source8, local8));
		assertEquals(Set.of(SkipReason.CONTAINED, SkipReason.NOT_EXISTING_LOCALLY), InterWikiTrackingService.getSkipReasons(base, source8, local8));
	}

	@Test
	public void sourceDeltaInLocallyDeletedLines() {
		// the source replaces a locally deleted line by two lines: the complete change is not applied, not only
		// the line paired with the deleted one
		assertEquals("a\nc", InterWikiTrackingService.applySourceChanges("a\nb\nc", "a\nB\nB2\nc", "a\nc"));
		// the source replaces two lines by one, only one of them is deleted locally: conflict, the source wins
		assertNull(InterWikiTrackingService.applySourceChanges("a\nb\nc\nd", "a\nB\nd", "a\nc\nd"));
		assertEquals("a\nB\nd", InterWikiTrackingService.overrideSourceChanges("a\nb\nc\nd", "a\nB\nd", "a\nc\nd"));
	}

	@Test
	public void insertionsAtTheSamePosition() {
		// the local content inserted the lines of the source and more: already applied
		assertEquals("a\nX\nY\nb", InterWikiTrackingService.applySourceChanges("a\nb", "a\nX\nb", "a\nX\nY\nb"));
		assertEquals("a\nY\nX\nb", InterWikiTrackingService.applySourceChanges("a\nb", "a\nX\nb", "a\nY\nX\nb"));
		// the source inserted the local lines and more: conflict, the source wins without losing anything
		assertNull(InterWikiTrackingService.applySourceChanges("a\nb", "a\nX\nZ\nb", "a\nX\nb"));
		assertEquals("a\nX\nZ\nb", InterWikiTrackingService.overrideSourceChanges("a\nb", "a\nX\nZ\nb", "a\nX\nb"));
		// partly the same lines, e.g. an empty line before a new paragraph: both are kept, nothing is lost
		assertEquals("a\n\nL\n\nS\nb", InterWikiTrackingService.applySourceChanges("a\nb", "a\n\nS\nb", "a\n\nL\nb"));
	}

	@Test
	public void conflictWithoutEffectIsNoConflict() throws Exception {
		// the diffs align the repeated line x differently, so applying conflicts, but the source winning
		// results in the local content: no conflict, nothing to apply
		assertNull(InterWikiTrackingService.applySourceChanges("a\nb\nc\nd", "a\nb\nx\nc", "a\nb\nx\nx"));
		VersionedAttachment attachment = new VersionedAttachment().add(T1, "a\nb\nc\nd").add(T2, "a\nb\nx\nc");
		InterWikiTrackingService.DiffOption option = InterWikiTrackingService.getDiffOption(attachment, 1, "a\nb\nx\nc", "a\nb\nx\nx");
		assertNotNull(option);
		assertFalse(option.conflict());
		assertEquals("a\nb\nx\nx", option.resultText());
	}

	@Test
	public void overrideKeepsLocalDeletions() {
		// the local content replaced lines b and c by X (paired as b -> X and deleting c), the source deleted b:
		// the source wins on b, so X is dropped, c stays deleted locally
		assertNull(InterWikiTrackingService.applySourceChanges("a\nb\nc\nd", "a\nc\nd", "a\nX\nd"));
		assertEquals("a\nd", InterWikiTrackingService.overrideSourceChanges("a\nb\nc\nd", "a\nc\nd", "a\nX\nd"));
	}

	@Test
	public void reviewShowsChangesSinceAcknowledgedVersion() throws Exception {
		// version 2 changed line 2, which is also done locally, version 3 changed line 8: the review (the
		// acknowledged entry) shows the changes since version 1, not only those of version 3
		String base = lines(1, 2, 3, 4, 5, 6, 7, 8);
		String version2 = base.replace("Zeile 2", "Zeile 2 geaendert");
		String version3 = version2.replace("Zeile 8", "Zeile 8 geaendert");
		VersionedAttachment attachment = new VersionedAttachment().add(T1, base).add(T2, version2).add(T3, version3);
		List<InterWikiTrackingService.DiffOption> options = InterWikiTrackingService.getDiffOptions(attachment, version3, version2, 1, 10);
		assertEquals(List.of(1, InterWikiTrackingService.ALL_CHANGES), options.stream().map(InterWikiTrackingService.DiffOption::version).toList());
		assertTrue(options.get(0).acknowledged());
		assertEquals(base, options.get(0).baseText());
		assertEquals(version3, options.get(0).appliedText());
	}

	@Test
	public void localChangesDiffConsidersSingleHiddenLine() {
		// changes at lines 3 and 11: the renderer also shows line 7 between their context lines (a single
		// line is not elided), so a local deviation there makes the diffs look different
		String base = lines(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
		String source = base.replace("Zeile 3", "Zeile 3 geaendert").replace("Zeile 11", "Zeile 11 geaendert");
		assertFalse(isLocalChangesDiffNeeded(base, source, base));
		assertTrue(isLocalChangesDiffNeeded(base, source, base.replace("Zeile 7", "Zeile 7 (lokal)")));
	}

	@Test
	public void sourceDeletionWithinLocallyDeletedBlockNeedsAcknowledgement() throws Exception {
		// D3: the source deletes lines 5-6, the local content deleted lines 4-7 before
		String base = lines(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		TrackingStatus status = InterWikiTrackingService.computeTrackingStatus(lines(1, 2, 3, 4, 7, 8, 9, 10),
				lines(1, 2, 3, 8, 9, 10), ACCEPTED, T2, at -> base);
		assertEquals(State.UNACCEPTED_DIFF, status.state());
		assertTrue(status.warningActive());
	}

	private static boolean isLocalChangesDiffNeeded(String base, String source, String local) {
		return InterWikiTrackingService.isLocalChangesDiffNeeded(new TextDiff(base, source),
				new TextDiff(local, InterWikiTrackingService.overrideSourceChanges(base, source, local)));
	}

	private static String lines(int... numbers) {
		return Arrays.stream(numbers).mapToObj(number -> "Zeile " + number).collect(Collectors.joining("\n"));
	}

	@Test
	public void sourceChangesInLocallyDeletedLines() {
		String base = "a\nb\nc\nd\ne\nf";
		// the local content only keeps the beginning, the source changed a line of the deleted rest: not
		// applied, but still to be acknowledged
		assertEquals("a\nb", InterWikiTrackingService.applySourceChanges(base, "a\nb\nc\nd\nE\nf", "a\nb"));
		// also lines inserted into the deleted rest or appended to it
		assertEquals("a\nb", InterWikiTrackingService.applySourceChanges(base, "a\nb\nc\nd\nd2\ne\nf\ng", "a\nb"));
		assertFalse(InterWikiTrackingService.containsSourceChanges(base, "a\nb\nc\nd\nE\nf", "a\nb"));
		// deleted on both sides: already applied, but not made locally, as the local content deleted more
		assertEquals("a\nb", InterWikiTrackingService.applySourceChanges(base, "a\nb\nc\nf", "a\nb"));
		assertFalse(InterWikiTrackingService.containsSourceChanges(base, "a\nb\nc\nf", "a\nb"));
		// the same lines deleted on both sides: made locally
		assertTrue(InterWikiTrackingService.containsSourceChanges(base, "a\nb\nc\nf", "a (lokal)\nb\nc\nf"));

		// a block deleted locally in the middle: changes there are skipped, changes elsewhere applied,
		// lines inserted next to a remaining local line are applied
		String middle = "a\nb\nf";
		assertEquals("A\nb\nf", InterWikiTrackingService.applySourceChanges(base, "A\nb\nc\nD\nf", middle));
		assertEquals("a\nb\nb2\nf", InterWikiTrackingService.applySourceChanges(base, "a\nb\nb2\nc\nd\ne\nf", middle));
		assertEquals("a\nb\nf", InterWikiTrackingService.applySourceChanges(base, "a\nb\nc\nc2\nd\ne\nf", middle));
		// a change of a remaining line and of the deleted block in one: conflict, the source wins
		assertNull(InterWikiTrackingService.applySourceChanges(base, "a\nB\nC\nd\ne\nf", middle));
		assertEquals("a\nB\nC\nf", InterWikiTrackingService.overrideSourceChanges(base, "a\nB\nC\nd\ne\nf", middle));

		// on a conflict elsewhere, the changes in the deleted lines are not applied either
		assertEquals("a\nB\nf", InterWikiTrackingService.overrideSourceChanges(base, "a\nB\nc\nD\nf", "a\nb (lokal)\nf"));
		// the source deleted lines the local content changed: the source wins
		assertEquals("a\nf", InterWikiTrackingService.overrideSourceChanges(base, "a\nf", "a\nb\nc (lokal)\nd\ne\nf"));
	}

	@Test
	public void applySourceChangesSkipsAlreadyAppliedChanges() {
		assertEquals("a (lokal)\nB\nc\nd",
				InterWikiTrackingService.applySourceChanges("a\nb\nc", "a\nB\nc\nd", "a (lokal)\nB\nc"));
		// the source replaced one line by two, the first one is already applied, the local content deleted the next line
		assertEquals("gelb (lokal)\ngruen/gelb\nviolett",
				InterWikiTrackingService.applySourceChanges("gelb\ngruen\nTKZ", "gelb\ngruen/gelb\nviolett\nTKZ", "gelb (lokal)\ngruen/gelb"));
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
