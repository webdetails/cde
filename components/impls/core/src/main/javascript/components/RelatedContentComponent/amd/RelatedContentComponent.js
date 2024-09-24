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
  'cdf/lib/jquery'
], function(BaseComponent, $) {

  return BaseComponent.extend({
    update: function() {

      var relatedContent = this.relatedContent;

      if(typeof relatedContent != "undefined") {

        var placeHolder = $("#" + this.htmlObject);
        placeHolder.empty();

        var contentString = '<div id="relatedContentMainDiv"><p>Related content</p><ul>';

        for(var key in relatedContent) {
          var relation = relatedContent[key];
          if(relatedContent.hasOwnProperty(key) && key != null && key != undefined) {
            contentString +=
              '<li><a href=\'' + relation[1] + '\'">' + relation[0] + '</a></li>';
          }
        }
        contentString = contentString + '</ul></div>';
        placeHolder.append(contentString);
      }
    }
  });

});
