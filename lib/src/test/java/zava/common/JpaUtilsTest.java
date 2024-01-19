package zava.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaUtilsTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    public void testPatch() {
        String json = "{\"id\": 100}";
        Sample sample = new Sample(50L, "abc", null);
        JpaUtils.patch(objectMapper, sample, objectMapper.readValue(json, JsonNode.class));
        Assert.assertEquals((long) Objects.requireNonNull(sample.getId()), 100L);
    }
}
