const users = []; //for user data

const createUser = (fullName, id,emailAddress,birthDate, gender, password) => {
    const user = {
        fullName,
        id,
        emailAddress,
        birthDate,
        gender,
        password
    };
    users.push(user);
    return user;
}

const findById =(id) => {
  return users.find(u => u.id === id);
}
const findByEmail = (emailAddress) => {
    return users.find(u => u.emailAddress === emailAddress);
}
//for checking
const getAllUsers = () => {
    return users;
}
module.exports = {
    createUser,
    findById,
    findByEmail,
    getAllUsers
};
