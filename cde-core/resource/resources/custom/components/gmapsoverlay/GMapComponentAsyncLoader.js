/*
This Source Code Form is subject to the 
terms of the Mozilla Public License, v. 2.0. 
If a copy of the MPL was not distributed 
with this file, You can obtain one at 
http://mozilla.org/MPL/2.0/.
*/
var loadGoogleMapsOverlay = (function($) {

	var now = $.now(), promise;

	return function() {

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
		callbackName = "loadGoogleMapsOverlay_" + (now++),
		
		//Ajax URL params
		params;

		//If google.maps exists, then Google Maps API was probably loaded with the <script> tag
		if (window.google && google.maps) {

			resolve();

		//If the google.load method exists, lets load the Google Maps API in Async.
		} else if (window.google && google.load) {

			google.load("maps", "3.exp", {
				"other_params": "sensor=false&libraries=places",
				"callback": resolve
			});

		//Last, try pure jQuery Ajax technique to load the Google Maps API in Async.
		} else {

			//Ajax URL params
			/*
			params = $.extend({
				'v': '3.exp',
				'sensor': false,
				'libraries': 'places',
				'callback': callbackName
				
			}: {});
			*/
			
			params = {
				'v': '3.exp',
				'sensor': false,
				'libraries': 'places',
				'callback': callbackName
			};
			
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