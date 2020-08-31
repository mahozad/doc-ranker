package ir.parsijoo.searchia.util;

import com.github.mfathi91.time.PersianDate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DecimalStyle;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.ChronoUnit.DAYS;

public class CommitLogExtractor {

    private final static LocalDateTime targetDay = LocalDateTime.of(2020, 8, 13, 0, 0);
    private final static String COMMAND_BASE = "git log --pretty=format:\"%%s\" --after=\"%s\" --before=\"%s\"";
    private final static String OUTPUT_DIRECTORY = "../../report/commit-logs/";

    public static void main(String[] args) throws IOException {
        String start = targetDay.format(ISO_LOCAL_DATE_TIME);
        String end = targetDay.plus(1, DAYS).format(ISO_LOCAL_DATE_TIME);
        String command = String.format(COMMAND_BASE, start, end);
        Process process = Runtime.getRuntime().exec(command);

        Locale locale = Locale.forLanguageTag("fa");
        DateTimeFormatter formatter = ofPattern("yyyy-MM-dd").localizedBy(locale).withDecimalStyle(DecimalStyle.of(locale));
        PersianDate persianDate = PersianDate.fromGregorian(LocalDate.from(targetDay));
        String date = formatter.format(persianDate);
        String fileName = date + ".txt";
        Path outputPath = Path.of(OUTPUT_DIRECTORY + fileName);

        String logs = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\r\n"));

        Files.deleteIfExists(outputPath);
        Files.createFile(outputPath);
        Files.writeString(outputPath, logs);
    }
}
