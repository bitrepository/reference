package org.bitrepository;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ToStringBuilder;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

public class ExtentedTestInfoParameterResolver implements ParameterResolver {

    @Override
    public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
        return ExtensionContextScope.TEST_METHOD;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return (parameterContext.getParameter().getType() == SuiteInfo.class);
    }

    @Override
    public SuiteInfo resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return new ExtentedTestInfoParameterResolver.ExtentedTestInfo(extensionContext);
    }

    private static class ExtentedTestInfo implements SuiteInfo {

        private final String displayName;
        private final Set<String> tags;
        private final Optional<Class<?>> testClass;
        private final Optional<Method> testMethod;
        private final Optional<String> pillarType;

        ExtentedTestInfo(ExtensionContext extensionContext) {
            this.displayName = extensionContext.getDisplayName();
            this.tags = extensionContext.getTags();
            this.testClass = extensionContext.getTestClass();
            this.testMethod = extensionContext.getTestMethod();
            this.pillarType = extensionContext.getConfigurationParameter("pillarType");
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
            return this.testClass;
        }

        @Override
        public Optional<Method> getTestMethod() {
            return this.testMethod;
        }

        @Override public Optional<String> getPillarType() {
            return pillarType;
        }

        @Override
        public String toString() {
            // @formatter:off
            return new ToStringBuilder(this)
                           .append("displayName", this.displayName)
                           .append("tags", this.tags)
                           .append("testClass", nullSafeGet(this.testClass))
                           .append("testMethod", nullSafeGet(this.testMethod))
                           .toString();
            // @formatter:on
        }

        private static Object nullSafeGet(Optional<?> optional) {
            return optional != null ? optional.orElse(null) : null;
        }

    }

}