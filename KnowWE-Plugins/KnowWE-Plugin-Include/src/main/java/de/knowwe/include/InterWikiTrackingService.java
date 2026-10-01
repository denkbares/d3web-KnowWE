package de.knowwe.include;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.denkbares.knowwe.textdiff.TextDiff;
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
		if (attachment == null || attachment.getDate() == null) return null;
		if (!attachment.getDate().toInstant().isAfter(acceptedAt)) {
			return Streams.getTextAndClose(attachment.getInputStream());
		}
		for (int version = attachment.getVersion() - 1; version >= 1; version--) {
			Date date;
			try {
				date = attachment.getDate(version);
			}
			catch (IllegalArgumentException e) {
				continue; // version was deleted
			}
			if (date != null && !date.toInstant().isAfter(acceptedAt)) {
				return Streams.getTextAndClose(attachment.getInputStream(version));
			}
		}
		return null;
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
