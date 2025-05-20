const users = []; //for user data

const createUser = (username, id, password) => {
    const user = {
        username,
        id,
        password
    };
    users.push(user);
    return user;
}

const findById =(id) => {
  return users.find(u => u.id === id);
}
const findByUsername = (username) => {
    return users.find(u => u.username === username);
}

module.exports = {
    createUser,
    findById,
    findByUsername
};
