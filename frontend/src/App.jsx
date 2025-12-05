import React, { useState, useEffect } from 'react';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { taskAPI, TASK_PRIORITIES, TASK_STATUSES } from './services/api';
import TaskCard from './components/TaskCard';
import TaskForm from './components/TaskForm';
import './App.css';

const App = () => {
  const [tasks, setTasks] = useState([]);
  const [filteredTasks, setFilteredTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [filterPriority, setFilterPriority] = useState('ALL');
  const [stats, setStats] = useState({
    total: 0,
    todo: 0,
    inProgress: 0,
    done: 0,
  });

  // Load tasks on component mount
  useEffect(() => {
    loadTasks();
  }, []);

  // Apply filters whenever tasks or filters change
  useEffect(() => {
    applyFilters();
  }, [tasks, searchQuery, filterStatus, filterPriority]);

  const loadTasks = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await taskAPI.getAllTasks();
      setTasks(data);
      calculateStats(data);
    } catch (err) {
      const errorMsg = 'Не удалось загрузить задачи: ' + err.message;
      setError(errorMsg);
      toast.error(errorMsg);
      console.error('Error loading tasks:', err);
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = (taskList) => {
    const newStats = {
      total: taskList.length,
      todo: taskList.filter((t) => t.status === 'TODO').length,
      inProgress: taskList.filter((t) => t.status === 'IN_PROGRESS').length,
      done: taskList.filter((t) => t.status === 'DONE').length,
    };
    setStats(newStats);
  };

  const applyFilters = () => {
    let filtered = [...tasks];

    // Search filter
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (task) =>
          task.title.toLowerCase().includes(query) ||
          (task.description && task.description.toLowerCase().includes(query))
      );
    }

    // Status filter
    if (filterStatus !== 'ALL') {
      filtered = filtered.filter((task) => task.status === filterStatus);
    }

    // Priority filter
    if (filterPriority !== 'ALL') {
      filtered = filtered.filter((task) => task.priority === filterPriority);
    }

    setFilteredTasks(filtered);
  };

  const handleCreateTask = async (taskData) => {
    try {
      const newTask = await taskAPI.createTask(taskData);
      setTasks([...tasks, newTask]);
      setShowForm(false);
      toast.success('✅ Задача успешно создана!');
    } catch (err) {
      toast.error('❌ Ошибка при создании задачи: ' + err.message);
      console.error('Error creating task:', err);
    }
  };

  const handleUpdateTask = async (taskData) => {
    try {
      const updatedTask = await taskAPI.updateTask(editingTask.id, taskData);
      setTasks(tasks.map((t) => (t.id === editingTask.id ? updatedTask : t)));
      setShowForm(false);
      setEditingTask(null);
      toast.success('✏️ Задача успешно обновлена!');
    } catch (err) {
      toast.error('❌ Ошибка при обновлении задачи: ' + err.message);
      console.error('Error updating task:', err);
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm('Вы уверены, что хотите удалить эту задачу?')) {
      return;
    }

    try {
      await taskAPI.deleteTask(taskId);
      setTasks(tasks.filter((t) => t.id !== taskId));
      toast.success('🗑️ Задача успешно удалена!');
    } catch (err) {
      toast.error('❌ Ошибка при удалении задачи: ' + err.message);
      console.error('Error deleting task:', err);
    }
  };

  const handleEditTask = (task) => {
    setEditingTask(task);
    setShowForm(true);
  };

  const handleCancelForm = () => {
    setShowForm(false);
    setEditingTask(null);
  };

  const handleSubmitForm = (taskData) => {
    if (editingTask) {
      handleUpdateTask(taskData);
    } else {
      handleCreateTask(taskData);
    }
  };

  const clearFilters = () => {
    setSearchQuery('');
    setFilterStatus('ALL');
    setFilterPriority('ALL');
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>📋 Task Management System</h1>
        <p className="subtitle">Управление задачами для вашего проекта</p>
      </header>

      {/* Statistics Dashboard */}
      <div className="stats-container">
        <div className="stat-card stat-total">
          <div className="stat-value">{stats.total}</div>
          <div className="stat-label">Всего задач</div>
        </div>
        <div className="stat-card stat-todo">
          <div className="stat-value">{stats.todo}</div>
          <div className="stat-label">To Do</div>
        </div>
        <div className="stat-card stat-progress">
          <div className="stat-value">{stats.inProgress}</div>
          <div className="stat-label">In Progress</div>
        </div>
        <div className="stat-card stat-done">
          <div className="stat-value">{stats.done}</div>
          <div className="stat-label">Done</div>
        </div>
      </div>

      {/* Filters and Actions */}
      <div className="controls-container">
        <div className="search-container">
          <input
            type="text"
            placeholder="🔍 Поиск задач..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="filters-container">
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="filter-select"
          >
            <option value="ALL">Все статусы</option>
            {Object.entries(TASK_STATUSES).map(([key, value]) => (
              <option key={key} value={value}>
                {value === 'TODO'
                  ? 'To Do'
                  : value === 'IN_PROGRESS'
                  ? 'In Progress'
                  : 'Done'}
              </option>
            ))}
          </select>

          <select
            value={filterPriority}
            onChange={(e) => setFilterPriority(e.target.value)}
            className="filter-select"
          >
            <option value="ALL">Все приоритеты</option>
            {Object.values(TASK_PRIORITIES).map((priority) => (
              <option key={priority} value={priority}>
                {priority}
              </option>
            ))}
          </select>

          {(searchQuery || filterStatus !== 'ALL' || filterPriority !== 'ALL') && (
            <button onClick={clearFilters} className="btn-clear-filters">
              Сбросить фильтры
            </button>
          )}
        </div>

        <button onClick={() => setShowForm(true)} className="btn-create">
          + Создать задачу
        </button>
      </div>

      {/* Tasks Grid */}
      <main className="main-content">
        {loading && <div className="loading">Загрузка задач...</div>}

        {error && (
          <div className="error-message">
            <p>{error}</p>
            <button onClick={loadTasks} className="btn-retry">
              Попробовать снова
            </button>
          </div>
        )}

        {!loading && !error && filteredTasks.length === 0 && (
          <div className="empty-state">
            <p>
              {tasks.length === 0
                ? '📝 Нет задач. Создайте первую задачу!'
                : '🔍 Задачи не найдены. Попробуйте изменить фильтры.'}
            </p>
          </div>
        )}

        {!loading && !error && filteredTasks.length > 0 && (
          <div className="tasks-grid">
            {filteredTasks.map((task) => (
              <TaskCard
                key={task.id}
                task={task}
                onEdit={handleEditTask}
                onDelete={handleDeleteTask}
              />
            ))}
          </div>
        )}
      </main>

      {/* Task Form Modal */}
      {showForm && (
        <TaskForm
          task={editingTask}
          onSubmit={handleSubmitForm}
          onCancel={handleCancelForm}
        />
      )}

      {/* Toast Notifications */}
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={true}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="light"
      />
    </div>
  );
};

export default App;
