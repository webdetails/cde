describe("CDF-DD-BASE tests", function() {

  	it("Check if previewMode generated the correct data", function() {
  		cdfdd.previewMode();
    	expect( PreviewRequests.status ).toBeTruthy();
  	});
});
