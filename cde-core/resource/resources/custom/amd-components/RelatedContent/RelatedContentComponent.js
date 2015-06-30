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

define(['cdf/components/BaseComponent', 'cdf/lib/jquery'], function(BaseComponent, $) {

  var RelatedContentComponent = BaseComponent.extend({
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

  return RelatedContentComponent;

});
