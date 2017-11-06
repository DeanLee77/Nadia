/*
 all contents are from MyClaim file, and modified on 3rd of Aug 2017
 in accordance with contents from following url;
https://objectpartners.com/2016/04/22/using-webpack-with-gradle/
In addition, some lines are changed in accordance with migrating from V1 to V2
Reworked from:
  http://jamesknelson.com/webpack-made-simple-build-es6-less-with-autorefresh-in-26-lines/

  Original Boilerplate taken from Christian Alfoni blog:

  http://www.christianalfoni.com/articles/2015_04_19_The-ultimate-webpack-setup
  http://www.christianalfoni.com/articles/2014_12_13_Webpack-and-react-is-awesome
  https://christianalfoni.github.io/react-webpack-cookbook/

  Git:
  https://github.com/christianalfoni/webpack-express-boilerplate

  TODO
    * Work out how react-hot loader works, and how to set it up properly.


*/

var Webpack = require('webpack');
var path = require('path');
var appPath = path.resolve(__dirname, 'src');
var nodeModulesPath = path.resolve(__dirname, 'node_modules');
var buildPath = path.resolve(__dirname, 'public', 'build');

var config = {
  // addVendor: function (name, path) {
	// this.resolve.alias[name] = path;
	// this.module.noParse.push(new RegExp(path));
  // },
  context: __dirname,

  // Makes sure errors in console map to the correct file and line number
  devtool: 'eval-source-map',

  entry:  {
		      'bundle': [
				          'webpack-hot-middleware/client?reload=true',
				          'babel-polyfill',
				          path.resolve(appPath, 'shim.js'),
				          path.resolve(appPath, 'main.js')
			            ]
		  },
  resolve: {
  				modules: [path.resolve(__dirname), nodeModulesPath],
  				alias: {'src': path.resolve( __dirname, 'src')}
           },

  output: {
	// We need to give Webpack a path. It does not actually need it,
    // because files are kept in memory in webpack-dev-server, but an
    // error will occur if nothing is specified. We use the buildPath
    // as that points to where the files will eventually be bundled
    // in production
    path: buildPath,
    filename: '[name].js',

	// Everything related to Webpack should go through a build path,
    // localhost:3000/build. That makes proxying easier to handle
    publicPath: '/build/'
  },
  module: {
           
            	rules: [

            	           // I highly recommend using the babel-loader as it gives you
                         // ES6/7 syntax and JSX transpiling out of the box
                        	{
                            test: /\.js$/,
                            loader: "babel-loader",
                            exclude: [],
                            options: {
                                        cacheDirectory: true,
                                        presets: ["react", "es2015", "stage-0"]
                                      }
                          },

                        	// Let us also add the style-loader and css-loader, which you can
                          // expand with less-loader etc.
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
                          },
                          {
                              test: /\.scss$/,
                              use: [
                                      "style-loader",
                                      "css-loader",
                                      "sass-loader"
                                   ]
                          },
                        	 {
                            test: /\.(png|jpg|svg|woff|woff2|eot|ttf)$/,
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
                          },
                      ]
            },


            devServer: {
                host: "localhost",
                port: 3000,
                https: false
              },
              
  // We have to manually add the Hot Replacement plugin when running from Node
  plugins: [
    new Webpack.HotModuleReplacementPlugin(),
  ]
};

//config.addVendor('react', nodeModulesPath + '/react/dist/react.js');

module.exports = config;
