import { useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import client from './api/client';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Signup from './pages/Signup';
import BabysitterList from './pages/BabysitterList';
import BabysitterDetail from './pages/BabysitterDetail';
import MyBookings from './pages/MyBookings';
import BabysitterDashboard from './pages/BabysitterDashboard';
import AdminDashboard from './pages/AdminDashboard';

export default function App() {
  // Ping the API as soon as the app opens: on Render's free tier the
  // instance sleeps after ~15 min idle, so this wake-up runs while the
  // user is still browsing and makes their first login/signup fast.
  useEffect(() => {
    client.get('/health').catch(() => {});
  }, []);

  return (
    <>
      <Navbar />
      <main className="container py-4">
        <Routes>
          <Route path="/" element={<Navigate to="/babysitters" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/babysitters" element={<BabysitterList />} />
          <Route path="/babysitters/:id" element={<BabysitterDetail />} />
          <Route
            path="/my-bookings"
            element={
              <ProtectedRoute>
                <MyBookings />
              </ProtectedRoute>
            }
          />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute role="BABYSITTER">
                <BabysitterDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute role="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
        </Routes>
      </main>
    </>
  );
}
