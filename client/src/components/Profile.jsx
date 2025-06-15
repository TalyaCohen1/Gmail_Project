import { useState } from "react";
import "../styles/AuthForm.css"; // Reuse styles from Compose popup


export default function EditProfilePopup({ onClose, currentName, currentImage }) {
  const [fullName, setFullName] = useState(currentName || "");
  const [profileImage, setProfileImage] = useState(null);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSuccess("");
    setError("");

    const formData = new FormData();
    formData.append("fullName", fullName);
   if (profileImage) {
      formData.append("profileImage", profileImage);
    }

    try {
      const res = await fetch(`http://localhost:3000/api/users/${localStorage.getItem('userId')}`, {
        method: "PATCH",
        body: formData,
        credentials: "include",
      });

      if (!res.ok) throw new Error("Failed to update profile");
      const data = await res.json();
      localStorage.setItem("fullName", data.fullName);
      localStorage.setItem("profileImage", data.profileImage);
      setSuccess("Profile updated successfully");
    } catch (err) {
      console.error(err);
      setError("Failed to update profile");
    }
  };

  return (
    <div className="compose-popup">
      <div className="header">
        <h2>Edit Profile</h2>
        <div className="window-controls">
          <button onClick={onClose}>✕</button>
        </div>
      </div>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Full Name"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          required
          className="w-full border p-2 mb-2 rounded"
        />
        <input
          type="file"
          accept="image/*"
          onChange={(e) => setProfileImage(e.target.files[0])}
          className="w-full border p-2 mb-2 rounded"
        />
        <button type="submit" className="mt-2">Save</button>
        {error && <div style={{ color: 'red' }}>{error}</div>}
        {success && <div style={{ color: 'green' }}>{success}</div>}
      </form>
    </div>
  );
}
