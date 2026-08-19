import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import client from '../api/client';

const AuthContext = createContext(null);

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem('user'));
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  // Keep every open tab on the same account: if you log in/out as a
  // different user in another tab, this tab follows instead of mixing
  // one account's screen with another account's token.
  const syncSession = useCallback(() => {
    setUser(readStoredUser());
  }, []);

  useEffect(() => {
    window.addEventListener('storage', syncSession);
    return () => window.removeEventListener('storage', syncSession);
  }, [syncSession]);

  const storeSession = (data) => {
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data));
    setUser(data);
  };

  const login = async (credentials) => {
    const res = await client.post('/auth/login', credentials);
    storeSession(res.data.data);
    return res.data.data;
  };

  const signup = async (payload) => {
    const res = await client.post('/auth/signup', payload);
    storeSession(res.data.data);
    return res.data.data;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
