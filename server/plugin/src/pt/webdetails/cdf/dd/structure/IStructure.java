package pt.webdetails.cdf.dd.structure;

import java.util.HashMap;

public interface IStructure {
	
	public abstract HashMap<String, String>  save(HashMap<String, Object> parameters) throws Exception;
	
	public abstract Object load(HashMap<String, Object> parameters) throws Exception;
	
	public abstract void delete(HashMap<String, Object> parameters) throws Exception;
	
}
