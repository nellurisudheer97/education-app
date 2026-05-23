const fs = require('fs');
const http = require('http');
const path = require('path');

const buildDir = path.join(__dirname, '..', 'build');
const port = Number(process.env.PORT || 3000);

const contentTypes = {
  '.css': 'text/css',
  '.gif': 'image/gif',
  '.html': 'text/html',
  '.ico': 'image/x-icon',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.js': 'text/javascript',
  '.json': 'application/json',
  '.png': 'image/png',
  '.svg': 'image/svg+xml',
  '.txt': 'text/plain',
  '.webmanifest': 'application/manifest+json'
};

const sendFile = (response, filePath) => {
  fs.readFile(filePath, (error, content) => {
    if (error) {
      response.writeHead(500);
      response.end('Unable to read static asset.');
      return;
    }

    response.writeHead(200, {
      'Content-Type': contentTypes[path.extname(filePath)] || 'application/octet-stream'
    });
    response.end(content);
  });
};

const server = http.createServer((request, response) => {
  const urlPath = decodeURIComponent(request.url.split('?')[0]);
  const requestedPath = path.normalize(path.join(buildDir, urlPath));

  if (!requestedPath.startsWith(buildDir)) {
    response.writeHead(403);
    response.end('Forbidden');
    return;
  }

  fs.stat(requestedPath, (error, stats) => {
    if (!error && stats.isFile()) {
      sendFile(response, requestedPath);
      return;
    }

    sendFile(response, path.join(buildDir, 'index.html'));
  });
});

server.listen(port, '0.0.0.0', () => {
  console.log(`Frontend listening on port ${port}`);
});
