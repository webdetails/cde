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
define([], function () {

  return {
    configurations: {
      charts: {
        baseAxisGrid: true,
        orthoAxisGrid: true,
        baseAxisTitleAlign: "left",
        orthoAxisTitleAlign: "top",
        baseAxisFont: "10px 'Open Sans', Arial, sans-serif",
        orthoAxisFont: "10px 'Open Sans', Arial, sans-serif",
        baseAxisTitleFont: "10px 'Open Sans', Arial, sans-serif",
        orthoAxisTitleFont: "10px 'Open Sans', Arial, sans-serif",
        baseAxisTitleMargins: {
          top: 10,
          left: -6
        },
        orthoAxisTitleMargins: {
          right: 10,
          top: -7
        },
        baseAxisTitleLabel_textStyle: "#777",
        orthoAxisTitleLabel_textStyle: "#777",
        baseAxisLabel_textStyle: "#777",
        orthoAxisLabel_textStyle: "#777",
        axisRule_strokeStyle: "#777",
        axisTicks_strokeStyle: "#777",
        axisGrid_strokeStyle: "#F2F2F2",
        orthoAxisTicks_width: 2,
        baseAxisTicks_height: 3,
        baseAxisLabel_textMargin: 8,
        orthoAxisLabel_textMargin: 6,
        baseAxisMinorTicks: false,
        orthoAxisMinorTicks: false,
        baseAxisTooltipEnabled: false,
        noDataMessage_text: "No data available.",
        noDataMessage_font: "14px 'Open Sans', Arial, sans-serif",
        noDataMessage_textStyle: "#333",
        crosstabMode: false,
        legendPosition: "top",
        plotFrameVisible: false,

        scatterPlot: {
          baseAxisTitle: "Electricity Generated (GWh)",
          orthoAxisTitle: "Generation Capacity (MW)",
          colorScaleType: 'discrete',
          colors: ["#B71C1C", "#F2C249", "#00845B"],
          colorDomain: [21, 51]
        },

        barChart: {
          baseAxisLabelDesiredAngles: [0, 535],
          baseAxisLabelRotationDirection: 'Counterclockwise',
          baseAxisOverlappedLabelsMode: 'RotateThenHide'
        },

        popupBarChart: {
          orthoAxisTitle: "Capacity Factor (%)",
          orthoAxisTitleMargins: {top: 10, right: 0, bottom: 0, left: -6},
          orthoAxisTitleAlign: "left",
          orthoAxisFixedMax: 100,
          orthoAxisTicks_height: 3,
          orientation: "Horizontal"
        }
      }
    }
  };

});
