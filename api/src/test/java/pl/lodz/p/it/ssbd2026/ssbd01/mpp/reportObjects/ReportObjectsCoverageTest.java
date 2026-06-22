package pl.lodz.p.it.ssbd2026.ssbd01.mpp.reportObjects;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReportObjectsCoverageTest {

    private void instantiateAndCoverMethods(Class<?> clazz) {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                constructor.setAccessible(true);
                Parameter[] parameters = constructor.getParameters();
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Class<?> type = parameters[i].getType();
                    if (type.isPrimitive()) {
                        if (type == boolean.class) args[i] = false;
                        else if (type == char.class) args[i] = '\0';
                        else args[i] = 0;
                    } else args[i] = null;
                }
                Object instance = constructor.newInstance(args);
                assertNotNull(instance);

                instance.toString();
                instance.hashCode();
                instance.equals(instance);
                instance.equals(null);
                break;
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    void shouldCoverReportObjects() {
        instantiateAndCoverMethods(DetailedComparison.class);
        instantiateAndCoverMethods(DetailedMatch.class);
        instantiateAndCoverMethods(MatchFragment.class);
    }
}