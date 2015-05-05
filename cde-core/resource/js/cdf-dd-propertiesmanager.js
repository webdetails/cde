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
