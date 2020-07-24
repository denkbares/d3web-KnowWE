/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.utils.progress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message;

/**
 * Abstract class for LongOperations with some basic functionality
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 05.10.2013
 */
public abstract class AbstractLongOperation implements LongOperation {

	private final DefaultAjaxProgressListener progressListener;
	private boolean canceled = false;
	private String id;
	private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());

	public AbstractLongOperation() {
		this(new DefaultAjaxProgressListener());
	}

	public AbstractLongOperation(@NotNull DefaultAjaxProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	public void addMessage(@NotNull Message msg) {
		this.messages.add(msg);
	}

	public boolean hasError() {
		return hasMessage(Message.Type.ERROR);
	}

	public boolean hasMessage(Message.Type type) {
		synchronized (messages) {
			for (Message msg : messages) {
				if (msg.getType() == type) return true;
			}
			return false;
		}
	}

	/**
	 * Returns the report of the current operation. As the default implementation this method returns the list of
	 * messages added to this operation since the last start of the operation.
	 *
	 * @param context the current user viewing the messages
	 * @param result  the result to write the report to
	 * @created 17.02.2014
	 */
	private void renderReport(UserActionContext context, RenderResult result) {
		RenderResult errors = new RenderResult(result);
		RenderResult warnings = new RenderResult(result);
		RenderResult other = new RenderResult(result);
		if (!messages.isEmpty()) result.appendHtml("<br>");
		synchronized (messages) {
			for (Message msg : messages) {
				Message.Type type = msg.getType();
				String details = msg.getDetails();
				RenderResult builder = (type == Message.Type.ERROR) ? errors :
						(type == Message.Type.WARNING) ? warnings : other;
				if (builder.length() > 0) {
					builder.appendHtml("<br>");
				}
				builder.append(msg.getVerbalization());
				if (!Strings.isBlank(details)) {
					builder.append(" ");
					builder.appendHtmlTag("span", "title", details, "class", "tooltipster")
							.appendHtml("<img src='KnowWEExtension/images/dt_icon_q_description_small.png'></img></span>");
				}
			}
		}

		if (errors.length() > 0) {
			result.appendHtml("<span class='error'>").append(errors).appendHtml("</span>");
		}
		if (warnings.length() > 0) {
			result.appendHtml("<span class='warning'>").append(warnings).appendHtml("</span>");
		}
		if (other.length() > 0) {
			result.appendHtml("<span class='information'>").append(other).appendHtml("</span>");
		}
	}

	@Override
	public void renderMessage(UserActionContext context, RenderResult result) {
		// check whether the user is the current one
		// and whether the progress allows to show the final actions
		// if not, simply return the current message
		if (getProgressListener().getProgress() < 1f) {
			result.append(getProgressListener().getMessage());
			return;
		}

		// if we have completed and the user is the requesting one
		// we show some more detailed information and the actions
		renderReport(context, result);
	}

	@Override
	public void reset() {
		this.canceled = false;
		this.progressListener.updateProgress(0, "");
		this.progressListener.setError(null);
		clearMessages();
	}

	public void clearMessages() {
		synchronized (messages) {
			this.messages.clear();
		}
	}

	@Override
	public void doFinally() {
		// default implementation doing nothing
	}

	@Override
	public void cleanUp() {
		// default implementation doing nothing
	}

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public String getId() {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		return id;
	}

	@NotNull
	@Override
	public DefaultAjaxProgressListener getProgressListener() {
		return progressListener;
	}
}
