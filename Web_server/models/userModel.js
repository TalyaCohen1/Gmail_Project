const users = []; //for user data

const createUser = (fullName, id,emailAdress,birthDate, gender, password) => {
    const user = {
        fullName,
        id,
        emailAdress,
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
const findByEmail = (emailAdress) => {
    return users.find(u => u.emailAdress === emailAdress);
}

module.exports = {
    createUser,
    findById,
    findByEmail
};
