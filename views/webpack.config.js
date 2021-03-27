const path = require('path');
module.exports = {
  // entry: './src/assets/pro/index.ts',
  // entry: {index:'./ts_js/index.js',spot_detail:'./ts_js/spot_detail.js'},
  entry: {
     index: './ts/index.ts', 
    },
  mode: "development",
  devtool: 'inline-source-map',
  // devtool: false,
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules/
      }
    ]
  },
  resolve: {
    extensions: ['.ts', '.tsx', '.js', 'json']
  },
  output: {
    filename: '[name].js',
    path: path.resolve(__dirname, 'js')
  }
};