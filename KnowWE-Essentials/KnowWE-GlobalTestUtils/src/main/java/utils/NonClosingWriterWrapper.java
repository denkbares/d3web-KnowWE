package utils;

import java.io.IOException;
import java.io.Writer;

import org.jetbrains.annotations.NotNull;

/**
 * This class is used for testing. It doesn't close the delegate writer, so the written text can later be accessed.
 * For example, using a jackson object mapper would otherwise directly close the stream.
 * DO NOT use it productively to avoid memory leaks.
 */
class NonClosingWriterWrapper extends Writer {

	private final Writer delegate;

	public NonClosingWriterWrapper(Writer delegate) {
		this.delegate = delegate;
	}

	@Override
	public void write(char @NotNull [] cbuf, int off, int len) throws IOException {
		delegate.write(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		// Do nothing to prevent the writer from being closed
	}

}
