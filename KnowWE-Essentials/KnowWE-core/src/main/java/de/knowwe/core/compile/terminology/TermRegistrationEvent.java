package de.knowwe.core.compile.terminology;

import java.util.Objects;

import com.denkbares.strings.Identifier;
import de.knowwe.core.event.CompilerEvent;

public class TermRegistrationEvent<T extends TermCompiler> implements CompilerEvent<T> {

	private final T compiler;
	private final Identifier identifier;

	private final Class<?> termClass;

	public TermRegistrationEvent(T compiler, Identifier identifier, Class<?> termClass) {
		this.compiler = compiler;
		this.identifier = identifier;
		this.termClass = termClass;
	}

	@Override
	public T getCompiler() {
		return compiler;
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
		if (!(o instanceof TermRegistrationEvent<?> that)) return false;
		return identifier.equals(that.identifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier);
	}
}
