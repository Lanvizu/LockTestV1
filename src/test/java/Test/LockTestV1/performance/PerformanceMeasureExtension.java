package Test.LockTestV1.performance;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;

public class PerformanceMeasureExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final String START_TIME = "startTime";

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(ExtensionContext.Namespace.create(context.getRequiredTestMethod()))
                .put(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        if (!testMethod.isAnnotationPresent(PerformanceMeasure.class)) return;

        long startTime = context.getStore(ExtensionContext.Namespace.create(testMethod))
                .remove(START_TIME, long.class);
        long duration = System.currentTimeMillis() - startTime;

        String testName = testMethod.getName();
        String label = testMethod.getAnnotation(PerformanceMeasure.class).value();

        System.out.printf("⏱ 성능 측정 - [%s%s] took %d ms%n",
                testName,
                label.isEmpty() ? "" : " - " + label,
                duration);
    }
}
