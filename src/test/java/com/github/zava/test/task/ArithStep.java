package com.github.zava.test.task;

import com.github.zava.core.task.AbstractStep;

public class ArithStep extends AbstractStep<Integer, StepImpl> {
    public Class getMetaType() {
        return Integer.class;
    }

    public Next init() {
        return Next.POLL;
    }

    public Next next(StepImpl callbackFrom) {
        if (callbackFrom == null)
            return fork(1, 1);
        if (callbackFrom.getType() == 1)
            return fork(2, callbackFrom.getMeta());
        return Next.DONE;
    }

}
