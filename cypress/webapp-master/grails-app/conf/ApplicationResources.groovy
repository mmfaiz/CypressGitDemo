modules = {

    // Start Bootstrap3
    // --------------------------------

    b3core {
        dependsOn( 'b3scripts, b3styleDependencies, b3styles')
    }

    b3adminCore {
        dependsOn('b3adminStyles, b3styleDependencies, b3scripts')
    }

    b3scripts {
        dependsOn('jquery, jquery-ui, jquery-ui-i18n, bootstrap-select, placeholderjs, application, matchi')
        resource url:'/js/underscore-min.js'
        resource url:'/js/flowtype.js'
        resource url:'/js/checkbox-radio-switch.js'
        resource url:'/bootstrap3/js/bootstrap.min.js'
        resource url:'/bootstrap3/jasny-bootstrap/jasny-bootstrap.min.js'
        resource url: 'https://kit.fontawesome.com/24073a19d4.js'
    }

    b3styles {
        dependsOn('animated')
        resource url:'/less/bootstrap3/matchi.css'
    }

    b3adminStyles {
        resource url:'/less/bootstrap3/admin/blackadmin.css'
    }

    b3styleDependencies {
        resource url:'/fonts/matchi-sports/css/matchi-sports.min.css'
        resource url:'/fonts/themify/themify-icons.css'
        resource url:'/bootstrap3/jasny-bootstrap/jasny-bootstrap.min.css'
    }

    // End Bootstrap3
    // --------------------------------

    core {
        dependsOn('jquery, jquery-ui, jquery-ui-i18n, application, matchi, bootstrap, bootstrap-scrollmodal')
        dependsOn('styles')
    }

    boostrapmin {
        dependsOn('bootstrap')
    }

    coreadmin {
        dependsOn('jquery, jquery-ui, jquery-ui-i18n, application, matchi, bootstrap, bootstrap-scrollmodal, jquery-rowlink')
        dependsOn('adminstyles')
        resource url:'/js/bootstrap/bootstrap-table.js'
    }

    styles {
        resource url:'/less/styles.css'
        resource url:'/css/bootstrap-scroll-modal.css'
        resource url:'/css/jasny-bootstrap-responsive.min.css'
        resource url:'/fonts/matchi-sports/css/matchi-sports.min.css'
        resource url: 'https://kit.fontawesome.com/24073a19d4.js'
    }

    adminstyles {
        resource url:'/less/admin.css'
        resource url:'/css/bootstrap-scroll-modal.css'
        resource url:'/css/jasny-bootstrap.min.css'
        resource url: 'https://kit.fontawesome.com/24073a19d4.js'
        resource url:'/css/print.css',attrs:[media: 'print']

        overrides {
            'jquery-theme' {
                resource id: 'theme', url: '/css/themes/bootstrap/jquery-ui-1.8.16.custom.css'
            }
        }
    }

    'animated' {
        resource url:'/css/animate/animate.min.css'
    }

    'application' {
        resource url:'/js/application.js'
    }

    'bootstrap' {
        resource url:'/js/bootstrap/bootstrap.min.js'
    }

    'bootstrap-responsive' {
        resource url:'/bootstrap/css/bootstrap-responsive.min.css'
    }

    'bootstrap-wysihtml5' {
        resource url:'/js/bootstrap/bootstrap-wysihtml5/wysihtml5-0.3.0.min.js'
        resource url:'/js/bootstrap/bootstrap-wysihtml5/bootstrap-wysihtml5-0.0.2.min.js'
        resource url:'/js/bootstrap/bootstrap-wysihtml5/custom.js'
        resource url:'/js/bootstrap/bootstrap-wysihtml5/bootstrap-wysihtml5-0.0.2.css'
    }

    'bootstrap3-wysiwyg' {
        resource url:'/js/bootstrap/bootstrap3-wysiwyg/bootstrap3-wysihtml5.all.min.js'
        resource url:'/js/bootstrap/bootstrap3-wysiwyg/bootstrap3-wysihtml5.min.css'
    }

    'bootstrap-slider' {
        resource url:'https://cdnjs.cloudflare.com/ajax/libs/bootstrap-slider/10.2.1/bootstrap-slider.min.js'
        resource url:'https://cdnjs.cloudflare.com/ajax/libs/bootstrap-slider/10.2.1/css/bootstrap-slider.min.css'
    }
    'bootstrap-datepicker' {
        resource url:'/js/bootstrap/bootstrap-datepicker/bootstrap-datepicker.js'
        resource url:'/js/bootstrap/bootstrap-datepicker/locales/bootstrap-datepicker.es.js'
        resource url:'/js/bootstrap/bootstrap-datepicker/locales/bootstrap-datepicker.no.js'
        resource url:'/js/bootstrap/bootstrap-datepicker/locales/bootstrap-datepicker.sv.js'
    }
    'bootstrap-select' {
        resource url:'/bootstrap3/bootstrap-select/bootstrap-select.min.js'
    }

    'bootstrap-typeahead' {
        resource url: '/bootstrap3/bootstrap3-typeahead/bootstrap3-typeahead.min.js'
    }

    'bootstrap-progressbar' {
        resource url:'/bootstrap3/bootstrap-progressbar/bootstrap-progressbar.min.js'
        resource url:'/bootstrap3/bootstrap-progressbar/bootstrap-progressbar.min.css'
    }

    'bootstrap-scrollmodal' {
        resource url:'/js/bootstrap/bootstrap-scroll-modal.js'
    }

    'bootstrap-switch' {
        resource url: '/js/bootstrap/bootstrap-switch/bootstrap-switch.min.js'
        resource url: '/js/bootstrap/bootstrap-switch/bootstrap-switch.min.css'
    }

    'bootstrap-timepicker' {
        resource url:'/bootstrap3/bootstrap3-timepicker/bootstrap-timepicker.min.js'
        resource url:'/bootstrap3/bootstrap3-timepicker/bootstrap-timepicker.min.css'
    }

    'bootstrap-tagsinput' {
        resource url:'/bootstrap3/bootstrap-tagsinput/bootstrap-tagsinput.min.js'
        resource url:'/bootstrap3/bootstrap-tagsinput/bootstrap-tagsinput.min.css'
    }

    'jquery-easing' {
        resource url:'/js/jquery.easing.1.3.js'
    }

    'jquery-hover' {
        resource url:'/js/jquery.hoverIntent.minified.js'
    }

    'jquery-ui-i18n' {
        resource url:'/js/moment-with-locales.js'
        resource url:[plugin: 'jqueryUi', dir:'js/jquery/i18n', file:"jquery-ui-i18n.js"], nominify: true, disposition: 'head'
    }

    'jquery-multiselect' {
        resource url:'/js/jquery.multiSelect-1.2.2/jquery.multiSelect.js'
        resource url:'/js/jquery.multiSelect-1.2.2/jquery.multiSelect.css'
    }

    'jquery-multiselect-widget' {
        resource url:'/js/jquery-ui-multiselect-widget-1.13/jquery.multiselect.min.js'
        resource url:'/js/jquery-ui-multiselect-widget-1.13/jquery.multiselect.css'
    }

    'jquery-chosen' {
        resource url:'/js/jquery.chosen/chosen.jquery.js'
        resource url:'/js/jquery.ajax.chosen/ajax-chosen.js'
        resource url:'/js/jquery.chosen/chosen.css'
        resource url:'/css/matchi-chosen.css'
    }

    'jquery-timepicker' {
        dependsOn 'matchi-timepicker'
        resource url: '/js/moment-with-locales.js'
        resource url: '/js/jquery.timepicker/jquery.ui.timepicker.js'
        resource url: '/js/jquery.timepicker/jquery.ui.timepicker.css'
    }

    'jquery-PrintArea' {
        resource url: "/js/jquery.PrintArea.js"
    }
    'jquery-periodicalUpdater' {
        resource url: "/js/JQuery-PeriodicalUpdater/jquery.periodicalupdater.js"
    }

    'jquery-rowlink' {
        resource url:'/js/bootstrap/jasny-bootstrap/bootstrap-rowlink.js'
    }

    'jquery-floatThead' {
        resource url:'/js/jquery.floatThead.min.js'
    }

    'jasny-fileinput' {
        resource url:'/js/bootstrap/jasny-bootstrap/bootstrap-fileupload.js'
        resource url:'/css/jasny-bootstrap.min.css'
    }

    'jquery-fastLiveFilter' {
        resource url:'/js/jquery.fastLiveFilter.js'
    }

    'jquery-slimscroll' {
        resource url:'/js/jQuery-slimScroll-1.3.0/jquery.slimscroll.min.js'
    }

    'jquery-sortable' {
        resource url:'/js/jquery-sortable.js'
    }

    'raty-fa' {
        resource url: "/js/jquery.raty-fa.js"
    }

    'highcharts' {
        resource url: "/js/Highcharts-2.3.3/highcharts.js"
        resource url: "/js/Highcharts-2.3.3/modules/exporting.js"
    }

    'highcharts3' {
        resource url: "/js/Highcharts-3.0.9/js/highcharts.js"
        resource url: "/js/Highcharts-3.0.9/js/modules/exporting.js"
    }

    'chartist' {
        resource url:'/js/chartist-js/chartist.min.js'
        resource url:'/js/chartist-js/chartist-plugin-pointlabels.min.js'
        resource url:'/js/chartist-js/chartist.min.css'
    }

    'jstorage' {
        resource url: "/js/jquery.json-2.4.min.js"
        resource url: "/js/jstorage.js"
    }

    'datejs' {
        resource url: '/js/date.js'
    }

    'daterangepicker' {
        dependsOn('datejs')
        resource url: '/js/moment-with-locales.js'
        resource url: '/js/daterangepicker.js'
        resource url: '/css/daterangepicker.css'
    }

    'mousetrap' {
        resource url: '/js/mousetrap/mousetrap.min.js'
        resource url: '/js/mousetrap/mousetrap-global.min.js'
    }

    'select2' {
        resource url: '/js/select2-3.5.2/select2.min.js'
        resource url: '/js/select2-3.5.2/select2.css'
        resource url: '/js/select2-3.5.2/select2-bootstrap.css'
    }

    'readmore' {
        resource url:'/js/readmore.min.js'
    }

    'waypoints' {
        resource url:'/js/jquery.waypoints.min.js'
    }

    'leaflet-open-maps' {
        resource url: 'https://unpkg.com/leaflet@1.4.0/dist/leaflet.css'
        resource url: 'https://unpkg.com/leaflet.markercluster@1.4.1/dist/MarkerCluster.Default.css'
        resource url: 'https://unpkg.com/leaflet.markercluster@1.4.1/dist/MarkerCluster.css'

        resource url: 'https://unpkg.com/leaflet@1.4.0/dist/leaflet.js'
        resource url: 'https://unpkg.com/leaflet.markercluster@1.4.1/dist/leaflet.markercluster.js'
    }

    'zero-clipboard' {
        resource url: '/js/zeroclipboard-1.3.5/ZeroClipboard.min.js'
    }

    'matchi' {
        resource url:'/js/matchi.js'
    }

    'matchi-blockbooking' {
        resource url: '/js/matchi.blockbooking.js'
    }

    'matchi-user-blockbooking' {
        resource url: '/js/matchi.user.blockbooking.js'
    }

    'matchi-schedule' {
        resource url: '/js/matchi.schedule.js'
    }

    'matchi-timepicker' {
        resource url: '/js/matchi.timepicker.js'
    }

    'matchi-userselect' {
        dependsOn('select2')
        resource url: '/js/matchi.userselect.js'
    }

    'matchi-invoice' {
        dependsOn('datejs')
        resource url: '/js/matchi.invoice.js'
    }

    'matchi-customerselect' {
        dependsOn('select2')
        resource url: '/js/matchi.customerselect.js'
    }

    'matchi-selectall' {
        resource url: '/js/matchi.selectall.js'
    }

    'matchi-truncate' {
        resource url: '/js/matchi.truncateText.js'
    }

    'matchi-selectpicker' {
        dependsOn('bootstrap-select')
        resource url: '/js/matchi.selectpicker.js'
    }

    'datatables' {
        resource url: '/js/datatables/js/jquery.dataTables.min.js'
        resource url: '/js/datatables/js/dataTables.bootstrap.min.js'
        resource url: '/js/datatables/css/dataTables.bootstrap.css'
    }

    'placeholderjs' {
        dependsOn('jquery')
        resource url: '/js/placeholders.jquery.min.js'
    }

    'pick-a-color' {
        resource url:'/bootstrap3/pick-a-color/pick-a-color-1.2.3.min.js'
        resource url:'/bootstrap3/pick-a-color/tinycolor-0.9.15.min.js'
        resource url:'/bootstrap3/pick-a-color/pick-a-color-1.2.3.min.css'
    }
    'animateNumber' {
        dependsOn('jquery')
        resource url: '/js/jquery.animateNumber.min.js'
    }

    'tinySort' {
        resource url: '/js/tinysort.min.js'
    }

    'jquery-payment' {
        resource url: '/js/jquery.payment.min.js'
    }

    translatable {
        dependsOn('jquery')
        resource url: '/js/translatable.js'
    }

    'jquery-fullscreen' {
        dependsOn('jquery')
        resource url: '/js/jquery.fullscreen.js'
    }

    'matchi-watch' {
        dependsOn('jquery, matchi')
        resource url: '/js/matchi.watch.js'
    }

    'video-player' {
        resource url:'https://cdn.jsdelivr.net/npm/mediaelement@4.2.16/build/mediaelement-and-player.min.js'
        resource url:'https://cdn.jsdelivr.net/npm/mediaelement@4.2.16/build/mediaelementplayer.min.css'
    }
}