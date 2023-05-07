/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong James Wong <jameswong1376@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.infra.core.web.error;

import org.junit.Test;

import com.wl4g.infra.core.web.error.handler.DefaultSmartErrorHandler;

/**
 * {@link DefaultSmartErrorHandlerTests}
 * 
 * @author James Wong
 * @version 2023-03-08
 * @since v1.0.0
 */
public class DefaultSmartErrorHandlerTests {

    @Test
    public void testExtractRootCauseMessage() {
        final String rootCauseMsg = DefaultSmartErrorHandler.extractRootCauseMessage(TEST_STACKTRACE);
        System.out.println(rootCauseMsg);
    }

    // @formatter:off
    static String TEST_STACKTRACE = "2023-16-46 17:46:27 WARN co.wl.re.ex.ex.DefaultWorkflowExecution] (vert.x-eventloop-thread-2) Failed to execution workflow graph for workflowId: 6150869239922668: com.wl4g.rengine.common.exception.ExecutionGraphException: com.wl4g.rengine.common.exception.EvaluationException: Failed to execution js script\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BaseOperator.doExecute(ExecutionGraph.java:300)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$ProcessOperator.execute(ExecutionGraph.java:383)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BaseOperator.apply(ExecutionGraph.java:268)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BootOperator.execute(ExecutionGraph.java:339)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BaseOperator.apply(ExecutionGraph.java:268)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BaseOperator.apply(ExecutionGraph.java:1)\n"
            + "    at com.wl4g.rengine.executor.execution.DefaultWorkflowExecution.execute(DefaultWorkflowExecution.java:101)\n"
            + "    at com.wl4g.rengine.executor.execution.DefaultWorkflowExecution_Subclass.execute$$superforward1(Unknown Source)\n"
            + "    at com.wl4g.rengine.executor.execution.DefaultWorkflowExecution_Subclass$$function$$1.apply(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:53)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.AbstractMethodValidationInterceptor.validateMethodInvocation(AbstractMethodValidationInterceptor.java:71)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.MethodValidationInterceptor.validateMethodInvocation(MethodValidationInterceptor.java:17)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.MethodValidationInterceptor_Bean.intercept(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:41)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:49)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:41)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:40)\n"
            + "    at io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:32)\n"
            + "    at com.wl4g.rengine.executor.execution.DefaultWorkflowExecution_Subclass.execute(Unknown Source)\n"
            + "    at com.wl4g.rengine.executor.service.impl.ReactiveEngineExecutionServiceImpl.lambda$execute$0(ReactiveEngineExecutionServiceImpl.java:166)\n"
            + "    at io.smallrye.context.impl.wrappers.SlowContextualFunction.apply(SlowContextualFunction.java:21)\n"
            + "    at io.smallrye.mutiny.operators.uni.UniOnItemTransform$UniOnItemTransformProcessor.onItem(UniOnItemTransform.java:36)\n"
            + "    at io.smallrye.mutiny.operators.uni.builders.UniCreateFromPublisher$PublisherSubscriber.onNext(UniCreateFromPublisher.java:73)\n"
            + "    at io.smallrye.mutiny.helpers.HalfSerializer.onNext(HalfSerializer.java:31)\n"
            + "    at io.smallrye.mutiny.helpers.StrictMultiSubscriber.onItem(StrictMultiSubscriber.java:85)\n"
            + "    at io.smallrye.mutiny.operators.multi.MultiCollectorOp$CollectorProcessor.onCompletion(MultiCollectorOp.java:100)\n"
            + "    at io.smallrye.mutiny.operators.multi.MultiOperatorProcessor.onCompletion(MultiOperatorProcessor.java:108)\n"
            + "    at io.smallrye.mutiny.operators.multi.MultiEmitOnOp$MultiEmitOnProcessor.isDoneOrCancelled(MultiEmitOnOp.java:248)\n"
            + "    at io.smallrye.mutiny.operators.multi.MultiEmitOnOp$MultiEmitOnProcessor.run(MultiEmitOnOp.java:188)\n"
            + "    at io.quarkus.mongodb.impl.Wrappers.lambda$toMulti$2(Wrappers.java:30)\n"
            + "    at io.vertx.core.impl.ContextInternal.dispatch(ContextInternal.java:264)\n"
            + "    at io.vertx.core.impl.ContextInternal.dispatch(ContextInternal.java:246)\n"
            + "    at io.vertx.core.impl.EventLoopContext.lambda$runOnContext$0(EventLoopContext.java:43)\n"
            + "    at io.netty.util.concurrent.AbstractEventExecutor.runTask(AbstractEventExecutor.java:174)\n"
            + "    at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:167)\n"
            + "    at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:470)\n"
            + "    at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:569)\n"
            + "    at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997)\n"
            + "    at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)\n"
            + "    at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)\n"
            + "    at java.base/java.lang.Thread.run(Thread.java:834)\n"
            + "Caused by: com.wl4g.rengine.common.exception.EvaluationException: Failed to execution js script\n"
            + "    at com.wl4g.rengine.executor.execution.engine.GraalJSScriptEngine.execute(GraalJSScriptEngine.java:175)\n"
            + "    at com.wl4g.rengine.executor.execution.engine.GraalJSScriptEngine_Subclass.execute$$superforward1(Unknown Source)\n"
            + "    at com.wl4g.rengine.executor.execution.engine.GraalJSScriptEngine_Subclass$$function$$6.apply(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:53)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.AbstractMethodValidationInterceptor.validateMethodInvocation(AbstractMethodValidationInterceptor.java:71)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.MethodValidationInterceptor.validateMethodInvocation(MethodValidationInterceptor.java:17)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.MethodValidationInterceptor_Bean.intercept(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:41)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:49)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:41)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:40)\n"
            + "    at io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:32)\n"
            + "    at com.wl4g.rengine.executor.execution.engine.GraalJSScriptEngine_Subclass.execute(Unknown Source)\n"
            + "    at com.wl4g.rengine.executor.execution.DefaultWorkflowExecution.lambda$execute$2(DefaultWorkflowExecution.java:92)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BaseOperator.doExecute(ExecutionGraph.java:298)\n"
            + "    ... 43 more\n"
            + "Caused by: org.graalvm.polyglot.PolyglotException: The current thread cannot be blocked: vert.x-eventloop-thread-2\n"
            + "    at io.smallrye.mutiny.operators.uni.UniBlockingAwait.await(UniBlockingAwait.java:30)\n"
            + "    at io.smallrye.mutiny.groups.UniAwait.atMost(UniAwait.java:65)\n"
            + "    at io.quarkus.redis.runtime.datasource.BlockingStringCommandsImpl.get(BlockingStringCommandsImpl.java:39)\n"
            + "    at com.wl4g.rengine.executor.execution.sdk.ScriptRedisLockClient$1.get(ScriptRedisLockClient.java:108)\n"
            + "    at com.wl4g.infra.common.locks.JedisLockManager$FastReentrantUnfairDistributedRedLock.doTryAcquire(JedisLockManager.java:222)\n"
            + "    at com.wl4g.infra.common.locks.JedisLockManager$FastReentrantUnfairDistributedRedLock.tryLock(JedisLockManager.java:169)\n"
            + "    at <js>.testSdkForRedisLockClient(test-sdk-all-examples-1.0.0.js@6150869239922668:141)\n"
            + "    at <js>.process(test-sdk-all-examples-1.0.0.js@6150869239922668:20)\n"
            + "    at org.graalvm.polyglot.Value.execute(Value.java:841)\n"
            + "    at com.wl4g.rengine.executor.execution.engine.GraalJSScriptEngine.execute(GraalJSScriptEngine.java:168)\n"
            + "    at com.wl4g.rengine.executor.execution.engine.GraalJSScriptEngine_Subclass.execute$$superforward1(Unknown Source)\n"
            + "    at com.wl4g.rengine.executor.execution.engine.GraalJSScriptEngine_Subclass$$function$$6.apply(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:53)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.AbstractMethodValidationInterceptor.validateMethodInvocation(AbstractMethodValidationInterceptor.java:71)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.MethodValidationInterceptor.validateMethodInvocation(MethodValidationInterceptor.java:17)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.MethodValidationInterceptor_Bean.intercept(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:41)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:49)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:41)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:40)\n"
            + "    at io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:32)\n"
            + "    at com.wl4g.rengine.executor.execution.engine.GraalJSScriptEngine_Subclass.execute(Unknown Source)\n"
            + "    at com.wl4g.rengine.executor.execution.DefaultWorkflowExecution.lambda$execute$2(DefaultWorkflowExecution.java:92)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BaseOperator.doExecute(ExecutionGraph.java:298)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$ProcessOperator.execute(ExecutionGraph.java:383)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BaseOperator.apply(ExecutionGraph.java:268)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BootOperator.execute(ExecutionGraph.java:339)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BaseOperator.apply(ExecutionGraph.java:268)\n"
            + "    at com.wl4g.rengine.common.graph.ExecutionGraph$BaseOperator.apply(ExecutionGraph.java:1)\n"
            + "    at com.wl4g.rengine.executor.execution.DefaultWorkflowExecution.execute(DefaultWorkflowExecution.java:101)\n"
            + "    at com.wl4g.rengine.executor.execution.DefaultWorkflowExecution_Subclass.execute$$superforward1(Unknown Source)\n"
            + "    at com.wl4g.rengine.executor.execution.DefaultWorkflowExecution_Subclass$$function$$1.apply(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:53)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.AbstractMethodValidationInterceptor.validateMethodInvocation(AbstractMethodValidationInterceptor.java:71)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.MethodValidationInterceptor.validateMethodInvocation(MethodValidationInterceptor.java:17)\n"
            + "    at io.quarkus.hibernate.validator.runtime.interceptor.MethodValidationInterceptor_Bean.intercept(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:41)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:49)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n"
            + "    at io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n"
            + "    at io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:41)\n"
            + "    at io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:40)\n"
            + "    at io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:32)\n"
            + "    at com.wl4g.rengine.executor.execution.DefaultWorkflowExecution_Subclass.execute(Unknown Source)\n"
            + "    at com.wl4g.rengine.executor.service.impl.ReactiveEngineExecutionServiceImpl.lambda$execute$0(ReactiveEngineExecutionServiceImpl.java:166)\n"
            + "    ... 21 more\n"
            + "Feb 16, 2023 5:46:31 PM io.opentelemetry.sdk.internal.ThrottlingLogger doLog";
    // @formatter:on

}
