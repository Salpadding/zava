package com.github.zava.test;

import com.github.zava.core.task.TaskRunner;
import com.github.zava.test.task.StepImpl;
import com.github.zava.test.task.TaskImpl;
import com.github.zava.test.task.TaskRunnerImpl;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class TaskTest {
    @Test
    @SneakyThrows
    public void test() {
        TaskRunner<TaskImpl, StepImpl> runner =
            new TaskRunnerImpl();

        long id = runner.submit(new TaskImpl(), 3, 1L);
        runner.threads.get(id).join();
    }
}
