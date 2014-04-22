package pt.webdetails.cdf.dd.api;


import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cdf.dd.testUtils.MockUserContentAccess;

public class EditorApiForTesting extends EditorApi {

  private MockUserContentAccess mockUserContentAccess;

  public void initMockUserContentAccess() {
    this.mockUserContentAccess = new MockUserContentAccess();
  }

  @Override protected IUserContentAccess getUserContentAccess() {
    return mockUserContentAccess;
  }

  public void setHasAccess( boolean hasAccess ) {
    mockUserContentAccess.setHasAccess( hasAccess );
  }

  public void setSavedFile( boolean savedFile ) {
    mockUserContentAccess.setSavedFile( savedFile );
  }
}
