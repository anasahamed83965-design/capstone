import { useEffect, useState } from 'react';
import client from '../api/client';
import { useAuth } from '../context/AuthContext';

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

function ReviewBox({ bookingId, onDone }) {
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await client.post(`/bookings/${bookingId}/reviews`, { rating, comment });
      setMessage('Review submitted. Thank you!');
      onDone();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not submit the review.');
    }
  };

  return (
    <form onSubmit={submit} className="mt-2 border-top pt-2">
      <div className="row g-2 align-items-end">
        <div className="col-auto">
          <label className="form-label small mb-1">Rating</label>
          <select className="form-select form-select-sm" value={rating} onChange={(e) => setRating(Number(e.target.value))}>
            {[5, 4, 3, 2, 1].map((n) => (
              <option key={n} value={n}>
                {n} {n === 1 ? 'star' : 'stars'}
              </option>
            ))}
          </select>
        </div>
        <div className="col">
          <label className="form-label small mb-1">Comment</label>
          <input
            type="text"
            className="form-control form-control-sm"
            maxLength={1000}
            value={comment}
            onChange={(e) => setComment(e.target.value)}
          />
        </div>
        <div className="col-auto">
          <button className="btn btn-sm btn-outline-success" type="submit">
            Submit review
          </button>
        </div>
      </div>
      {message && <div className="alert alert-success small mt-2 mb-0">{message}</div>}
      {error && <div className="alert alert-danger small mt-2 mb-0">{error}</div>}
    </form>
  );
}

export default function MyBookings() {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reviewed, setReviewed] = useState({});

  const load = async () => {
    setLoading(true);
    try {
      const res = await client.get('/bookings');
      setBookings(res.data.data);
    } catch (err) {
      setError('Could not load your bookings.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const updateStatus = async (id, status) => {
    setError('');
    try {
      await client.patch(`/bookings/${id}/status`, { status });
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not update the booking.');
    }
  };

  if (loading) {
    return <p className="text-muted">Loading...</p>;
  }

  if (bookings.length === 0) {
    return <div className="alert alert-info">You have no bookings yet.</div>;
  }

  const isBabysitter = user?.role === 'BABYSITTER';

  return (
    <div>
      <h3 className="mb-3">My Bookings</h3>
      {error && <div className="alert alert-danger">{error}</div>}
      {bookings.map((b) => (
        <div className="card mb-3" key={b.id}>
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-start">
              <div>
                <h6 className="mb-1">
                  {isBabysitter ? b.parentName : b.babysitterName}
                </h6>
                <div className="text-muted small">
                  {formatDate(b.startTime)} - {formatDate(b.endTime)}
                </div>
                {b.notes && <div className="small mt-1">Notes: {b.notes}</div>}
                <div className="mt-2">
                  <span className={`badge ${STATUS_BADGE[b.status]}`}>{b.status}</span>
                </div>
              </div>
              <div className="text-end">
                <div className="fw-semibold">${b.totalAmount}</div>
              </div>
            </div>

            <div className="mt-3 d-flex gap-2">
              {isBabysitter && b.status === 'PENDING' && (
                <>
                  <button className="btn btn-sm btn-success" onClick={() => updateStatus(b.id, 'CONFIRMED')}>
                    Confirm
                  </button>
                  <button className="btn btn-sm btn-outline-danger" onClick={() => updateStatus(b.id, 'CANCELLED')}>
                    Decline
                  </button>
                </>
              )}
              {!isBabysitter && b.status === 'CONFIRMED' && (
                <>
                  <button className="btn btn-sm btn-success" onClick={() => updateStatus(b.id, 'COMPLETED')}>
                    Mark completed
                  </button>
                  <button className="btn btn-sm btn-outline-danger" onClick={() => updateStatus(b.id, 'CANCELLED')}>
                    Cancel
                  </button>
                </>
              )}
              {!isBabysitter && b.status === 'PENDING' && (
                <button className="btn btn-sm btn-outline-danger" onClick={() => updateStatus(b.id, 'CANCELLED')}>
                  Cancel
                </button>
              )}
              {!isBabysitter && b.status === 'COMPLETED' && !reviewed[b.id] && (
                <ReviewBox bookingId={b.id} onDone={() => setReviewed((r) => ({ ...r, [b.id]: true }))} />
              )}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
