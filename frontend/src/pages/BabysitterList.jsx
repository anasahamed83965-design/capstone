import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client';

export default function BabysitterList() {
  const [babysitters, setBabysitters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [minRating, setMinRating] = useState('');
  const [maxRate, setMaxRate] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {};
      if (minRating) params.minRating = minRating;
      if (maxRate) params.maxRate = maxRate;
      const res = await client.get('/babysitters', { params });
      setBabysitters(res.data.data);
    } catch (err) {
      setError('Could not load babysitters right now.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="mb-0">Find a Babysitter</h3>
        <div className="d-flex gap-2">
          <select
            className="form-select form-select-sm"
            value={minRating}
            onChange={(e) => setMinRating(e.target.value)}
          >
            <option value="">Any rating</option>
            <option value="3">3+ stars</option>
            <option value="4">4+ stars</option>
            <option value="4.5">4.5+ stars</option>
          </select>
          <select
            className="form-select form-select-sm"
            value={maxRate}
            onChange={(e) => setMaxRate(e.target.value)}
          >
            <option value="">Any rate</option>
            <option value="10">Up to $10/hr</option>
            <option value="15">Up to $15/hr</option>
            <option value="20">Up to $20/hr</option>
            <option value="25">Up to $25/hr</option>
          </select>
          <button className="btn btn-sm btn-outline-primary" onClick={load}>
            Filter
          </button>
        </div>
      </div>

      {loading && <p className="text-muted">Loading...</p>}
      {error && <div className="alert alert-warning">{error}</div>}

      {!loading && babysitters.length === 0 && (
        <div className="alert alert-info">No verified babysitters match your search yet.</div>
      )}

      <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-3">
        {babysitters.map((b) => (
          <div className="col" key={b.id}>
            <div className="card h-100">
              <div className="card-body">
                <h5 className="card-title mb-1">{b.fullName}</h5>
                <div className="text-muted small mb-2">
                  {b.experienceYears} {b.experienceYears === 1 ? 'year' : 'years'} experience
                </div>
                <div className="mb-2">
                  <span className="text-warning me-2">{'\u2605'} {b.avgRating.toFixed(1)}</span>
                  <span className="badge bg-primary">${b.hourlyRate}/hr</span>
                </div>
                {b.bio && <p className="card-text small">{b.bio}</p>}
                <Link className="btn btn-sm btn-outline-primary" to={`/babysitters/${b.id}`}>
                  View profile
                </Link>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
