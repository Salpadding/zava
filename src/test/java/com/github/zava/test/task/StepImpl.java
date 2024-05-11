package com.github.zava.test.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.zava.core.task.Status;
import com.github.zava.core.task.Step;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class StepImpl implements Step<TaskImpl, JsonNode> {
    public static final Map<Long, StepImpl> DB = new HashMap<>();
    public static final AtomicLong SEQ = new AtomicLong();

    private long id;


    private long taskId;

    // 父 step 用于实现回调
    private long parentId;

    // 回调的 step 的 id
    private long callbackFrom;


    private Status status;

    private long type;

    // 状态码 用于调度
    private String code;

    // 关键字 用于查找
    private String keyWord;

    // 元数据 主要用于传参
    private JsonNode meta;

    // 用于返回值
    private JsonNode ret;

    private String error;

    private String warn;

    private LocalDateTime createdAt;

    // 用于 fork 新的 step
    private TaskImpl task;

    @Override
    public void updateError(String err) {
        setError(err);
    }

    @Override
    public void updateWarn(String warn) {
        setWarn(warn);
    }

    @Override
    public long insert() {
        this.id = SEQ.incrementAndGet();
        synchronized (DB) {
            DB.put(this.id, this);
        }
        return this.id;
    }

    @Override
    public void updateAll() {

    }
}
