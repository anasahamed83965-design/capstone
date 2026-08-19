import { useEffect, useState } from 'react';
import client from '../api/client';
import { useAuth } from '../context/AuthContext';

function greeting() {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 17) return 'Good afternoon';
  return 'Good evening';
}

function formatSlot(value) {
  return new Date(value).toLocaleString(undefined, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

export default function BabysitterDashboard() {
  const { user } = useAuth();
  const [profile, setProfile] = useState({ bio: '', experienceYears: 0, hourlyRate: '' });
  const [slots, setSlots] = useState([]);
  const [slotForm, setSlotForm] = useState({ startTime: '', endTime: '' });
  const [profileMsg, setProfileMsg] = useState('');
  const [slotMsg, setSlotMsg] = useState('');
  const [error, setError] = useState('');
  const [status, setStatus] = useState('');

  const load = async () => {
    try {
      const [p, s] = await Promise.all([
        client.get('/babysitters/me'),
        client.get('/babysitters/me/slots'),
      ]);
      setProfile({ bio: p.data.data.bio || '', experienceYears: p.data.data.experienceYears, hourlyRate: p.data.data.hourlyRate });
      setSlots(s.data.data);
      setStatus(p.data.data.verified ? 'verified' : 'pending');
    } catch (err) {
      setError('Could not load your dashboard.');
    }
  };

  useEffect(() => {
    load();
  }, []);

  const saveProfile = async (e) => {
    e.preventDefault();
    setProfileMsg('');
    setError('');
    try {
      await client.put('/babysitters/me', profile);
      setProfileMsg('Profile saved.');
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save the profile.');
    }
  };

  const addSlot = async (e) => {
    e.preventDefault();
    setSlotMsg('');
    setError('');
    try {
      await client.post('/babysitters/me/slots', slotForm);
      setSlotMsg('Slot added.');
      setSlotForm({ startTime: '', endTime: '' });
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add the slot.');
    }
  };

  const removeSlot = async (slotId) => {
    setError('');
    try {
      await client.delete(`/babysitters/me/slots/${slotId}`);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not delete the slot.');
    }
  };

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfile({ ...profile, [name]: name === 'experienceYears' ? Number(value) : value });
  };

  const slotStatus = status === 'verified'
    ? <span className="badge bg-success">Verified</span>
    : <span className="badge bg-warning text-dark">Pending approval</span>;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-1">
        <h3 className="mb-0">
          {greeting()}{user?.fullName ? `, ${user.fullName.split(' ')[0]}` : ''}!
        </h3>
        {slotStatus}
      </div>
      <p className="text-muted small mb-3">Your dashboard — profile on the left, availability on the right.</p>

      {status === 'pending' && (
        <div className="alert alert-info">
          Your profile is waiting for admin approval. Fill in your bio, experience and rate below,
          add a few slots, and you'll appear in parent searches once approved.
        </div>
      )}

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row">
        <div className="col-lg-6">
          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title mb-3">My Profile</h5>
              <form onSubmit={saveProfile}>
                <div className="mb-3">
                  <label className="form-label">Bio</label>
                  <textarea
                    className="form-control"
                    rows="4"
                    name="bio"
                    value={profile.bio}
                    onChange={handleProfileChange}
                    placeholder="Tell parents a little about yourself..."
                  />
                </div>
                <div className="row g-2">
                  <div className="col">
                    <label className="form-label">Years of experience</label>
                    <input
                      type="number"
                      className="form-control"
                      min="0"
                      name="experienceYears"
                      value={profile.experienceYears}
                      onChange={handleProfileChange}
                    />
                  </div>
                  <div className="col">
                    <label className="form-label">Hourly rate ($)</label>
                    <input
                      type="number"
                      className="form-control"
                      min="0"
                      step="0.50"
                      name="hourlyRate"
                      value={profile.hourlyRate}
                      onChange={handleProfileChange}
                      required
                    />
                  </div>
                </div>
                <button className="btn btn-primary mt-3" type="submit">
                  Save profile
                </button>
                {profileMsg && <div className="alert alert-success small mt-2 mb-0">{profileMsg}</div>}
              </form>
            </div>
          </div>
        </div>

        <div className="col-lg-6">
          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title mb-3">Add a Slot</h5>
              <form onSubmit={addSlot}>
                <div className="mb-3">
                  <label className="form-label">Start time</label>
                  <input
                    type="datetime-local"
                    className="form-control"
                    value={slotForm.startTime}
                    onChange={(e) => setSlotForm({ ...slotForm, startTime: e.target.value })}
                    required
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">End time</label>
                  <input
                    type="datetime-local"
                    className="form-control"
                    value={slotForm.endTime}
                    onChange={(e) => setSlotForm({ ...slotForm, endTime: e.target.value })}
                    required
                  />
                </div>
                <button className="btn btn-primary" type="submit">
                  Add slot
                </button>
                {slotMsg && <div className="alert alert-success small mt-2 mb-0">{slotMsg}</div>}
              </form>
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-baseline mb-3">
            <h5 className="card-title mb-0">My Slots</h5>
            {slots.length > 0 && (
              <span className="small text-muted">
                {slots.filter((s) => !s.booked).length} open · {slots.filter((s) => s.booked).length} booked
              </span>
            )}
          </div>
          {slots.length === 0 && <p className="text-muted mb-0">No slots yet. Add your first one above.</p>}
          <ul className="list-group">
            {slots.map((s) => (
              <li
                key={s.id}
                className="list-group-item d-flex justify-content-between align-items-center"
              >
                <div>
                  <div>
                    {formatSlot(s.startTime)} - {formatSlot(s.endTime)}
                  </div>
                  <span className={`badge ${s.booked ? 'bg-danger' : 'bg-success'}`}>
                    {s.booked ? 'Booked' : 'Open'}
                  </span>
                </div>
                {!s.booked && (
                  <button className="btn btn-sm btn-outline-danger" onClick={() => removeSlot(s.id)}>
                    Delete
                  </button>
                )}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}
