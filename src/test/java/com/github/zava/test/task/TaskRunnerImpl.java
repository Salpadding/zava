package com.github.zava.test.task;

import com.github.zava.core.task.AbstractStep;
import com.github.zava.core.task.TaskListener;
import com.github.zava.core.task.TaskRunner;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j(topic = "task")
public class TaskRunnerImpl extends TaskRunner<TaskImpl, StepImpl> {
    static {
        AbstractStep.register(1, IncrStep.class);
        AbstractStep.register(2, DoubleStep.class);
        AbstractStep.register(3, ArithStep.class);
    }

    public static class Listener implements TaskListener<TaskImpl, StepImpl> {

        @Override
        public void onTaskStatusChange(TaskImpl task) {
            log.info("task {} status = {}", task.getId(), task.getStatus());
        }

        @Override
        public void onStepStatusChange(StepImpl step) {
            log.info("step {} status = {}", step.getId(), step.getStatus());
        }
    }

    public TaskRunnerImpl() {
        super(new Listener(), log);
    }

    @Override
    public int tryPause() {
        return 0;
    }

    @Override
    public List<TaskImpl> fetchPaused() {
        return Collections.emptyList();
    }
}
