package com.phoenix.answer.util;

import static java.util.stream.Collectors.toCollection;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class DateAdjuster {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter
			.ofPattern("yyyyMMdd");

	private DateAdjuster() {
	}

	/**
	 * calculate the last days starting from param startDate 
	 * @param startDate
	 * @param nbDays
	 * @return
	 */
	public static List<LocalDate> getLastKPreviousDays(LocalDate startDate,
			int nbDays) {
		return Stream//
				.iterate(startDate, date -> date.minusDays(1))//
				.limit(nbDays)
				.collect(toCollection(() -> new ArrayList<>(nbDays)));
	}
	
	/**
	 * format localDate
	 * @param startDate
	 * @return
	 */
	public static String format(LocalDate startDate) {
		return FORMATTER.format(startDate);
	}
}
