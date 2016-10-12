package eu.eidas.auth.engine.configuration.dom;

import org.apache.commons.lang.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentVariableSubstitutor extends StrSubstitutor {

    public EnvironmentVariableSubstitutor() {
        this(System.getenv());
    }

    public EnvironmentVariableSubstitutor(Map<String, String> environmentVariables) {
        super(new EnvironmentVariableLookup(environmentVariables));
        this.setEnableSubstitutionInVariables(false);
    }

    public <T> Map<T, String> replaceValues(Map<T, String> map) {
        HashMap<T, String> result = new HashMap<>();
        for (T key : map.keySet()) {
            result.put(key, this.replace(map.get(key)));
        }
        return result;
    }
}

