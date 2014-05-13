describe("CDF-DD-BASE tests", function() {

  	it("Check if previewMode generated the currect data", function() {
  		cdfdd.previewMode();
    	expect( PreviewRequests.status ).toBeTruthy();
  	});
});
