var loadGoogleMaps = (function($) {

    var now = $.now(),
        promise;

    return function(version, apiKey) {

        if (promise) {
            return promise;
        }

        //Create a Deferred Object
        var deferred = $.Deferred(),

            //Declare a resolve function, pass google.maps for the done functions
            resolve = function() {
                deferred.resolve(window.google && google.maps ? google.maps: false);

            },

            //global callback name
            callbackName = "loadGoogleMaps_" + (now++),

            //Ajax URL params
            params;

        //If google.maps exists, then Google Maps API was probably loaded with the <script> tag
        if (window.google && google.maps) {

            resolve();

            //If the google.load method exists, lets load the Google Maps API in Async.

        } else if (window.google && google.load) {

            google.load("maps", version || 3, {
                "other_params": "sensor=false",
                "callback": resolve
            });

            //Last, try pure jQuery Ajax technique to load the Google Maps API in Async.

        } else {

            //Ajax URL params
            params = $.extend({
                'v': version || 3,
                'sensor': false,
                'callback': callbackName

            },
                              apiKey ? {
                                  "key": apiKey
                              }: {});

            //Declare the global callback
            window[callbackName] = function() {

                resolve();

                //Delete callback
                setTimeout(function() {
                    try {
                        delete window[callbackName];

                    } catch(e) {}

                },
                           20);

            };

            //Can't use the jXHR promise because 'script' doesn't support 'callback=?'
            $.ajax({
                dataType: 'script',
                data: params,
                url: 'http://maps.googleapis.com/maps/api/js'

            });


        }

        promise = deferred.promise();

        return promise;

    };


} (jQuery));
