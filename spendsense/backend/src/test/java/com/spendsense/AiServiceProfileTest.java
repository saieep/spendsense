package com.spendsense;

import static org.assertj.core.api.Assertions.assertThat;

import com.spendsense.service.AiService;
import com.spendsense.service.MockAiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiServiceProfileTest extends AbstractIntegrationTest {

    @Autowired
    private AiService aiService;

    @Test
    void testProfileWiresMockAiService() {
        assertThat(aiService).isInstanceOf(MockAiService.class);
    }
}
