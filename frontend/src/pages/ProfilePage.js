// src/pages/ProfilePage.js
import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { usersAPI } from '../services/api';
import toast from 'react-hot-toast';
import {
  User, Mail, Lock, FileText, CheckCircle,
  Edit3, Save, X, Shield, Calendar, Award
} from 'lucide-react';
import { format } from 'date-fns';
import './ProfilePage.css';

const ProfilePage = () => {
  const {} = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editMode, setEditMode] = useState(false);
  const [passwordMode, setPasswordMode] = useState(false);
  const [saving, setSaving] = useState(false);

  const [profileForm, setProfileForm] = useState({
    firstName: '', lastName: '', bio: '', avatarUrl: ''
  });

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '', newPassword: '', confirmPassword: ''
  });

  const [passwordErrors, setPasswordErrors] = useState({});

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await usersAPI.getProfile();
      const data = res.data.data;
      setProfile(data);
      setProfileForm({
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        bio: data.bio || '',
        avatarUrl: data.avatarUrl || '',
      });
    } catch {
      toast.error('Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfileForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSaveProfile = async () => {
    setSaving(true);
    try {
      const res = await usersAPI.updateProfile(profileForm);
      setProfile(res.data.data);
      setEditMode(false);
      toast.success('Profile updated!');
    } catch {
      toast.error('Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const validatePassword = () => {
    const errs = {};
    if (!passwordForm.currentPassword) errs.currentPassword = 'Required';
    if (!passwordForm.newPassword) errs.newPassword = 'Required';
    else if (passwordForm.newPassword.length < 6) errs.newPassword = 'At least 6 characters';
    if (passwordForm.newPassword !== passwordForm.confirmPassword)
      errs.confirmPassword = 'Passwords do not match';
    return errs;
  };

  const handleChangePassword = async () => {
    const errs = validatePassword();
    if (Object.keys(errs).length > 0) { setPasswordErrors(errs); return; }
    setSaving(true);
    try {
      await usersAPI.changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordMode(false);
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      setPasswordErrors({});
      toast.success('Password changed successfully!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to change password');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return (
    <div className="loading-screen">
      <div className="loading-spinner" />
    </div>
  );

  const userInitials = profile?.firstName
    ? `${profile.firstName[0]}${profile.lastName?.[0] || ''}`.toUpperCase()
    : profile?.username?.[0].toUpperCase() || 'U';

  const completionRate = profile?.totalTasks > 0
    ? Math.round((profile.completedTasks / profile.totalTasks) * 100)
    : 0;

  return (
    <div className="profile-page">
      <h1 className="profile-heading">Profile</h1>

      <div className="profile-grid">
        {/* Left: Avatar + Stats */}
        <div className="profile-sidebar-col">
          {/* Avatar card */}
          <div className="card profile-avatar-card">
            <div className="profile-avatar-lg">{userInitials}</div>
            <h2 className="profile-fullname">
              {profile?.firstName && profile?.lastName
                ? `${profile.firstName} ${profile.lastName}`
                : profile?.username}
            </h2>
            <p className="profile-username">@{profile?.username}</p>
            {profile?.bio && <p className="profile-bio">{profile.bio}</p>}

            <div className="profile-roles">
              {profile?.roles?.map(role => (
                <span key={role} className="role-badge">
                  <Shield size={11} />
                  {role.replace('ROLE_', '')}
                </span>
              ))}
            </div>

            {profile?.createdAt && (
              <div className="profile-since">
                <Calendar size={12} />
                Member since {format(new Date(profile.createdAt), 'MMMM yyyy')}
              </div>
            )}
          </div>

          {/* Task Stats Card */}
          <div className="card stats-sidebar-card">
            <h3>Task Statistics</h3>
            <div className="stats-sidebar-list">
              <div className="stat-sidebar-row">
                <span>Total Tasks</span>
                <span className="stat-sidebar-val">{profile?.totalTasks || 0}</span>
              </div>
              <div className="stat-sidebar-row">
                <span>Completed</span>
                <span className="stat-sidebar-val success">{profile?.completedTasks || 0}</span>
              </div>
              <div className="stat-sidebar-row">
                <span>Pending</span>
                <span className="stat-sidebar-val warning">
                  {(profile?.totalTasks || 0) - (profile?.completedTasks || 0)}
                </span>
              </div>
            </div>
            {/* Completion rate bar */}
            <div className="completion-rate">
              <div className="completion-rate-header">
                <span>Completion Rate</span>
                <span className="completion-pct">{completionRate}%</span>
              </div>
              <div className="completion-bar">
                <div
                  className="completion-bar-fill"
                  style={{ width: `${completionRate}%` }}
                />
              </div>
              {completionRate >= 80 && (
                <div className="achievement">
                  <Award size={14} />
                  High Achiever!
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right: Edit forms */}
        <div className="profile-main-col">
          {/* Profile Info */}
          <div className="card profile-info-card">
            <div className="card-section-header">
              <h3><User size={16} /> Personal Information</h3>
              {!editMode ? (
                <button className="btn btn-secondary btn-sm" onClick={() => setEditMode(true)}>
                  <Edit3 size={14} /> Edit
                </button>
              ) : (
                <div className="flex gap-2">
                  <button className="btn btn-secondary btn-sm" onClick={() => setEditMode(false)}>
                    <X size={14} /> Cancel
                  </button>
                  <button className="btn btn-primary btn-sm" onClick={handleSaveProfile} disabled={saving}>
                    {saving ? <div className="btn-spinner" /> : <Save size={14} />}
                    Save
                  </button>
                </div>
              )}
            </div>

            {editMode ? (
              <div className="profile-form">
                <div className="form-row-2">
                  <div className="form-group">
                    <label className="form-label">First Name</label>
                    <input type="text" name="firstName" className="form-input"
                      value={profileForm.firstName} onChange={handleProfileChange}
                      placeholder="First name" />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Last Name</label>
                    <input type="text" name="lastName" className="form-input"
                      value={profileForm.lastName} onChange={handleProfileChange}
                      placeholder="Last name" />
                  </div>
                </div>
                <div className="form-group">
                  <label className="form-label">Bio</label>
                  <textarea name="bio" className="form-textarea"
                    value={profileForm.bio} onChange={handleProfileChange}
                    placeholder="Tell us about yourself..." rows={3} />
                </div>
                <div className="form-group">
                  <label className="form-label">Avatar URL</label>
                  <input type="text" name="avatarUrl" className="form-input"
                    value={profileForm.avatarUrl} onChange={handleProfileChange}
                    placeholder="https://example.com/avatar.jpg" />
                </div>
              </div>
            ) : (
              <div className="profile-info-display">
                <ProfileInfoRow icon={<User size={15} />} label="Full Name"
                  value={profile?.firstName
                    ? `${profile.firstName} ${profile.lastName || ''}`.trim()
                    : 'Not set'} />
                <ProfileInfoRow icon={<Mail size={15} />} label="Email" value={profile?.email} />
                <ProfileInfoRow icon={<FileText size={15} />} label="Bio"
                  value={profile?.bio || 'No bio added yet'} />
                <ProfileInfoRow icon={<CheckCircle size={15} />} label="Account Status"
                  value={profile?.isActive ? 'Active' : 'Inactive'}
                  highlight={profile?.isActive} />
              </div>
            )}
          </div>

          {/* Change Password */}
          <div className="card profile-info-card">
            <div className="card-section-header">
              <h3><Lock size={16} /> Security</h3>
              {!passwordMode ? (
                <button className="btn btn-secondary btn-sm" onClick={() => setPasswordMode(true)}>
                  Change Password
                </button>
              ) : (
                <button className="btn btn-secondary btn-sm"
                  onClick={() => { setPasswordMode(false); setPasswordErrors({}); }}>
                  <X size={14} /> Cancel
                </button>
              )}
            </div>

            {passwordMode ? (
              <div className="profile-form">
                <div className="form-group">
                  <label className="form-label">Current Password</label>
                  <input type="password" className={`form-input ${passwordErrors.currentPassword ? 'input-error' : ''}`}
                    placeholder="Your current password"
                    value={passwordForm.currentPassword}
                    onChange={e => setPasswordForm(p => ({ ...p, currentPassword: e.target.value }))} />
                  {passwordErrors.currentPassword && <span className="form-error">{passwordErrors.currentPassword}</span>}
                </div>
                <div className="form-row-2">
                  <div className="form-group">
                    <label className="form-label">New Password</label>
                    <input type="password" className={`form-input ${passwordErrors.newPassword ? 'input-error' : ''}`}
                      placeholder="New password"
                      value={passwordForm.newPassword}
                      onChange={e => setPasswordForm(p => ({ ...p, newPassword: e.target.value }))} />
                    {passwordErrors.newPassword && <span className="form-error">{passwordErrors.newPassword}</span>}
                  </div>
                  <div className="form-group">
                    <label className="form-label">Confirm Password</label>
                    <input type="password" className={`form-input ${passwordErrors.confirmPassword ? 'input-error' : ''}`}
                      placeholder="Confirm new password"
                      value={passwordForm.confirmPassword}
                      onChange={e => setPasswordForm(p => ({ ...p, confirmPassword: e.target.value }))} />
                    {passwordErrors.confirmPassword && <span className="form-error">{passwordErrors.confirmPassword}</span>}
                  </div>
                </div>
                <button className="btn btn-primary" onClick={handleChangePassword} disabled={saving}>
                  {saving ? <><div className="btn-spinner" />Saving...</> : <><Lock size={15} />Update Password</>}
                </button>
              </div>
            ) : (
              <p style={{ fontSize: 14, color: 'var(--text-muted)' }}>
                Keep your account secure by using a strong, unique password.
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

const ProfileInfoRow = ({ icon, label, value, highlight }) => (
  <div className="profile-info-row">
    <div className="profile-info-icon">{icon}</div>
    <div className="profile-info-content">
      <span className="profile-info-label">{label}</span>
      <span className={`profile-info-value ${highlight ? 'text-success' : ''}`}>{value}</span>
    </div>
  </div>
);

export default ProfilePage;
