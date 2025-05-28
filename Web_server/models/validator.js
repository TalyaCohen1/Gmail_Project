

function isValidDate(dateStr) {
  const date = new Date(dateStr);
  return !isNaN(date) && /^\d{4}-\d{2}-\d{2}$/.test(dateStr);
}

function isValidGender(gender) {
  return gender === "male" || gender === "female";
}

function isValidGmail(email) {
  return typeof email === 'string' && email.endsWith('@gmail.com');
}
module.exports = {
  isValidGmail,
  isValidDate,
  isValidGender
};