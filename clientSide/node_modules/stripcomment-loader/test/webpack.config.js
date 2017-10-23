module.exports = {
    entry: "./test.js",
    output: {
        path: __dirname,
        filename: "bundle.js"
    },
    module: {
        preLoaders: [
            {
                test: /\.js$/,
                loader: require.resolve("../")+"?-line&-block"
            }
        ]
    }
}
