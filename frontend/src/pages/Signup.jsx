import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Signup() {
  const { signup } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: '',
    role: 'PARENT',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [slow, setSlow] = useState(false);
  const slowTimer = useRef(null);

  useEffect(() => () => clearTimeout(slowTimer.current), []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;
    setError('');
    setLoading(true);
    setSlow(false);
    // After 8s the server is probably cold-starting; tell the user so the
    // wait feels intentional instead of like a frozen page.
    slowTimer.current = setTimeout(() => setSlow(true), 8000);
    try {
      const data = await signup(form);
      if (data.role === 'BABYSITTER') {
        navigate('/dashboard');
      } else {
        navigate('/babysitters');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Signup failed. Please try again.');
      setLoading(false);
      setSlow(false);
      clearTimeout(slowTimer.current);
    }
  };

  return (
    <div className="row justify-content-center">
      <div className="col-md-6 col-lg-5">
        <div className="card">
          <div className="card-body p-4">
            <h4 className="card-title mb-3">Create your account</h4>
            {error && <div className="alert alert-danger py-2">{error}</div>}
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Full name</label>
                <input
                  type="text"
                  name="fullName"
                  className="form-control"
                  value={form.fullName}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="mb-3">
                <label className="form-label">Email</label>
                <input
                  type="email"
                  name="email"
                  className="form-control"
                  value={form.email}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="mb-3">
                <label className="form-label">Password</label>
                <input
                  type="password"
                  name="password"
                  className="form-control"
                  minLength={6}
                  value={form.password}
                  onChange={handleChange}
                  required
                />
                <div className="form-text">At least 6 characters</div>
              </div>
              <div className="mb-3">
                <label className="form-label">Phone</label>
                <input
                  type="tel"
                  name="phone"
                  className="form-control"
                  value={form.phone}
                  onChange={handleChange}
                />
              </div>
              <div className="mb-3">
                <label className="form-label">I am a...</label>
                <select name="role" className="form-select" value={form.role} onChange={handleChange}>
                  <option value="PARENT">Parent looking for care</option>
                  <option value="BABYSITTER">Babysitter offering care</option>
                </select>
              </div>
              <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                {loading ? (
                  <>
                    <span
                      className="spinner-border spinner-border-sm me-2"
                      aria-hidden="true"
                    ></span>
                    Creating account…
                  </>
                ) : (
                  'Create account'
                )}
              </button>
              {loading && slow && (
                <div className="alert alert-warning mt-2 mb-0 py-2 small" role="status">
                  Still connecting — the server may be waking up (first request can
                  take up to about a minute). Please wait…
                </div>
              )}
            </form>
            <p className="mt-3 mb-0 text-center small">
              Already registered? <Link to="/login">Log in</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
