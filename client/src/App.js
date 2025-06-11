import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './styles/App.css'; // main design file
import Register from './pages/Register';
import Login from './pages/Login';
import Inbox from './pages/InboxPage';
import ProtectedRoute from './components/ProtectedRoute';
import { LabelProvider } from './context/LabelContext';


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
        <Route
          path="/inbox"
          element={
            <LabelProvider>
            <ProtectedRoute>
              <Inbox />
            </ProtectedRoute>
          </LabelProvider>
          } /> 
      </Routes>
    </Router>
    </div>
  );
}

export default App;
