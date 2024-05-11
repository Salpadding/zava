package com.github.zava.core.task;

import lombok.SneakyThrows;

public interface Step<T extends Task<?>, M> {
    long getId();

    void setId(long id);

    long getTaskId();

    void setTaskId(long taskId);

    long getParentId();

    void setParentId(long parentId);

    long getCallbackFrom();

    void setCallbackFrom(long id);


    Status getStatus();

    void setStatus(Status status);

    default void updateStatus(Status status) {
        setStatus(status);
    }

    long getType();

    void setType(long type);

    void updateError(String err);

    void updateWarn(String warn);

    // insert 同时生成 id
    long insert();

    void setTask(T task);

    T getTask();

    M getMeta();

    void setMeta(M meta);


    @SneakyThrows
    default Step fork(long type, Object meta) {
        Step inst = getTask().fork(type);
        inst.setMeta(getTask().serializeMeta(meta));
        inst.setParentId(getId());
        return inst;
    }

    void updateAll();
}
