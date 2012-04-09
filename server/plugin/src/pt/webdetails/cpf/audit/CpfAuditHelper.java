/*
 * Helper for audit logs
 */
package pt.webdetails.cpf.audit;

import java.util.Iterator;
import org.pentaho.platform.api.engine.IParameterProvider;


import java.util.UUID;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;

public class CpfAuditHelper {
	
	/**
	 * 
	 * Start Audit Event
	 *  
	 * @param actionName  Name of the action
	 * @param objectName Object of the action
	 * @param userSession Pentaho User Session 
	 * @param logger Logger object
   * @param requestParams parameters associated to the request
	 * @return  UUID of start event
	 */
	static public UUID startAudit(String actionName, String objectName, IPentahoSession userSession,ILogger logger, IParameterProvider requestParams) {
		UUID uuid=UUID.randomUUID();

    StringBuilder sb = new StringBuilder();
    if (requestParams != null) {
      Iterator iter = requestParams.getParameterNames();
      while (iter.hasNext()) {
        String paramName = iter.next().toString();
        sb.append(paramName).append("=").append(requestParams.getStringParameter(paramName, "novalue")).append(";");
      }
    }
      
    
    
		AuditHelper.audit(userSession.getId(), userSession.getName(), actionName, objectName, userSession.getProcessId(),
				MessageTypes.INSTANCE_START, uuid.toString(), sb.toString(), 0, logger);

		return uuid;
	}
	
	/**
	 * 
	 * End Audit Event
	 * 
	 * @param actionName Name of the action
	 * @param objectName Object of the action
	 * @param userSession Pentaho User Session 
	 * @param logger Logger object
	 * @param start Start time in Millis Seconds 
	 * @param uuid  UUID of start event
	 * @param end End time in Millis Seconds
	 */
	static public void endAudit(String actionName, String objectName, IPentahoSession userSession,ILogger logger, long start,UUID uuid, long end) {

    AuditHelper.audit(userSession.getId(), userSession.getName(), actionName, objectName, userSession.getProcessId(),
				MessageTypes.INSTANCE_END, uuid.toString(),"", ((float) (end - start) / 1000), logger);
	}

}

