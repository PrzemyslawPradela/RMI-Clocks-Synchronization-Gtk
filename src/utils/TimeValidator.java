package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeValidator {
	private Pattern pattern;
	private Matcher matcher;

	private static final String TIME_PATTERN = "^(?:[01]\\d|2[0123]):(?:[012345]\\d):(?:[012345]\\d)$";

	public TimeValidator() {
		pattern = Pattern.compile(TIME_PATTERN);
	}

	public boolean validate(String digits) {
		matcher = pattern.matcher(digits);
		return matcher.matches();
	}
}
