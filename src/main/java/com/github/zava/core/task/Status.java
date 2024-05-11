package com.github.zava.core.task;

public enum Status {
    unknown,
    created,
    running,
    waitCallback,
    done,
    error,
    pause;

    public AbstractStep.Next asNext() {
        return this == done ? AbstractStep.Next.DONE : AbstractStep.Next.ERROR;
    }

    public boolean isCompleted() {
        return this == done || this == error;
    }
}
