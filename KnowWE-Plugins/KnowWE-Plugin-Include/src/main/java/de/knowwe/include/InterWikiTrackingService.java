package de.knowwe.include;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.knowwe.textdiff.TextDiff;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Streams;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * Central status calculation for InterWikiImport tracking mode.
 * <p>
 * This service derives the current tracking state from:
 * - reference attachment content and last-modified timestamp
 * - local comparison text
 * - optional {@code @trackingAcceptedAt} acknowledgement timestamp, together with the reference
 *   attachment version that was current at that time
 */
final class InterWikiTrackingService {

	/** Maximum number of previous reference versions examined for the "Differences since" selection. */
	private static final int MAX_DIFF_OPTION_CANDIDATES = 30;

	private InterWikiTrackingService() {
	}

	/**
	 * Computes the current tracking status for one InterWikiImport markup section.
	 * <p>
	 * The comparison is trim-based (as planned) and uses the reference attachment as
	 * source of truth for "has remote changed since acceptance?" checks.
	 */
	static TrackingStatus getTrackingStatus(Section<InterWikiImportMarkup> markup) throws IOException {
		return computeTrackingStatus(
				markup.get().getTrackingReferenceText(markup),
				markup.get().getTrackingLocalComparisonText(markup),
				markup.get().getTrackingAcceptedAt(markup),
				markup.get().getTrackingReferenceLastModified(markup),
				acceptedAt -> markup.get().getTrackingAcceptedReferenceText(markup, acceptedAt));
	}

	static TrackingStatus computeTrackingStatus(
			@Nullable String referenceText,
			@Nullable String localComparisonText,
			@Nullable Instant acceptedAt,
			@Nullable Instant referenceLastModified,
			AcceptedReferenceLookup acceptedReferenceLookup) throws IOException {
		boolean localComparisonAvailable = localComparisonText != null;

		if (referenceText == null) {
			return new TrackingStatus(
					State.MISSING_REFERENCE,
					false,
					localComparisonAvailable,
					true,
					normalizeForComparison(localComparisonText).isEmpty(),
					null,
					null,
					null,
					null);
		}

		String normalizedReference = normalizeForComparison(referenceText);
		String normalizedLocal = normalizeForComparison(localComparisonText);
		boolean referenceBlank = normalizedReference.isEmpty();
		boolean localBlank = normalizedLocal.isEmpty();

		if (normalizedReference.equals(normalizedLocal)) {
			return new TrackingStatus(
					State.EQUAL,
					false,
					localComparisonAvailable,
					referenceBlank,
					localBlank,
					null,
					null,
					acceptedAt,
					referenceLastModified);
		}

		// Warning is active until explicitly accepted, and becomes active again once the
		// reference text changed after the last acceptance. Then only the source changes since the
		// acknowledged reference version have to be reviewed, not all local differences again.
		boolean warningActive;
		TextDiff sourceChanges = null;
		String acceptedReferenceText = acceptedAt == null ? null : acceptedReferenceLookup.find(acceptedAt);
		if (acceptedReferenceText != null) {
			String normalizedAcceptedReference = normalizeForComparison(acceptedReferenceText);
			warningActive = !normalizedAcceptedReference.equals(normalizedReference);
			if (warningActive) sourceChanges = new TextDiff(normalizedAcceptedReference, normalizedReference);
		}
		else {
			// acknowledged reference version is unknown (e.g. acknowledged before the attachment was
			// versioned), fall back to the timestamp and the complete differences
			warningActive = acceptedAt == null
					|| (referenceLastModified != null && referenceLastModified.isAfter(acceptedAt));
		}

		return new TrackingStatus(
				warningActive ? State.UNACCEPTED_DIFF : State.ACCEPTED_DIFF,
				warningActive,
				localComparisonAvailable,
				referenceBlank,
				localBlank,
				new TextDiff(normalizedReference, normalizedLocal),
				sourceChanges,
				acceptedAt,
				referenceLastModified);
	}

