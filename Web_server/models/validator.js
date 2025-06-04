
/**
 * checks if the given date is a valid date string in the format "YYYY-MM-DD".
 * @param {*} dateString - The date string to validate.
 * @returns {boolean} True if the date string is valid.
 */
function isValidDateFormat(dateString) {
  return /^\d{4}-\d{2}-\d{2}$/.test(dateString);
}

/**
 * validates if the given date in string format is not in the future.
 * @param {string} dateStr - The date string to validate.
 * @returns {boolean} True if the date is valid and not in the future.
 */
function isPastDate(dateStr) {
  const date = new Date(dateStr);
  const today = new Date();
  return !isNaN(date.getTime()) && date <= today;
}

/**
 * checks if the given date string old enough to be over 13 years old and open an account.
 * @param {*} dateStr - The date string to validate (format: "YYYY-MM-DD").
 * @returns - {boolean} True if the user is over 13 years old.
 */
function isAgeOver13(dateStr) {
  const date = new Date(dateStr);
  const today = new Date();
  const age = today.getFullYear() - date.getFullYear();
  const monthDiff = today.getMonth() - date.getMonth();
  
  // If the birth month is later in the year or it's the same month but the day hasn't occurred yet
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < date.getDate())) {
    return age > 13;
  }
  
  return age >= 13;
}


/**
 * Validates if the gender is either "male" or "female".
 * @param {string} gender - The gender string to validate.
 * @returns {boolean} True if gender is valid. 
 */
function isValidGender(gender) {
  return gender === "male" || gender === "female";
}
/**
 * checks if the given password is a string, has at least 8 characters, and contains only ASCII characters.
 * It also checks if the password contains at least one digit and one uppercase letter.
 * @param {*} password  - The password string to validate.
 * @returns  {boolean} True if the password is valid.
 */
function isValidPassword(password) {
    if (typeof password !== 'string' || password.length < 8) return false;

    //check if password contains only ASCII characters
    for (let i = 0; i < password.length; i++) {
      if (password.charCodeAt(i) > 127) {
        return false; //non-ASCII character found
      }
    }
    //check if password contains at least one digit, one uppercase letter
    const hasDigit = /[0-9]/.test(password);
    const hasUpperCase = /[A-Z]/.test(password);
    if (!hasDigit || !hasUpperCase) {
      return false; // password does not meet complexity requirements
    }

  return true; // good password
}

/**
 * Checks if the given email is a string and ends with "@gmail.com".
 * @param {string} email - The email string to validate.
 * @returns {boolean} True if the email is a valid Gmail address.
 */
function isValidGmail(email) {
  return typeof email === 'string' && email.endsWith('@gmail.com');
}
module.exports = {
  isValidGmail,
  isValidGender,
  isValidPassword,
  isValidDateFormat,
  isPastDate,
  isAgeOver13
};