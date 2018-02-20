/*!
* JavaScript - loadGoogleMaps( version, apiKey, language )
*
* - Load Google Maps API using jQuery Deferred.
* Useful if you want to only load the Google Maps API on-demand.
* - Requires jQuery 1.5
*
* Copyright (c) 2011 Glenn Baker
* Dual licensed under the MIT and GPL licenses.
*
* The MIT License (MIT)
* Copyright (c) 2011 Glenn Baker
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:

* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
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