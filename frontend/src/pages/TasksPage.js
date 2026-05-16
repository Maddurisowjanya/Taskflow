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

/* ---------------- Task Modal ---------------- */

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
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

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
          <button className="modal-close" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">

          <div className="form-group">
            <label className="form-label">Title *</label>
            <input
              type="text"
              name="title"
              className={`form-input ${errors.title ? 'input-error' : ''}`}
              value={formData.title}
              onChange={handleChange}
            />
            {errors.title && <span className="form-error">{errors.title}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              name="description"
              className="form-textarea"
              value={formData.description}
              onChange={handleChange}
              rows={3}
            />
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>

            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : isEditing ? 'Save Changes' : 'Create Task'}
            </button>
          </div>

        </form>
      </div>
    </div>
  );
};

/* ---------------- Task Card ---------------- */

const TaskCard = ({ task, onEdit, onDelete, onComplete }) => {
  const [menuOpen, setMenuOpen] = useState(false);

  const priorityConfig = {
    LOW: 'Low',
    MEDIUM: 'Medium',
    HIGH: 'High',
    URGENT: 'Urgent',
  };

  return (
    <div className="task-card">

      <h4>{task.title}</h4>

      <div className="task-actions">
        <button onClick={() => onEdit(task)}>Edit</button>
        <button onClick={() => onDelete(task.id)}>Delete</button>
        {task.status !== 'COMPLETED' && (
          <button onClick={() => onComplete(task.id)}>Complete</button>
        )}
      </div>

    </div>
  );
};

/* ---------------- Main Page ---------------- */

const TasksPage = () => {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filters, setFilters] = useState({
    status: '',
    priority: '',
    category: ''
  });

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

      const res = await tasksAPI.getAll(params);
      setTasks(res.data.data || []);
    } catch {
      toast.error('Failed to load tasks');
    } finally {
      setLoading(false);
    }
  }, [searchQuery, filters]);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchTasks();
    }, searchQuery ? 400 : 0);

    return () => clearTimeout(timer);
  }, [fetchTasks]);

  const handleDelete = async (id) => {
    await tasksAPI.delete(id);
    toast.success('Deleted');
    fetchTasks();
  };

  const handleComplete = async (id) => {
    await tasksAPI.complete(id);
    toast.success('Completed');
    fetchTasks();
  };

  const openCreate = () => {
    setEditingTask(null);
    setModalOpen(true);
  };

  const openEdit = (task) => {
    setEditingTask(task);
    setModalOpen(true);
  };

  return (
    <div>

      <h1>Tasks</h1>

      <button onClick={openCreate}>New Task</button>

      {loading ? (
        <p>Loading...</p>
      ) : (
        tasks.map(task => (
          <TaskCard
            key={task.id}
            task={task}
            onEdit={openEdit}
            onDelete={handleDelete}
            onComplete={handleComplete}
          />
        ))
      )}

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