
// PropertiesManager

var PropertiesManager = Base.extend({

		// Static class only

	},{
		propertyTypes: {},
		
		register: function(propertyType){
			PropertiesManager.propertyTypes[propertyType.getType()] = propertyType;
		},

		getPropertyType: function(propertyType){
			return PropertiesManager.propertyTypes[propertyType];
		},
		
		getProperty: function(type,options){
			return PropertiesManager.getPropertyType(type).getPropertyObject(options);
		}

	});


var BasePropertyType = Base.extend({

		type: "",
		logger: {},
		stub: {},

		constructor: function(){
			this.logger = new Logger("BasePropertyType");
		},

		getPropertyObject: function(options){

			// this.logger.debug("Getting properties for type " + this.getType());
			return $.extend({}, this.stub, options);
			
		},

		// Accessors
		setType: function(type){this.type = type},
		getType: function(){return this.type}

	});
