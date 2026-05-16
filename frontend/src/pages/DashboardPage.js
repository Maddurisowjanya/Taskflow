// src/pages/DashboardPage.js
import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { tasksAPI } from '../services/api';
import {
  CheckSquare, Clock, TrendingUp, AlertTriangle,
  Zap, Calendar, ArrowRight, BarChart2
} from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';
import { format } from 'date-fns';
import { Link } from 'react-router-dom';
import './DashboardPage.css';

/** Color palette for charts */
const STATUS_COLORS = {
  TODO: '#9198b5',
  IN_PROGRESS: '#6c8eff',
  COMPLETED: '#34d399',
  CANCELLED: '#5c6386',
};

const PRIORITY_COLORS = {
  LOW: '#34d399',
  MEDIUM: '#fbbf24',
  HIGH: '#fb923c',
  URGENT: '#f87171',
};

/**
 * StatCard - Individual stat number card at the top of the dashboard.
 */
const StatCard = ({ icon, label, value, color, subtitle }) => (
  <div className="stat-card">
    <div className="stat-icon" style={{ color, background: `${color}15` }}>
      {icon}
    </div>
    <div className="stat-body">
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
      {subtitle && <div className="stat-subtitle">{subtitle}</div>}
    </div>
  </div>
);

/**
 * TaskRow - A compact task row for the recent tasks list.
 */
const TaskRow = ({ task }) => {
  const statusClass = {
    TODO: 'badge-todo',
    IN_PROGRESS: 'badge-inprogress',
    COMPLETED: 'badge-completed',
    CANCELLED: 'badge-cancelled',
  }[task.status] || 'badge-todo';

  const priorityClass = {
    LOW: 'badge-low',
    MEDIUM: 'badge-medium',
    HIGH: 'badge-high',
    URGENT: 'badge-urgent',
  }[task.priority] || 'badge-medium';

  return (
    <div className="task-row">
      <div className="task-row-main">
        <span className={`badge ${priorityClass}`}>{task.priority}</span>
        <span className="task-row-title">{task.title}</span>
        {task.isOverdue && <span className="overdue-dot" title="Overdue" />}
      </div>
      <div className="task-row-meta">
        {task.dueDate && (
          <span className="task-row-date">
            <Calendar size={12} />
            {format(new Date(task.dueDate), 'MMM d')}
          </span>
        )}
        <span className={`badge ${statusClass}`}>
          {task.status.replace('_', ' ')}
        </span>
      </div>
    </div>
  );
};

/**
 * DashboardPage - Main dashboard with statistics and charts.
 */
