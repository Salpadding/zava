package com.github.zava.test.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zava.core.task.Status;
import com.github.zava.core.task.Step;
import com.github.zava.core.task.Task;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class TaskImpl implements Task<JsonNode> {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final Map<Long, TaskImpl> DB = new HashMap<>();
    public static final AtomicLong SEQ = new AtomicLong();

    private long id;

    // 当前焦点 Step
    private long focus;

    private long type;

    private volatile Status status;

    public LocalDateTime createdAt;

    public LocalDateTime updatedAt;

    // 错误断点 用于恢复任务
    private long breakpoint;


    @Override
    public Step<?, JsonNode> createDefault() {
        return new StepImpl();
    }

    @Override
    public <T> T deserializeMeta(Object any, Class<? extends T> metaType) {
        return OBJECT_MAPPER.convertValue(any, metaType);
    }

    @Override
    public JsonNode serializeMeta(Object any) {
        return OBJECT_MAPPER.valueToTree(any);
    }

    @Override
    public boolean swapStatus(Status prev, Status next) {
        setStatus(next);
        return true;
    }

    @Override
    public Step fetchStep(long id) {
        return StepImpl.DB.get(id);
    }


    @Override
    public void touch() {

    }

    @Override
    public long insert() {
        this.id = SEQ.incrementAndGet();
        synchronized (DB) {
            DB.put(this.id, this);
        }
        return this.id;
    }

}