	/**
	 * Returns the text of the newest version of the attachment stored not after {@code acceptedAt},
	 * or {@code null} if no such version is available (anymore).
	 */
	@Nullable
	static String findReferenceTextAt(@Nullable WikiAttachment attachment, Instant acceptedAt) throws IOException {
		if (attachment == null) return null;
		int version = findVersionAt(attachment, acceptedAt);
		if (version < 0) return null;
		if (version == attachment.getVersion()) return Streams.getTextAndClose(attachment.getInputStream());
		return Streams.getTextAndClose(attachment.getInputStream(version));
	}

	/**
	 * Returns the number of the newest version of the attachment stored not after {@code instant}, or -1
	 * if no such version is available (anymore).
	 */
	static int findVersionAt(@NotNull WikiAttachment attachment, Instant instant) {
		if (attachment.getDate() == null) return -1;
		if (!attachment.getDate().toInstant().isAfter(instant)) return attachment.getVersion();
		for (int version = attachment.getVersion() - 1; version >= 1; version--) {
			Date date = getDate(attachment, version);
			if (date != null && !date.toInstant().isAfter(instant)) return version;
		}
		return -1;
	}

	/**
	 * Returns the versions of the attachment before the current one, newest first, at most {@code limit}.
	 * Deleted versions are skipped.
	 */
	static List<ReferenceVersion> getPreviousVersions(@Nullable WikiAttachment attachment, int limit) {
		List<ReferenceVersion> versions = new ArrayList<>();
		if (attachment == null) return versions;
		for (int version = attachment.getVersion() - 1; version >= 1 && versions.size() < limit; version--) {
			Date date = getDate(attachment, version);
			if (date != null) versions.add(new ReferenceVersion(version, date.toInstant()));
		}
		return versions;
	}

	@Nullable
	private static Date getDate(WikiAttachment attachment, int version) {
		try {
			return attachment.getDate(version);
		}
		catch (IllegalArgumentException e) {
			return null; // version was deleted
		}
	}

	/**
	 * One stored version of the reference attachment.
	 */
	record ReferenceVersion(int version, Instant date) {
	}

	/**
	 * Value of the diff option showing all differences between the local content and the reference.
	 */
	static final int ALL_CHANGES = -1;

	/**
	 * One entry of the "Differences since" selection: the changes of the reference since a previous
	 * version ({@link #ALL_CHANGES} for all differences to the reference) and the local content after
	 * applying them, which is {@code null} if they conflict with a local deviation.
	 *
	 * @param version      the reference version the changes are shown since, or {@link #ALL_CHANGES}
	 * @param date         the date of that version, {@code null} for {@link #ALL_CHANGES}
	 * @param baseText     the reference text of that version
	 * @param appliedText  the local content after applying the changes, {@code null} on a conflict
	 * @param acknowledged whether the option shows the changes since the last acknowledgement
	 */
	record DiffOption(int version, @Nullable Instant date, String baseText, @Nullable String appliedText, boolean acknowledged) {

		boolean conflict() {
			return appliedText == null;
		}

		DiffOption asAcknowledged() {
			return new DiffOption(version, date, baseText, appliedText, true);
		}
	}

