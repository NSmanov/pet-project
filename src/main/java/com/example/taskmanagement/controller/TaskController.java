package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.dto.TaskDto;
import com.example.taskmanagement.dto.UpdateTaskRequest;
import com.example.taskmanagement.entity.TaskStatus;
import com.example.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskDto>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        log.info("POST /api/tasks - Creating new task");
        TaskDto task = taskService.createTask(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDto>> getTaskById(@PathVariable Long id) {
        log.info("GET /api/tasks/{} - Fetching task", id);
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskDto>>> getAllTasks(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page != null && size != null) {
            log.info("GET /api/tasks - Fetching tasks with pagination: page={}, size={}", page, size);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<TaskDto> tasksPage = taskService.getAllTasks(pageable);
            return ResponseEntity.ok(ApiResponse.success(tasksPage.getContent()));
        } else {
            log.info("GET /api/tasks - Fetching all tasks");
            List<TaskDto> tasks = taskService.getAllTasks();
            return ResponseEntity.ok(ApiResponse.success(tasks));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDto>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        log.info("PUT /api/tasks/{} - Updating task", id);
        TaskDto task = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        log.info("DELETE /api/tasks/{} - Deleting task", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getTasksByStatus(@PathVariable TaskStatus status) {
        log.info("GET /api/tasks/status/{} - Fetching tasks by status", status);
        List<TaskDto> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TaskDto>>> searchTasks(@RequestParam String keyword) {
        log.info("GET /api/tasks/search?keyword={} - Searching tasks", keyword);
        List<TaskDto> tasks = taskService.searchTasksByTitle(keyword);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponse<Long>> getTotalTaskCount() {
        log.info("GET /api/tasks/stats/count - Getting total task count");
        long count = taskService.getTotalTaskCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/stats/count/{status}")
    public ResponseEntity<ApiResponse<Long>> getTaskCountByStatus(@PathVariable TaskStatus status) {
        log.info("GET /api/tasks/stats/count/{} - Getting task count by status", status);
        long count = taskService.getTaskCountByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
