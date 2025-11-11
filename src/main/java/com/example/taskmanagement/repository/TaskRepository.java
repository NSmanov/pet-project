package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.deleted = false")
    List<Task> findAllActive();

    @Query("SELECT t FROM Task t WHERE t.deleted = false")
    Page<Task> findAllActive(Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.deleted = false")
    Optional<Task> findByIdActive(@Param("id") Long id);

    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.deleted = false")
    List<Task> findByStatus(@Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND t.deleted = false")
    List<Task> searchByTitle(@Param("keyword") String keyword);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.deleted = false")
    long countActive();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status AND t.deleted = false")
    long countByStatus(@Param("status") TaskStatus status);
}
