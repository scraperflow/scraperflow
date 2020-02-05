package scraper.api.service.impl;


import org.slf4j.Logger;
import scraper.annotations.NotNull;
import scraper.api.service.ExecutorsService;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorsServiceImpl implements ExecutorsService {
    private @NotNull static final Logger log = org.slf4j.LoggerFactory.getLogger(ExecutorsServiceImpl.class);
    private @NotNull final Map<String, ExecutorService> executorServiceMap = new ConcurrentHashMap<>();

    @Override
    public synchronized @NotNull ExecutorService getService(@NotNull String jobName, @NotNull String group, @NotNull Integer count) {
        return getService(count, group, jobName);
    }

    private synchronized @NotNull ExecutorService getService(int count, @NotNull final String group, String jobName){
        String id = jobName+"-"+group;

        ExecutorService pool = executorServiceMap.get(id);
        if(pool == null) {
            pool = createExecutorService(count, id);
        }

        return pool;
    }

    private synchronized @NotNull ExecutorService createExecutorService(int count, @NotNull final String group) {
        BlockingQueue<Runnable> arrayBlockingQueue = new ArrayBlockingQueue<>(count);
        ThreadPoolExecutor executorService =
                new ThreadPoolExecutor(count, count, 1, TimeUnit.SECONDS, arrayBlockingQueue, new DefaultThreadFactory(group, true, count));
        executorService.allowCoreThreadTimeOut(true);

        // when the blocking queue is full, this tries to put into the queue which blocks
        executorService.setRejectedExecutionHandler((r, executor) -> {
            try {
                // block until there's room
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                // TODO how to handle interrupted exception
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("Producer thread interrupted", e);
            }
        });

        log.info("Created executor service for group {} and capacity {}", group, count);
        executorServiceMap.put(group, executorService);
        return executorService;
    }



    public static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final boolean show;
        private final int maxCount;

        DefaultThreadFactory(@NotNull final String name, boolean show, int maxCount) {
            this.maxCount = maxCount;
            this.show = show;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();

            poolNumber.getAndIncrement();

            namePrefix = name;
        }

        public synchronized Thread newThread(@NotNull final Runnable r) {
            int number = (threadNumber.getAndIncrement() % maxCount) + 1;

            Thread t;
            if(!show) {
                t = new Thread(group, r,
                        namePrefix,
                        0);
            } else {
                t = new Thread(group, r,
                        namePrefix+ "-" + number,
                        0);
            }

            t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);

            return t;
        }
    }
}
