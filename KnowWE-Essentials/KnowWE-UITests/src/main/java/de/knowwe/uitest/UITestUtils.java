/*
 * Copyright (C) 2015 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.uitest;

import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Utils methods for selenium UI tests.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.07.15
 */
public class UITestUtils {

	public enum UseCase {
		LOGIN_PAGE, NORMAL_PAGE
	}

	/**
	 * Rule allowing for retries if a test fails.
	 */
	public static class RetryRule implements TestRule {

		private int retryCount;

		public RetryRule(int retryCount) {
			this.retryCount = retryCount;
		}

		public Statement apply(Statement base, Description description) {
			return statement(base, description);
		}

		private Statement statement(final Statement base, final Description description) {

			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Throwable caughtThrowable = null;
					for (int i = 0; i < retryCount; i++) {
						try {
							base.evaluate();
							return;
						}
						catch (Throwable t) {
							caughtThrowable = t;
							System.err.println("Run " + (i + 1) + "/" + retryCount + " of '" + description.getDisplayName() + "' failed");
						}
					}
					System.err.println("Giving up after " + retryCount + " failures of '" + description.getDisplayName() + "'");
					throw caughtThrowable;
				}
			};
		}
	}

	/**
	 * Rule allowing for tests to run a defined number of times. Prints failures along the way.
	 */
	public static class RerunRule implements TestRule {

		private int rerunCount;
		private int successes;

		public RerunRule(int rerunCount) {
			this.rerunCount = rerunCount;
			this.successes = 0;
		}

		public Statement apply(Statement base, Description description) {
			return statement(base, description);
		}

		private Statement statement(final Statement base, final Description description) {

			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Throwable caughtThrowable = null;
					for (int i = 0; i < rerunCount; i++) {
						try {
							base.evaluate();
							successes++;
							System.err.println("Run " + (i + 1) + "/" + rerunCount + " of '" + description.getDisplayName() + "' successful");
						}
						catch (Throwable throwable) {
							caughtThrowable = throwable;
							System.err.println("Run " + (i + 1) + "/" + rerunCount + " of '" + description.getDisplayName() + "' failed");
							throwable.printStackTrace();
						}
					}
					System.err.println("Final statistic for " + description.getDisplayName() + ": " + successes + "/" + rerunCount + " successes");
					if (caughtThrowable != null) throw caughtThrowable;
				}
			};
		}
	}

	public static void logIn(WebDriver driver, String username, String password, UseCase use) {
		List<WebElement> elements = null;
		if (use == UseCase.LOGIN_PAGE) {
			elements = driver.findElements(By.id("logincontent"));
		} else if (use == UseCase.NORMAL_PAGE) {
			elements = driver.findElements(By.cssSelector("a.action.login"));
		}

		if (elements == null) {
			throw new NullPointerException("No Login Interface found.");
		} else if (elements.isEmpty()) {
			return; // already logged in
		}

		elements.get(0).click();
		driver.findElement(By.id("j_username")).sendKeys(username);
		driver.findElement(By.id("j_password")).sendKeys(password);
		driver.findElement(By.name("submitlogin")).click();
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.action.logout")));
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("edit-source-button")));
	}

	public static void awaitRerender(WebDriver driver, By by) {
		try {
			if (!driver.findElements(by).isEmpty()) {
				new WebDriverWait(driver, 5).until(ExpectedConditions.stalenessOf(driver.findElement(by)));
			}
		}
		catch (TimeoutException ignore) {
		}
		new WebDriverWait(driver, 5).until(ExpectedConditions.presenceOfElementLocated(by));
	}
}
