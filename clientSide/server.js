const path = require('path');
const express = require('express');
const proxy = require('express-http-proxy');
const webpack = require('webpack');
const webpackMiddleware = require('webpack-dev-middleware');
const webpackHotMiddleware = require('webpack-hot-middleware');
const config = require('./webpack.config.js');

const Agent = require('agentkeepalive');
const isDeveloping = process.env.NODE_ENV !== 'production';
const port = isDeveloping? 3000: process.env.PORT;

// This is a Node.js Express App
var app = express();

if (isDeveloping)
{
    const compiler = webpack(config);
    const middleware = webpackMiddleware(compiler, {
        publicPath: config.output.publicPath,
        contentBase: 'src',
        stats: {
            colors: true,
            hash: false,
            timings: true,
            chunks: false,
            chunkModules: false,
            modules: false
        }
    });

    app.use(middleware);
    app.use(webpackHotMiddleware(compiler));
    
//    app.use('/service', proxy('localhost:3030', {
//
//      // This is needed to stop the proxy from corrupting byte streams particularly on upload.
//      reqBodyEncoding: null,
//
//      forwardPath: function(req, res) {
//          return '/service' + req.path;
//      },
//      intercept: function(responseIn, data, req, responseOut, callback) {
//          let key = 'WWW-Authenticate';
//          responseOut.set(key, responseOut.get(key) ? responseOut.get(key).split(',') : []);
//
//          callback(null, data);
//      }
//  }));
    
  app.use('/', express.static('public/'));

} else {
  app.use('/', express.static('public/'));
}


app.listen(port, '0.0.0.0', function onStart(err) {
    if (err) {
        console.log(err);
    }
    console.info('==> 🌎 Listening on port %s. Open up http://0.0.0.0:%s/ in your browser.', port, port);
});
