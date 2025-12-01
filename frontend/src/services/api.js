const API_BASE_URL = 'http://localhost:8080/api';

// Helper function to handle API responses
const handleResponse = async (response) => {
  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || 'Something went wrong');
  }

  return data.data; // Spring Boot returns data in { success, message, data } format
};

// Task API
export const taskAPI = {
  // Get all tasks
  getAllTasks: async () => {
    const response = await fetch(`${API_BASE_URL}/tasks`);
    return handleResponse(response);
  },

  // Get task by ID
  getTaskById: async (id) => {
    const response = await fetch(`${API_BASE_URL}/tasks/${id}`);
    return handleResponse(response);
  },

  // Create new task
  createTask: async (taskData) => {
    const response = await fetch(`${API_BASE_URL}/tasks`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(taskData),
    });
    return handleResponse(response);
  },

  // Update task
  updateTask: async (id, taskData) => {
    const response = await fetch(`${API_BASE_URL}/tasks/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(taskData),
    });
    return handleResponse(response);
  },

  // Delete task
  deleteTask: async (id) => {
    const response = await fetch(`${API_BASE_URL}/tasks/${id}`, {
      method: 'DELETE',
    });
    return handleResponse(response);
  },

  // Get tasks by status
  getTasksByStatus: async (status) => {
    const response = await fetch(`${API_BASE_URL}/tasks/status/${status}`);
    return handleResponse(response);
  },

  // Get tasks by priority
  getTasksByPriority: async (priority) => {
    const response = await fetch(`${API_BASE_URL}/tasks/priority/${priority}`);
    return handleResponse(response);
  },

  // Search tasks
  searchTasks: async (keyword) => {
    const response = await fetch(`${API_BASE_URL}/tasks/search?keyword=${keyword}`);
    return handleResponse(response);
  },

  // Get task statistics
  getTotalCount: async () => {
    const response = await fetch(`${API_BASE_URL}/tasks/stats/count`);
    return handleResponse(response);
  },

  getCountByStatus: async (status) => {
    const response = await fetch(`${API_BASE_URL}/tasks/stats/count/${status}`);
    return handleResponse(response);
  },
};

// Task priorities
export const TASK_PRIORITIES = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
  CRITICAL: 'CRITICAL',
};

// Task statuses
export const TASK_STATUSES = {
  TODO: 'TODO',
  IN_PROGRESS: 'IN_PROGRESS',
  DONE: 'DONE',
};
