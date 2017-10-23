var Webpack = require('webpack');
var path = require('path');
var nodeModulesPath = path.resolve(__dirname, 'node_modules');
var buildPath = path.resolve(__dirname, 'public', 'build');
var appPath = path.resolve(__dirname, 'src');


var config = {
    context: __dirname,
    entry: [ 'babel-polyfill',

    	// Our application entry point
    	path.resolve(appPath, 'index.js')],

    resolve: {
    		modules: [__dirname, 'node_modules'],
        alias: {
            'semantic-ui-less': path.resolve(__dirname, 'src/Interface/Ui/Styles/semantic-ui-less'),
            'src': path.resolve(__dirname, 'src')
        }
    },
    output: {
        path: buildPath,
        filename: '[name].js',
        publicPath: 'build/'
    },
    module: {
        noParse: function(content) {
                                      return /jquery|lodash/.test(content);
                                   },
        rules: [

            // babel-loader gives you ES6/7 syntax and JSX transpiling
            {
                test: /\.js$/,
                loader: "babel-loader",
                exclude: [nodeModulesPath],
                options: {
                    cacheDirectory: true,
                    presets: ["react", "es2015", "stage-0"]
                }
            },

            // Let us also add the style-loader and css-loader
            {
                test: /\.css$/,
                use:[
                		"style-loader",
                		"stripcomment-loader",
                		"css-loader"
                ]
            },
            // As well as the style-loader and less-loader
            {
                test: /\.less$/,
                use:[
	            		"style-loader",
	            		"stripcomment-loader",
	            		"css-loader",
	            		"less-loader"
            		]
            }, {
                test: /\.(png|gif|jpg|svg|woff|woff2|eot|ttf)$/,
                use: [
	                    {
	                      loader: 'url-loader',
	                      options: {
	                        limit: 25000
	                      }
	                    }
	                 ]
            },
            {
                test: /\.(png|jpg|gif)$/,
                use: [
                  {
                    loader: 'file-loader',
                    options: {}
                  }
                ]
              }
        ]
    },
    plugins: [
        (new Webpack.optimize.UglifyJsPlugin({
        	    parallel: true,
        	    compressor: {
                warnings: false,
                screw_ie8: true,
                drop_console: true
        	    },
        	    output: {
                comments: false
        	    }
        })),
        new Webpack.optimize.CommonsChunkPlugin('common'),
        new Webpack.LoaderOptionsPlugin({
            debug: true
          })
    ]
};

module.exports = config;
