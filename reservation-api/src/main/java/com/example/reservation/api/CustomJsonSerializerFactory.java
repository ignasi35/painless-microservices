package com.example.reservation.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.lightbend.lagom.javadsl.jackson.JacksonSerializerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;

/**
 * This is required because https://github.com/lagom/lagom/issues/682 :-(
 */
public class CustomJsonSerializerFactory extends JacksonSerializerFactory {

    private static ObjectMapper buildDefault() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Config config = ConfigFactory.load();
        List<String> configuredModules = config.getStringList(
            "lagom.serialization.json.jackson-modules"
        );
        configuredModules
            .stream()
            .map(fqcn -> {
                    try {

                        Class<?> aClass = Class.forName(fqcn);
                        return (Module) aClass.newInstance();
                    } catch (Throwable e) {
                        return null;
                    }
                }
            )
            .filter(module -> module != null)
            .forEach(module -> {
                if (module instanceof ParameterNamesModule) {
                    // ParameterNamesModule needs a special case for the constructor to ensure that single-parameter
                    // constructors are handled the same way as constructors with multiple parameters.
                    // See https://github.com/FasterXML/jackson-module-parameter-names#delegating-creator
                    mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
                } else {
                    mapper.registerModule(module);
                }
            });

        return mapper;
    }

    private static final ObjectMapper myCustomObjectMapper = buildDefault();

    public CustomJsonSerializerFactory() {
        super(myCustomObjectMapper);
    }
}