const DashboardPage = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  const fetchDashboardStats = async () => {
    try {
      const response = await tasksAPI.getDashboard();
      setStats(response.data.data);
    } catch (error) {
      console.error('Failed to load dashboard stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner" />
        <p>Loading your dashboard...</p>
      </div>
    );
  }

  // Prepare chart data from API response
  const statusChartData = stats?.tasksByStatus
    ? Object.entries(stats.tasksByStatus).map(([name, value]) => ({
        name: name.replace('_', ' '), value, fill: STATUS_COLORS[name]
      }))
    : [];

  const priorityChartData = stats?.tasksByPriority
    ? Object.entries(stats.tasksByPriority).map(([name, value]) => ({
        name, value, fill: PRIORITY_COLORS[name]
      }))
    : [];

  const greeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  };

  return (
    <div className="dashboard">
      {/* Page Header */}
      <div className="dashboard-header">
        <div>
          <h1 className="dashboard-greeting">
            {greeting()}, {user?.firstName || user?.username} 👋
          </h1>
          <p className="dashboard-subtitle">
            Here's what's happening with your tasks today
          </p>
        </div>
        <Link to="/tasks" className="btn btn-primary">
          <Zap size={16} />
          New Task
        </Link>
      </div>

      {/* Stat Cards Row */}
      <div className="stats-grid">
        <StatCard
          icon={<BarChart2 size={22} />}
          label="Total Tasks"
          value={stats?.totalTasks || 0}
          color="var(--accent-primary)"
          subtitle={`${stats?.completionRate || 0}% completion rate`}
        />
        <StatCard
          icon={<Clock size={22} />}
          label="In Progress"
          value={stats?.inProgressTasks || 0}
          color="var(--accent-info)"
        />
        <StatCard
          icon={<CheckSquare size={22} />}
          label="Completed"
          value={stats?.completedTasks || 0}
          color="var(--accent-success)"
        />
        <StatCard
          icon={<AlertTriangle size={22} />}
          label="Overdue"
          value={stats?.overdueTasks || 0}
          color="var(--accent-danger)"
        />
      </div>

      {/* Charts Row */}
      <div className="charts-grid">
        {/* Tasks by Status — Bar Chart */}
        <div className="card chart-card">
          <div className="chart-header">
            <h3>Tasks by Status</h3>
            <span className="chart-badge">Overview</span>
          </div>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={statusChartData} barSize={32}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
              <XAxis
                dataKey="name"
                tick={{ fill: 'var(--text-muted)', fontSize: 11 }}
                axisLine={false}
                tickLine={false}
              />
              <YAxis
                tick={{ fill: 'var(--text-muted)', fontSize: 11 }}
                axisLine={false}
                tickLine={false}
                allowDecimals={false}
              />
              <Tooltip
                contentStyle={{
                  background: 'var(--bg-elevated)',
                  border: '1px solid var(--border-medium)',
                  borderRadius: '8px',
                  color: 'var(--text-primary)',
                  fontSize: '13px',
                }}
                cursor={{ fill: 'rgba(255,255,255,0.03)' }}
              />
              <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                {statusChartData.map((entry, index) => (
                  <Cell key={index} fill={entry.fill} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Tasks by Priority — Pie Chart */}
        <div className="card chart-card">
          <div className="chart-header">
            <h3>Priority Distribution</h3>
            <span className="chart-badge">Breakdown</span>
          </div>
          {priorityChartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie
                  data={priorityChartData}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {priorityChartData.map((entry, index) => (
                    <Cell key={index} fill={entry.fill} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    background: 'var(--bg-elevated)',
                    border: '1px solid var(--border-medium)',
                    borderRadius: '8px',
                    color: 'var(--text-primary)',
                    fontSize: '13px',
                  }}
                />
                <Legend
                  formatter={(value) => (
                    <span style={{ color: 'var(--text-secondary)', fontSize: '12px' }}>
                      {value}
                    </span>
                  )}
                />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-chart">
              <TrendingUp size={32} />
              <p>No tasks yet. Create one to see your priority distribution.</p>
            </div>
          )}
        </div>
      </div>

      {/* Bottom Row: Recent Tasks + Upcoming */}
      <div className="bottom-grid">
        {/* Recent Tasks */}
        <div className="card">
          <div className="section-header">
            <h3>Recent Tasks</h3>
            <Link to="/tasks" className="section-link">
              View all <ArrowRight size={14} />
            </Link>
          </div>
          {stats?.recentTasks?.length > 0 ? (
            <div className="tasks-list">
              {stats.recentTasks.map(task => (
                <TaskRow key={task.id} task={task} />
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <CheckSquare size={32} />
              <p>No tasks yet. <Link to="/tasks">Create your first task →</Link></p>
            </div>
          )}
        </div>

        {/* Upcoming Due Tasks */}
        <div className="card">
          <div className="section-header">
            <h3>Due This Week</h3>
            <span className="section-badge">{stats?.upcomingDueTasks?.length || 0}</span>
          </div>
          {stats?.upcomingDueTasks?.length > 0 ? (
            <div className="tasks-list">
              {stats.upcomingDueTasks.map(task => (
                <TaskRow key={task.id} task={task} />
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <Calendar size={32} />
              <p>No tasks due this week. You're ahead of schedule! 🎉</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
