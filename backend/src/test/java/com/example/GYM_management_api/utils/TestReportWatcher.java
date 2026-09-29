package com.example.GYM_management_api.utils;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.Normalizer;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestReportWatcher implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final String START_TIME = "start_time";

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(START_TIME, System.currentTimeMillis());
        String displayName = formatText(context.getDisplayName());
        Method method = context.getRequiredTestMethod();

        Class<?> testClass = context.getRequiredTestClass();
        while (testClass.isMemberClass()) {
            testClass = testClass.getEnclosingClass();
        }
        String className = testClass.getSimpleName();

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  [KIEM THU]   : " + displayName);
        System.out.println("  [LOP TEST]   : " + className);
        System.out.println("  [PHUONG THUC]: " + method.getName() + "()");
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        long startTime = context.getStore(ExtensionContext.Namespace.GLOBAL).get(START_TIME, long.class);
        long duration = System.currentTimeMillis() - startTime;
        boolean failed = context.getExecutionException().isPresent();

        if (failed) {
            System.out.println("  [TRANG THAI] : [FAILED - KHONG DAT] (" + duration + " ms)");
            System.out.println("  [NGUYEN NHAN]: " + formatText(context.getExecutionException().get().getMessage()));
        } else {
            System.out.println("  [TRANG THAI] : [PASSED - DAT YEU CAU] (" + duration + " ms)");
        }
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    private static String formatText(String text) {
        if (text == null) {
            return "";
        }
        if (isConsoleEncodingRestricted()) {
            return removeAccents(text);
        }
        return text;
    }

    private static boolean isConsoleEncodingRestricted() {
        try {
            Charset cs = System.out.charset();
            if (cs != null && !cs.name().toLowerCase().contains("utf")) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static String removeAccents(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String noAccents = normalized.replaceAll("\\p{M}", "");
        return noAccents.replace('\u0111', 'd').replace('\u0110', 'D');
    }
}

