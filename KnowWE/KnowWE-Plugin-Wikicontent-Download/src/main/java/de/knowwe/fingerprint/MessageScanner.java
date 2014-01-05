package de.knowwe.fingerprint;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;

public class MessageScanner implements Scanner {

	@Override
	public void scan(Article article, File target) throws IOException {
		PrintStream printStream = new PrintStream(target);
		try {
			printMessages(article.getRootSection(), printStream);
		}
		finally {
			printStream.close();
		}
	}

	private void printMessages(Section<?> section, PrintStream out) {
		// create all compilers for section
		Collection<Compiler> compilers = Compilers.getCompilers(section,
				Compiler.class);

		// print the messages of the section
		for (Type type : Type.values()) {
			for (Compiler compiler : compilers) {
				Collection<Message> messages = Messages.getMessages(compiler, section, type);
				for (Message message : messages) {
					out.printf("%s: '%s\n", type.name(), message.getVerbalization());
				}
			}
			Collection<Message> messages = Messages.getMessages(section, type);
			for (Message message : messages) {
				out.printf("%s: '%s\n", type.name(), message.getVerbalization());
			}
		}

		// print successors
		for (Section<?> child : section.getChildren()) {
			printMessages(child, out);
		}
	}

	@Override
	public String getExtension() {
		return ".msg";
	}

	@Override
	public Diff compare(File file1, File file2) throws IOException {
		return Fingerprint.compareTextFiles(file1, file2);
	}

	@Override
	public String getItemName() {
		return "Messages";
	}

}
