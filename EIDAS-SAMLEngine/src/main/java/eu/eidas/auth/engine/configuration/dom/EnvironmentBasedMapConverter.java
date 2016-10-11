package eu.eidas.auth.engine.configuration.dom;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;


public class EnvironmentBasedMapConverter<T> implements MapConverter<T>{

    public static final String ENV_VARIABLE_PREFIX = "$";
    public static final int ENV_VARIABLE_PREFIX_INDEX = 1;

    @Override
    public T convert(Map<String, String> map) {
        Map<String, String> newMapInstance = new HashMap<String, String>();
        for(String key: map.keySet()){
            String value = map.get(key);
            if(!StringUtils.isEmpty(value) && value.startsWith(ENV_VARIABLE_PREFIX)){
                String envVariableValue = provideEnvironmentVariables().get(value.substring(ENV_VARIABLE_PREFIX_INDEX));
                newMapInstance.put(key, envVariableValue);
            }else{
                newMapInstance.put(key, value);
            }
        }
        return (T)newMapInstance;
    }

    protected Map<String, String> provideEnvironmentVariables(){
        return System.getenv();
    }
}
