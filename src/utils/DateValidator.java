package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateValidator {
	private Pattern pattern;
	private Matcher matcher;

	private static final String DATE_PATTERN = "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$";

	public DateValidator() {
		pattern = Pattern.compile(DATE_PATTERN);
	}

	public boolean validate(String digits) {
		matcher = pattern.matcher(digits);
		return matcher.matches();
	}
}
