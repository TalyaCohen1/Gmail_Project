import React, {useState, useNavigate} from "react";
import '../styles/AuthForm.css';

const RegistrationForm = () => {
    //state to hold form data
    const [formData, setFormData] = React.useState({
        fullName: '',
        emailAddress: '',
        birthDate: '',
        gender: '',
        profileImage: '',
        password: '',
        confirmPassword: ''
    });

    //state to hold error messages
    const [errors, setErrors] = React.useState({});
    //state to hold success message
    const [successMessage, setSuccessMessage] = React.useState('');
    const navigate = useNavigate();
    //handle input change
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: value
        }));
    };

    //handle image upload
    const [selectedImage, setSelectedImage] = useState(null);
    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setSelectedImage(file);
        }
    };
    //validate form data
    const validateForm = () => {
        const newErrors = {};
        const { fullName, emailAddress, birthDate, gender, password,  profileImage, confirmPassword } = formData;

        if (!fullName.trim()) newErrors.fullName = 'Full name is required';
        if (!emailAddress.endsWith('@gmail.com')) newErrors.emailAddress = 'Email must be a @gmail.com address';
        if (!birthDate) newErrors.birthDate = 'Birth date is required';
        else {
        const birth = new Date(birthDate);
        const today = new Date();
        const age = today.getFullYear() - birth.getFullYear();
        if (age < 13) newErrors.birthDate = 'You must be at least 13 years old';
        }

        if (!['male', 'female'].includes(gender)) newErrors.gender = 'Select gender';
        if (!password.match(/^(?=.*[A-Z])(?=.*\d).{8,}$/)) {
        newErrors.password = 'Password must be at least 8 chars, 1 uppercase, 1 digit';
        }
        if (password !== confirmPassword) newErrors.confirmPassword = 'Passwords do not match';

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    //handle form submission
    const handleSubmit = async (e) => {
        e.preventDefault();
        setSuccessMessage('');
        if (!validateForm()) return;

        try {
            const formDataToSend = new FormData();
            formDataToSend.append('fullName', formData.fullName);
            formDataToSend.append('emailAddress', formData.emailAddress);
            formDataToSend.append('birthDate', formData.birthDate);
            formDataToSend.append('gender', formData.gender);
            formDataToSend.append('password', formData.password);
            if (selectedImage) {
                formDataToSend.append('profileImage', selectedImage);
            }
            const response = await fetch('http://localhost:3000/api/users', {
                method: 'POST',
                body: formDataToSend
            });

            if (!response.ok) {
                const errorData = await response.json();
                setErrors({ general: errorData.error || 'Registration failed' });
            } else {
                setSuccessMessage('Registration successful! You can now log in.');
                setFormData({
                    fullName: '',
                    emailAddress: '',
                    birthDate: '',
                    gender: '',
                    password: '',
                    confirmPassword: '' });
                setErrors({});

                setTimeout(() => {
                    navigate('/login');
                }, 2000); // Redirect after 2 seconds
            }
        } catch (error) {
            console.error('Fetch error:', error);
            setErrors({ general: 'An error occurred. Please try again later.' });
        }
    };

    return (
     <form className="auth-form" onSubmit={handleSubmit}>
      <label>Full Name</label>
      <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} required />

      <label>Email Address</label>
      <input type="email" name="emailAddress" value={formData.emailAddress} onChange={handleChange} required />

      <label>Birth Date</label>
      <input type="date" name="birthDate" value={formData.birthDate} onChange={handleChange} required />

      <label>Gender</label>
      <select name="gender" value={formData.gender} onChange={handleChange}>
        <option value="female">Female</option>
        <option value="male">Male</option>
      </select>

        <label>Upload Profile Picture (optional)</label>
        <input type="file" name="profileImage" accept="image/*" onChange={handleImageChange} />

        {selectedImage && (
            <div style={{ marginBottom: '1rem' }}>
                <img
                src={URL.createObjectURL(selectedImage)}
                alt="Preview"
                width="100"
                style={{ borderRadius: '50%' }}
                />
            </div>
            )}

      <label>Password</label>
      <input type="password" name="password" value={formData.password} onChange={handleChange} required />

        <label>Confirm Password</label>
        <input type="password" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} required />
      <button type="submit">Register</button>
    </form>
  );
};

export default RegistrationForm;
        


