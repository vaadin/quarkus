/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.quarkus.graal;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.util.ExecutorsFactory;

/**
 * A replacement for {@link ExecutorsFactory#getScheduler(AtmosphereConfig)}
 * that returns a {@link ScheduledExecutorService} that postpones initialization
 * until it is effectively used.
 * <p>
 * </p>
 * This is needed to prevent thread to be started during STATIC_INIT phase of
 * native build. The {@link ExecutorsFactory#getScheduler(AtmosphereConfig)}
 * method is renamed by bytecode transformation, and all calls to redirected to
 * {@link #getScheduler(AtmosphereConfig)}. The
 * {@link #newScheduler(AtmosphereConfig)} method is also rewritten to call the
 * renamed {@link ExecutorsFactory#getScheduler(AtmosphereConfig)} method.
 */
public class DelayedSchedulerExecutorsFactory {

    public static ScheduledExecutorService getScheduler(
            final AtmosphereConfig config) {
        return new DelayedSchedulerExecutor(config);
    }

    private static ScheduledExecutorService newScheduler(
            AtmosphereConfig config) {
        // This call will be replaced at build time
        return ExecutorsFactory.getScheduler(config);
    }

    private static class DelayedSchedulerExecutor
            implements ScheduledExecutorService {

        private final AtmosphereConfig config;
        private final AtomicReference<ScheduledExecutorService> delegate = new AtomicReference<>();

        public DelayedSchedulerExecutor(AtmosphereConfig config) {
            this.config = config;
        }

        public ScheduledExecutorService getDelegate() {
            return delegate.updateAndGet(executor -> {
                if (executor == null) {
                    return DelayedSchedulerExecutorsFactory
                            .newScheduler(config);
                }
                return executor;
            });
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay,
                TimeUnit unit) {
            return getDelegate().schedule(command, delay, unit);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
                TimeUnit unit) {
            return getDelegate().schedule(callable, delay, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                long initialDelay, long period, TimeUnit unit) {
            return getDelegate().scheduleAtFixedRate(command, initialDelay,
                    period, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                long initialDelay, long delay, TimeUnit unit) {
            return getDelegate().scheduleWithFixedDelay(command, initialDelay,
                    delay, unit);
        }

        @Override
        public void shutdown() {
            getDelegate().shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return getDelegate().shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return getDelegate().isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return getDelegate().isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit)
                throws InterruptedException {
            return getDelegate().awaitTermination(timeout, unit);
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return getDelegate().submit(task);
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return getDelegate().submit(task, result);
        }

        @Override
        public Future<?> submit(Runnable task) {
            return getDelegate().submit(task);
        }

        @Override
        public <T> List<Future<T>> invokeAll(
                Collection<? extends Callable<T>> tasks)
                throws InterruptedException {
            return getDelegate().invokeAll(tasks);
        }

        @Override
        public <T> List<Future<T>> invokeAll(
                Collection<? extends Callable<T>> tasks, long timeout,
                TimeUnit unit) throws InterruptedException {
            return getDelegate().invokeAll(tasks, timeout, unit);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
                throws InterruptedException, ExecutionException {
            return getDelegate().invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            return getDelegate().invokeAny(tasks, timeout, unit);
        }

        @Override
        public void execute(Runnable command) {
            getDelegate().execute(command);
        }
    }
}
