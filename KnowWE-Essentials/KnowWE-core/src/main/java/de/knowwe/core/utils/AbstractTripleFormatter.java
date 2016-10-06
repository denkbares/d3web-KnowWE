package de.knowwe.core.utils;

/**
 * Provides additional utils for formatting Triples.
 *
 * Created by Adrian Müller on 05.10.16.
 */
public abstract class AbstractTripleFormatter extends AbstractFormatter {
	protected int addDepth = 0; // additional indents for TripleParts
	protected TriplePart currentPart = TriplePart.NONE;

	protected AbstractTripleFormatter(String wikiText) {
		super(wikiText);
	}

	protected void resetIndentSpecials() {
		currentPart = TriplePart.NONE;
		if (depth > addDepth) {
			depth -= addDepth;
		}
		else {
			depth = 0;
		}
		addDepth = 0;
	}

	@Override
	protected int skip(int i, char begin, char end) {
		currentPart = currentPart.next();
		return super.skip(i, begin, end);
	}

	@Override
	protected int handleNewline(int i) {
		if (currentPart == TriplePart.THIRD) {
			resetIndentSpecials();
		}
		super.handleNewline(i);
		return i;
	}

	protected int handleSemicolon(int i) {
		i = handlePoint(i);
		addDepth++;
		depth++;
		currentPart = TriplePart.FIRST;
		return i;
	}

	protected int handlePoint(int i) {
		resetIndentSpecials();
		i = guaranteeBefore(i, ' ');
		removeFollowingSpaces(i, false);
		guaranteeNext(i, '\n');
		return i;
	}

	public enum TriplePart { // access must be public
		FIRST, SECOND, THIRD, NONE;

		public TriplePart next() {
			switch (this) {
				case NONE:
					return TriplePart.FIRST;
				case FIRST:
					return TriplePart.SECOND;
				case SECOND:
					return TriplePart.THIRD;
				default:
					return TriplePart.NONE;
			}
		}

		public TriplePart prev() {
			switch (this) {
				case SECOND:
					return TriplePart.FIRST;
				case THIRD:
					return TriplePart.SECOND;
				default:
					return TriplePart.NONE;
			}
		}
	}
}
