import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import client from '../api/client';
import { useAuth } from '../context/AuthContext';

function formatDate(value) {
  const d = new Date(value);
  return d.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

export default function BabysitterDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [slots, setSlots] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedSlot, setSelectedSlot] = useState('');
  const [notes, setNotes] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAll = async () => {
      try {
        const [p, s, r] = await Promise.all([
          client.get(`/babysitters/${id}`),
          client.get(`/babysitters/${id}/slots`),
          client.get(`/babysitters/${id}/reviews`),
        ]);
        setProfile(p.data.data);
        setSlots(s.data.data);
        setReviews(r.data.data);
      } catch (err) {
        setError('This babysitter profile could not be found.');
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, [id]);

  const book = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');
    try {
      await client.post('/bookings', {
        babysitterId: Number(id),
        slotId: Number(selectedSlot),
        notes,
      });
      setMessage('Booking request sent! The babysitter needs to confirm it.');
      setNotes('');
      setSelectedSlot('');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not make the booking.');
    }
  };

  if (loading) {
    return <p className="text-muted">Loading...</p>;
  }

  if (error) {
    return <div className="alert alert-warning">{error}</div>;
  }

  const openSlots = slots.filter((s) => !s.booked);

  return (
    <div className="row">
      <div className="col-lg-4">
        <div className="card mb-4">
          <div className="card-body">
            <h4 className="mb-1">{profile.fullName}</h4>
            <p className="text-muted mb-2">{profile.phone}</p>
            <div className="mb-2">
              <span className="text-warning me-3">{'\u2605'} {profile.avgRating.toFixed(1)}</span>
              <span className="badge bg-primary">${profile.hourlyRate}/hr</span>
            </div>
            <p className="mb-0">
              {profile.experienceYears} {profile.experienceYears === 1 ? 'year' : 'years'} experience
            </p>
          </div>
        </div>

        <div className="card mb-4">
          <div className="card-body">
            <h5 className="card-title mb-3">About</h5>
            <p className="mb-0">{profile.bio || 'No bio yet.'}</p>
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            <h5 className="card-title mb-3">Reviews</h5>
            {reviews.length === 0 && <p className="text-muted mb-0">No reviews yet.</p>}
            {reviews.map((r) => (
              <div key={r.id} className="border-bottom py-2">
                <div className="fw-semibold small">{r.reviewerName}</div>
                <div className="text-warning small">{'\u2605'.repeat(r.rating)}</div>
                {r.comment && <p className="mb-0 small">{r.comment}</p>}
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="col-lg-8">
        <div className="card">
          <div className="card-body">
            <h5 className="card-title mb-3">Available Slots</h5>
            {openSlots.length === 0 && <p className="text-muted">No open slots right now.</p>}

            {openSlots.length > 0 && user?.role === 'PARENT' && (
              <form onSubmit={book}>
                <div className="mb-3">
                  <label className="form-label">Pick a time</label>
                  <select
                    className="form-select"
                    value={selectedSlot}
                    onChange={(e) => setSelectedSlot(e.target.value)}
                    required
                  >
                    <option value="">Choose a slot...</option>
                    {openSlots.map((s) => (
                      <option key={s.id} value={s.id}>
                        {formatDate(s.startTime)} - {formatDate(s.endTime)}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Notes for the babysitter</label>
                  <textarea
                    className="form-control"
                    rows="3"
                    maxLength={500}
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="Kids' routines, allergies, drop-off details..."
                  />
                </div>
                <button type="submit" className="btn btn-primary">
                  Request booking
                </button>
              </form>
            )}

            {openSlots.length > 0 && user?.role !== 'PARENT' && (
              <ul className="list-group">
                {openSlots.map((s) => (
                  <li key={s.id} className="list-group-item d-flex justify-content-between align-items-center">
                    <span>
                      {formatDate(s.startTime)} - {formatDate(s.endTime)}
                    </span>
                    {!user && <Link to="/login" className="btn btn-sm btn-outline-primary">Login to book</Link>}
                    {user?.role === 'BABYSITTER' && <span className="badge bg-secondary">Unavailable to book</span>}
                  </li>
                ))}
              </ul>
            )}

            {message && <div className="alert alert-success mt-3 mb-0">{message}</div>}
            {error && <div className="alert alert-danger mt-3 mb-0">{error}</div>}
          </div>
        </div>
      </div>
    </div>
  );
}
