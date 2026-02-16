package org.bitrepository;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

public class SuiteInfoParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == SuiteInfo.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return new SuiteInfo() {
            @Override
            public Optional<String> getPillarType() {
                return Optional.empty();
            }

            @Override
            public String getDisplayName() {
                return extensionContext.getDisplayName();
            }

            @Override
            public Set<String> getTags() {
                return extensionContext.getTags();
            }

            @Override
            public Optional<Class<?>> getTestClass() {
                return extensionContext.getTestClass();
            }

            @Override
            public Optional<Method> getTestMethod() {
                return extensionContext.getTestMethod();
            }
        };
    }
}