const multer = require('multer');

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/'); // Specify the directory to save uploaded files
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    const extension = file.originalname.split('.').pop(); // Get the file extension
    cb(null, `${file.fieldname}-${uniqueSuffix}.${extension}`); // Use fieldname and unique suffix for the filename
  }
});
const upload = multer({ storage });
module.exports = upload;