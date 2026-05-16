// src/pages/TasksPage.js
import React, { useState, useEffect, useCallback } from 'react';
import { tasksAPI } from '../services/api';
import toast from 'react-hot-toast';
import {
  Plus, Search, Filter, CheckCircle, Trash2, Edit3,
  Calendar, Flag, Tag, ChevronDown, X, MoreVertical
} from 'lucide-react';
import { format } from 'date-fns';
import './TasksPage.css';

// ==========================================
// Task Modal Component
// ==========================================
const TaskModal = ({ task, onClose, onSave }) => {
  const isEditing = !!task?.id;
  const [formData, setFormData] = useState({
    title: task?.title || '',
    description: task?.description || '',
    status: task?.status || 'TODO',
    priority: task?.priority || 'MEDIUM',
    category: task?.category || '',
    dueDate: task?.dueDate || '',
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!formData.title.trim()) e.title = 'Title is required';
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }
    setLoading(true);
    try {
      if (isEditing) {
        await tasksAPI.update(task.id, formData);
        toast.success('Task updated!');
      } else {
        await tasksAPI.create(formData);
        toast.success('Task created!');
      }
      onSave();
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save task');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{isEditing ? 'Edit Task' : 'Create New Task'}</h2>
          <button className="modal-close" onClick={onClose}><X size={18} /></button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {/* Title */}
          <div className="form-group">
            <label className="form-label">Title *</label>
            <input
              type="text" name="title"
              className={`form-input ${errors.title ? 'input-error' : ''}`}
              placeholder="What needs to be done?"
              value={formData.title}
              onChange={handleChange}
              autoFocus
            />
            {errors.title && <span className="form-error">{errors.title}</span>}
          </div>

          {/* Description */}
          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              name="description"
              className="form-textarea"
              placeholder="Add more details..."
              value={formData.description}
              onChange={handleChange}
              rows={3}
            />
          </div>

          {/* Row: Status + Priority */}
          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label">Status</label>
              <select name="status" className="form-select" value={formData.status} onChange={handleChange}>
                <option value="TODO">To Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="COMPLETED">Completed</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Priority</label>
              <select name="priority" className="form-select" value={formData.priority} onChange={handleChange}>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </select>
            </div>
          </div>

          {/* Row: Category + Due Date */}
          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label">Category</label>
              <input
                type="text" name="category"
                className="form-input"
                placeholder="e.g. Work, Personal..."
                value={formData.category}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Due Date</label>
              <input
                type="date" name="dueDate"
                className="form-input"
                value={formData.dueDate}
                onChange={handleChange}
                min={new Date().toISOString().split('T')[0]}
              />
            </div>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <><div className="btn-spinner" />{isEditing ? 'Saving...' : 'Creating...'}</> :
                isEditing ? 'Save Changes' : 'Create Task'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// ==========================================
// Task Card Component
// ==========================================
const TaskCard = ({ task, onEdit, onDelete, onComplete }) => {
  const [menuOpen, setMenuOpen] = useState(false);

  const priorityConfig = {
    LOW: { label: 'Low', color: 'var(--priority-low)', cls: 'badge-low' },
    MEDIUM: { label: 'Medium', color: 'var(--priority-medium)', cls: 'badge-medium' },
    HIGH: { label: 'High', color: 'var(--priority-high)', cls: 'badge-high' },
    URGENT: { label: 'Urgent', color: 'var(--priority-urgent)', cls: 'badge-urgent' },
  };

  const statusConfig = {
    TODO: { label: 'To Do', cls: 'badge-todo' },
    IN_PROGRESS: { label: 'In Progress', cls: 'badge-inprogress' },
    COMPLETED: { label: 'Completed', cls: 'badge-completed' },
    CANCELLED: { label: 'Cancelled', cls: 'badge-cancelled' },
  };

  const p = priorityConfig[task.priority] || priorityConfig.MEDIUM;
  const s = statusConfig[task.status] || statusConfig.TODO;
  const isCompleted = task.status === 'COMPLETED';

  return (
    <div className={`task-card ${isCompleted ? 'task-card-completed' : ''} ${task.isOverdue ? 'task-card-overdue' : ''}`}>
      {/* Priority indicator bar */}
      <div className="task-priority-bar" style={{ background: p.color }} />

      <div className="task-card-body">
        {/* Header row */}
        <div className="task-card-header">
          <div className="task-badges">
            <span className={`badge ${p.cls}`}>
              <Flag size={10} />
              {p.label}
            </span>
            <span className={`badge ${s.cls}`}>{s.label}</span>
            {task.isOverdue && <span className="badge badge-overdue">Overdue</span>}
          </div>
          {/* Actions menu */}
          <div className="task-menu-wrapper">
            <button
              className="task-menu-btn"
              onClick={() => setMenuOpen(!menuOpen)}
            >
              <MoreVertical size={16} />
            </button>
            {menuOpen && (
              <div className="task-menu" onClick={() => setMenuOpen(false)}>
                {!isCompleted && (
                  <button className="task-menu-item success" onClick={() => onComplete(task.id)}>
                    <CheckCircle size={14} /> Mark Complete
                  </button>
                )}
                <button className="task-menu-item" onClick={() => onEdit(task)}>
                  <Edit3 size={14} /> Edit
                </button>
                <button className="task-menu-item danger" onClick={() => onDelete(task.id)}>
                  <Trash2 size={14} /> Delete
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Title */}
        <h4 className={`task-title ${isCompleted ? 'task-title-done' : ''}`}>{task.title}</h4>

        {/* Description */}
        {task.description && (
          <p className="task-description">{task.description}</p>
        )}

        {/* Footer */}
        <div className="task-card-footer">
          {task.category && (
            <span className="task-meta-item">
              <Tag size={12} />
              {task.category}
            </span>
          )}
          {task.dueDate && (
            <span className={`task-meta-item ${task.isOverdue ? 'task-meta-overdue' : ''}`}>
              <Calendar size={12} />
              {format(new Date(task.dueDate), 'MMM d, yyyy')}
            </span>
          )}
          {isCompleted && task.completedAt && (
            <span className="task-meta-item task-meta-success">
              <CheckCircle size={12} />
              Done {format(new Date(task.completedAt), 'MMM d')}
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

// ==========================================
// Main TasksPage Component
// ==========================================
const TasksPage = () => {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filters, setFilters] = useState({ status: '', priority: '', category: '' });
  const [showFilters, setShowFilters] = useState(false);

  const fetchTasks = useCallback(async () => {
    try {
      setLoading(true);
      const params = {};
      if (searchQuery) params.search = searchQuery;
      else {
        if (filters.status) params.status = filters.status;
        if (filters.priority) params.priority = filters.priority;
        if (filters.category) params.category = filters.category;
      }
      const response = await tasksAPI.getAll(params);
      setTasks(response.data.data || []);
    } catch (err) {
      toast.error('Failed to load tasks');
    } finally {
      setLoading(false);
    }
  }, [searchQuery, filters]);

  useEffect(() => {
    const timer = setTimeout(fetchTasks, searchQuery ? 400 : 0);
    return () => clearTimeout(timer);
  }, [fetchTasks]);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this task? This cannot be undone.')) return;
    try {
      await tasksAPI.delete(id);
      toast.success('Task deleted');
      fetchTasks();
    } catch {
      toast.error('Failed to delete task');
    }
  };

  const handleComplete = async (id) => {
    try {
      await tasksAPI.complete(id);
      toast.success('Task completed! 🎉');
      fetchTasks();
    } catch {
      toast.error('Failed to complete task');
    }
  };

  const openCreate = () => { setEditingTask(null); setModalOpen(true); };
  const openEdit = (task) => { setEditingTask(task); setModalOpen(true); };

  const clearFilters = () => {
    setFilters({ status: '', priority: '', category: '' });
    setSearchQuery('');
  };

  const hasActiveFilters = filters.status || filters.priority || filters.category || searchQuery;

  // Group tasks by status for Kanban-like display
  const tasksByStatus = {
    TODO: tasks.filter(t => t.status === 'TODO'),
    IN_PROGRESS: tasks.filter(t => t.status === 'IN_PROGRESS'),
    COMPLETED: tasks.filter(t => t.status === 'COMPLETED'),
    CANCELLED: tasks.filter(t => t.status === 'CANCELLED'),
  };

  return (
    <div className="tasks-page">
      {/* Header */}
      <div className="tasks-header">
        <div>
          <h1>My Tasks</h1>
          <p className="tasks-subtitle">
            {tasks.length} {tasks.length === 1 ? 'task' : 'tasks'} total
          </p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>
          <Plus size={18} /> New Task
        </button>
      </div>

      {/* Search + Filter Bar */}
      <div className="tasks-toolbar">
        <div className="search-box">
          <Search size={16} className="search-icon" />
          <input
            type="text"
            placeholder="Search tasks..."
            className="search-input"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
          />
          {searchQuery && (
            <button className="search-clear" onClick={() => setSearchQuery('')}>
              <X size={14} />
            </button>
          )}
        </div>

        <button
          className={`btn btn-secondary filter-toggle ${showFilters ? 'active' : ''}`}
          onClick={() => setShowFilters(!showFilters)}
        >
          <Filter size={16} />
          Filters
          {hasActiveFilters && <span className="filter-dot" />}
          <ChevronDown size={14} className={showFilters ? 'rotated' : ''} />
        </button>

        {hasActiveFilters && (
          <button className="btn btn-secondary" onClick={clearFilters}>
            <X size={14} /> Clear
          </button>
        )}
      </div>

      {/* Filter Panel */}
      {showFilters && (
        <div className="filter-panel">
          <div className="filter-group">
            <label className="form-label">Status</label>
            <select
              className="form-select filter-select"
              value={filters.status}
              onChange={e => setFilters(prev => ({ ...prev, status: e.target.value }))}
            >
              <option value="">All Statuses</option>
              <option value="TODO">To Do</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
          <div className="filter-group">
            <label className="form-label">Priority</label>
            <select
              className="form-select filter-select"
              value={filters.priority}
              onChange={e => setFilters(prev => ({ ...prev, priority: e.target.value }))}
            >
              <option value="">All Priorities</option>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>
          <div className="filter-group">
            <label className="form-label">Category</label>
            <input
              type="text"
              className="form-input filter-select"
              placeholder="e.g. Work"
              value={filters.category}
              onChange={e => setFilters(prev => ({ ...prev, category: e.target.value }))}
            />
          </div>
        </div>
      )}

      {/* Task Grid */}
      {loading ? (
        <div className="tasks-loading">
          <div className="loading-spinner" />
          <p>Loading tasks...</p>
        </div>
      ) : tasks.length === 0 ? (
        <div className="tasks-empty">
          <div className="tasks-empty-icon">📋</div>
          <h3>{hasActiveFilters ? 'No tasks match your filters' : 'No tasks yet'}</h3>
          <p>{hasActiveFilters ? 'Try adjusting your search or filters.' : 'Create your first task to get started!'}</p>
          {!hasActiveFilters && (
            <button className="btn btn-primary" onClick={openCreate}>
              <Plus size={16} /> Create Task
            </button>
          )}
        </div>
      ) : (
        <div className="tasks-grid">
          {tasks.map(task => (
            <TaskCard
              key={task.id}
              task={task}
              onEdit={openEdit}
              onDelete={handleDelete}
              onComplete={handleComplete}
            />
          ))}
        </div>
      )}

      {/* Task Create/Edit Modal */}
      {modalOpen && (
        <TaskModal
          task={editingTask}
          onClose={() => setModalOpen(false)}
          onSave={fetchTasks}
        />
      )}
    </div>
  );
};

export default TasksPage;
