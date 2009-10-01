package de.d3web.we.hermes.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeStringInterpreter {

    private String timeString;
    private final String startString;
    private String endString;
    private Double startTime;
    private Double endTime;

    public TimeStringInterpreter(String timeString) {
	super();
	this.timeString = timeString;
	startTime = Double.NEGATIVE_INFINITY;
	endTime = Double.NEGATIVE_INFINITY;

	String[] stArray = timeString.split("-");
	if (stArray.length > 2) {
	    // do nothing right now
	    // Malformed timeStrings are simply tried to parse...
	}

	startString = stArray[0].trim();
	parseStartOrStopDate(startString);
	if (stArray.length > 1) {
	    endString = stArray[1].trim();
	    parseStartOrStopDate(endString);
	} else {
	    endString = "";
	    endTime = startTime;
	}

	if (startTime > 0 && endTime < 0) {
	    startTime *= -1;
	}
    }

    private String parseStartOrStopDate(String timeS) {
	// on empty String do nothing (e.g. no EndString);
	if (timeS.equals(""))
	    return "";
	double interpretableTime = -10000.0;
	String returnString = "";
	if (timeS.contains("x"))
	    returnString += "ca. ";
	if (timeS.contains("b"))
	    returnString += "bis ";
	if (timeS.contains("e"))
	    returnString += "seit ";

	if (timeS.contains("c"))
	    returnString += "Anfang (des) ";
	if (timeS.contains("e"))
	    returnString += "Ende (des) ";
	if (timeS.contains("m"))
	    returnString += "Mitte (des) ";

	if (timeS.contains("p"))
	    returnString += "nach ";
	if (timeS.contains("a"))
	    returnString += "vor ";

	if (timeS.contains("s"))
	    returnString += "Sommer ";
	if (timeS.contains("f"))
	    returnString += "Frühling ";
	if (timeS.contains("h"))
	    returnString += "Herbst ";
	if (timeS.contains("w"))
	    returnString += "Winter ";

	// find year:
	Pattern p = Pattern.compile("[0-9]+");
	Matcher m = p.matcher(timeS);

	while (m.find()) {
	    String digits = m.group();
	    try {
		interpretableTime = Double.parseDouble(digits);
	    } catch (NumberFormatException nfe) {
		// do nothing
	    }
	    returnString += digits + " ";
	}

	if (timeS.contains("j")) {
	    returnString += ". Jhd. ";
	    interpretableTime *= 100;
	}

	if (timeS.contains("v")) {
	    interpretableTime *= -1;
	}

	// set the parsed Time
	if (timeS.equals(startString))
	    startTime = interpretableTime;
	else
	    endTime = interpretableTime;
	return returnString;
    }

    public String getTimeString() {
	return timeString;
    }

    public void setTimeString(String timeString) {
	this.timeString = timeString;
    }

    public String getOutputString() {
	String resultString = parseStartOrStopDate(startString);

	// if there is a endTime
	if (!endString.equals("")) {
	    // also generates the endTime
	    String outputEndString = parseStartOrStopDate(endString);
	    if (startTime < 0 && endTime > 0)
		resultString += " v. Chr. ";
	    resultString += " bis ";
	    resultString += outputEndString;
	    resultString += (endTime < 0) ? "v. Chr." : "n. Chr.";
	}
	// no end Time
	else {
	    resultString += (startTime < 0) ? "v. Chr." : "n. Chr.";
	}
	return resultString;
    }

    public Double getStartTime() {
	if (startTime == Double.NEGATIVE_INFINITY) {
	    parseStartOrStopDate(startString);
	}
	return startTime;
    }

    public Double getEndTime() {
	if (endTime == Double.NEGATIVE_INFINITY) {
	    parseStartOrStopDate(endString);
	}
	return endTime;
    }

}
