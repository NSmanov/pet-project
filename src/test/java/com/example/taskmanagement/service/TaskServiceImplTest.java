package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.dto.TaskDto;
import com.example.taskmanagement.dto.UpdateTaskRequest;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.TaskPriority;
import com.example.taskmanagement.entity.TaskStatus;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task testTask;
    private CreateTaskRequest createRequest;
    private UpdateTaskRequest updateRequest;

    @BeforeEach
    void setUp() {
        testTask = Task.builder()
                .id(1L)
                .taskCode("TASK-1")
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateTaskRequest.builder()
                .title("New Task")
                .description("New Description")
                .priority(TaskPriority.MEDIUM)
                .build();

        updateRequest = UpdateTaskRequest.builder()
                .title("Updated Task")
                .status(TaskStatus.IN_PROGRESS)
                .build();
    }

    @Test
    @DisplayName("Should create task successfully")
    void shouldCreateTaskSuccessfully() {
        // Arrange
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        TaskDto result = taskService.createTask(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testTask.getId());
        assertThat(result.getTitle()).isEqualTo(testTask.getTitle());
        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
        verify(taskRepository, times(2)).save(any(Task.class)); // Called twice: once to get ID, once to set taskCode
    }

    @Test
    @DisplayName("Should get task by id successfully")
    void shouldGetTaskByIdSuccessfully() {
        // Arrange
        when(taskRepository.findByIdActive(1L)).thenReturn(Optional.of(testTask));

        // Act
        TaskDto result = taskService.getTaskById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskRepository, times(1)).findByIdActive(1L);
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void shouldThrowExceptionWhenTaskNotFound() {
        // Arrange
        when(taskRepository.findByIdActive(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");
        verify(taskRepository, times(1)).findByIdActive(999L);
    }

    @Test
    @DisplayName("Should get all tasks successfully")
    void shouldGetAllTasksSuccessfully() {
        // Arrange
        Task task2 = Task.builder()
                .id(2L)
                .taskCode("TASK-2")
                .title("Task 2")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.LOW)
                .deleted(false)
                .build();

        when(taskRepository.findAllActive()).thenReturn(Arrays.asList(testTask, task2));

        // Act
        List<TaskDto> results = taskService.getAllTasks();

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTitle()).isEqualTo("Test Task");
        assertThat(results.get(1).getTitle()).isEqualTo("Task 2");
        verify(taskRepository, times(1)).findAllActive();
    }

    @Test
    @DisplayName("Should update task successfully")
    void shouldUpdateTaskSuccessfully() {
        // Arrange
        when(taskRepository.findByIdActive(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        TaskDto result = taskService.updateTask(1L, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository, times(1)).findByIdActive(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() {
        // Arrange
        when(taskRepository.findByIdActive(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository, times(1)).findByIdActive(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should get tasks by status successfully")
    void shouldGetTasksByStatusSuccessfully() {
        // Arrange
        when(taskRepository.findByStatus(TaskStatus.TODO)).thenReturn(Arrays.asList(testTask));

        // Act
        List<TaskDto> results = taskService.getTasksByStatus(TaskStatus.TODO);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(TaskStatus.TODO);
        verify(taskRepository, times(1)).findByStatus(TaskStatus.TODO);
    }

    @Test
    @DisplayName("Should search tasks by title successfully")
    void shouldSearchTasksByTitleSuccessfully() {
        // Arrange
        when(taskRepository.searchByTitle("Test")).thenReturn(Arrays.asList(testTask));

        // Act
        List<TaskDto> results = taskService.searchTasksByTitle("Test");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("Test");
        verify(taskRepository, times(1)).searchByTitle("Test");
    }

    @Test
    @DisplayName("Should get total task count successfully")
    void shouldGetTotalTaskCountSuccessfully() {
        // Arrange
        when(taskRepository.countActive()).thenReturn(5L);

        // Act
        long count = taskService.getTotalTaskCount();

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(taskRepository, times(1)).countActive();
    }
}
