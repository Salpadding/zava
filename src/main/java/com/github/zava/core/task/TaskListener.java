package com.github.zava.core.task;

public interface TaskListener<T extends Task, S extends Step> {
    void onTaskStatusChange(T task);

    void onStepStatusChange(S step);
}
