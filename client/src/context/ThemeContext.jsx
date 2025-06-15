// src/context/ThemeContext.jsx
import React, { createContext, useState, useEffect, useMemo } from 'react';

// Create the context
export const ThemeContext = createContext();

// Create the provider component
export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light');

  useEffect(() => {
    document.body.className = theme; // e.g., <body class="dark">
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prevTheme => (prevTheme === 'light' ? 'dark' : 'light'));
  };

  // useMemo prevents the value object from being recreated on every render
  const value = useMemo(() => ({ theme, toggleTheme }), [theme]);

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  );
}