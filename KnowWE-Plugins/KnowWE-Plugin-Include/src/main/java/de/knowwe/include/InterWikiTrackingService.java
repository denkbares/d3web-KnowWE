package de.knowwe.include;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.knowwe.textdiff.DiffRenderOptions;
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
		boolean changesContained = false;
		TextDiff sourceChanges = null;
		String acceptedReferenceText = acceptedAt == null ? null : acceptedReferenceLookup.find(acceptedAt);
		if (acceptedReferenceText != null) {
			String normalizedAcceptedReference = normalizeForComparison(acceptedReferenceText);
			warningActive = !normalizedAcceptedReference.equals(normalizedReference);
			if (warningActive) {
				sourceChanges = new TextDiff(normalizedAcceptedReference, normalizedReference);
				// the local content already contains the changes (probably made by the same author), so
				// there is nothing to review
				changesContained = containsSourceChanges(normalizedAcceptedReference, normalizedReference, normalizedLocal);
				if (changesContained) warningActive = false;
			}
		}
		else {
			// acknowledged reference version is unknown (e.g. acknowledged before the attachment was
			// versioned), fall back to the timestamp and the complete differences
			warningActive = acceptedAt == null
					|| (referenceLastModified != null && referenceLastModified.isAfter(acceptedAt));
		}

		return new TrackingStatus(
				changesContained ? State.CONTAINED_DIFF : warningActive ? State.UNACCEPTED_DIFF : State.ACCEPTED_DIFF,
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
	 * @param appliedText    the local content after applying the changes, {@code null} on a conflict
	 * @param overriddenText the local content after applying the changes, the source winning on a conflict
	 * @param acknowledged   whether the option shows the changes since the last acknowledgement
	 */
	record DiffOption(int version, @Nullable Instant date, String baseText, @Nullable String appliedText,
					  String overriddenText, boolean acknowledged) {

		boolean conflict() {
			return appliedText == null;
		}

		/**
		 * The local content after applying the changes, on a conflict the source winning.
		 */
		String resultText() {
			return appliedText == null ? overriddenText : appliedText;
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
					// the acknowledged version replaces the newer entry, so the changes are shown since then
					options.set(options.size() - 1, option);
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
		return new DiffOption(ALL_CHANGES, null, reference, reference, reference, false);
	}

	private static DiffOption createDiffOption(WikiAttachment attachment, ReferenceVersion version, String referenceText, String localText, boolean acknowledged) throws IOException {
		String baseText = normalizeForComparison(Streams.getTextAndClose(attachment.getInputStream(version.version())));
		String appliedText = applySourceChanges(baseText, referenceText, localText);
		String overriddenText = appliedText != null ? appliedText : overrideSourceChanges(baseText, referenceText, localText);
		// e.g. differently aligned repeated lines may conflict, though the source winning changes nothing
		if (overriddenText.equals(normalizeForComparison(localText))) appliedText = overriddenText;
		return new DiffOption(version.version(), version.date(), baseText, appliedText, overriddenText, acknowledged);
	}

	/**
	 * Applies the changes of the reference since the acknowledged version to the local text, i.e. a
	 * three-way merge with the acknowledged reference as base: each change of the reference is
	 * applied at its position in the local text, shifted by the local insertions and deletions before
	 * it, so local deviations elsewhere are kept. Returns {@code null} if a change overlaps a local
	 * deviation (conflict), then nothing is applied. Changes the local content already contains
	 * identically are skipped, as well as changes only of lines deleted locally (see
	 * {@link Merge#isInLocallyDeletedLines}), as the local content does not want these parts of the source.
	 */
	@Nullable
	static String applySourceChanges(@Nullable String acceptedReference, @Nullable String currentReference, @Nullable String local) {
		Merge merge = new Merge(acceptedReference, currentReference, local);
		List<String> result = new ArrayList<>(merge.localLines);
		// apply from the end, so the positions of the changes before stay valid
		List<Change> sourceChanges = merge.getSourceChangesToApply();
		for (int i = sourceChanges.size() - 1; i >= 0; i--) {
			Change change = sourceChanges.get(i);
			int offset = 0;
			for (Change localChange : merge.localChanges) {
				if (overlaps(change, localChange)) return null;
				if (localChange.end() <= change.start()) offset += localChange.lines().size() - (localChange.end() - localChange.start());
			}
			int position = change.start() + offset;
			result.subList(position, position + change.end() - change.start()).clear();
			result.addAll(position, change.lines());
		}
		return String.join("\n", result);
	}

	/**
	 * Whether the changes to apply to the local text have to be shown in addition to the changes of the
	 * reference: if their diffs would look different, as the local text differs at or around the changed
	 * lines (e.g. lines changed by the reference do not exist locally, or the local text has other lines
	 * next to them). If the local text is unchanged there, the changes of the reference suffice.
	 */
	static boolean isLocalChangesDiffNeeded(TextDiff referenceChanges, TextDiff localChanges) {
		return !getShownLines(localChanges).equals(getShownLines(referenceChanges));
	}

	/**
	 * Returns the lines a rendered diff shows: the added and removed lines and the unchanged lines around
	 * them, without their line numbers.
	 */
	private static List<String> getShownLines(TextDiff diff) {
		List<TextDiff.Line> lines = diff.lines();
		int context = DiffRenderOptions.defaults().contextLines();
		if (context < 0) context = lines.size(); // all lines are shown
		boolean[] visible = new boolean[lines.size()];
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).status() == TextDiff.Line.Status.COMMON) continue;
			for (int j = Math.max(0, i - context); j <= Math.min(lines.size() - 1, i + context); j++) {
				visible[j] = true;
			}
		}
		// like the renderer, a single hidden line is shown instead of being elided
		for (int i = 0; i < lines.size(); i++) {
			boolean single = !visible[i] && (i == 0 || visible[i - 1]) && (i == lines.size() - 1 || visible[i + 1]);
			if (single && lines.size() > 1) visible[i] = true;
		}
		List<String> shown = new ArrayList<>();
		for (int i = 0; i < lines.size(); i++) {
			if (visible[i]) shown.add(lines.get(i).status() + ":" + lines.get(i).text());
		}
		return shown;
	}

	/**
	 * Why changes of the reference are not applied to the local text.
	 */
	enum SkipReason {
		/** The local text already contains the change. */
		CONTAINED,
		/** The changed lines do not exist locally (anymore). */
		NOT_EXISTING_LOCALLY
	}

	/**
	 * Returns why the changes of the reference since the acknowledged version are not applied to the local
	 * text, for all changes that are not applied.
	 */
	static Set<SkipReason> getSkipReasons(@Nullable String acceptedReference, @Nullable String currentReference, @Nullable String local) {
		Merge merge = new Merge(acceptedReference, currentReference, local);
		Set<SkipReason> reasons = EnumSet.noneOf(SkipReason.class);
		for (Delta delta : merge.sourceDeltas) {
			if (merge.isMadeLocally(delta)) reasons.add(SkipReason.CONTAINED);
			else if (merge.isInLocallyDeletedLines(delta)) reasons.add(SkipReason.NOT_EXISTING_LOCALLY);
			else if (splitDeletions(delta.changes()).stream().anyMatch(merge::isContainedLocally)) reasons.add(SkipReason.CONTAINED);
		}
		return reasons;
	}

	/**
	 * Whether the local text already contains all changes of the reference since the acknowledged
	 * version, i.e. they were also made locally, so there is nothing to review. Deletions are compared as
	 * a whole here: lines deleted by the source within a larger block deleted locally are not considered
	 * as made locally (the local content just does not want that part), so they are still to be reviewed.
	 */
	static boolean containsSourceChanges(@Nullable String acceptedReference, @Nullable String currentReference, @Nullable String local) {
		Merge merge = new Merge(acceptedReference, currentReference, local);
		return merge.sourceDeltas.stream().allMatch(merge::isMadeLocally);
	}

	/**
	 * Like {@link #applySourceChanges(String, String, String)}, but on a conflict the source wins: the
	 * changes of the source are applied to the accepted reference, together with the local changes not
	 * touching the lines changed by the source. So only the conflicting lines get the text of the source,
	 * the local deviations elsewhere are kept. Local deletions are considered line by line, so lines
	 * deleted locally stay deleted, unless the source changed them.
	 */
	static String overrideSourceChanges(@Nullable String acceptedReference, @Nullable String currentReference, @Nullable String local) {
		String merged = applySourceChanges(acceptedReference, currentReference, local);
		if (merged != null) return merged;

		Merge merge = new Merge(acceptedReference, currentReference, local);
		List<Change> sourceChanges = merge.getSourceChangesToApply();
		List<Change> changes = new ArrayList<>(sourceChanges);
		for (Change localChange : merge.localChanges) {
			if (sourceChanges.stream().noneMatch(sourceChange -> overlaps(sourceChange, localChange))) {
				changes.add(localChange);
			}
		}
		// apply from the end, so the positions of the changes before stay valid; of changes at the same
		// position, the insertion is applied last, so it ends up before the replaced lines, and of
		// insertions at the same position, the local one, so it ends up before the one of the source
		changes.sort(Comparator.comparingInt(Change::start).thenComparingInt(Change::end).reversed());
		List<String> result = new ArrayList<>(merge.base);
		for (Change change : changes) {
			result.subList(change.start(), change.end()).clear();
			result.addAll(change.start(), change.lines());
		}
		return String.join("\n", result);
	}

	/**
	 * The changes of the reference and of the local text, both since the acknowledged version (the base),
	 * as deltas, i.e. the blocks of changed lines. For merging, deletions are considered line by line, so
	 * the deletion of a line on both sides is recognized as the same change, also within larger deleted
	 * blocks.
	 */
	private static final class Merge {
		private final List<String> base;
		private final List<String> localLines;
		private final List<Delta> sourceDeltas;
		private final List<Delta> localDeltas;
		/** The local changes with deletions line by line. */
		private final List<Change> localChanges;
		private final Set<Integer> locallyDeletedLines = new HashSet<>();

		private Merge(@Nullable String acceptedReference, @Nullable String currentReference, @Nullable String local) {
			this.base = toLines(normalizeForComparison(acceptedReference));
			this.localLines = toLines(normalizeForComparison(local));
			this.sourceDeltas = toDeltas(base, toLines(normalizeForComparison(currentReference)));
			this.localDeltas = toDeltas(base, localLines);
			this.localChanges = splitDeletions(localDeltas.stream().flatMap(delta -> delta.changes().stream()).toList());
			for (Change change : localChanges) {
				if (change.lines().isEmpty()) locallyDeletedLines.add(change.start());
			}
		}

		/**
		 * The changes of the source not yet contained in the local text and not of deltas only of lines
		 * deleted locally. A delta only partly of lines deleted locally is applied completely, so it
		 * conflicts with the local deletion.
		 */
		private List<Change> getSourceChangesToApply() {
			return sourceDeltas.stream()
					.filter(delta -> !isInLocallyDeletedLines(delta))
					.flatMap(delta -> splitDeletions(delta.changes()).stream())
					.filter(change -> !isContainedLocally(change))
					.toList();
		}

		/**
		 * Whether the local text contains the change: the same change was made locally, or for an
		 * insertion, the local text inserted these lines (and maybe more) at the same position.
		 */
		private boolean isContainedLocally(Change change) {
			for (Change localChange : localChanges) {
				if (localChange.equals(change)) return true;
				if (change.isInsertion() && localChange.isInsertion() && localChange.start() == change.start()
						&& Collections.indexOfSubList(localChange.lines(), change.lines()) >= 0) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Whether the delta of the source was also made locally exactly like this (deletions as a whole,
		 * so lines deleted by the source within a larger block deleted locally are not made locally).
		 */
		private boolean isMadeLocally(Delta delta) {
			List<Change> madeLocally = localDeltas.stream().flatMap(localDelta -> localDelta.changes().stream()).toList();
			return madeLocally.containsAll(delta.changes());
		}

		/**
		 * Whether the delta only concerns lines deleted locally: all its lines are deleted locally, or for
		 * an insertion, the lines around it (one of them may be the start or end).
		 */
		private boolean isInLocallyDeletedLines(Delta delta) {
			if (delta.start() < delta.end()) {
				for (int line = delta.start(); line < delta.end(); line++) {
					if (!locallyDeletedLines.contains(line)) return false;
				}
				return true;
			}
			boolean deletedBefore = locallyDeletedLines.contains(delta.start() - 1);
			boolean deletedAfter = locallyDeletedLines.contains(delta.start());
			return (deletedBefore || deletedAfter)
					&& (deletedBefore || delta.start() == 0)
					&& (deletedAfter || delta.start() == base.size());
		}
	}

	/**
	 * Splits the deletions of several lines into deletions of single lines.
	 */
	private static List<Change> splitDeletions(List<Change> changes) {
		List<Change> split = new ArrayList<>();
		for (Change change : changes) {
			if (!change.lines().isEmpty()) {
				split.add(change);
				continue;
			}
			for (int line = change.start(); line < change.end(); line++) {
				split.add(new Change(line, line + 1, List.of()));
			}
		}
		return split;
	}

	/**
	 * A change of the base lines [start, end) to the given lines.
	 */
	private record Change(int start, int end, List<String> lines) {
		boolean isInsertion() {
			return start == end;
		}
	}

	/**
	 * A block of changed base lines [start, end) (empty for an insertion), with its changes.
	 */
	private record Delta(int start, int end, List<Change> changes) {
	}

	/**
	 * Returns the deltas from the base to the target. The lines of a delta are paired in order as
	 * changes of single lines, the remaining lines become one insertion (or deletion) at its end. So
	 * changes of neighboring lines on both sides do not overlap, and the change of a line contained in a
	 * larger delta of the other side is recognized as already applied. Applying the changes still results
	 * in the target of the delta.
	 */
	private static List<Delta> toDeltas(List<String> base, List<String> target) {
		List<Delta> deltas = new ArrayList<>();
		for (AbstractDelta<String> delta : DiffUtils.diff(base, target).getDeltas()) {
			int start = delta.getSource().getPosition();
			List<String> source = delta.getSource().getLines();
			List<String> targetLines = delta.getTarget().getLines();
			List<Change> changes = new ArrayList<>();
			int paired = Math.min(source.size(), targetLines.size());
			for (int line = 0; line < paired; line++) {
				if (!source.get(line).equals(targetLines.get(line))) {
					changes.add(new Change(start + line, start + line + 1, List.of(targetLines.get(line))));
				}
			}
			if (source.size() != targetLines.size()) {
				changes.add(new Change(start + paired, start + source.size(), List.copyOf(targetLines.subList(paired, targetLines.size()))));
			}
			deltas.add(new Delta(start, start + source.size(), changes));
		}
		return deltas;
	}

	/**
	 * Whether a change of the source and a local change touch the same base lines. Insertions at the same
	 * position only overlap if the source inserts the local lines (and more), so the source winning loses
	 * nothing. Otherwise both are kept, the lines of the source after the local ones (if the source
	 * inserts the same lines as the local content and less, they are already applied).
	 */
	private static boolean overlaps(Change sourceChange, Change localChange) {
		int start1 = sourceChange.start(), end1 = sourceChange.end(), start2 = localChange.start(), end2 = localChange.end();
		if (sourceChange.isInsertion() && localChange.isInsertion()) {
			return start1 == start2 && Collections.indexOfSubList(sourceChange.lines(), localChange.lines()) >= 0;
		}
		if (sourceChange.isInsertion()) return start2 < start1 && start1 < end2;
		if (localChange.isInsertion()) return start1 < start2 && start2 < end1;
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
		ACCEPTED_DIFF,
		/**
		 * Diff exists, and the reference changed since the acknowledgement, but the local content
		 * already contains these changes, so they need no review.
		 */
		CONTAINED_DIFF
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
