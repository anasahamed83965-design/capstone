import { useEffect, useState } from 'react';
import client from '../api/client';

const STATUS_BADGE = {
  PENDING: 'bg-warning text-dark',
  CONFIRMED: 'bg-info text-dark',
  COMPLETED: 'bg-success',
  CANCELLED: 'bg-secondary',
};

function formatDate(value) {
  const d = new Date(value);
  return d.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

function formatDay(value) {
  const d = new Date(value);
  return d.toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

export default function AdminDashboard() {
  const [stats, setStats] = useState({ totalUsers: 0, pendingBabysitters: 0, totalBookings: 0 });
  const [pending, setPending] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [users, setUsers] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const [statsRes, pendingRes, bookingsRes, usersRes] = await Promise.all([
        client.get('/admin/stats'),
        client.get('/admin/pending-babysitters'),
        client.get('/admin/bookings'),
        client.get('/admin/users'),
      ]);
      setStats(statsRes.data.data);
      setPending(pendingRes.data.data);
      setBookings(bookingsRes.data.data);
      setUsers(usersRes.data.data);
    } catch (err) {
      setError('Could not load the admin panel.');
    }
  };

  useEffect(() => {
    load();
  }, []);

  const verify = async (id, name, approved) => {
    setMessage('');
    setError('');
    try {
      await client.post(`/admin/babysitters/${id}/verify`, { approved });
      setMessage(approved
        ? `${name} is now visible to parents.`
        : `${name} was rejected and stays hidden.`);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not update the babysitter.');
    }
  };

  const today = new Date().toLocaleDateString(undefined, {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
  });

  const removeUser = async (id, name) => {
    if (!window.confirm(`Delete ${name}? Their profile, slots and notifications go too. This cannot be undone.`)) {
      return;
    }
    setMessage('');
    setError('');
    try {
      await client.delete(`/admin/users/${id}`);
      setMessage(`${name} has been deleted.`);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not delete the user.');
    }
  };

  const parents = users.filter((u) => u.role === 'PARENT');
  const sitters = users.filter((u) => u.role === 'BABYSITTER');

  return (
    <div>
      <h3 className="mb-0">Admin Panel</h3>
      <p className="text-muted small mb-3">{today} — here's what's happening on the platform.</p>
      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row g-3 mb-4">
        <div className="col-md-4">
          <div className="card text-center">
            <div className="card-body">
              <div className="display-6">{stats.totalUsers}</div>
              <div className="text-muted">Registered users</div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card text-center">
            <div className="card-body">
              <div className="display-6">{stats.pendingBabysitters}</div>
              <div className="text-muted">Pending approvals</div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card text-center">
            <div className="card-body">
              <div className="display-6">{stats.totalBookings}</div>
              <div className="text-muted">Total bookings</div>
            </div>
          </div>
        </div>
      </div>

      <div className="card mb-4">
        <div className="card-body">
          <h5 className="card-title mb-3">
            Pending Babysitters
            {pending.length > 0 && <span className="text-muted fw-normal"> ({pending.length} waiting)</span>}
          </h5>
          {pending.length === 0 && <p className="text-muted mb-0">All caught up — nothing waiting for approval.</p>}
          <ul className="list-group">
            {pending.map((b) => (
              <li key={b.id} className="list-group-item d-flex justify-content-between align-items-center">
                <div>
                  <div className="fw-semibold">{b.fullName}</div>
                  <div className="text-muted small">
                    {b.experienceYears} yrs experience &middot; ${b.hourlyRate}/hr
                  </div>
                  {b.bio && <div className="small">{b.bio}</div>}
                </div>
                <div className="d-flex gap-2">
                  <button className="btn btn-sm btn-success" onClick={() => verify(b.id, b.fullName, true)}>
                    Approve
                  </button>
                  <button className="btn btn-sm btn-outline-danger" onClick={() => verify(b.id, b.fullName, false)}>
                    Reject
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="card mb-4">
        <div className="card-body">
          <h5 className="card-title mb-3">Users</h5>
          <div className="row">
            <div className="col-lg-6 mb-3 mb-lg-0">
              <h6 className="text-muted">Parents ({parents.length})</h6>
              {parents.length === 0 && <p className="text-muted small mb-0">No parents registered yet.</p>}
              {parents.length > 0 && (
                <div className="table-responsive">
                  <table className="table table-sm align-middle">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody>
                      {parents.map((u) => (
                        <tr key={u.id}>
                          <td>
                            <div className="fw-semibold">{u.fullName}</div>
                            <div className="text-muted small">Joined {formatDay(u.createdAt)}</div>
                          </td>
                          <td className="small">{u.email}</td>
                          <td className="text-end">
                            <button
                              className="btn btn-sm btn-outline-danger"
                              onClick={() => removeUser(u.id, u.fullName)}
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
            <div className="col-lg-6">
              <h6 className="text-muted">Sitters ({sitters.length})</h6>
              {sitters.length === 0 && <p className="text-muted small mb-0">No sitters registered yet.</p>}
              {sitters.length > 0 && (
                <div className="table-responsive">
                  <table className="table table-sm align-middle">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody>
                      {sitters.map((u) => (
                        <tr key={u.id}>
                          <td>
                            <div className="fw-semibold">{u.fullName}</div>
                            <div className="text-muted small">Joined {formatDay(u.createdAt)}</div>
                          </td>
                          <td className="small">{u.email}</td>
                          <td className="text-end">
                            <button
                              className="btn btn-sm btn-outline-danger"
                              onClick={() => removeUser(u.id, u.fullName)}
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
          <p className="text-muted small mb-0 mt-2">
            Accounts with bookings can't be deleted, to keep booking history intact.
          </p>
        </div>
      </div>

      <div className="card">
        <div className="card-body">
          <h5 className="card-title mb-3">All Bookings</h5>
          {bookings.length === 0 && <p className="text-muted mb-0">No bookings yet.</p>}
          <div className="table-responsive">
            <table className="table table-sm">
              <thead>
                <tr>
                  <th>Parent</th>
                  <th>Babysitter</th>
                  <th>When</th>
                  <th>Amount</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((b) => (
                  <tr key={b.id}>
                    <td>{b.parentName}</td>
                    <td>{b.babysitterName}</td>
                    <td>{formatDate(b.startTime)}</td>
                    <td>${b.totalAmount}</td>
                    <td>
                      <span className={`badge ${STATUS_BADGE[b.status]}`}>{b.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
