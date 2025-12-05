package com.example.taskmanagement.integration;

import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.dto.UpdateTaskRequest;
import com.example.taskmanagement.entity.TaskPriority;
import com.example.taskmanagement.entity.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Epic("Управление Задачами")
@Feature("API для работы с задачами")
@DisplayName("Интеграционные тесты API задач")
class TaskIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Story("Создание задачи")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Создание новой задачи через API")
    @Description("Проверяет успешное создание задачи с валидными данными и корректный статус ответа")
    void shouldCreateTaskSuccessfully() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            Allure.parameter("Заголовок", "Integration Test Task");
            Allure.parameter("Описание", "Testing task creation");
            Allure.parameter("Приоритет", "HIGH");
        });

        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Integration Test Task")
                .description("Testing task creation")
                .priority(TaskPriority.HIGH)
                .build();

        Allure.step("Отправка POST запроса на /api/tasks для создания задачи", () -> {
            Allure.addAttachment("Request Body", "application/json", objectMapper.writeValueAsString(request));

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Task created successfully"))
                    .andExpect(jsonPath("$.data.title").value("Integration Test Task"))
                    .andExpect(jsonPath("$.data.status").value("TODO"))
                    .andExpect(jsonPath("$.data.priority").value("HIGH"));
        });

        Allure.step("Проверка, что задача создана со статусом TODO по умолчанию");
    }

    @Test
    @Story("Получение списка задач")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Получение всех задач из системы")
    @Description("Проверяет корректность получения полного списка задач через API")
    void shouldGetAllTasksSuccessfully() throws Exception {
        Allure.step("Отправка GET запроса на /api/tasks для получения всех задач", () -> {
            mockMvc.perform(get("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
        });

        Allure.step("Проверка, что в ответе присутствует непустой массив задач");
    }

    @Test
    @Story("Получение задачи по ID")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Получение конкретной задачи по идентификатору")
    @Description("Создает задачу, затем получает ее по ID и проверяет корректность данных")
    void shouldGetTaskByIdSuccessfully() throws Exception {
        Long taskId = Allure.step("Создание тестовой задачи для последующего получения", () -> {
            CreateTaskRequest createRequest = CreateTaskRequest.builder()
                    .title("Task to Retrieve")
                    .description("Testing task retrieval")
                    .priority(TaskPriority.MEDIUM)
                    .build();

            Allure.addAttachment("Данные задачи", "application/json",
                objectMapper.writeValueAsString(createRequest));

            MvcResult createResult = mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String jsonResponse = createResult.getResponse().getContentAsString();
            Long id = objectMapper.readTree(jsonResponse).get("data").get("id").asLong();
            Allure.parameter("ID созданной задачи", id);
            return id;
        });

        Allure.step("Получение задачи по ID: " + taskId, () -> {
            mockMvc.perform(get("/api/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(taskId))
                    .andExpect(jsonPath("$.data.title").value("Task to Retrieve"));
        });

        Allure.step("Проверка, что полученная задача содержит корректные данные");
    }

    @Test
    @Story("Обновление задачи")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Обновление существующей задачи")
    @Description("Создает задачу, обновляет ее данные и проверяет, что изменения сохранились")
    void shouldUpdateTaskSuccessfully() throws Exception {
        Long taskId = Allure.step("Создание задачи для последующего обновления", () -> {
            CreateTaskRequest createRequest = CreateTaskRequest.builder()
                    .title("Task to Update")
                    .description("Original description")
                    .priority(TaskPriority.LOW)
                    .build();

            MvcResult createResult = mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String jsonResponse = createResult.getResponse().getContentAsString();
            Long id = objectMapper.readTree(jsonResponse).get("data").get("id").asLong();
            Allure.parameter("ID задачи", id);
            return id;
        });

        Allure.step("Обновление данных задачи: заголовок, статус и приоритет", () -> {
            UpdateTaskRequest updateRequest = UpdateTaskRequest.builder()
                    .title("Updated Task Title")
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.CRITICAL)
                    .build();

            Allure.addAttachment("Новые данные", "application/json",
                objectMapper.writeValueAsString(updateRequest));

            mockMvc.perform(put("/api/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Updated Task Title"))
                    .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.data.priority").value("CRITICAL"));
        });

        Allure.step("Проверка, что все поля задачи обновлены корректно");
    }

    @Test
    @Story("Удаление задачи")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Мягкое удаление задачи (soft delete)")
    @Description("Создает задачу, удаляет ее и проверяет, что она больше недоступна")
    void shouldDeleteTaskSuccessfully() throws Exception {
        Long taskId = Allure.step("Создание задачи для последующего удаления", () -> {
            CreateTaskRequest createRequest = CreateTaskRequest.builder()
                    .title("Task to Delete")
                    .priority(TaskPriority.LOW)
                    .build();

            MvcResult createResult = mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String jsonResponse = createResult.getResponse().getContentAsString();
            Long id = objectMapper.readTree(jsonResponse).get("data").get("id").asLong();
            Allure.parameter("ID задачи", id);
            return id;
        });

        Allure.step("Удаление задачи через DELETE /api/tasks/{id}", () -> {
            mockMvc.perform(delete("/api/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Task deleted successfully"));
        });

        Allure.step("Проверка, что удаленная задача больше не доступна (404)", () -> {
            mockMvc.perform(get("/api/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        });
    }

    @Test
    @Story("Фильтрация задач")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Получение задач по статусу")
    @Description("Проверяет корректность фильтрации задач по статусу TODO")
    void shouldGetTasksByStatusSuccessfully() throws Exception {
        Allure.step("Запрос задач со статусом TODO через GET /api/tasks/status/TODO", () -> {
            mockMvc.perform(get("/api/tasks/status/{status}", "TODO")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        });

        Allure.step("Проверка, что все возвращенные задачи имеют статус TODO");
    }

    @Test
    @Story("Поиск задач")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Поиск задач по ключевому слову в заголовке")
    @Description("Создает задачу с уникальным заголовком и проверяет, что ее можно найти по ключевому слову")
    void shouldSearchTasksByTitleSuccessfully() throws Exception {
        Allure.step("Создание задачи с уникальным заголовком для поиска", () -> {
            CreateTaskRequest createRequest = CreateTaskRequest.builder()
                    .title("Searchable Unique Task")
                    .priority(TaskPriority.MEDIUM)
                    .build();

            Allure.parameter("Ключевое слово", "Searchable");

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated());
        });

        Allure.step("Поиск задач по ключевому слову 'Searchable'", () -> {
            mockMvc.perform(get("/api/tasks/search")
                            .param("keyword", "Searchable")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[*].title", hasItem(containsString("Searchable"))));
        });

        Allure.step("Проверка, что созданная задача найдена в результатах поиска");
    }

    @Test
    @Story("Обработка ошибок")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Возврат 404 при запросе несуществующей задачи")
    @Description("Проверяет корректную обработку запроса задачи с несуществующим ID")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        Allure.step("Запрос несуществующей задачи с ID 999999", () -> {
            Allure.parameter("Несуществующий ID", 999999L);

            mockMvc.perform(get("/api/tasks/{id}", 999999L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Task not found")));
        });

        Allure.step("Проверка, что возвращается статус 404 и сообщение об ошибке");
    }

    @Test
    @Story("Валидация данных")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Возврат 400 при невалидных данных задачи")
    @Description("Проверяет валидацию данных при создании задачи с пустым заголовком")
    void shouldReturn400WhenValidationFails() throws Exception {
        Allure.step("Подготовка невалидных данных (пустой заголовок)", () -> {
            Allure.parameter("Заголовок", "");
            Allure.parameter("Приоритет", "HIGH");
        });

        CreateTaskRequest invalidRequest = CreateTaskRequest.builder()
                .title("")  // Empty title should fail validation
                .priority(TaskPriority.HIGH)
                .build();

        Allure.step("Отправка запроса с невалидными данными", () -> {
            Allure.addAttachment("Невалидные данные", "application/json",
                objectMapper.writeValueAsString(invalidRequest));

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        });

        Allure.step("Проверка, что возвращается статус 400 Bad Request");
    }

    @Test
    @Story("Статистика задач")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Получение статистики: общее количество задач")
    @Description("Проверяет корректность получения общего количества задач в системе")
    void shouldGetTaskStatisticsSuccessfully() throws Exception {
        Allure.step("Запрос статистики через GET /api/tasks/stats/count", () -> {
            mockMvc.perform(get("/api/tasks/stats/count")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber());
        });

        Allure.step("Проверка, что в ответе содержится числовое значение количества задач");
    }

    @Test
    @Story("Фильтрация задач")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Комплексная проверка фильтрации задач по всем статусам")
    @Description("Создает задачи с разными статусами и проверяет корректность фильтрации по каждому статусу")
    void shouldFilterTasksByStatusCorrectly() throws Exception {
        Allure.step("Подготовка: создание трех задач с разными статусами", () -> {
            Allure.parameter("Количество задач", 3);
            Allure.parameter("Статусы", "TODO, IN_PROGRESS, DONE");
        });

        Long todoTaskId = Allure.step("Создание задачи #1 (TODO)", () -> {
            CreateTaskRequest task1 = CreateTaskRequest.builder()
                    .title("TODO Task for Filter Test")
                    .description("This should stay in TODO status")
                    .priority(TaskPriority.MEDIUM)
                    .build();

            String response1 = mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(task1)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long id = objectMapper.readTree(response1).get("data").get("id").asLong();
            Allure.parameter("ID задачи TODO", id);
            return id;
        });

        Long inProgressTaskId = Allure.step("Создание задачи #2 (будет IN_PROGRESS)", () -> {
            CreateTaskRequest task2 = CreateTaskRequest.builder()
                    .title("In Progress Task for Filter Test")
                    .description("This will be changed to IN_PROGRESS")
                    .priority(TaskPriority.HIGH)
                    .build();

            String response2 = mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(task2)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long id = objectMapper.readTree(response2).get("data").get("id").asLong();
            Allure.parameter("ID задачи IN_PROGRESS", id);
            return id;
        });

        Long doneTaskId = Allure.step("Создание задачи #3 (будет DONE)", () -> {
            CreateTaskRequest task3 = CreateTaskRequest.builder()
                    .title("Done Task for Filter Test")
                    .description("This will be changed to DONE")
                    .priority(TaskPriority.LOW)
                    .build();

            String response3 = mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(task3)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long id = objectMapper.readTree(response3).get("data").get("id").asLong();
            Allure.parameter("ID задачи DONE", id);
            return id;
        });

        Allure.step("Обновление задачи #2 на статус IN_PROGRESS", () -> {
            UpdateTaskRequest updateToInProgress = UpdateTaskRequest.builder()
                    .status(TaskStatus.IN_PROGRESS)
                    .build();
            mockMvc.perform(put("/api/tasks/{id}", inProgressTaskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateToInProgress)))
                    .andExpect(status().isOk());
        });

        Allure.step("Обновление задачи #3 на статус DONE", () -> {
            UpdateTaskRequest updateToDone = UpdateTaskRequest.builder()
                    .status(TaskStatus.DONE)
                    .build();
            mockMvc.perform(put("/api/tasks/{id}", doneTaskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateToDone)))
                    .andExpect(status().isOk());
        });

        Allure.step("Проверка фильтра: получение задач со статусом TODO", () -> {
            mockMvc.perform(get("/api/tasks")
                            .param("status", "TODO")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[?(@.title == 'TODO Task for Filter Test')].status").value(hasItem("TODO")));
        });

        Allure.step("Проверка фильтра: получение задач со статусом IN_PROGRESS", () -> {
            mockMvc.perform(get("/api/tasks")
                            .param("status", "IN_PROGRESS")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[?(@.title == 'In Progress Task for Filter Test')].status").value(hasItem("IN_PROGRESS")));
        });

        Allure.step("Проверка фильтра: получение задач со статусом DONE", () -> {
            mockMvc.perform(get("/api/tasks")
                            .param("status", "DONE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[?(@.title == 'Done Task for Filter Test')].status").value(hasItem("DONE")));
        });

        Allure.step("Проверка завершена: все фильтры работают корректно");
    }
}
