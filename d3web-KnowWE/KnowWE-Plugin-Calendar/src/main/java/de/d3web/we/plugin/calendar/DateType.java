/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.plugin.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateType {
	
	private int day;
	private int month;
	private int year;
	private int hour;
	private int minute;
	
	public DateType() {
		
		Calendar c = new GregorianCalendar();
		
		year = c.get(1);
		month = c.get(2) + 1;
		day = c.get(5);
		hour = c.get(11);
		minute = c.get(12);
		
	}
	
	public DateType(int day, int month, int year, int hour, int minute) {
		
		this.day = day;
		this.month = month;
		this.year = year;
		this.hour = hour;
		this.minute = minute;
		
	}
	
	public DateType(int day, int month, int year) {
		
		this(day, month, year, 0, 0);
		
	}
	
	public DateType(String date, String time) {
		setDate(date);
		setTime(time);
	}
	
	public DateType(String s) {
		
		String time = "0.0.00";
		String date = "0:0";
		
		String[] dateSplit = s.split(" ");
		
		for(int i = 0; i < dateSplit.length; i++) {
			if(dateSplit[i].length() < 3) continue;
			else {
				if(dateSplit[i].length() > 5) date = dateSplit[i];
				else time = dateSplit[i];
			}
		}
		
		setDate(date);
		setTime(time);
		
	}
	
	// setters:
	private void setDate(String date) {
		
		String[] c = {".", "/", "-"};
		
		date = date.trim();
		
		// set seperator
		String sep = "";
		for(int i = 0; i < c.length; i++) {
			
			if(date.contains(c[i])) {
				if(c[i] == ".") sep = "\\.";
				else sep = c[i];
				break;
			}
			
		}

		int[] array = new int[3];
		String[] dateSplit = date.split(sep);
		for(int i = 0; i < 3; i++) array[i] = Integer.valueOf(dateSplit[i]);
		
		// set date
		if(array[0] > 31) {
			setDate(array[2], array[1], array[1]);
		} else {
			setDate(array[0], array[1], array[2]);
		}
	}
	
	public void setDate(int day, int month, int year) {
		this.day = day;
		this.month = month;
		if(year < 1000) {
			this.year = 2000 + year;
		} else {
			this.year = year;
		}
	}
	
	private void setTime(String time) {
		
		String[] c = {".", ":"};

		time = time.trim();
		
		// set seperator
		String sep = "";
		for(int i = 0; i < c.length; i++) {
			
			if(time.contains(c[i])) {
				if(c[i] == ".") sep = "\\.";
				else sep = c[i];
				break;
			}
			
		}

		int[] array = new int[2];
		String[] timeSplit = time.split(sep);
		for(int i = 0; i < 2; i++) array[i] = Integer.valueOf(timeSplit[i]);
		
		setTime(array[0], array[1]);
	}
	
	public void setTime(int hour, int minute) {
		this.hour = hour;
		this.minute = minute;
	}

	// getters:
	public String getDate() {
		
		String d =  Integer.toString(day);
		String m = Integer.toString(month);
		String y = Integer.toString(year);
		
		if(d.length() < 2) d = "0" + d;
		if(m.length() < 2) m = "0" + m;
			
		return d + "." + m + "." + y;

	}
	
	public String getTime() {

		String h =  Integer.toString(hour);
		String m = Integer.toString(minute);
		
		if(h.length() < 2) h = "0" + h;
		if(m.length() < 2) m = "0" + m;
			
		return h + ":" + m;
		
	}
	
	public int getHour() {
		return hour;
	}
	
	public int getYear() {
		return year;
	}
	
	/**
	 * Returns the number of the day. From sunday = 0 to saturday = 6.
	 * @return day number
	 */
	public int getDay() {
		
		int d = day % 7;
		
		int m = 0;
		
		for(int i = 1; i < month; i++) m = (m + (i==2?28:daysInMonth(i, year))) % 7;
		
		int y1 = year;
		int y2 = 0;
		
		while(y1 > 100) {
			y1 -= 100;
			y2++;
		}
		
		y1 = (y1 + (int) y1 / 4) % 7;
		y2 = (3 - y2 % 4) * 2;
		
		int s = 0;
		if(year % 4 == 0 && month <= 2) s = -1;
		
		return (d + m + y1 + y2 + s) % 7;
		
	}
	
	public void getNextDay() {
		getNextDay(1);
	}

	private void getNextDay(int difference) {
		
		if(difference == 0) return;
		
		if(difference < 0) {
			
			for(int i = difference; i < 0; i++) {
				
				if(day > 1) day--;
				else {
				
					if(month > 1) month--;
					else {
						month = 12;
						year--;
					}
					
					day = days();
					
				}
			}
			
		} else {
			
			for(int i = 0; i < difference; i++) {

				if(day < days()) day++;
				else {
					day = 1;
					if(month < 12) month++;
					else {
						month = 1;
						year++;
					}
				}
			}
		}
	}
	
	public void getDayBefore() {
		getNextDay(-1);
	}
	
	private DateType getDayIn(int days) {
		DateType d = clone();
		d.getNextDay(days);
		return d;
	}
	
	private int getDayOfYear() {
		int d = day;
		for(int i = 1; i < month; i++) {
			d += daysInMonth(i, year);
		}
		return d;
	}
	
	public DateType getMonday() {
		DateType monday = clone();
		
		int today = (monday.getDay() + 6) % 7;
		
		monday.getNextDay((-1)*today);
		monday.setTime(0,0);
		
		return monday;
	}
	
	public static DateType getMonday(int week, int year) {
		DateType d = new DateType(1, 1, year, 0, 0);
		
		d = d.getMonday();
		
		d.getNextDay(7*(week-1));
		
		return d;
		
	}
	
	public DateType[] getTimeIntervall(int days) {
		DateType[] intervall = {getDayIn((int) days/-2), getDayIn((int) days/2)};
		intervall[0].setTime("00:00");
		intervall[1].setTime("23:59");
		return intervall;
	}
	
	public int getWeek() {
		DateType monday = getMonday();
		return (int) ((double) monday.getDayOfYear() / 7 + 1.5);
	}
	
	public int getAbsoluteWeek() {
		DateType monday = getMonday();
		DateType today = new DateType();
		
		if(today.year == year) return getWeek();
		
		int days = monday.getDayOfYear();
		
		if(today.year > year) {

			for(int y = today.year - 1; y >= year; y--) {
				days -= (y % 4 == 0)?366:365;
			}
			
			return (int) ((double) days / 7 + 0.5);
		
		} else {

			for(int y = today.year; y < year; y++) {
				days += (y % 4 == 0)?366:365;
			}
			
			return (int) ((double) days / 7 + 1.5);
		}
		
	}
		
	public static DateType[] getWeek(int week, int year) {
		DateType[] d = new DateType[2];
		
		d[0] = getMonday(week, year);
		d[1] = d[0].clone();
		d[1].getNextDay(6);
		d[1].setTime(23,59);
	
		return d;
	}
	
	private static int daysInMonth(int month, int year) {
		
		int[] y = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		
		if(year % 4 == 0 && month == 2) return 29;
			
		return y[month - 1];
		
	}
	
	private int days() {
		return daysInMonth(month, year);
	}
	
	public int hashCode() {
		return (int) longHashCode()^(3/13);
	}
	
	public long longHashCode() {
		String s = "1";

		String[] tmp = this.getDate().split("\\.");
		
		for(int i = 0; i < tmp.length; i++) {
			s += tmp[i];
		}

		tmp = this.getTime().split(":");
		
		for(int i = 0; i < tmp.length; i++) {
			s += tmp[i];
		}
		
		return Long.valueOf(s); 
	}

	public Calendar toCalendar() {
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.set(year, month, day, hour, minute);
		
		return calendar;
	}
	
	public Date toDate() {
		Date d = new Date();
		
		d.setDate(day);
		d.setMonth(month);
		d.setYear(year);
		d.setHours(hour);
		d.setMinutes(minute);
		
		return d;
	}
	
	public boolean after(DateType d) {
		
		if(year > d.year) return true;
		if(year < d.year) return false;
		
		if(month > d.month) return true;
		if(month < d.month) return false;
		
		if(day > d.day) return true;
		if(day < d.day) return false;
		
		if(hour > d.hour) return true;
		if(hour < d.hour) return false;
		
		if(minute > d.minute) return true;
		if(minute < d.minute) return false;
		
		return false;
	}
	
	public boolean before(DateType d) {
		if (this.equals(d)) {
			return false;
		} else {
			return !this.after(d);
		}
	}
	
	public boolean equals(DateType d) {
		return year == d.year && month == d.month && day == d.day 
		&& hour == d.hour && minute == d.minute;
	}
	
	public boolean sameDay(DateType d) {
		return year == d.year && month == d.month && day == d.day;
	}
	
	public DateType clone() {
		DateType d = new DateType();
		
		d.day = day;
		d.month = month;
		d.year = year;
		d.hour = hour;
		d.minute = minute;
		
		return d;
	}
	
	public String toString() {
		return getDate() + " " + getTime();
	}

}
