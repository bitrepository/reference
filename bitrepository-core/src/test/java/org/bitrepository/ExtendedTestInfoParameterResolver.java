package org.bitrepository;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ToStringBuilder;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

public class ExtendedTestInfoParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return (parameterContext.getParameter().getType() == SuiteInfo.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return new ExtendedTestInfo(extensionContext);
    }

    private static class ExtendedTestInfo implements SuiteInfo {

        private final String displayName;
        private final Set<String> tags;
        private final Class<?> testClass;
        private final Method testMethod;
        private final String pillarType;

        ExtendedTestInfo(ExtensionContext extensionContext) {
            this.displayName = extensionContext.getDisplayName();
            this.tags = extensionContext.getTags();
            this.testClass = extensionContext.getTestClass().orElse(null);
            this.testMethod = extensionContext.getTestMethod().orElse(null);
            this.pillarType = extensionContext.getConfigurationParameter("pillarType").orElse(null);
        }

        @Override
        public String getDisplayName() {
            return this.displayName;
        }

        @Override
        public Set<String> getTags() {
            return this.tags;
        }

        @Override
        public Optional<Class<?>> getTestClass() {
            return Optional.ofNullable(this.testClass);
        }

        @Override
        public Optional<Method> getTestMethod() {
            return Optional.ofNullable(this.testMethod);
        }

        @Override
        public Optional<String> getPillarType() {
            return Optional.ofNullable(pillarType);
        }

        @Override
        public String toString() {
            // @formatter:off
            return new ToStringBuilder(this)
                    .append("displayName", this.displayName)
                    .append("tags", this.tags)
                    .append("testClass", this.testClass)
                    .append("testMethod", this.testMethod)
                    .toString();
            // @formatter:on
        }
    }

}