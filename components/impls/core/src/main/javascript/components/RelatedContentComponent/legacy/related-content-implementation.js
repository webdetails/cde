/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
var RelatedContentComponent = BaseComponent.extend({
  update : function() {
    if(typeof(this.relatedContent) != "undefined") {
      var placeHolder = $("#" + this.htmlObject);
      placeHolder.empty();
      var contentString = '<div id="relatedContentMainDiv"><p>Related content</p><ul>';
      for(key in this.relatedContent) {
        if(this.relatedContent.hasOwnProperty(key) && key != null && key != undefined) {
          contentString = contentString + '<li><a href=\'' + this.relatedContent[key][1]
            + '\'">' + this.relatedContent[key][0] + '</a></li>';
        }
      }
      contentString = contentString + '</ul></div>';
      placeHolder.append(contentString);
    }
  }
});
  
