const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const HOST = '0.0.0.0';
const PUBLIC_DIR = path.join(__dirname, 'web');
const APK_PATH = path.join(__dirname, 'InterestCalculator-release.apk');

const MIME_TYPES = {
    '.html': 'text/html',
    '.css': 'text/css',
    '.js': 'text/javascript',
    '.json': 'application/json',
    '.png': 'image/png',
    '.apk': 'application/vnd.android.package-archive'
};

const server = http.createServer((req, res) => {
    let reqUrl = req.url.split('?')[0];

    // Download APK route
    if (reqUrl === '/download-apk' || reqUrl === '/InterestCalculator-release.apk') {
        if (fs.existsSync(APK_PATH)) {
            const stat = fs.statSync(APK_PATH);
            res.writeHead(200, {
                'Content-Type': 'application/vnd.android.package-archive',
                'Content-Length': stat.size,
                'Content-Disposition': 'attachment; filename="InterestCalculator-release.apk"'
            });
            const stream = fs.createReadStream(APK_PATH);
            return stream.pipe(res);
        } else {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            return res.end('APK file not found.');
        }
    }

    if (reqUrl === '/') reqUrl = '/index.html';
    const filePath = path.join(PUBLIC_DIR, reqUrl);

    if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
        const ext = path.extname(filePath).toLowerCase();
        const contentType = MIME_TYPES[ext] || 'application/octet-stream';
        res.writeHead(200, { 'Content-Type': contentType });
        fs.createReadStream(filePath).pipe(res);
    } else {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('404 Not Found');
    }
});

server.listen(PORT, HOST, () => {
    console.log('====================================================');
    console.log(` Interest Calculator Web & APK Server is running!`);
    console.log(` > Local:    http://localhost:${PORT}`);
    console.log(` > Network:  http://10.28.95.33:${PORT}`);
    console.log(` > APK link: http://localhost:${PORT}/download-apk`);
    console.log('====================================================');
});

// Keep process active
setInterval(() => {}, 60000);
