// src/components/ProtectedRoute.jsx
import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
  const [isValid, setIsValid] = useState(null); // null indicates loading state
  // isValid will be true if the token is valid, false if invalid or not present

  useEffect(() => {
    const token = localStorage.getItem('token');

    if (!token) {
      setIsValid(false); // No token found, redirect to login
      console.warn("No token found in localStorage.");
      return;
    }

    // Validate the token format and expiration
    try {
      const payload = JSON.parse(atob(token.split('.')[1])); // 
      const currentTime = Date.now() / 1000; //with seconds

      if (payload.exp && currentTime > payload.exp) { //the token is expired
        localStorage.removeItem('token');
        setExpired(true);
        setIsValid(false);
      } else {
        setIsValid(true);
      }
    } catch (err) {
      console.error("Invalid token format:", err);
      localStorage.removeItem('token');
      setIsValid(false);
    }
  }, []);

  if (isValid === null) return null; // still loading

  if (!isValid) {
    const redirectUrl = `/login?expired=${expired ? '1' : '0'}&from=${location.pathname}`;
    return <Navigate to={redirectUrl} replace />;
  }

  return children;
};

export default ProtectedRoute;
