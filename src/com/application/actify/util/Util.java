package com.application.actify.util;

import java.util.Calendar;

public class Util {
	public static Calendar getStartDay (Calendar iCal) {
		Calendar cal = (Calendar) iCal.clone();
		cal.set(Calendar.HOUR_OF_DAY, 0); 
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		return cal;
	}
	
	public static Calendar getEndDay (Calendar iCal) {
		Calendar cal = getStartDay(iCal);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		return cal;
	}
	
	public static Calendar getStartWeek (Calendar iCal) {
		Calendar cal = (Calendar) iCal.clone();
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		return cal;
	}
	
	public static Calendar getEndWeek (Calendar iCal) {
		Calendar cal = getStartWeek(iCal);
		cal.add(Calendar.DAY_OF_YEAR, 7);
		return cal;
	}
}
