package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.dto.TaskDto;
import com.example.taskmanagement.dto.UpdateTaskRequest;
import com.example.taskmanagement.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {

    TaskDto createTask(CreateTaskRequest request);

    TaskDto getTaskById(Long id);

    List<TaskDto> getAllTasks();

    Page<TaskDto> getAllTasks(Pageable pageable);

    TaskDto updateTask(Long id, UpdateTaskRequest request);

    void deleteTask(Long id);

    List<TaskDto> getTasksByStatus(TaskStatus status);

    List<TaskDto> searchTasksByTitle(String keyword);

    long getTotalTaskCount();

    long getTaskCountByStatus(TaskStatus status);

    List<TaskDto> getTasksByPriority(String priority);
}
