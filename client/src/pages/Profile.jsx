// src/pages/Profile.jsx
import React from 'react';

const Profile = () => {
  const fullName = localStorage.getItem('fullName');
  const profileImage = localStorage.getItem('profileImage') || '/default-profile.png';

  return (
    <div style={{ padding: '20px' }}>
      <h2>Welcome, {fullName}</h2>
      <img src={profileImage} alt="Profile" width="100" />
    </div>
  );
};

export default Profile;
