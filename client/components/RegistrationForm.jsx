import React from "react";

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

    //handle input change
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: value
        }));
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
            const response = await fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                setErrors({ general: data.error || 'Registration failed' });
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
            }
        } catch (error) {
            setErrors({ general: 'An error occurred. Please try again later.' });
        }
    };

    return (
    <form onSubmit={handleSubmit}>
      {errors.general && <p className="error">{errors.general}</p>}
      {successMessage && <p className="success">{successMessage}</p>}

      <input type="text" name="fullName" placeholder="Full Name" value={formData.fullName} onChange={handleChange} />
      {errors.fullName && <p className="error">{errors.fullName}</p>}

      <input type="email" name="emailAddress" placeholder="Email Address" value={formData.emailAddress} onChange={handleChange} />
      {errors.emailAddress && <p className="error">{errors.emailAddress}</p>}

      <input type="date" name="birthDate" value={formData.birthDate} onChange={handleChange} />
      {errors.birthDate && <p className="error">{errors.birthDate}</p>}

      <select name="gender" value={formData.gender} onChange={handleChange}>
        <option value="">Select Gender</option>
        <option value="male">Male</option>
        <option value="female">Female</option>
      </select>
      {errors.gender && <p className="error">{errors.gender}</p>}

      <input type="password" name="password" placeholder="Password" value={formData.password} onChange={handleChange} />
      {errors.password && <p className="error">{errors.password}</p>}

      <input type="password" name="confirmPassword" placeholder="Confirm Password" value={formData.confirmPassword} onChange={handleChange} />
      {errors.confirmPassword && <p className="error">{errors.confirmPassword}</p>}

      <button type="submit">Register</button>
    </form>
  );
};

export default RegistrationForm;
        


