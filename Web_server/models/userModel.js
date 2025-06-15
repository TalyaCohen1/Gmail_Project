const users = []; //for user data

/**
 * create a new user and add it to the users array
 * @param {string} fullName - The user's full name.
 * @param {string} id - Unique user identifier.
 * @param {string} emailAddress - The user's email address.
 * @param {string} birthDate - Date of birth (YYYY-MM-DD).
 * @param {string} gender - Gender ("male" or "female").
 * @param {string} password - The user's password (should be hashed in production).
 * @returns {Object} The created user object.
 */
const createUser = (fullName, id, emailAddress, birthDate, gender, password, profileImage = '/uploads/default-profile.png') => {
    const user = {
        fullName,
        id,
        emailAddress,
        birthDate,
        gender,
        password , 
        profileImage // Default profile image if not provided
    };
    users.push(user);
    return user;
}

/**
 * Finds a user by their unique ID.
 * @param {string} id - The user ID.
 * @returns {Object|undefined} The user object, if found.
 */
const findById =(id) => {
  return users.find(u => u.id === id);
}

/**
 * Finds a user by their email address.
 * @param {string} emailAddress - The email to search for.
 * @returns {Object|undefined} The user object, if found.
 */
const findByEmail = (emailAddress) => {
    return users.find(u => u.emailAddress === emailAddress);
}
/**
 * Returns all users (for testing/debugging).
 * @returns {Array<Object>} List of all registered users.
 */
const getAllUsers = () => {
    return users;
}
module.exports = {
    createUser,
    findById,
    findByEmail,
    getAllUsers
};
