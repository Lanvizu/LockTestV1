package Test.LockTestV1.performance;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PerformanceMeasure {
    String value() default "";
}
