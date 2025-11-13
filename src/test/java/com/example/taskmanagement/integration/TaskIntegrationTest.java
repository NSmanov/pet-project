package com.example.taskmanagement.integration;

import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.dto.UpdateTaskRequest;
import com.example.taskmanagement.entity.TaskPriority;
import com.example.taskmanagement.entity.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@DisplayName("Task Integration Tests")
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
    @DisplayName("Should create task successfully")
    void shouldCreateTaskSuccessfully() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Integration Test Task")
                .description("Testing task creation")
                .priority(TaskPriority.HIGH)
                .build();

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
    }

    @Test
    @DisplayName("Should get all tasks successfully")
    void shouldGetAllTasksSuccessfully() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should get task by id successfully")
    void shouldGetTaskByIdSuccessfully() throws Exception {
        // First, create a task
        CreateTaskRequest createRequest = CreateTaskRequest.builder()
                .title("Task to Retrieve")
                .description("Testing task retrieval")
                .priority(TaskPriority.MEDIUM)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = createResult.getResponse().getContentAsString();
        Long taskId = objectMapper.readTree(jsonResponse).get("data").get("id").asLong();

        // Then, retrieve it
        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(taskId))
                .andExpect(jsonPath("$.data.title").value("Task to Retrieve"));
    }

    @Test
    @DisplayName("Should update task successfully")
    void shouldUpdateTaskSuccessfully() throws Exception {
        // First, create a task
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
        Long taskId = objectMapper.readTree(jsonResponse).get("data").get("id").asLong();

        // Then, update it
        UpdateTaskRequest updateRequest = UpdateTaskRequest.builder()
                .title("Updated Task Title")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.CRITICAL)
                .build();

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Task Title"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.priority").value("CRITICAL"));
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() throws Exception {
        // First, create a task
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
        Long taskId = objectMapper.readTree(jsonResponse).get("data").get("id").asLong();

        // Then, delete it
        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task deleted successfully"));

        // Verify it's deleted
        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get tasks by status successfully")
    void shouldGetTasksByStatusSuccessfully() throws Exception {
        mockMvc.perform(get("/api/tasks/status/{status}", "TODO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Should search tasks by title successfully")
    void shouldSearchTasksByTitleSuccessfully() throws Exception {
        // Create a task with a specific title
        CreateTaskRequest createRequest = CreateTaskRequest.builder()
                .title("Searchable Unique Task")
                .priority(TaskPriority.MEDIUM)
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // Search for it
        mockMvc.perform(get("/api/tasks/search")
                        .param("keyword", "Searchable")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].title", hasItem(containsString("Searchable"))));
    }

    @Test
    @DisplayName("Should return 404 when task not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Task not found")));
    }

    @Test
    @DisplayName("Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        CreateTaskRequest invalidRequest = CreateTaskRequest.builder()
                .title("")  // Empty title should fail validation
                .priority(TaskPriority.HIGH)
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should get task statistics successfully")
    void shouldGetTaskStatisticsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/tasks/stats/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("Should filter tasks by status correctly")
    void shouldFilterTasksByStatusCorrectly() throws Exception {
        // Create three tasks (all will be TODO by default)
        CreateTaskRequest task1 = CreateTaskRequest.builder()
                .title("TODO Task for Filter Test")
                .description("This should stay in TODO status")
                .priority(TaskPriority.MEDIUM)
                .build();

        CreateTaskRequest task2 = CreateTaskRequest.builder()
                .title("In Progress Task for Filter Test")
                .description("This will be changed to IN_PROGRESS")
                .priority(TaskPriority.HIGH)
                .build();

        CreateTaskRequest task3 = CreateTaskRequest.builder()
                .title("Done Task for Filter Test")
                .description("This will be changed to DONE")
                .priority(TaskPriority.LOW)
                .build();

        // Create all three tasks and get their IDs
        String response1 = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long todoTaskId = objectMapper.readTree(response1).get("data").get("id").asLong();

        String response2 = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long inProgressTaskId = objectMapper.readTree(response2).get("data").get("id").asLong();

        String response3 = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task3)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long doneTaskId = objectMapper.readTree(response3).get("data").get("id").asLong();

        // Update task2 to IN_PROGRESS
        UpdateTaskRequest updateToInProgress = UpdateTaskRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        mockMvc.perform(put("/api/tasks/{id}", inProgressTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateToInProgress)))
                .andExpect(status().isOk());

        // Update task3 to DONE
        UpdateTaskRequest updateToDone = UpdateTaskRequest.builder()
                .status(TaskStatus.DONE)
                .build();
        mockMvc.perform(put("/api/tasks/{id}", doneTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateToDone)))
                .andExpect(status().isOk());

        // Filter by TODO status - should find task1
        mockMvc.perform(get("/api/tasks")
                        .param("status", "TODO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.title == 'TODO Task for Filter Test')].status").value(hasItem("TODO")));

        // Filter by IN_PROGRESS status - should find task2
        mockMvc.perform(get("/api/tasks")
                        .param("status", "IN_PROGRESS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.title == 'In Progress Task for Filter Test')].status").value(hasItem("IN_PROGRESS")));

        // Filter by DONE status - should find task3
        mockMvc.perform(get("/api/tasks")
                        .param("status", "DONE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.title == 'Done Task for Filter Test')].status").value(hasItem("DONE")));
    }
}
