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


define([
  'cdf/components/BaseComponent',
  'cdf/lib/raphael'
], function(BaseComponent, Raphael) {

  return BaseComponent.extend({
    update: function() {
      var myself = this,
          parameters = myself.parameters;

      myself.customfunction.apply(myself, parameters ? parameters : []);
    }
  });

});
