
/**
 * Validates if a given string is a properly formatted date (YYYY-MM-DD)
 * @param {*} dateStr - The date string to validate 
 * @returns if the date string is valid
 */
function isValidDate(dateStr) {
  const date = new Date(dateStr);
  return !isNaN(date) && /^\d{4}-\d{2}-\d{2}$/.test(dateStr);
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
 * Checks if the given email is a string and ends with "@gmail.com".
 * @param {string} email - The email string to validate.
 * @returns {boolean} True if the email is a valid Gmail address.
 */
function isValidGmail(email) {
  return typeof email === 'string' && email.endsWith('@gmail.com');
}
module.exports = {
  isValidGmail,
  isValidDate,
  isValidGender
};