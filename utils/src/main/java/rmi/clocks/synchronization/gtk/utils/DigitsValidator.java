package rmi.clocks.synchronization.gtk.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DigitsValidator {
	private static final String DIGITS_PATTERN = "\\d+";
	private final Pattern pattern;

	public DigitsValidator() {
		pattern = Pattern.compile(DIGITS_PATTERN);
	}

	public boolean validate(String digits) {
		Matcher matcher = pattern.matcher(digits);
		return !matcher.matches();
	}
}
