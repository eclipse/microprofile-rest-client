/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.microprofile.rest.client.ext;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

public class MockConfigProviderResolver extends ConfigProviderResolver {

    @Override
    public Config getConfig() {
        return new Config() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getValue(String propertyName, Class<T> propertyType) {
                Map<String, String> props = getConfigSources().iterator().next().getProperties();
                if (!props.containsKey(propertyName)) {
                    throw new NoSuchElementException("Unknown propertyName: " + propertyName);
                }

                String value = props.get(propertyName);
                if (String.class.equals(propertyType)) {
                    return (T) value;
                }
                return null;
            }

            @Override
            public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
                try {
                    return Optional.ofNullable(getValue(propertyName, propertyType));
                } catch (NoSuchElementException ex) {
                    return Optional.empty();
                }
            }

            @Override
            public Iterable<String> getPropertyNames() {
                return getSystemProps().keySet();
            }

            @Override
            public Iterable<ConfigSource> getConfigSources() {
                return Collections.singleton(new ConfigSource() {
                    @Override
                    public Map<String, String> getProperties() {
                        return getSystemProps();
                    }

                    @Override
                    public String getValue(String propertyName) {
                        return getSystemProps().get(propertyName);
                    }

                    @Override
                    public String getName() {
                        return "MockConfigSource";
                    }

                    @Override
                    public Set<String> getPropertyNames() {
                        return getSystemProps().keySet();
                    }
                });
            }

            @SuppressWarnings({"unchecked", "rawtypes"})
            private Map<String, String> getSystemProps() {
                Map sysProps = (Map) System.getProperties();
                return (Map<String, String>) sysProps;
            }

            @Override
            public ConfigValue getConfigValue(String propertyName) {
                return new ConfigValue() {

                    @Override
                    public String getName() {
                        return propertyName;
                    }

                    @Override
                    public String getValue() {
                        return getSystemProps().get(propertyName);
                    }

                    @Override
                    public String getRawValue() {
                        return getSystemProps().get(propertyName);
                    }

                    @Override
                    public String getSourceName() {
                        return getConfigSources().iterator().next().getName();
                    }

                    @Override
                    public int getSourceOrdinal() {
                        return getConfigSources().iterator().next().getOrdinal();
                    }
                };
            }

            @Override
            public <T> Optional<Converter<T>> getConverter(Class<T> forType) {
                return Optional.empty();
            }

            @Override
            public <T> T unwrap(Class<T> type) {
                throw new IllegalArgumentException();
            }
        };
    }

    @Override
    public Config getConfig(ClassLoader loader) {
        return getConfig();
    }

    @Override
    public ConfigBuilder getBuilder() {
        return null;
    };

    @Override
    public void registerConfig(Config config, ClassLoader classLoader) {
        // no-op
    }

    @Override
    public void releaseConfig(Config config) {
        // no-op
    }
}
