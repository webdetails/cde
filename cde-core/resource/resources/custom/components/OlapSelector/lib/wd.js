/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. 
 * 
 */

/** 
 * Define Array map function
 * @global
 * @param {function} func - function to register
 * @param {Object} this
 *
 */
if (!Array.prototype.map)
{
  Array.prototype.map = function(fun /*, thisp*/)
  {
    var len = this.length;
    if (typeof fun != "function")
      throw new TypeError();

    var res = new Array(len);
    var thisp = arguments[1];
    for (var i = 0; i < len; i++)
    {
      if (i in this)
        res[i] = fun.call(thisp, this[i], i, this);
    }

    return res;
  };
}

/** 
 * Define Array indexOf function. Taken from MDN.
 * @global
 * @param {Object} searchElement - element to find
 * @param {int} fromIndex
 *
 */

if (!Array.prototype.indexOf) {  
    Array.prototype.indexOf = function (searchElement /*, fromIndex */ ) {  
        "use strict";  
        if (this == null) {  
            throw new TypeError();  
        }  
        var t = Object(this);  
        var len = t.length >>> 0;  
        if (len === 0) {  
            return -1;  
        }  
        var n = 0;  
        if (arguments.length > 0) {  
            n = Number(arguments[1]);  
            if (n != n) { // shortcut for verifying if it's NaN  
                n = 0;  
            } else if (n != 0 && n != Infinity && n != -Infinity) {  
                n = (n > 0 || -1) * Math.floor(Math.abs(n));  
            }  
        }  
        if (n >= len) {  
            return -1;  
        }  
        var k = n >= 0 ? n : Math.max(len - Math.abs(n), 0);  
        for (; k < len; k++) {  
            if (k in t && t[k] === searchElement) {  
                return k;  
            }  
        }  
        return -1;  
    }  
}




/** 
 * Utility function to add a method to a function's prototype
 * @global
 * @param {string} name - method name
 * @param {function} func - function to register
 *
 */

Function.prototype.method = Function.prototype.method || function(name, func) {
    this.prototype[name] = func;
    return this;
};


/**
 * Webdetails namespace
 * @namespace
 */
var wd = wd || {};


/**
 * The logging priority order
 * @const
 * @type Array
 */

wd.loglevels = ['debug', 'info', 'warn', 'error', 'exception'];

/**
 * Defines the threshold level for logging.
 * @member
 */

wd.loglevel = 'debug';

/**
 * 
 * Logging function. Use this to append messages to the console with the appropriate
 * log level. Logging will only occur if the log level is above the defined threshold
 * Should be used instead of console.log
 * @param {string} m - message
 * @param {string} type - Log type: 'info','debug', 'log', 'warn', 'error', 'exception'
 * @see wd.loglevel
 */

wd.log = function (m, type){
    
    type = type || "info";
    if (wd.loglevels.indexOf(type) < wd.loglevels.indexOf(wd.loglevel)) {
        return;
    }
    if (typeof console !== "undefined" ){
        
        if (type && console[type]) {
            console[type]("["+ type +"] WD: " + m);
        }else if (type === 'exception' &&
            !console.exception) {
            console.error("["+ type +"] WD: "  + (m.stack || m));
        }
        else {
            console.log("WD: " + m);
        }
    }
   
}


/**
 * Shortcut to wd.log(m,"warn");
 * @param {string} m - message
 */

wd.warn = function(m){
    return wd.log(m, "warn");
}


/**
 * Shortcut to wd.log(m,"error");
 * @param {string} m - message
 */
wd.error = function(m){
    return wd.log(m, "error");
}


/**
 * Shortcut to wd.log(m,"info");
 * @param {string} m - message
 */
wd.info = function(m){
    return wd.log(m, "info");
}



/**
 * Shortcut to wd.log(m,"debug");
 * @param {string} m - message
 */

wd.debug = function(m){
    return wd.log(m, "debug");
}
