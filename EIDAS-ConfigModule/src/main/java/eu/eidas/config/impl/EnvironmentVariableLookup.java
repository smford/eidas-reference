package eu.eidas.config.impl;

import org.apache.commons.lang.text.StrLookup;

import java.util.Map;

public class EnvironmentVariableLookup extends StrLookup {

    private final Map<String, String> environmentVariables;

    public EnvironmentVariableLookup(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    @Override
    public String lookup(String key) {
        String value = environmentVariables.get(key);
        if (value == null) {
            throw new IllegalArgumentException("The environment variable '" + key + "' is not defined; could not substitute the expression '${" + key + "}'.");
        }
        return value;
    }
}

