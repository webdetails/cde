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

