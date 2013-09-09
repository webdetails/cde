package pt.webdetails.cdf.dd.bean.factory;

public interface ICdeBeanFactory {
	
	  public Object getBean(String id);
	  public boolean containsBean(String id);
	  public String[] getBeanNamesForType(@SuppressWarnings("rawtypes") Class clazz);

}
