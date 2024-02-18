package com.task10.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateUserUtil {

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PATTERN = "^(?=.*[a-zA-Z0-9])(?=.*[$%^*])[a-zA-Z0-9$%^*]{12,}$";

    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern passwordPattern = Pattern.compile(PATTERN);

    public static boolean validateEmail(final String email) {
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    public static boolean validatePassword(String password) {
        Matcher matcher = passwordPattern.matcher(password);
        return matcher.matches() || password.length() > 12;
    }
}
