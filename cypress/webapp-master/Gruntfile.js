module.exports = function(grunt) {
  require('jit-grunt')(grunt);

  grunt.initConfig({
    less: {
      development: {
        options: {
          compress: false,
          yuicompress: false,
          optimization: 2
        },
        files: {
          "web-app/less/bootstrap3/matchi.css": "web-app/less/bootstrap3/matchi.less",
          "web-app/less/bootstrap3/admin/blackadmin.css": "web-app/less/bootstrap3/admin/blackadmin.less",
          "web-app/less/admin.css": "web-app/less/admin.less"
        }
      }
    },
    watch: {
      styles: {
        files: ['web-app/**/*.less'], // which files to watch
        tasks: ['less'],
        options: {
          nospawn: true
        }
      }
    }
  });

  grunt.registerTask('default', ['less', 'watch']);
};