package com.example.GYM_management_api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test to verify Maven Surefire Plugin reporting capabilities:
 * - Total, Passed, Failed, Skipped test counters
 * - Execution time tracking
 * - Detailed stack trace recording on assertion error (via trimStackTrace=false)
 */
class SurefireReportVerificationTest {

    @Test
    @DisplayName("Verify passing test is recorded in surefire reports")
    void testSampleSuccess() {
        assertEquals(2, 1 + 1, "Arithmetic addition should equal 2");
    }

    @Test
    @Disabled("Sample skipped test to verify surefire report skipped counter")
    @DisplayName("Verify skipped test is recorded in surefire reports")
    void testSampleSkipped() {
        fail("This test should be skipped and not executed");
    }

    @Test
    @DisplayName("Verify assertion failure and full stack trace recording when triggered")
    void testAssertionFailureWhenTriggered() {
        if (Boolean.getBoolean("test.trigger.assertion")) {
            assertEquals("expectedResult", "actualResult", "Simulated assertion error for stack trace verification");
        }
    }
}
