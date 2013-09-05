package pt.webdetails.cdf.dd.utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

import org.pentaho.platform.api.engine.IParameterProvider;

import pt.webdetails.cpf.http.ICommonParameterProvider;

public class CommonParameterProvider implements ICommonParameterProvider {
	
	private IParameterProvider parameterProvider;
	  
	public CommonParameterProvider(IParameterProvider parameterProvider){
		this.parameterProvider = parameterProvider;
	}
	  
	@Override
	public Object[] getArrayParameter(String arg0, Object[] arg1) {
		return parameterProvider.getArrayParameter(arg0, arg1);
	}

	@Override
	public Date getDateParameter(String arg0, Date arg1) {
		return parameterProvider.getDateParameter(arg0, arg1);
	}

	@Override
	public BigDecimal getDecimalParameter(String arg0, BigDecimal arg1) {
		return parameterProvider.getDecimalParameter(arg0, arg1);
	}

	@Override
	public long getLongParameter(String arg0, long arg1) {
		return parameterProvider.getLongParameter(arg0, arg1);
	}

	@Override
	public Object getParameter(String arg0) {
		return parameterProvider.getParameter(arg0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<String> getParameterNames() {
		return parameterProvider.getParameterNames();
	}

	@Override
	public String[] getStringArrayParameter(String arg0, String[] arg1) {
		return parameterProvider.getStringArrayParameter(arg0, arg1);
	}

	@Override
	public String getStringParameter(String arg0, String arg1) {
		return parameterProvider.getStringParameter(arg0, arg1);
	}

	@Override
	public boolean hasParameter(String arg0) {
		return parameterProvider.hasParameter(arg0);
	}

	@Override
	public void put(String arg0, Object arg1) {
		// IParameterProvider does not expose a put method
	}
}
