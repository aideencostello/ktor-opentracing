package com.zopa.ktor.opentracing

import io.opentracing.Span
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.util.Stack
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun tracingContext(): CoroutineContext {
    val activeSpan: Span? = getGlobalTracer().scopeManager().activeSpan()

    val spanStack = Stack<Span>()
    if (activeSpan != null) {
        spanStack.push(activeSpan)
    }

    return threadLocalSpanStack.asContextElement(spanStack)
}

fun CoroutineScope.launchTraced(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
): Job = launch(context + tracingContext(), start, block)

fun <T> CoroutineScope.asyncTraced(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T> = async(context + tracingContext(), start) {
    block()
}
