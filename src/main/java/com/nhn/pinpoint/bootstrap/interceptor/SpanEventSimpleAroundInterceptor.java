package com.nhn.pinpoint.bootstrap.interceptor;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

/**
 * @author emeroad
 */
public abstract class SpanEventSimpleAroundInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {
    protected final PLogger logger;
    protected final boolean isDebug;

    private MethodDescriptor descriptor;

    private TraceContext traceContext;

    protected SpanEventSimpleAroundInterceptor(PLogger logger) {
        this.logger = logger;
        this.isDebug = logger.isDebugEnabled();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        prepareBeforeTrace(target, args);

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            // blockBegin 인터페이스 분리가 필요함.
            trace.traceBlockBegin();
            doInBeforeTrace(trace, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("before. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected void prepareBeforeTrace(Object target, Object[] args) {

    }

    protected abstract void doInBeforeTrace(final Trace trace, final Object target, final Object[] args);


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }

        prepareAfterTrace(target, args, result, throwable);

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            doInAfterTrace(trace, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {

    }

    protected abstract void doInAfterTrace(final Trace trace, final Object target, final Object[] args, final Object result, Throwable throwable);


    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }

    public MethodDescriptor getMethodDescriptor() {
        return descriptor;
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }
}