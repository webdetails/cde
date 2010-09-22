package pt.webdetails.cdf.dd.structure;

import java.util.HashMap;

@SuppressWarnings("unchecked")
public interface IStructure {
	
	public abstract void save(HashMap parameters) throws Exception;
	
	public abstract Object load(HashMap parameters) throws Exception;
	
	public abstract void delete(HashMap parameters) throws Exception;
	
	
}
