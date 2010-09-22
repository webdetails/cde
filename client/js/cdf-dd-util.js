
var Util = 
	{
	
	};

Util.clone = function clone(obj) {

	var c = obj instanceof Array ? [] : {};

	for (var i in obj) {
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