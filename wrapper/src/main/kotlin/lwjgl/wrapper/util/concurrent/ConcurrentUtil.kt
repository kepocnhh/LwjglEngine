package lwjgl.wrapper.util.concurrent

import java.util.concurrent.*

private val DEFAULT_REJECTED_EXECUTION_HANDLER by lazy(ThreadPoolExecutor::AbortPolicy)
private val DEFAULT_BLOCKING_QUEUE: BlockingQueue<Runnable> get() = SynchronousQueue()
private val DEFAULT_THREAD_FACTORY: ThreadFactory get() = Executors.defaultThreadFactory()

fun executorService(
    corePoolSize: Int = 0,
    maximumPoolSize: Int = 8,
    keepAliveTime: Long = 60_000,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    blockingQueue: BlockingQueue<Runnable> = DEFAULT_BLOCKING_QUEUE,
    threadFactory: ThreadFactory = DEFAULT_THREAD_FACTORY,
    rejectedExecutionHandler: RejectedExecutionHandler = DEFAULT_REJECTED_EXECUTION_HANDLER
): ExecutorService = ThreadPoolExecutor(
    corePoolSize,
    maximumPoolSize,
    keepAliveTime,
    timeUnit,
    blockingQueue,
    threadFactory,
    rejectedExecutionHandler
)
