define(["cdf/lib/jquery"], function($) {
  /*
   Adapted by Webdetails
   */

  /*
   The MIT License (http://opensource.org/licenses/mit-license.php)

   Copyright (c) 2015 Gordon Freeman <eax@gmx.us>

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
   */

  var module_messages = {
    get: function(code, param) {
      var _messages = {
        0: "Unknown Error",
        1: "No Transport",
        2: param + " Method Not Allowed",
        3: param + " Scheme Not Supported",
        4: "URI source and target scheme must be the same",
        5: "No Data",
        6: "Bad Data: " + param,
        7: "Network Error",
        8: "Timeout"
      };

      return _messages[code in _messages ? code : 0];
    }
  };

  $.ajaxTransport("+*", function(opts, optsUser, xhr) {
    if (opts.crossDomain && (document.addEventListener || document.querySelector) && !window.atob && window.XDomainRequest) {
      var text = module_messages,
        xdr = new XDomainRequest(),
        method = opts.type.toUpperCase(),
        contentType = opts.contentType || optsUser.contentType,
        scheme = opts.url.substring(0, opts.url.indexOf(":")).toUpperCase(),
        uri = opts.url,
        data = optsUser.data || {},
        _error = function(code, param) {
          return {
            send: function(hdr, cb) {
              cb(-1, text.get(code, param));
            },
            abort: $.noop
          };
        };

      if (!xdr) {
        return _error(1);
      }
      if (!optsUser.forceMethod && $.inArray(method, ["GET", "POST"]) === -1) {
        return _error(2, method);
      }
      if ($.inArray(scheme, ["HTTP", "HTTPS"]) === -1) {
        return _error(3, scheme);
      }
      if (scheme !== location.protocol.substring(0, location.protocol.indexOf(":")).toUpperCase()) {
        return _error(4);
      }

      if (optsUser.forceMethod) {
        if (method === "HEAD") {
          method = "GET";
          uri += (opts.url.indexOf("?") === -1 ? "?" : "&") + "__ethod=" + method;
        }

        if ($.inArray(method, ["PUT", "DELETE", "PATCH"]) !== -1) {
          method = "POST";

          if ($.isPlainObject(data)) {
            data.__method = method;
          } else if (typeof data === "string") {
            data += (data.length ? "&" : "") + "__method=" + method;
          }
        }
      }

      if (optsUser.forceContentType) {
        if (method === "GET") {
          uri += (opts.url.indexOf("?") === -1 ? "?" : "&") + "__contentType=" + encodeURIComponent(contentType);
        }

        if (method === "POST") {
          if ($.isPlainObject(data)) {
            data.__contentType = contentType;
          } else if (typeof data === "string") {
            data += (data.length ? "&" : "") + $.param({
                __contentType: contentType
              });
          }
        }
      }

      if (opts.timeout) {
        xdr.timeout = opts.timeout;
      }

      xdr.onprogress = $.noop;

      return {
        send: function(hdr, cb) {
          xdr.onload = function() {
            var data = {},
              error = null;

            switch (opts.dataType) {
              case "json":
                try {
                  data.json = $.parseJSON(xdr.responseText);
                } catch (e) {
                  error = e.message;
                }
                break;
              case "xml":
                try {
                  data.xml = $.parseXML(xdr.responseText);
                } catch (e) {
                  error = e.message;
                }
                break;
              case "text":
                data.text = xdr.responseText;
                break;
              case "html":
                data.html = xdr.responseText;
                break;
            }

            if (error) {
              return cb(500, text.get(6, error));
            }

            var headers = ["Content-Type: " + xdr.contentType, "Content-Length: " + xdr.responseText.length];

            cb(200, "OK", data, headers.join("\r\n"));
          };

          xdr.onerror = function() {
            cb(500, text.get(7));
          };
          xdr.ontimeout = function() {
            cb(500, text.get(8));
          };

          if (optsUser.__test === true) {
            xhr.__method = method;
            xhr.__uri = uri;
          }

          xdr.open(method, uri);

          setTimeout(function() {
            xdr.send(method === "POST" ? typeof data === "string" ? data : $.isPlainObject(data) ? $.param(data) : null : null);
          }, 0);
        },
        abort: function() {
          xdr.abort();
        }
      };
    }
  });

  return $;
});
