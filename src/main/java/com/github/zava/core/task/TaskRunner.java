package com.github.zava.core.task;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@RequiredArgsConstructor
public abstract class TaskRunner<T extends Task<?>, S extends Step<?, ?>> {
    // 执行中的任务
    public final Map<Long, Task<?>> tasks = new ConcurrentSkipListMap<>();
    // 执行任务的线程
    public final Map<Long, Thread> threads = new ConcurrentSkipListMap<>();

    private final TaskListener<T, S> listener;
    private final Logger log;

    // 把超时任务标记为暂停状态
    public abstract int tryPause();

    public abstract List<T> fetchPaused();

    private final TaskManger taskManger = new TaskManger(this);


    public void interrupt() {
        this.taskManger.thread.interrupt();
    }

    @RequiredArgsConstructor
    public static class TaskManger implements Runnable {
        private final TaskRunner<?, ?> runner;

        public volatile boolean running = true;
        public Thread thread;

        public void start() {
            this.thread = new Thread(this);
            this.thread.start();
        }

        @SneakyThrows
        public void stop() {
            this.running = false;
            if (this.thread != null) {
                LockSupport.unpark(this.thread);
                this.thread.join();
            }
        }


        @Override
        public void run() {
            while (running) {
                tryPause();
                tryResume();
                try {
                    LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(30));
                } catch (Exception ex) {
                    runner.log.error("", ex);
                }
            }
        }

        public int tryPause() {
            return runner.tryPause();
        }

        @SneakyThrows
        public void tryResume() {
            List<? extends Task<?>> tasks = runner.fetchPaused();
            for (Task<?> task : tasks) {
                runner.run(task);
            }
        }
    }


    // 提交任务
    @SneakyThrows
    public long submit(T template, long type, Object stepMeta) {
        // 创建任务 有向图
        template.setType(type);
        template.setStatus(Status.created);
        long taskId = template.insert();

        // 有向图根节点
        Step root = template.fork(type);
        root.setMeta(template.serializeMeta(stepMeta));
        root.insert();
        template.updateFocus(root.getId());
        run(template);
        return taskId;
    }

    // 执行新任务或者恢复未执行完成的任务
    public void run(Task<?> task) {
        if (!taskManger.running) return;
        Thread runner = new Thread(() -> {
            task.execute(log, listener);
            if (task.getStatus().isCompleted()) {
                log.info("task {} completed", task.getId());
            }
            this.tasks.remove(task.getId());
            this.threads.remove(task.getId());
        });
        tasks.put(task.getId(), task);
        threads.put(task.getId(), runner);
        runner.start();
    }

    @SneakyThrows
    public void killTask(long id) {
        Task<?> t = this.tasks.get(id);
        Thread runner = this.threads.get(id);
        if (t != null) {
            t.updateStatus(Status.error);
            if (runner != null) {
                LockSupport.unpark(runner);
                runner.join();
            }
        }
    }

    public void close() throws Exception {
        if (!this.tasks.isEmpty())
            log.info("挂起任务 {} 个", this.tasks.size());

        this.taskManger.stop();
        for (Map.Entry<Long, Task<?>> entry : this.tasks.entrySet()) {
            entry.getValue().updateStatus(Status.pause);
            Thread runner = threads.get(entry.getKey());
            if (runner != null) {
                LockSupport.unpark(runner);
                runner.join();
            }
        }
    }
}
