/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

var Util = {
	 logger : new Logger("CDE.Util")
	};

Util.clone = function clone(obj) {

	var c = obj instanceof Array ? [] : {};

	for (var i in obj) if (obj.hasOwnProperty(i)) {
		var prop = obj[i];

		if (typeof prop == 'object') {
			if (prop instanceof Array) {
				c[i] = [];

				for (var j = 0; j < prop.length; j++) {
					if (typeof prop[j] != 'object') {
						c[i].push(prop[j]);
					} else {
						c[i].push(Util.clone(prop[j]));
					}
				}
			} else {
				c[i] = Util.clone(prop);
			}
		} else {
			c[i] = prop;
		}
	}

	return c;
}

//parse a json result as string, always yielding valid result
Util.parseJsonResult = function(jsonStr){
	var json = { status : false, result : 'Could not parse result.' }
	if(jsonStr){
		try{
		 json = eval("(" + jsonStr + ")");//ToDo: is jquery's json parser a viable alternative?
		}
		catch (e) {
			this.logger.error('Could not parse json result �' + jsonStr + '�, ' + e);
		}
	}
	return json;
}
