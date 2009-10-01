package de.d3web.we.hermes.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SQLReaderForTimelineEvents {

	public static void main(String[] args) {
		List<TimelineEvent> l = readDatabase();
		try {
			String filename = "C:\\wikis\\TimelineEntries.txt";

			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			for (TimelineEvent te : l) {
				out.write(TimelineEventParser.parseTimelineEvent2FileEntry(te));
				out.write("\n\n");
			}
			out.close();
		} catch (IOException e) {
		}

	}

	// double starttime = te.getStartTime();
	// double stoptime = te.getStopTime();
	// if ((starttime > -515 && starttime < -400) || ((stoptime > -515 &&
	// stoptime < -400)))
	// System.out.println(te.getStartTime());

	private static List<TimelineEvent> readDatabase() {
		// start with an empty result list
		ArrayList<TimelineEvent> allEvents = new ArrayList<TimelineEvent>();

		// connect
		System.out.println("MySQL Connect Example.");
		Connection conn = null;
		String url = "jdbc:mysql://localhost:3306/";
		String dbName = "agagzl";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "root";
		String password = "";

		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url + dbName, userName, password);
			System.out.println("Connected to the database");

			// query
			String sqlQuery = "SELECT * from agagzl";
			Statement stmt = null;
			ResultSet rs = null;
			stmt = conn.createStatement();
			if (stmt.execute(sqlQuery)) {
				// here we get the result
				rs = stmt.getResultSet();
			}

			// go through all results
			while (rs.next()) {
				// Get the data from the row using the column header

				String header = rs.getString("ueberschrift");
				String timeString = rs.getString("jahr");
				String abstractText = rs.getString("text");
				String fullText = rs.getString("text2") + rs.getString("text3");
				int relevance = rs.getInt("rel");
				String sourceString = rs.getString("quellen");
				// tokenize sources
				StringTokenizer tokenizer = new StringTokenizer(sourceString, "\n");
				ArrayList<String> sources = new ArrayList<String>();
				while (tokenizer.hasMoreTokens()) {
					sources.add(tokenizer.nextToken());
				}

				TimelineEvent dbe = new TimelineEvent(header, timeString, abstractText, fullText, relevance,
						sources);

				allEvents.add(dbe);
			}

			// close connection
			conn.close();
			System.out.println("Disconnected from database");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return allEvents;
	}
}
