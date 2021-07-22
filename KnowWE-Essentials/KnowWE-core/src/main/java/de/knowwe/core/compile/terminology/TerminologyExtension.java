package de.knowwe.core.compile.terminology;

public interface TerminologyExtension {

	String[] getTermNames();

	Class<? extends de.knowwe.core.compile.Compiler> getCompilerClass();
}
