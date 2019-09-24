package pbouda.broadcaster.server.rabbit;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * - Creates a thread factory where we can easily specify a name of the newly created threads.
 * - Sets implementation of {@link Thread.UncaughtExceptionHandler} to properly notify about exceptions in the threads.
 * - The factory is not responsible for sharing/caching the threads (it's usually some component above)
 */
public class CustomThreadFactory implements ThreadFactory {

    private final AtomicInteger counter = new AtomicInteger();
    private final String prefix;
    private final Thread.UncaughtExceptionHandler exceptionHandler;

    /**
     * Creates new ThreadFactory instance with a predefined prefix of a thread's name.
     *
     * @param prefix thread's prefix.
     */
    public CustomThreadFactory(String prefix) {
        this(prefix, null);
    }

    /**
     * Creates new ThreadFactory instance with a predefined prefix of a thread's name and a handler when completes
     * exceptionally.
     *
     * @param prefix thread's prefix.
     * @param exceptionHandler handler to process when thread completes exceptionally.
     */
    public CustomThreadFactory(String prefix, Thread.UncaughtExceptionHandler exceptionHandler) {
        this.prefix = prefix;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, prefix + "-" + counter.getAndIncrement());
        if (exceptionHandler != null) {
            thread.setUncaughtExceptionHandler(exceptionHandler);
        }
        return thread;
    }
}