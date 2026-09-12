package org.codeit.sb06.team03.mopl.account.domain.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class TempPasswordRandomGenerationPolicy implements TempPasswordGenerationPolicy {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGIT = "23456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL_CHARS = UPPER + LOWER + DIGIT + SPECIAL;
    private static final int PASSWORD_LENGTH = 12;

    private static final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        List<Character> passwordChars = new ArrayList<>();
        passwordChars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        passwordChars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        passwordChars.add(DIGIT.charAt(random.nextInt(DIGIT.length())));
        passwordChars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            passwordChars.add(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (char c : passwordChars) {
            sb.append(c);
        }
        return sb.toString();
    }
}
