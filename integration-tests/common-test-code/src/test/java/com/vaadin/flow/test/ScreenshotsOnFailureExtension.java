/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.test;

import java.util.logging.Logger;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.runner.Description;
import org.mockito.Mockito;

import com.vaadin.testbench.ScreenshotOnFailureRule;

public class ScreenshotsOnFailureExtension
        implements TestExecutionExceptionHandler {

    private static class ScreenshotOnFailureRuleDelegate
            extends ScreenshotOnFailureRule {
        public ScreenshotOnFailureRuleDelegate(AbstractChromeIT test) {
            super(test);
        }

        @Override
        protected void failed(Throwable throwable, Description description) {
            super.failed(throwable, description);
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context,
            Throwable throwable) throws Throwable {
        if (!context.getTestInstance().isPresent()) {
            getLogger().warning(
                    "There is no test instance in the context, can't generate a screenshot");
            throw throwable;
        }
        Object object = context.getTestInstance().get();
        AbstractChromeIT test = (AbstractChromeIT) object;

        ScreenshotOnFailureRuleDelegate delegate = new ScreenshotOnFailureRuleDelegate(
                test);
        if (!context.getTestClass().isPresent()) {
            getLogger().warning(
                    "There is no test class in the context, can't generate a screenshot");
            throw throwable;
        }

        Description description;
        if (context.getTestMethod().isPresent()) {
            description = Description.createTestDescription(
                    context.getTestClass().get(),
                    context.getTestMethod().get().getName());
        } else {
            description = Mockito.mock(Description.class);
            Mockito.when(description.getDisplayName())
                    .thenReturn(context.getDisplayName());
        }
        delegate.failed(throwable, description);
        throw throwable;
    }

    private Logger getLogger() {
        return Logger.getLogger(ScreenshotsOnFailureExtension.class.getName());
    }
}
