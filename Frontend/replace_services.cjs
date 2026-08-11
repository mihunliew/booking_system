const fs = require('fs');
const path = require('path');

function walk(dir, callback) {
  fs.readdirSync(dir).forEach(f => {
    let dirPath = path.join(dir, f);
    let isDirectory = fs.statSync(dirPath).isDirectory();
    isDirectory ? walk(dirPath, callback) : callback(path.join(dir, f));
  });
}

walk('c:/N2N/Frontend/src', function(filePath) {
  if (filePath.endsWith('.vue')) {
    let content = fs.readFileSync(filePath, 'utf8');
    let original = content;
    content = content.replace(/\bauthService\b/g, 'AuthApi');
    content = content.replace(/\bbookingService\b/g, 'BookingApi');
    content = content.replace(/\badminService\b/g, 'AdminApi');
    content = content.replace(/\bproductService\b/g, 'ProductApi');
    content = content.replace(/\bcartService\b/g, 'CartApi');
    if (content !== original) {
      fs.writeFileSync(filePath, content, 'utf8');
      console.log('Updated: ' + filePath);
    }
  }
});