	/**
	 * Returns the entries of the "Differences since" selection: the previous versions of the reference,
	 * newest first, but only those that change what would be applied to the local content (versions whose
	 * changes are already contained in the local content show the same as the next newer entry), and at
	 * last all differences to the reference. At most {@code maxOptions} entries, the acknowledged version
	 * is always contained (if available).
	 */
	static List<DiffOption> getDiffOptions(@Nullable WikiAttachment attachment, String referenceText, String localText,
			int acknowledgedVersion, int maxOptions) throws IOException {
		List<DiffOption> options = new ArrayList<>();
		String previousKey = normalizeForComparison(localText);
		boolean acknowledgedContained = false;
		for (ReferenceVersion version : getPreviousVersions(attachment, MAX_DIFF_OPTION_CANDIDATES)) {
			if (options.size() >= maxOptions - 1) break;
			DiffOption option = createDiffOption(attachment, version, referenceText, localText, version.version() == acknowledgedVersion);
			String key = option.conflict() ? "conflict:" + option.baseText() : option.appliedText();
			if (key.equals(previousKey)) {
				// shows the same as the next newer entry (or nothing to apply)
				if (!option.acknowledged()) continue;
				if (!options.isEmpty()) {
					options.set(options.size() - 1, options.get(options.size() - 1).asAcknowledged());
					acknowledgedContained = true;
					continue;
				}
			}
			options.add(option);
			acknowledgedContained |= option.acknowledged();
			previousKey = key;
		}
		if (!acknowledgedContained && acknowledgedVersion > 0 && attachment != null && acknowledgedVersion < attachment.getVersion()) {
			// acknowledged version older than the candidates, still offer the changes since then
			Date date = getDate(attachment, acknowledgedVersion);
			if (date != null) {
				if (options.size() >= maxOptions - 1) options.remove(options.size() - 1);
				options.add(createDiffOption(attachment, new ReferenceVersion(acknowledgedVersion, date.toInstant()),
						referenceText, localText, true));
			}
		}
		options.add(getAllChangesOption(referenceText));
		return options;
	}

	/**
	 * Returns the entry for the changes of the reference since the given version, or all differences
	 * for {@link #ALL_CHANGES}, or {@code null} if the version is not available.
	 */
	@Nullable
	static DiffOption getDiffOption(@Nullable WikiAttachment attachment, int version, String referenceText, String localText) throws IOException {
		if (version == ALL_CHANGES) return getAllChangesOption(referenceText);
		if (attachment == null || version < 1 || version >= attachment.getVersion()) return null;
		Date date = getDate(attachment, version);
		if (date == null) return null;
		return createDiffOption(attachment, new ReferenceVersion(version, date.toInstant()), referenceText, localText, false);
	}

	private static DiffOption getAllChangesOption(String referenceText) {
		String reference = normalizeForComparison(referenceText);
		return new DiffOption(ALL_CHANGES, null, reference, reference, false);
	}

	private static DiffOption createDiffOption(WikiAttachment attachment, ReferenceVersion version, String referenceText, String localText, boolean acknowledged) throws IOException {
		String baseText = normalizeForComparison(Streams.getTextAndClose(attachment.getInputStream(version.version())));
		return new DiffOption(version.version(), version.date(), baseText,
				applySourceChanges(baseText, referenceText, localText), acknowledged);
	}

	/**
	 * Applies the changes of the reference since the acknowledged version to the local text, i.e. a
	 * three-way merge with the acknowledged reference as base: each change of the reference is
	 * applied at its position in the local text, shifted by the local insertions and deletions before
	 * it, so local deviations elsewhere are kept. Returns {@code null} if a change overlaps a local
	 * deviation (conflict), then nothing is applied. Changes the local content already contains
	 * identically are skipped.
	 */
	@Nullable
	static String applySourceChanges(@Nullable String acceptedReference, @Nullable String currentReference, @Nullable String local) {
		List<String> base = toLines(normalizeForComparison(acceptedReference));
		List<String> localLines = toLines(normalizeForComparison(local));
		List<Change> sourceChanges = toChanges(DiffUtils.diff(base, toLines(normalizeForComparison(currentReference))).getDeltas());
		List<Change> localChanges = toChanges(DiffUtils.diff(base, localLines).getDeltas());

		List<String> result = new ArrayList<>(localLines);
		// apply from the end, so the positions of the changes before stay valid
		for (int i = sourceChanges.size() - 1; i >= 0; i--) {
			Change change = sourceChanges.get(i);
			int offset = 0;
			boolean alreadyApplied = false;
			for (Change localChange : localChanges) {
				if (localChange.equals(change)) {
					// the local content already contains the change
					alreadyApplied = true;
					break;
				}
				if (overlaps(change.start(), change.end(), localChange.start(), localChange.end())) return null;
				if (localChange.end() <= change.start()) offset += localChange.lines().size() - (localChange.end() - localChange.start());
			}
			if (alreadyApplied) continue;
			int position = change.start() + offset;
			result.subList(position, position + change.end() - change.start()).clear();
			result.addAll(position, change.lines());
		}
		return String.join("\n", result);
	}

