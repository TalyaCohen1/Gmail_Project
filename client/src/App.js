import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './styles/App.css'; // main design file
import Register from './pages/Register';
import Login from './pages/Login';
import ProtectedRoute from './components/ProtectedRoute';


function App() {
  return (
    <div className="App">
      <Router>
      <nav>
        <Link to="/login" style={{ marginRight: '10px' }}>Login</Link>
        <Link to="/register">Register</Link>
      </nav>

      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        {/* <Route
          path="/inbox"
          element={
            <ProtectedRoute>
              <h1>Inbox</h1>
              <p>This is a protected route. You must be logged in to view this page.</p>
            </ProtectedRoute>
          } /> */}
      </Routes>
    </Router>
    </div>
  );
}

export default App;
