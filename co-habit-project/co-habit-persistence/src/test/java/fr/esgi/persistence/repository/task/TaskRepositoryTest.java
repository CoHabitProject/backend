package fr.esgi.persistence.repository.task;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("elasticsearch")
@EnabledIfSystemProperty(named = "elasticsearch.enabled", matches = "true")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

}

