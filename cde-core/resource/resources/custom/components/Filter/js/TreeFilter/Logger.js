'use strict';

/**
 * @module TreeFilter
 */

/**
 * Mixin that provides console logging abilities
 * @class Logger
 * @static
 * @extensionfor AbstractSelect
 * @extensionfor Models.Tree
 * @extensionfor Views.Abstract
 * @main
 */
(function(TreeFilter) {
  return TreeFilter.Logger = {

    /**
     * @property logLevel
     * @type {Integer}
     * @default 0
     */
    logLevel: 0,

    /**
     * @property ID
     * @type {String}
     * @default "TreeFilter.{{Namespace}}.{{Class}}"
     */
    ID: '',

    /**
     * Outputs a message to the console (using console.log), if the logLevel is right
     * @method log
     * @param {String} message
     * @chainable
     */
    log: function(msg) {
      if (this.logLevel >= 1) {
        if (typeof console !== "undefined" && console !== null) {
          if (typeof console.log === "function") {
            console.log(this.ID != null ? this.ID + " : " + msg : msg);
          }
        }
      }
      return this;
    },

    /**
     * Outputs a debugg message to the console (using console.debug), if the logLevel is right
     * @method debug
     * @param {String} message
     * @chainable
     */
    debug: function(msg) {
      if (this.logLevel >= 2) {
        if (typeof console !== "undefined" && console !== null) {
          if (typeof console.debug === "function") {
            console.debug(this.ID != null ? this.ID + " : " + msg : msg);
          }
        }
      }
      return this;
    },

    /**
     * Outputs a warning message to the console (using console.warn), if the logLevel is right
     * @method warn
     * @param {String} message
     * @chainable
     */
    warn: function(msg) {
      if (typeof console !== "undefined" && console !== null) {
        if (typeof console.warn === "function") {
          console.warn(this.ID != null ? this.ID + " : " + msg : msg);
        }
      }
      return this;
    },

    /**
     * Outputs an informative message to the console (using console.info)
     * @method info
     * @chainable
     * @param {String} message
     */
    info: function(msg) {
      if (typeof console !== "undefined" && console !== null) {
        if (typeof console.info === "function") {
          console.info(this.ID != null ? this.ID + " : " + msg : msg);
        }
      }
      return this;
    },

    /**
     * Outputs an error message to the console (using console.error)
     * @method error
     * @param {String} message
     * @chainable
     */
    error: function(msg) {
      if (typeof console !== "undefined" && console !== null) {
        if (typeof console.error === "function") {
          console.error(this.ID != null ? this.ID + " : " + msg : msg);
        }
      }
      return this;
    }
  };
})(TreeFilter);
