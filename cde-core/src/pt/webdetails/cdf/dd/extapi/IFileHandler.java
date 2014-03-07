package pt.webdetails.cdf.dd.extapi;

public interface IFileHandler {

  public boolean saveDashboardAs( String path, String title, String description, String cdfdeJsText ) throws Exception;

}
