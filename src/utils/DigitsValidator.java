package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DigitsValidator {
	private Pattern pattern;
	private Matcher matcher;

	private static final String DIGITS_PATTERN = "\\d+";

	public DigitsValidator() {
		pattern = Pattern.compile(DIGITS_PATTERN);
	}

	public boolean validate(String digits) {
		matcher = pattern.matcher(digits);
		return matcher.matches();
	}
}
