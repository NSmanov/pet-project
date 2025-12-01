import React from 'react';
import './TaskCard.css';

const TaskCard = ({ task, onEdit, onDelete }) => {
  const getPriorityColor = (priority) => {
    const colors = {
      LOW: '#6c757d',
      MEDIUM: '#ffc107',
      HIGH: '#fd7e14',
      CRITICAL: '#dc3545',
    };
    return colors[priority] || '#6c757d';
  };

  const getStatusBadge = (status) => {
    const badges = {
      TODO: { text: 'To Do', class: 'status-todo' },
      IN_PROGRESS: { text: 'In Progress', class: 'status-in-progress' },
      DONE: { text: 'Done', class: 'status-done' },
    };
    return badges[status] || { text: status, class: '' };
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('ru-RU', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const statusBadge = getStatusBadge(task.status);

  return (
    <div className="task-card">
      <div className="task-card-header">
        <div className="task-title-container">
          {task.taskCode && (
            <span className="task-code">{task.taskCode}</span>
          )}
          <h3 className="task-title">{task.title}</h3>
        </div>
        <div className="task-actions">
          <button
            className="btn-icon btn-edit"
            onClick={() => onEdit(task)}
            title="Редактировать"
          >
            ✏️
          </button>
          <button
            className="btn-icon btn-delete"
            onClick={() => onDelete(task.id)}
            title="Удалить"
          >
            🗑️
          </button>
        </div>
      </div>

      <p className="task-description">{task.description || 'Нет описания'}</p>

      <div className="task-meta">
        <span className={`status-badge ${statusBadge.class}`}>
          {statusBadge.text}
        </span>
        <span
          className="priority-badge"
          style={{ backgroundColor: getPriorityColor(task.priority) }}
        >
          {task.priority}
        </span>
      </div>

      <div className="task-footer">
        <small className="task-date">
          📅 Создано: {formatDate(task.createdAt)}
        </small>
        {task.updatedAt !== task.createdAt && (
          <small className="task-date">
            🔄 Обновлено: {formatDate(task.updatedAt)}
          </small>
        )}
      </div>
    </div>
  );
};

export default TaskCard;
