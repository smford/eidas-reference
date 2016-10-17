package eu.eidas.config.impl;

import org.apache.commons.lang.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EnvironmentVariableSubstitutor extends StrSubstitutor {

    public EnvironmentVariableSubstitutor() {
        this(System.getenv());
    }

    public EnvironmentVariableSubstitutor(Map<String, String> environmentVariables) {
        super(new EnvironmentVariableLookup(environmentVariables));
        this.setEnableSubstitutionInVariables(false);
    }

    public Properties mutatePropertiesReplaceValues(Properties properties) {
       for (Object key : properties.keySet()) {
            String keyString = (String)key;
            properties.setProperty(keyString, this.replace(properties.getProperty(keyString)));
        }
        return properties;
    }

    public <T> Map<T, String> replaceValues(Map<T, String> map) {
        HashMap<T, String> result = new HashMap<>();
        for (T key : map.keySet()) {
            result.put(key, this.replace(map.get(key)));
        }
        return result;
    }
}

