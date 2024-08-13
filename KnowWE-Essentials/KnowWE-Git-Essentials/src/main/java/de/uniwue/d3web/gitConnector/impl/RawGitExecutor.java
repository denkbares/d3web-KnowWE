package de.uniwue.d3web.gitConnector.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Do NEVER use this class outside of BareGitConnector! It is just meant to keep the BareGitConnector clean
 */
public class RawGitExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(RawGitExecutor.class);

	public static String executeGitCommand(String[] command, String repositoryPath) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(
					command, null, new File(repositoryPath));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		InputStream responseStream = process.getInputStream();

		try {
			int exitVal = process.waitFor();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		String response = null;
		try {
			response = new String(responseStream.readAllBytes());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			process.destroy();
		}

		stopWatch.stop();
		LOGGER.info("Executed command: " + Arrays.toString(command) + " in " + stopWatch.getTime());
		return response;
	}

	public static String executeGitCommand(String command, String repositoryPath) {
		String[] split = command.split(" ");
		return RawGitExecutor.executeGitCommand(split, repositoryPath);
	}

	public static byte[] executeGitCommandWithTempFile(String[] command, String repositoryPath) {

		//TODO maybe it is slow to create temp files on linux?
		File outputFile = null;
		try {
			outputFile = File.createTempFile("git-log-output", ".txt");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		long time = System.currentTimeMillis();

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(new File(repositoryPath));
		processBuilder.redirectOutput(outputFile);

		Process process = null;
		int exitCode = 0;
		try {
			process = processBuilder.start();
			exitCode = process.waitFor();
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (process != null) {
				process.destroy();
			}
		}

		if (exitCode == 0) {
			LOGGER.info("Successfully execute: " + processBuilder.command() + " in " + (System.currentTimeMillis() - time) + "ms");
//			System.out.println("Command executed successfully");
		}
		else {
			LOGGER.error("Failed to execute command with exit code: " + (exitCode) + " for command: " + Arrays.toString(command));
		}

		byte[] response = null;
		try {
			response = Files.readAllBytes(outputFile.toPath());
		}
		catch (IOException e) {

			throw new RuntimeException(e);
		}
		finally {
			outputFile.delete();
		}
		return response;
	}
}
