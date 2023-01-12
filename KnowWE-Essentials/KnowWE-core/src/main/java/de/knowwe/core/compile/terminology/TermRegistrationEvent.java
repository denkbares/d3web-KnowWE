package de.knowwe.core.compile.terminology;

import java.util.Objects;

import com.denkbares.strings.Identifier;
import de.knowwe.core.event.CompilerEvent;

public class TermRegistrationEvent extends CompilerEvent {

	private final Identifier identifier;

	private final Class<?> termClass;

	public TermRegistrationEvent(TermCompiler compiler, Identifier identifier, Class<?> termClass) {
		super(compiler);
		this.identifier = identifier;
		this.termClass = termClass;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public Class<?> getTermClass() {
		return termClass;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (! (o instanceof TermRegistrationEvent that)) return false;
		return identifier.equals(that.identifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier);
	}
}
