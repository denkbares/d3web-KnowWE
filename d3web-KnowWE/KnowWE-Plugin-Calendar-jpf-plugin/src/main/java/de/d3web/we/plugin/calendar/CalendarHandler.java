/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.plugin.calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class CalendarHandler extends AbstractTagHandler {

	protected static DateType day = new DateType();
	private static boolean firstStart = true;
	private static ResourceBundle cb;

	public CalendarHandler() {
		super("calendar");
	}

	public static ResourceBundle getCalendarBundle() {
		cb = CalendarModule.getCalendarBundle();
		return cb;
	}

	public static ResourceBundle getCalendarBundle(KnowWEUserContext user) {
		cb = CalendarModule.getCalendarBundle(user);
		return cb;
	}

	public static List<CalendarEntry> getAppointments(String web) {
		return getAppointments(null, null, web);
	}

	private static List<CalendarEntry> getAppointments(String author, String web) {
		return getAppointments(null, author, web);
	}

	private static List<CalendarEntry> getAppointments(DateType[] intervall, String author, String web) {

		List<CalendarEntry> result = new ArrayList<CalendarEntry>();

		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();

		Map<String, String> articleMap = instance.getWikiConnector().getAllArticles(web);

		for (String elem : articleMap.keySet()) {

			Map<String, Section<Appointment>> found = new HashMap<String, Section<Appointment>>();

			KnowWEArticle article = instance.getArticle(web, elem);

			if (article == null) continue; // TODO perhaps some dates are not
											// imported

			article.getSection().findSuccessorsOfTypeAsMap(Appointment.class, found);

			for (java.util.Map.Entry<String, Section<Appointment>> mapEntry : found.entrySet()) {
				Section[] s = new Section[4];
				String[] entry = new String[4];

				s[0] = mapEntry.getValue().findChildOfType(AppointmentStartSymbol.class).findChildOfType(
						AppointmentDate.class);
				s[1] = mapEntry.getValue().findChildOfType(AppointmentStartSymbol.class).findChildOfType(
						AppointmentTime.class);
				s[2] = mapEntry.getValue().findChildOfType(AppointmentAuthor.class);
				s[3] = mapEntry.getValue().findChildOfType(AppointmentText.class);

				for (int j = 0; j < 4; j++) {
					if (s[j] != null) entry[j] = s[j].getOriginalText();
					else entry[j] = "-NA-";
				}

				CalendarEntry addEntry = new CalendarEntry(entry);
				DateType date = addEntry.getDate();

				if ((author == null || addEntry.getAuthor().equals(author))
						&& (intervall == null || date.equals(intervall[0])
								|| date.equals(intervall[1])
								|| (date.after(intervall[0])
									&& date.before(intervall[1])))) {
					result.add(addEntry);
				}
			}
		}
		Collections.sort(result);
		return result;
	}

	public static int search(List<CalendarEntry> apps, DateType date) {
		return search(apps, date, true);
	}

	private static int search(List<CalendarEntry> apps, DateType dateType, boolean after) {
		String author = ""; // To provide that ce is set on the right place
		if (!after) {
			author = "ZZZZZZZZZZ";
		}
		CalendarEntry ce = new CalendarEntry(new String[] {
				dateType.getDate(), dateType.getTime(), author, "" });
		apps.add(ce);
		Collections.sort(apps);
		int left = 0;
		int right = apps.size() - 1;
		int mid = (right + left) / 2;
		while (left < right) {
			if (apps.get(mid).getDate().equals(dateType)) {
				left = mid;
				right = mid;
			}
			else if (apps.get(mid).getDate().before(dateType)) {
				left = mid;
				mid = (right + left + 1) / 2;
			}
			else {
				right = mid;
				mid = (right + left) / 2;
			}
		}
		int direc = after == true ? -1 : 1;
		while (mid + direc >= 0 && mid + direc < apps.size()
				&& apps.get(mid).getDate().equals(apps.get(mid + direc).getDate())) {
			mid += direc;
		}
		apps.remove(ce);
		return mid;
	}

	private int translateIntervall(String s) {

		int size = s.length() - 1;

		for (int i = 0; i <= size; i++) {
			if (i != size && !Character.isDigit(s.charAt(i))) return 0;
			else if (i == size) {
				if (!Character.isDigit(s.charAt(i))) {
					switch (s.charAt(i)) {
					case 'm':
						return 30 * Integer.valueOf(s.substring(0, size));
					case 'y':
						return 365 * Integer.valueOf(s.substring(0, size));
					default:
						return Integer.valueOf(s.substring(0, size));
					}
				}
				else {
					return Integer.valueOf(s);
				}
			}
		}

		return 0;
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {

		DateType today = new DateType();

		getCalendarBundle(user);

		String js = "<script type=text/javascript src=KnowWEExtension/scripts/CalendarPlugin.js></script>\n";

		// load Action once after server restart (javascript-failure)
		if (user.getUserName() != "Guest" && firstStart) {
			js += "<script>loadAction('" + today.getDate() + "')</script>\n";
			firstStart = false;
		}

		DateType[] intervall = new DateType[2];

		String author = values.get("author");

		int week = 1;

		if (values.containsKey("method") && values.get("method").equals("list") ||
				values.get("calendar").equals("list")) {

			String buttons = "";

			// buttons activated
			if (values.containsKey("navigatable") || values.containsKey("navigable")) {

				if (user.getUrlParameterMap().containsKey("week")) {
					week = Integer.valueOf(user.getUrlParameterMap().get("week"));
					day = DateType.getMonday(week, day.getYear());
				}
				else {
					if (!user.getUrlParameterMap().containsKey("manually")) day = today;
					week = day.getAbsoluteWeek();
				}

				buttons = KnowWEEnvironment.maskHTML(js) + buttons(topic, week);
			}

			if (values.containsKey("time")) {

				String time = values.get("time");

				// "show all" activated:
				if (time.equals("all")) return renderList(getAppointments(author, web));

				intervall = day.getTimeIntervall(translateIntervall(time));

			}
			else return renderList(getAppointments(author, web));

			return buttons + renderList(getAppointments(intervall, author, web));

		}
		else {

			if (user.getUrlParameterMap().containsKey("week")) {
				week = Integer.valueOf(user.getUrlParameterMap().get("week"));
				day = today;
			}
			else {
				if (!user.getUrlParameterMap().containsKey("manually")) day = today;
				week = day.getAbsoluteWeek();
			}

			intervall = DateType.getWeek(week, today.getYear());

			// wide calendar activated
			if (values.containsKey("wide")) {
				return KnowWEEnvironment.maskHTML(js) + buttons(topic, week)
						+ renderWeekWide(intervall, getAppointments(intervall, author, web));
			}

			return KnowWEEnvironment.maskHTML(js) + buttons(topic, week) +
					renderWeek(intervall, getAppointments(intervall, author, web));
		}
	}

	private static String buttons(String topic, int week) {

		StringBuffer buttons = new StringBuffer();

		buttons.append("<a name=Calendar></a><table style=border-style:hidden width=98%><tr>");

		// "previous week"-button
		buttons.append("<td style=text-align:left;border-style:hidden>");
		buttons.append("<button title='"
				+ cb.getString("KnowWE2-Plugin-CalendarHandler.previous")
				+ "' onclick=link('"
				+ topic
				+ "','"
				+ (week - 1)
				+ "')>"
				+
						"<img src=KnowWEExtension/images/calendar_previous.png title='previous week'>"
				+
							"</button>");
		buttons.append("</td>");

		// "goto date"-formular
		buttons.append("<td style=text-align:center;border-style:hidden>");
		buttons.append("<form name=date method=post action=Wiki.jsp?page="
				+ topic
				+ "&manually#Calendar accept-charset=UTF-8> "
				+
						"<input type=text name=input value='"
				+ day.getDate()
				+ "' size='12' maxlength='10'>"
				+
						" <button title='"
				+ cb.getString("KnowWE2-Plugin-CalendarHandler.goto")
				+ "' type=submit name=submit onclick=gotoDate()>"
				+
						"<img src=KnowWEExtension/images/calendar_go.png title='go to date'></button>");

		DateType today = new DateType();
		if (!day.sameDay(today)) {

			// "today"-button
			buttons.append("<input type=hidden name=today value='" + today.getDate()
					+ "' size='12' maxlength='10'>" +
					" <button title='" + cb.getString("KnowWE2-Plugin-CalendarHandler.today")
					+ "' type=submit name=submit onclick=gotoToday()>" +
					"<img src=KnowWEExtension/images/calendar_today.png title='today'></button>");
		}

		buttons.append("</form></td>");

		// "next week"-button
		buttons.append("<td style=text-align:right;border-style:hidden>");
		buttons.append("<button title='" + cb.getString("KnowWE2-Plugin-CalendarHandler.next")
				+ "' onclick=link('" + topic + "','" + (week + 1) + "')>" +
						"<img src=KnowWEExtension/images/calendar_next.png tilte='next week'>" +
						"</button></td>");
		buttons.append("</tr></table>");

		return KnowWEEnvironment.maskHTML(buttons.toString());
	}

	private String renderWeek(DateType[] intervall, List<CalendarEntry> appointments) {

		DateType today = new DateType();

		// import calendar.css
		StringBuffer toHTML = new StringBuffer(
				"<link rel=stylesheet type=text/css href=KnowWEExtension/css/calendar.css>");

		String[] days = (cb.getString("KnowWE2-Plugin-CalendarHandler.days")).split(", ");

		toHTML.append("<table class=calendar width=98% border=1>\n");

		// header:
		toHTML.append("<tr>\n");

		DateType d = intervall[0].clone();

		for (int i = 0; i < 7; i++) {
			toHTML.append("<th width=14%");
			if (d.sameDay(today)) toHTML.append(" class=today");
			toHTML.append(">" + days[d.getDay()] + "<br>" + d.getDate() + "</th>\n");
			d.getNextDay();
		}

		toHTML.append("</tr>\n<tr>");

		// content:
		d = intervall[0];

		int j = 0;
		CalendarEntry entry = null;

		if (appointments != null && appointments.size() > 0) entry = appointments.get(0);

		for (int i = 0; i < 7; i++) {
			toHTML.append("<td");

			if (today.sameDay(d)) toHTML.append(" class=today");

			toHTML.append(" height=200>");

			if (entry != null && entry.getDate().sameDay(d)) {
				toHTML.append("<table class=appointment border=1 width=95%>");

				while (entry != null && entry.getDate().sameDay(d)) {

					toHTML.append("<tr><td style=text-align:left><b>" + entry.getDate().getTime()
							+ "</b> " +
								"("
							+ (entry.getAuthor() == "-NA-" ? "<i>-NA-</i>" : entry.getAuthor())
							+ ")<br>" + entry.getValue() + "</td></tr>\n");

					j++;
					if (appointments.size() > j) entry = appointments.get(j);
					else break;
				}

				toHTML.append("</table>\n");

			}

			toHTML.append("</td>\n");
			d.getNextDay();
		}

		toHTML.append("</tr></table>\n");
		return KnowWEEnvironment.maskHTML(toHTML.toString());
	}

	private String renderWeekWide(DateType[] intervall, List<CalendarEntry> appointments) {

		DateType today = new DateType();

		// import calendar.css
		StringBuffer toHTML = new StringBuffer(
				"<link rel=stylesheet type=text/css href=KnowWEExtension/layout/calendar.css>");

		String[] days = (cb.getString("KnowWE2-Plugin-CalendarHandler.days")).split(", ");

		toHTML.append("<table class=calendar width=98% border=1>\n");

		toHTML.append("<tr>\n");

		DateType d = intervall[0].clone();

		int j = 0; // number of saved appointments in list yet
		int t = -1; // is set if today is in rendered week

		CalendarEntry[][] entries = new CalendarEntry[7][];

		for (int i = 0; i < 7; i++) {

			// header
			toHTML.append("<th width=14%");
			if (d.sameDay(today)) {
				toHTML.append(" class=today");
				t = i;
			}
			toHTML.append(">" + days[d.getDay()] + "<br>" + d.getDate() + "</th>\n");

			// import appointments
			List<CalendarEntry> list = new ArrayList<CalendarEntry>();

			if (appointments != null) {

				for (int k = j; k < appointments.size(); k++) {
					CalendarEntry entry = appointments.get(k);
					if (d.sameDay(entry.getDate())) {
						list.add(entry);
					}
					else { // --> next day
						j = k;
						break;
					}
				}
			}

			CalendarEntry[] tmp = new CalendarEntry[list.size()];

			for (int k = 0; k < tmp.length; k++) {
				tmp[k] = list.get(k);
			}

			entries[i] = tmp;
			d.getNextDay();
		}

		toHTML.append("</tr>\n");

		int[] x = {
				0, 0, 0, 0, 0, 0, 0 }; // status for each day in entries[]

		CalendarEntry entry = null;

		// content:
		for (int h = 0; h < 24; h++) { // hour

			toHTML.append("<tr>");

			for (int day = 0; day < 7; day++) { // day

				toHTML.append("<td");
				if (day == t) toHTML.append(" class=today");
				toHTML.append(">");

				// Appointments
				if (entries[day].length > x[day]) {
					entry = entries[day][x[day]];

					if (entry.getDate().getHour() == h) {

						toHTML.append("<table class=appointment width=95%>");

						while (entry.getDate().getHour() == h) {

							toHTML.append("<tr style=text-align:left><td><b>"
									+ entry.getDate().getTime()
									+ "</b> "
									+
										"("
									+ (entry.getAuthor() == "-NA-"
											? "<i>-NA-</i>"
											: entry.getAuthor()) + ")<br>" + entry.getValue()
									+ "</td></tr>\n");

							x[day]++;
							if (entries[day].length > x[day]) entry = entries[day][x[day]];
							else break;
						}

						toHTML.append("</table>\n");
					}
				}

				toHTML.append("</td>\n");
			}

			toHTML.append("</tr>");
		}

		toHTML.append("</table>\n");

		return KnowWEEnvironment.maskHTML(toHTML.toString());
	}

	private String renderList(List<CalendarEntry> appointments) {
		StringBuffer toHTML = new StringBuffer();

		toHTML.append("<table class=wikitable width=98% border=0>\n");

		toHTML.append("<tr><th width=100>" + cb.getString("KnowWE2-Plugin-CalendarHandler.date")
				+ "</th><th width=50>" + cb.getString("KnowWE2-Plugin-CalendarHandler.time")
				+ "</th><th width=50>" + cb.getString("KnowWE2-Plugin-CalendarHandler.person")
				+ "</th><th>" + cb.getString("KnowWE2-Plugin-CalendarHandler.text")
				+ "</th></tr>\n");

		if (appointments != null && appointments.size() > 0) {
			for (int i = 0; i < appointments.size(); i++) {
				CalendarEntry entry = appointments.get(i);
				toHTML.append("<tr><td style=text-align:center>" + entry.getDate().getDate()
						+ "</td>" +
							"<td style=text-align:center>" + entry.getDate().getTime() + "</td>" +
									"<td style=text-align:center>"
						+ (entry.getAuthor() == "-NA-" ? "<i>-NA-</i>" : entry.getAuthor())
						+ "</td>" +
											"<td>" + entry.getValue() + "</td></tr>\n");
			}
		}
		else toHTML.append("<tr><td colspan=4 style=text-align:center><i>"
				+ cb.getString("KnowWE2-Plugin-CalendarHandler.empty") + "</i></td></tr>");

		toHTML.append("</table>\n");
		return KnowWEEnvironment.maskHTML(toHTML.toString());
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return getCalendarBundle(user).getString("KnowWE2-Plugin-CalendarHandler.description");
	}

	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin calendar, method=list, author=XY, time=10d}]";
	}

}
