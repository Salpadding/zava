package com.github.zava.core.task;

import lombok.SneakyThrows;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public interface Task<M> {
    Step<?, ? extends M> createDefault();

    <T> T deserializeMeta(Object any, Class<? extends T> metaType);

    M serializeMeta(Object any);

    // 更新状态 原子操作
    default boolean swapStatus(Status prev, Status next) {
        setStatus(next);
        return true;
    }

    // 更新状态 不需要原子操作
    default void updateStatus(Status next) {
        setStatus(next);
    }

    // 从底层加载焦点 step
    default Step<?, ? extends M> fetchFocus() {
        return fetchStep(getFocus());
    }

    // 从底层数据库读取 step
    Step<?, ? extends M> fetchStep(long id);

    // 更新 focus
    default void updateFocus(long focus) {
        setFocus(focus);
    }

    // 标记断点 用于恢复任务
    default void updateBreakpoint(long breakpoint) {
        setBreakpoint(breakpoint);
    }

    // 更新 updated_at
    void touch();

    // 写入底层数据库 同时生成 id
    long insert();

    @SneakyThrows
    default void execute(Logger log, TaskListener listener) {
        if (!activate(listener)) return;
        this.touch();
        Step<?, ?> current = null;

        while (getStatus() == Status.running) {
            if (current == null || current.getId() != getFocus()) {
                current = fetchFocus();
            }

            AbstractStep step = AbstractStep.load(current);
            step.step = current;

            Step<?, ?> callbackFrom = null;

            // 收到回调
            if (current.getStatus() == Status.waitCallback) {
                current.updateStatus(Status.running);
                listener.onTaskStatusChange(this);
                callbackFrom = fetchStep(current.getCallbackFrom());
            }

            while ((current.getStatus() == Status.running ||
                current.getStatus() == Status.created) &&
                getStatus() == Status.running) {
                AbstractStep.Next next;

                try {
                    if (current.getStatus() == Status.created) {
                        next = step.init();
                        // warn 还能重来
                        current.updateStatus(Status.running);
                        listener.onStepStatusChange(current);
                    } else {
                        next = step.next(callbackFrom);
                        // warn 还能重来
                        callbackFrom = null;
                    }
                } catch (Throwable e) {
                    if (TaskException.class.isAssignableFrom(e.getClass())) {
                        current.updateError(e.getMessage());
                        next = AbstractStep.Next.ERROR;
                        log.error("task error: {}", e.getMessage());
                    } else if (TaskWarn.class.isAssignableFrom(e.getClass())) {
                        current.updateWarn(e.getMessage());
                        next = AbstractStep.Next.WARN;
                        log.warn("task warn: {}", e.getMessage());
                    } else {
                        log.error("task exception: {}", ExceptionUtils.getStackTrace(e));
                        next = AbstractStep.Next.WARN;
                    }
                } finally {
                    this.touch();
                }

                if (next == AbstractStep.Next.POLL || (next == AbstractStep.Next.WARN && (++step.warnCount) <
                    AbstractStep.MAX_WARN_COUNT)) {
                    LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(step.getSleep()));
                    continue;
                }

                if (next == AbstractStep.Next.WARN) {
                    next = AbstractStep.Next.ERROR;
                    current.updateError("too many warnings");
                }

                if (next == AbstractStep.Next.ERROR)
                    updateBreakpoint(current.getId());

                if (next.getFork() != null) {
                    long newStepId = next.getFork().insert();
                    current.setCallbackFrom(newStepId);
                    current.setStatus(Status.waitCallback);
                    current.updateAll();
                    updateFocus(newStepId);
                    listener.onStepStatusChange(current);
                    break;
                }

                if (next == AbstractStep.Next.DONE || next == AbstractStep.Next.ERROR) {
                    current.updateStatus(next.getStepStatus());
                    listener.onStepStatusChange(current);
                }
            }
            if (getStatus() != Status.running) break;
            if (current.getStatus() == Status.waitCallback) continue;
            assert current.getStatus() != Status.pause : "pause step is forbidden";
            if (!current.getStatus().isCompleted()) continue;
            if (current.getParentId() != 0) {
                setFocus(current.getParentId());
            } else {
                updateStatus(current.getStatus());
                listener.onTaskStatusChange(this);
            }
        }
    }

    // 尝试拉起任务
    default boolean activate(TaskListener listener) {
        if (!getStatus().equals(Status.created) && !getStatus().equals(Status.pause)) return false;
        boolean ok = swapStatus(getStatus(), Status.running);
        if (ok)
            listener.onTaskStatusChange(this);
        return ok;
    }


    long getId();

    void setId(long id);

    long getFocus();

    void setFocus(long focus);

    long getType();

    void setType(long type);

    Status getStatus();

    void setStatus(Status status);

    long getBreakpoint();

    void setBreakpoint(long breakpoint);


    @SneakyThrows
    default Step fork(long type) {
        Step obj = createDefault();
        obj.setTaskId(getId());
        obj.setStatus(Status.created);
        obj.setType(type);
        obj.setTask(this);
        return obj;
    }
}
