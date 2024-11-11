/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
		  json = JSON.parse(jsonStr);
		}
		catch (e) {
			this.logger.error('Could not parse json result �' + jsonStr + '�, ' + e);
		}
	}
	return json;
}