	/**
	 * A change of the base lines [start, end) to the given lines (an insertion has start == end).
	 */
	private record Change(int start, int end, List<String> lines) {
	}

	/**
	 * Converts the deltas to changes of single lines: the lines of a delta are paired in order as
	 * replacements, the remaining lines become one insertion (or deletion) at its end. So changes of
	 * neighboring lines on both sides do not overlap, and a change contained in a larger one of the other
	 * side is recognized as already applied. Applying the changes still results in the target of the delta.
	 */
	private static List<Change> toChanges(List<AbstractDelta<String>> deltas) {
		List<Change> changes = new ArrayList<>();
		for (AbstractDelta<String> delta : deltas) {
			int start = delta.getSource().getPosition();
			List<String> source = delta.getSource().getLines();
			List<String> target = delta.getTarget().getLines();
			int paired = Math.min(source.size(), target.size());
			for (int line = 0; line < paired; line++) {
				if (!source.get(line).equals(target.get(line))) {
					changes.add(new Change(start + line, start + line + 1, List.of(target.get(line))));
				}
			}
			if (source.size() != target.size()) {
				changes.add(new Change(start + paired, start + source.size(), List.copyOf(target.subList(paired, target.size()))));
			}
		}
		return changes;
	}

	/**
	 * Whether two changes of the base lines [start, end) touch the same lines, or insert at the same
	 * position (an insertion has start == end).
	 */
	private static boolean overlaps(int start1, int end1, int start2, int end2) {
		if (start1 == end1 && start2 == end2) return start1 == start2;
		if (start1 == end1) return start2 < start1 && start1 < end2;
		if (start2 == end2) return start1 < start2 && start2 < end1;
		return start1 < end2 && start2 < end1;
	}

	private static List<String> toLines(String text) {
		return text.isEmpty() ? new ArrayList<>() : new ArrayList<>(List.of(text.split("\\R", -1)));
	}

	@FunctionalInterface
	interface AcceptedReferenceLookup {
		/** Returns the reference text that was acknowledged at the given time, if still available. */
		@Nullable
		String find(Instant acceptedAt) throws IOException;
	}

	/**
	 * Normalizes a text for tracking comparison: {@code null}-safe trim. Used for both the
	 * equality check and the {@link TextDiff} input so equal-after-trim cases produce no
	 * visible diff (no leading/trailing empty lines).
	 */
	static String normalizeForComparison(@org.jetbrains.annotations.Nullable String text) {
		return text == null ? "" : Strings.trim(text);
	}

	enum State {
		/** Reference attachment does not exist yet. */
		MISSING_REFERENCE,
		/** Reference and local text are equal after trim-based comparison. */
		EQUAL,
		/** Diff exists and requires user acknowledgement. */
		UNACCEPTED_DIFF,
		/** Diff exists but was already acknowledged for current reference timestamp. */
		ACCEPTED_DIFF
	}

	/**
	 * Immutable tracking snapshot consumed by the InterWikiImport renderer.
	 */
	record TrackingStatus(
			State state,
			boolean warningActive,
			boolean localComparisonAvailable,
			boolean referenceBlank,
			boolean localBlank,
			@Nullable TextDiff diff,
			/* changes of the reference since the acknowledged reference version, if known and unacknowledged */
			@Nullable TextDiff sourceChanges,
			@Nullable Instant trackingAcceptedAt,
			@Nullable Instant referenceLastModified
	) {
		Optional<TextDiff> diffOptional() {
			return Optional.ofNullable(diff);
		}

		/**
		 * True if the local comparison area is empty and the reference attachment has content
		 * — the situation where local copy can be initialized from the source wiki.
		 */
		boolean canInitializeFromReference() {
			return localComparisonAvailable && localBlank && !referenceBlank;
		}
	}
}
