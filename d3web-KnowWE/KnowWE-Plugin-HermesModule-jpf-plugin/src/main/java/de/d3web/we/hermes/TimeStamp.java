package de.d3web.we.hermes;

public class TimeStamp implements Comparable<TimeStamp> {

	String encodedString;
	PointInTime startPoint;

	public TimeStamp(String encodedString) {
		this.encodedString = encodedString;
		if (encodedString.contains("-")) {
			String[] stringParts = encodedString.split("-");
			startPoint = new PointInTime(stringParts[0].trim());
			endPoint = new PointInTime(stringParts[1].trim());

			// check, if string was something like 1000-700v
			// ok, that is not that nice, but that was the initial agreement
			// with
			// historians...
			if ((startPoint.getInterpretableTime() > endPoint
					.getInterpretableTime())
					&& (startPoint.getInterpretableTime() * (-1) < endPoint
							.getInterpretableTime())) {
				startPoint.setInterpretableTime(startPoint
						.getInterpretableTime()
						* (-1));
			}
		}
		else {
			startPoint = new PointInTime(encodedString.trim());
		}
	}

	public PointInTime getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(PointInTime startPoint) {
		this.startPoint = startPoint;
	}

	public PointInTime getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(PointInTime endPoint) {
		this.endPoint = endPoint;
	}

	public boolean hasEndpoint() {
		return endPoint != null;
	}

	PointInTime endPoint;

	@Override
	public String toString() {
		String result = startPoint.toString();
		if (endPoint != null) {
			result += " - " + endPoint;
		}
		return result;
	}

	public String getDescription() {
		return toString();
	}

	public double getInterpretableTime() {
		return startPoint.getInterpretableTime();
	}

	@Override
	public int compareTo(TimeStamp o) {
		return Double.compare(getInterpretableTime(), o.getInterpretableTime());
	}

	public static String decode(String encodedString) {
		return new TimeStamp(encodedString).getDescription();
	}

}
