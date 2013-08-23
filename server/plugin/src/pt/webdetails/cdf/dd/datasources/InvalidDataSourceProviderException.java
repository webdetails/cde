package pt.webdetails.cdf.dd.datasources;

public class InvalidDataSourceProviderException extends Exception 
{
  private static final long serialVersionUID = 8885274026585468691L;

  public InvalidDataSourceProviderException() 
  {
  }

  public InvalidDataSourceProviderException(String message) 
  {
    super(message);
  }

  public InvalidDataSourceProviderException(Throwable cause) 
  {
    super(cause);
  }

  public InvalidDataSourceProviderException(String message, Throwable cause) 
  {
    super(message, cause);
  }
}
