package eu.eidas.auth.engine.configuration.dom;

import eu.eidas.config.impl.EnvironmentVariableSubstitutor;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EnvironmentVariableSubstitutorTest {

    public static final String ACTUAL_PATH1 = "ACTUAL_PATH1";
    public static final String ENVIRONMENT_VARIABLE1 = "ENVIRONMENT_VARIABLE1";
    public static final String ACTUAL_PATH2 = "ACTUAL_PATH2";
    public static final String ENVIRONMENT_VARIABLE2 = "ENVIRONMENT_VARIABLE2";
    public static final String KEY = "key";
    EnvironmentVariableSubstitutor target;

    @Before
    public void setup() {
        Map<String, String> envVariables = new HashMap<>();
        envVariables.put(ENVIRONMENT_VARIABLE1, ACTUAL_PATH1);
        envVariables.put(ENVIRONMENT_VARIABLE2, ACTUAL_PATH2);
        target = new EnvironmentVariableSubstitutor(envVariables);
    }

    @Test
    public void testExpandValues(){
        Map<String, String> input = new HashMap<>();
        input.put(KEY, "${ENVIRONMENT_VARIABLE1}");
        Map<String, String> actual = target.replaceValues(input);
        assertEquals(ACTUAL_PATH1, actual.get(KEY));
    }
    @Test(expected = IllegalArgumentException.class)
    public void testExpandValuesEnvVarNotSet(){
        Map<String, String> input = new HashMap<>();
        input.put(KEY, "${banana}");
        Map<String, String> actual = target.replaceValues(input);
        assertEquals(ACTUAL_PATH1, actual.get(KEY));
    }

    @Test
    public void testExpandValuesMultipleEnvVar(){
        Map<String, String> input = new HashMap<>();
        input.put(KEY, "${ENVIRONMENT_VARIABLE1}${ENVIRONMENT_VARIABLE2}");
        Map<String, String> actual = target.replaceValues(input);
        assertEquals(ACTUAL_PATH1 + ACTUAL_PATH2, actual.get(KEY));
    }
}
