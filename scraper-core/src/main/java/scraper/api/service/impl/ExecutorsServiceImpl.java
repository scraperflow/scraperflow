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

    private boolean warned = false;

    @Override
    public synchronized @NotNull ExecutorService getService(@NotNull String jobName, @NotNull String group, @NotNull Integer count) {
        return getService(count, group, jobName);
    }

    @Override
    public synchronized @NotNull ExecutorService getService(@NotNull String jobName, @NotNull String group) {
        return getService(999, group, jobName);
    }


    private synchronized @NotNull ExecutorService getService(int count, @NotNull final String group, String jobName){
        String id = jobName+" > "+group;
        // one time warnings for l formatting
        if(!warned && count > 999) {
            log.warn("Using more than 999 threads the id {}. Log formatting will be affected.", group);
            warned = true;
        }
        if(!warned && id.length() > 14) {
            log.warn("Thread id name (jobname + group name) is longer than 14 characters, '{}'. Log formatting will be affected.", group);
            warned = true;
        }

        ExecutorService pool = executorServiceMap.get(id);
        if(pool == null) {
            pool = createExecutorService(count, id);
        }

        return pool;
    }

    private synchronized @NotNull ExecutorService createExecutorService(int count, @NotNull final String group) {
        BlockingQueue<Runnable> arrayBlockingQueue = new ArrayBlockingQueue<>(count);
        ThreadPoolExecutor executorService =
                new ThreadPoolExecutor(count, count, 1, TimeUnit.SECONDS, arrayBlockingQueue, new DefaultThreadFactory(group, true));
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

        executorServiceMap.put(group, executorService);
        return executorService;
    }



    public static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final boolean show;

        DefaultThreadFactory(@NotNull final String name, boolean show) {
            this.show = show;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();

            poolNumber.getAndIncrement();

            namePrefix = name;
        }

        public synchronized Thread newThread(@NotNull final Runnable r) {
            int number = threadNumber.getAndIncrement();

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
