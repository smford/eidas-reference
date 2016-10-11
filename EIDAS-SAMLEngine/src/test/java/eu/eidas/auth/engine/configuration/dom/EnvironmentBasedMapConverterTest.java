package eu.eidas.auth.engine.configuration.dom;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EnvironmentBasedMapConverterTest {

    public static final String ACTUAL_PATH = "ACTUAL_PATH";
    public static final String ENVIRONMENT_VARIABLE = "ENVIRONMENT_VARIABLE";
    public static final String KEY = "key";
    public static final String ENV_VARIABLE_PREFIX = "$";
    EnvironmentBasedMapConverter<Map> target = new EnvironmentBasedMapConverter<Map>(){
        @Override
        public Map<String, String> provideEnvironmentVariables() {
            Map<String, String> envVariables = new HashMap<>();
            envVariables.put(ENVIRONMENT_VARIABLE, ACTUAL_PATH);
            return envVariables;
        }
    };

    @Test
    public void testConvert(){
        Map<String, String> input = new HashMap<>();
        input.put(KEY, ENV_VARIABLE_PREFIX + ENVIRONMENT_VARIABLE);
        Map<String, String> actual = target.convert(input);
        assertEquals(ACTUAL_PATH, actual.get(KEY));
    }
}
