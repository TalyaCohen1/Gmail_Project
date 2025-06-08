import {useNavigate} from 'react-router-dom';

const LogOut = () => {
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('token'); // Remove the token from localStorage
        console.log("Logged out successfully");
        navigate('/login'); // Redirect to the login page
    };

    return (
        <button onClick={handleLogout} style={{ padding: '10px 20px', fontSize: '16px' }}>
            Log Out
        </button>
    );
};

export default LogOut;