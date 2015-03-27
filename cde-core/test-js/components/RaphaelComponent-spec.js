/*!
 * Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

define(['cdf/Dashboard.Clean', 'cde/components/RaphaelComponent', 'cdf/lib/jquery'],
  function(Dashboard, RaphaelComponent, $) {

  /**
   * ## The Raphael Component
   */
  describe("The Raphael Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var raphaelComponent = new RaphaelComponent({
      type: "RaphaelComponent",
      name: "raphaelComponent",
      priority: 5,
        width: 400,
        height: 400,
        parameters: [],
        executeAtStart: true,
        customfunction: function f() {

         Raphael.fn.pieChart = function (cx, cy, r, values, labels, stroke) {
          var paper = this,
              rad = Math.PI / 180,
              chart = this.set();
          function sector(cx, cy, r, startAngle, endAngle, params) {
            var x1 = cx + r * Math.cos(-startAngle * rad),
                x2 = cx + r * Math.cos(-endAngle * rad),
                y1 = cy + r * Math.sin(-startAngle * rad),
                y2 = cy + r * Math.sin(-endAngle * rad);
            return paper.path(["M", cx, cy, "L", x1, y1, "A", r, r, 0, + (endAngle - startAngle > 180), 0, x2, y2, "z"]).attr(params);
          }
          var angle = 0,
              total = 0,
              start = 0,
              colors = ["#1E90FF", "#FF6347", "#32CD32"],
              process = function(j) {
                var value = values[j],
                    angleplus = 360 * value / total,
                    popangle = angle + (angleplus / 2),
                    color = colors[j],
                    ms = 500,
                    delta = 30,
                    bcolor = Raphael.hsb(start, 1, 10),
                    p = sector(cx, cy, r, angle, angle + angleplus,{fill: "90-" + bcolor + "-" + color, stroke: stroke, "stroke-width": 3}),
                    txt = paper.text(cx + (r + delta + 55) * Math.cos(-popangle * rad), cy + (r + delta + 25) * Math.sin(-popangle * rad), labels[j]).attr({fill: colors[j], stroke: "none", opacity: 0, "font-size": 20});
                p.mouseover(function () {
                  p.stop().animate({transform: "s1.1 1.1 " + cx + " " + cy}, ms, "elastic");
                  txt.stop().animate({opacity: 1}, ms, "elastic");
                }).mouseout(function () {
                  p.stop().animate({transform: ""}, ms, "elastic");
                  txt.stop().animate({opacity: 0}, ms);
                });
                angle += angleplus;
                chart.push(p);
                chart.push(txt);
                start += .1;
              };
          for(var i = 0, ii = values.length; i < ii; i++) {
            total += values[i];
          }
          for(i = 0; i < ii; i++) {
            process(i);
          }
          return chart;
        };

        var values = [30, 50, 20],
            labels = ["Work", "Sleep", "Fun"];

        Raphael(this.htmlObject, this.height, this.width)
          .pieChart(200, 150, 80, values, labels, "#222");
      },
      htmlObject: "sampleObject",
      listeners: [],
      queryDefinition: {}
    });
  
    dashboard.addComponent(raphaelComponent);
  
    /**
     * ## The Raphael Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(raphaelComponent, 'update').and.callThrough();
      dashboard.update(raphaelComponent);
      setTimeout(function() {
        expect(raphaelComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
