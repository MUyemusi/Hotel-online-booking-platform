package com.zhisu.hotel.database;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayDataRepairContractTest {

    @Test
    void migrationRepairsSeedRowsWithoutDependingOnAlreadyCorruptedText() throws IOException {
        String migration = Files.readString(Path.of("..", "database", "migrations", "member5_completion.sql"));

        assertThat(migration).contains("holiday_code IN ('MID_AUTUMN','NATIONAL_DAY')");
        assertThat(migration).contains("holiday_year=2026", "中秋节假期", "国庆节假期", "假期第");
        assertThat(migration).doesNotContain("holiday_name LIKE '%holiday%'");
    }
}
