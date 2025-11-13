package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.dto.TaskDto;
import com.example.taskmanagement.dto.UpdateTaskRequest;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.TaskPriority;
import com.example.taskmanagement.entity.TaskStatus;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.TaskRepository;
import io.qameta.allure.*;
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
@Epic("Управление Задачами")
@Feature("Сервисный слой задач")
@DisplayName("Модульные тесты TaskService")
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
    @Story("Создание задачи")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Успешное создание задачи через сервис")
    @Description("Проверяет, что сервис корректно создает задачу и возвращает DTO с правильными данными")
    void shouldCreateTaskSuccessfully() {
        Allure.step("Настройка mock-объекта: repository вернет сохраненную задачу", () -> {
            Allure.parameter("Mock метод", "taskRepository.save()");
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        });

        TaskDto result = Allure.step("Вызов метода createTask() сервиса", () -> {
            Allure.parameter("Заголовок", createRequest.getTitle());
            Allure.parameter("Приоритет", createRequest.getPriority());
            return taskService.createTask(createRequest);
        });

        Allure.step("Проверка результата создания задачи", () -> {
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testTask.getId());
            assertThat(result.getTitle()).isEqualTo(testTask.getTitle());
            assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);

            Allure.addAttachment("ID созданной задачи", String.valueOf(result.getId()));
            Allure.addAttachment("Статус по умолчанию", result.getStatus().toString());
        });

        Allure.step("Проверка, что repository.save() был вызван ровно 1 раз", () -> {
            verify(taskRepository, times(1)).save(any(Task.class));
        });
    }

    @Test
    @Story("Получение задачи по ID")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Получение существующей задачи по идентификатору")
    @Description("Проверяет корректное получение задачи из репозитория по ID")
    void shouldGetTaskByIdSuccessfully() {
        Allure.step("Настройка mock: repository найдет задачу с ID=1", () -> {
            Allure.parameter("ID задачи", 1L);
            when(taskRepository.findByIdActive(1L)).thenReturn(Optional.of(testTask));
        });

        TaskDto result = Allure.step("Получение задачи через сервис", () ->
            taskService.getTaskById(1L)
        );

        Allure.step("Проверка полученных данных задачи", () -> {
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Test Task");

            Allure.addAttachment("Заголовок задачи", result.getTitle());
            Allure.addAttachment("Статус задачи", result.getStatus().toString());
        });

        Allure.step("Проверка вызова repository.findByIdActive()", () -> {
            verify(taskRepository, times(1)).findByIdActive(1L);
        });
    }

    @Test
    @Story("Обработка ошибок")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Выброс исключения при запросе несуществующей задачи")
    @Description("Проверяет, что сервис выбрасывает ResourceNotFoundException для несуществующего ID")
    void shouldThrowExceptionWhenTaskNotFound() {
        Allure.step("Настройка mock: repository не найдет задачу с ID=999", () -> {
            Allure.parameter("Несуществующий ID", 999L);
            when(taskRepository.findByIdActive(999L)).thenReturn(Optional.empty());
        });

        Allure.step("Попытка получить несуществующую задачу", () -> {
            assertThatThrownBy(() -> taskService.getTaskById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task not found with id: 999");
        });

        Allure.step("Проверка, что repository был вызван для поиска", () -> {
            verify(taskRepository, times(1)).findByIdActive(999L);
        });
    }

    @Test
    @Story("Получение списка задач")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Получение всех активных задач")
    @Description("Проверяет корректность получения списка всех не удаленных задач")
    void shouldGetAllTasksSuccessfully() {
        Task task2 = Allure.step("Подготовка второй тестовой задачи", () ->
            Task.builder()
                    .id(2L)
                    .title("Task 2")
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.LOW)
                    .deleted(false)
                    .build()
        );

        Allure.step("Настройка mock: repository вернет 2 задачи", () -> {
            Allure.parameter("Количество задач", 2);
            when(taskRepository.findAllActive()).thenReturn(Arrays.asList(testTask, task2));
        });

        List<TaskDto> results = Allure.step("Получение всех задач через сервис", () ->
            taskService.getAllTasks()
        );

        Allure.step("Проверка количества и содержимого полученных задач", () -> {
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getTitle()).isEqualTo("Test Task");
            assertThat(results.get(1).getTitle()).isEqualTo("Task 2");

            Allure.addAttachment("Количество задач", String.valueOf(results.size()));
        });

        Allure.step("Проверка вызова repository.findAllActive()", () -> {
            verify(taskRepository, times(1)).findAllActive();
        });
    }

    @Test
    @Story("Обновление задачи")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Успешное обновление существующей задачи")
    @Description("Проверяет, что сервис корректно обновляет данные задачи")
    void shouldUpdateTaskSuccessfully() {
        Allure.step("Настройка mock: repository найдет и сохранит задачу", () -> {
            Allure.parameter("ID задачи для обновления", 1L);
            when(taskRepository.findByIdActive(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        });

        TaskDto result = Allure.step("Обновление задачи через сервис", () -> {
            Allure.parameter("Новый заголовок", updateRequest.getTitle());
            Allure.parameter("Новый статус", updateRequest.getStatus());
            return taskService.updateTask(1L, updateRequest);
        });

        Allure.step("Проверка, что задача обновлена", () -> {
            assertThat(result).isNotNull();
            Allure.addAttachment("Результат", "Задача успешно обновлена");
        });

        Allure.step("Проверка вызовов repository", () -> {
            verify(taskRepository, times(1)).findByIdActive(1L);
            verify(taskRepository, times(1)).save(any(Task.class));
        });
    }

    @Test
    @Story("Удаление задачи")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Мягкое удаление задачи через сервис")
    @Description("Проверяет, что сервис корректно помечает задачу как удаленную (soft delete)")
    void shouldDeleteTaskSuccessfully() {
        Allure.step("Настройка mock: repository найдет и сохранит задачу", () -> {
            Allure.parameter("ID задачи для удаления", 1L);
            when(taskRepository.findByIdActive(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        });

        Allure.step("Удаление задачи через сервис", () -> {
            taskService.deleteTask(1L);
        });

        Allure.step("Проверка вызовов repository для soft delete", () -> {
            verify(taskRepository, times(1)).findByIdActive(1L);
            verify(taskRepository, times(1)).save(any(Task.class));
            Allure.addAttachment("Тип удаления", "Soft Delete (deleted=true)");
        });
    }

    @Test
    @Story("Фильтрация задач")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Получение задач по статусу")
    @Description("Проверяет корректность фильтрации задач по заданному статусу")
    void shouldGetTasksByStatusSuccessfully() {
        Allure.step("Настройка mock: repository вернет задачи со статусом TODO", () -> {
            Allure.parameter("Фильтр статуса", TaskStatus.TODO);
            when(taskRepository.findByStatus(TaskStatus.TODO)).thenReturn(Arrays.asList(testTask));
        });

        List<TaskDto> results = Allure.step("Получение задач по статусу TODO", () ->
            taskService.getTasksByStatus(TaskStatus.TODO)
        );

        Allure.step("Проверка результатов фильтрации", () -> {
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getStatus()).isEqualTo(TaskStatus.TODO);

            Allure.addAttachment("Количество найденных задач", String.valueOf(results.size()));
            Allure.addAttachment("Статус фильтра", "TODO");
        });

        Allure.step("Проверка вызова repository с правильным статусом", () -> {
            verify(taskRepository, times(1)).findByStatus(TaskStatus.TODO);
        });
    }

    @Test
    @Story("Поиск задач")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Поиск задач по ключевому слову в заголовке")
    @Description("Проверяет поиск задач по части заголовка")
    void shouldSearchTasksByTitleSuccessfully() {
        Allure.step("Настройка mock: repository найдет задачи по ключевому слову", () -> {
            Allure.parameter("Ключевое слово", "Test");
            when(taskRepository.searchByTitle("Test")).thenReturn(Arrays.asList(testTask));
        });

        List<TaskDto> results = Allure.step("Поиск задач с ключевым словом 'Test'", () ->
            taskService.searchTasksByTitle("Test")
        );

        Allure.step("Проверка результатов поиска", () -> {
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).contains("Test");

            Allure.addAttachment("Найдено задач", String.valueOf(results.size()));
            Allure.addAttachment("Заголовок найденной задачи", results.get(0).getTitle());
        });

        Allure.step("Проверка вызова repository.searchByTitle()", () -> {
            verify(taskRepository, times(1)).searchByTitle("Test");
        });
    }

    @Test
    @Story("Статистика задач")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Получение общего количества активных задач")
    @Description("Проверяет корректность подсчета количества не удаленных задач")
    void shouldGetTotalTaskCountSuccessfully() {
        Allure.step("Настройка mock: repository вернет количество задач", () -> {
            Allure.parameter("Количество активных задач", 5L);
            when(taskRepository.countActive()).thenReturn(5L);
        });

        long count = Allure.step("Получение количества задач через сервис", () ->
            taskService.getTotalTaskCount()
        );

        Allure.step("Проверка полученного количества", () -> {
            assertThat(count).isEqualTo(5L);
            Allure.addAttachment("Общее количество задач", String.valueOf(count));
        });

        Allure.step("Проверка вызова repository.countActive()", () -> {
            verify(taskRepository, times(1)).countActive();
        });
    }
}
