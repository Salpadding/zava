package com.github.zava.test.task;

import com.github.zava.core.task.AbstractStep;

public class IncrStep extends AbstractStep<Integer, StepImpl> {
    public Class getMetaType() {
        return Integer.class;
    }

    public Next init() {
        super.init();
        this.meta++;
        step.setMeta(TaskImpl.OBJECT_MAPPER.valueToTree(this.meta));
        return Next.DONE;
    }
}
