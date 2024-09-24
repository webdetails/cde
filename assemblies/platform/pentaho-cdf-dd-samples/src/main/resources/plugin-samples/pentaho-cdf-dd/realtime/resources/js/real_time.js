/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

define(['cdf/lib/jquery',
  'amd!cdf/lib/underscore',
  'cdf/lib/CCC/protovis',
  'cdf/Dashboard.Clean',
  'cdf/lib/CCC/pvc',
  'cdf/dashboard/Utils',
  'cdf/lib/moment'], function ($, _, pv, Dashboard, pvc, Utils, moment) {

  /*-------------------------------------------------------------------------*
   *                      MISC functions && settings                         *
   *-------------------------------------------------------------------------*/

  $(document).ready(function () {
    $('body').addClass('dashboard-example');
    $(document).prop('title', 'Real Time Dashboard');
    $("head").append("<link href='https://fonts.googleapis.com/css?family=Open+Sans:400,600,700' rel='stylesheet' type='text/css'>");
  });

});

var dashboard_example = {};

dashboard_example.lineChartOptions = function () {
  var cd = this.chartDefinition;
  // bars
  cd.colors = [
    '#613d9b', '#e5650e'
  ];

  cd.baseAxisLabel_textAngle = 0;

  var labels = [];

  cd.timeSeriesFormat = "%Y-%m-%d %H:%M:%S";

  cd.timeSeries = true;

  cd.baseAxisScale_dateTickFormat = "%H:%M:%S";

  cd.baseAxisDomainRoundMode = "none";

  cd.orthoAxisTitleLabel_textAlign = "left";
  cd.orthoAxisTitleLabel_textBaseline = "bottom";
  cd.orthoAxisTitleLabel_textAngle = 0;
  cd.orthoAxisTitleLabel_textMargin = 17;
  cd.contentPaddings = null;
  cd.contentMargins = 0;
  cd.baseAxisLabel_textStyle = "#414141";
  cd.orthoAxisLabel_textStyle = "#414141";

  cd.axisGrid_strokeStyle = '#f0efe9';
  cd.axisRule_strokeStyle = '#f0efe9';
  cd.axisZeroLine_strokeStyle = '#f0efe9';

  cd.baseAxisDesiredTickCount = 8;

  cd.orthoAxisSize = null;

  cd.legendAlign = "right";
  cd.legendFont = "lighter 12px Open Sans";
  cd.orthoAxisTitleLabel_textStyle = "#999999";

}
 