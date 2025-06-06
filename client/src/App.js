import React from 'react';
import './styles/App.css'; // main design file
import Header from './components/Header';
import SideBar from './components/Sidebar';

function App() {
  return (
    <div className="App">
      <Header />
      <div className="app-container">
        <SideBar />
        <main className="main-content">
          {/* Your main content goes here */}
          <h1>Welcome to your Gmail-like app!</h1>
        </main>
      </div>
    </div>
  );
}

export default App;
