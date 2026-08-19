import axios from 'axios';

const client = axios.create({
  // Local dev: relative '/api' goes through the Vite proxy to localhost:8080.
  // Production (Vercel + Render): set VITE_API_BASE_URL to the backend URL.
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default client;
