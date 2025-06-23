import React, {useState} from "react";
import { useNavigate, Link } from 'react-router-dom';
import '../styles/AuthForm.css';

const RegistrationForm = () => {
    //state to hold form data
    const [formData, setFormData] = React.useState({
        fullName: '',
        emailAddress: '',
        birthDate: '',
        gender: '',
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

    // //handle image upload
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
        const { fullName, emailAddress, birthDate, gender, password, confirmPassword } = formData;

        if (!fullName.trim()) newErrors.fullName = 'Full name is required';
        if (!emailAddress.endsWith('@gmail.com')) newErrors.emailAddress = 'Email must be a @gmail.com address';
        if (!birthDate) newErrors.birthDate = 'Birth date is required';
        else {
        const birth = new Date(birthDate);
        const today = new Date();
        let age = today.getFullYear() - birth.getFullYear();
        const monthDiff = today.getMonth() - birth.getMonth();
    
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
            age--;
        }
        
        if (age < 13) {
            newErrors.birthDate = 'You must be at least 13 years old';
        }
        }

        if (!gender || !['male', 'female'].includes(gender)) newErrors.gender = 'Select gender';
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
    
    const isValid = validateForm();
    if (!validateForm()) {
            return;
    }

    const formDataToSend = new FormData();
    formDataToSend.append('fullName', formData.fullName);
    formDataToSend.append('emailAddress', formData.emailAddress);
    formDataToSend.append('birthDate', formData.birthDate);
    formDataToSend.append('gender', formData.gender);
    formDataToSend.append('password', formData.password);
    if (selectedImage) {
        formDataToSend.append('profileImage', selectedImage);
    }

    try {
        const response = await fetch('http://localhost:3000/api/users', {
            method: 'POST',
            body: formDataToSend
        });

        const data = await response.json();

        if (!response.ok) {
            setErrors({ general: data.error || 'Registration failed' });
        } else {
            setSuccessMessage('Registration successful! You can now log in.');

            // Store user data in localStorage
            localStorage.setItem('fullName', data.fullName);
            localStorage.setItem('profileImage', data.profileImage);
            localStorage.setItem('userId', data.userId); // Assuming the response contains a user ID
            localStorage.setItem('token', data.token || ''); // Assuming the response contains a token
            setFormData({
                fullName: '',
                emailAddress: '',
                birthDate: '',
                gender: '',
                password: '',
                confirmPassword: ''
            });
            setErrors({});

            setTimeout(() => {
                navigate('/login');
            }, 1000);
        }
    } catch (error) {
        console.error('Fetch error:', error);
        setErrors({ general: 'An error occurred. Please try again later.' });
    }
};


    return (
    <div className="auth-container">
        <div className="logo-container">
        <img src="/Smail_logo.svg" alt="ReeMail logo" />
      </div>

        <div className="auth-subtitle">
           Create your account 
        </div>

    <form onSubmit={handleSubmit}>
    <div className="form-group">
      <label htmlFor="fullName">Full Name</label>
      <input type="text"  name="fullName" value={formData.fullName} onChange={handleChange} className={errors.fullName ? 'error' : ''} placeholder="Write your full name"  autoComplete="name" />
       {errors.fullName && <div className="error-message">{errors.fullName}</div>}
    </div>

    <div className="form-group">
      <label htmlFor="emailAddress">Email Address</label>
      <input type="email" name="emailAddress" value={formData.emailAddress} onChange={handleChange} className={errors.emailAddress ? 'error' : ''} placeholder="your.name@gmail.com" autoComplete="email" />
        {errors.emailAddress && <div className="error-message">{errors.emailAddress} </div>}
        </div>

      <div className="form-group">  
      <label htmlFor="birthDate">Birth Date</label>
      <input type="date" name="birthDate" value={formData.birthDate} onChange={handleChange} className={errors.birthDate ? 'error' : ''} autoComplete="bday" />
        {errors.birthDate && <div className="error-message">{errors.birthDate}</div>}
      </div>

    <div className="form-group">
      <label htmlFor="gender">Gender</label>
      <select name="gender" value={formData.gender} onChange={handleChange} className={errors.gender ? 'error' : ''}>
        <option value= ""> choose gender</option>
        <option value="female">Female</option>
        <option value="male">Male</option>
      </select>
      {errors.gender && <div className="error-message">{errors.gender}</div>}
    </div>

    <div className="form-group">
        <label >Upload Profile Picture (optional)</label>
        <div className="file-input-wrapper">
                        <div className="file-input">
                            <input 
                                type="file" 
                                name="profileImage" 
                                accept="image/*" 
                                onChange={handleImageChange} 
                            />
                            📷 choose your profile picture
                        </div>
                    </div>
                    {selectedImage && (
                        <div className="image-preview">
                            <img
                                src={URL.createObjectURL(selectedImage)}
                                alt="preview"
                            />
                        </div>
                    )}
                </div>

    <div className="form-group">
      <label htmlFor="password" >Password</label>
      <input type="password" name="password" value={formData.password} onChange={handleChange} className={errors.password ? 'error' : ''}
      placeholder="Password must be at least 8 chars, 1 uppercase, 1 digit" 
      autoComplete="new-password"/>
        {errors.password && <div className="error-message">{errors.password}</div>}
    </div>

    <div className="form-group">
    <label htmlFor="confirmPassword">Confirm Password</label>
        <input 
                        type="password" 
                        id="confirmPassword"
                        name="confirmPassword" 
                        value={formData.confirmPassword} 
                        onChange={handleChange} 
                        className={errors.confirmPassword ? 'error' : ''}
                        placeholder="write your password again"
                        autoComplete="new-password"
                    />
                    {errors.confirmPassword && <div className="error-message">{errors.confirmPassword}</div>}
                </div>
                      
    <button type="submit" className="auth-button">Register</button>

    {errors.general && <div className="general-error">{errors.general}</div>}
    {successMessage && <div className="success-message">{successMessage}</div>}
            
    </form>
    <div className="form-footer">
                <Link to="/login"> already have accunt? login</Link>
            </div>
        </div>
    
  );
};

export default RegistrationForm;
        


