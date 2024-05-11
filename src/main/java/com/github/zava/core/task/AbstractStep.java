package com.github.zava.core.task;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;


public abstract class AbstractStep<T, S extends Step<?, ?>> {
    public static final int MAX_WARN_COUNT = 3;

    protected Class<T> getMetaType() {
        return null;
    }

    public int getSleep() {
        return 1;
    }

    @Getter
    public static class Next {
        private final Step<?, ?> fork;

        private Next(Step<?, ?> fork) {
            this.fork = fork;
        }

        public Status getStepStatus() {
            if (this == DONE) return Status.done;
            if (this == ERROR) return Status.error;
            return Status.running;
        }

        public Status getTaskStatus() {
            if (this == DONE) return Status.done;
            if (this == ERROR) return Status.error;
            return Status.running;
        }

        private static Next fork(Step fork) {
            return new Next(fork);
        }

        public static final Next POLL = new Next(null);
        public static final Next DONE = new Next(null);
        public static final Next ERROR = new Next(null);
        public static final Next WARN = new Next(null);
    }

    private static final Map<Integer, Class<?>> TYPE_CLASS_MAP = new ConcurrentSkipListMap<>();

    public static void register(int type, Class<? extends AbstractStep> stepRunner) {
        TYPE_CLASS_MAP.put(type, stepRunner);
    }

    @Getter
    @Setter
    public S step;

    protected T meta;

    public void setup() {
        if (meta == null && getMetaType() != null)
            this.meta = (T) step.getTask()
                .deserializeMeta(getStep().getMeta(), getMetaType());
    }

    public Next init() {
        setup();
        return Next.DONE;
    }

    public int warnCount;

    @SneakyThrows
    public static AbstractStep load(Step<?, ?> step) {
        return (AbstractStep) TYPE_CLASS_MAP.get((int) step.getType())
            .getConstructor()
            .newInstance();
    }

    public Next next(S callbackFrom) {
        setup();
        return Next.DONE;
    }


    // fork 新的 step
    protected Next fork(long type, Object meta) {
        Step newStep = step.fork(type, meta);
        return Next.fork(newStep);
    }
}

