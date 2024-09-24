/*!
 * Copyright 2020 Webdetails, a Hitachi Vantara company. All rights reserved.
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

define([
  'cde/components/NewMapComponent/engines/google/MapEngineGoogle',
  'cdf/lib/jquery',
  'google'
], function (MapEngineGoogle, $, google) {

  /**
   * ## Show Popup on click event
   */
  describe("Show Popup with Map Engine Google Component #", function() {

    const mapEngineGoogle = new MapEngineGoogle();

    const data = [];
    const feature = {
      getGeometry: function() {
        return {
          get: function () {
            return null;
          }
        }
      }
    };

    const popupHeight = 300;
    const popupWidth = 350;
    const contents = "";
    const popupContentDiv = "pie";
    const borderColor = "#FFFFFF";


    it( "_addControlClick should be call", function () {
      spyOn( mapEngineGoogle, "_addControlHover");
      spyOn( mapEngineGoogle, "_addControlClick");
      spyOn( mapEngineGoogle, "_addControlZoomBox");
      spyOn( mapEngineGoogle, "_addControlBoxSelector");
      spyOn( mapEngineGoogle, "_addLimitZoomLimits");
      mapEngineGoogle.addControls();
      expect( mapEngineGoogle._addControlClick).toHaveBeenCalled();
    });

    it("Verify if popup was created successful", function() {

      spyOn( mapEngineGoogle, "showPopup").and.callThrough();

      $('body').append(
        '<div id="test_container">' +
        '  <div id="' + popupContentDiv + '">' +
        '   chart_content' +
        '   </div>' +
        '  <div id="map">' +
        '   map_content' +
        '  </div>' +
        '</div>'
      );

      mapEngineGoogle._popups = [];
      mapEngineGoogle.showPopup( data, feature, popupHeight, popupWidth, contents, popupContentDiv, borderColor );
      expect( $('#test_container').has($('#'+popupContentDiv)).length ).toBe( 0 );
      expect(mapEngineGoogle._popups.length).toBe(1);
    });
  });
});

