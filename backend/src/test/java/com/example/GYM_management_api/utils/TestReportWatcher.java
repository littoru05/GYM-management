package com.example.GYM_management_api.utils;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestReportWatcher implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final String START_TIME = "start_time";

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(START_TIME, System.currentTimeMillis());
        String displayName = context.getDisplayName();
        Method method = context.getRequiredTestMethod();

        Class<?> testClass = context.getRequiredTestClass();
        while (testClass.isMemberClass()) {
            testClass = testClass.getEnclosingClass();
        }
        String className = testClass.getSimpleName();

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  [KIỂM THỬ]   : " + displayName);
        System.out.println("  [LỚP TEST]   : " + className);
        System.out.println("  [PHƯƠNG THỨC]: " + method.getName() + "()");
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        long startTime = context.getStore(ExtensionContext.Namespace.GLOBAL).get(START_TIME, long.class);
        long duration = System.currentTimeMillis() - startTime;
        boolean failed = context.getExecutionException().isPresent();

        if (failed) {
            System.out.println("  [TRẠNG THÁI] : [FAILED - KHÔNG ĐẠT] (" + duration + " ms)");
            System.out.println("  [NGUYÊN NHÂN]: " + context.getExecutionException().get().getMessage());
        } else {
            System.out.println("  [TRẠNG THÁI] : [PASSED - ĐẠT YÊU CẦU] (" + duration + " ms)");
        }
        System.out.println("--------------------------------------------------------------------------------\n");
    }
}
