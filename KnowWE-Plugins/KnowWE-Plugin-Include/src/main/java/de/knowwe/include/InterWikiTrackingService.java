package de.knowwe.include;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.denkbares.knowwe.textdiff.TextDiff;
import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Central status calculation for InterWikiImport tracking mode.
 * <p>
 * This service derives the current tracking state from:
 * - reference attachment content and last-modified timestamp
 * - local comparison text
 * - optional {@code @trackingAcceptedAt} acknowledgement timestamp
 */
final class InterWikiTrackingService {

	private InterWikiTrackingService() {
	}

	/**
	 * Computes the current tracking status for one InterWikiImport markup section.
	 * <p>
	 * The comparison is trim-based (as planned) and uses the reference attachment as
	 * source of truth for "has remote changed since acceptance?" checks.
	 */
	static TrackingStatus getTrackingStatus(Section<InterWikiImportMarkup> markup) throws IOException {
		String referenceText = markup.get().getTrackingReferenceText(markup);
		String localComparisonText = markup.get().getTrackingLocalComparisonText(markup);
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
					markup.get().getTrackingAcceptedAt(markup),
					markup.get().getTrackingReferenceLastModified(markup));
		}

		Instant acceptedAt = markup.get().getTrackingAcceptedAt(markup);
		Instant referenceLastModified = markup.get().getTrackingReferenceLastModified(markup);
		// Warning is active until explicitly accepted, and becomes active again once the
		// reference attachment changed after the last acceptance.
		boolean warningActive = acceptedAt == null
				|| (referenceLastModified != null && referenceLastModified.isAfter(acceptedAt));

		return new TrackingStatus(
				warningActive ? State.UNACCEPTED_DIFF : State.ACCEPTED_DIFF,
				warningActive,
				localComparisonAvailable,
				referenceBlank,
				localBlank,
				new TextDiff(normalizedReference, normalizedLocal),
				acceptedAt,
				referenceLastModified);
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
