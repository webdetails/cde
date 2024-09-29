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
    
    // **Creates** an instance property of this property type.
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
    setType: function(type){this.type = type;},
    getType: function(){return this.type;}

  });
