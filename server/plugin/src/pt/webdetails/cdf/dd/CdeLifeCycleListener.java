/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISolutionRepositoryService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import pt.webdetails.cpf.repository.RepositoryAccess;

public class CdeLifeCycleListener implements IPluginLifecycleListener
{

  static Log logger = LogFactory.getLog(CdeLifeCycleListener.class);

  private static IPentahoSession getAdminSession()
  {
    IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.getUserDetailsRoleListService();
    UserSession session = new UserSession("admin", null, false, null);
    GrantedAuthority[] auths = userDetailsRoleListService.getUserRoleListService().getAllAuthorities();
    Authentication auth = new AnonymousAuthenticationToken("admin", SecurityHelper.SESSION_PRINCIPAL, auths);
    session.setAttribute(SecurityHelper.SESSION_PRINCIPAL, auth);
    session.doStartupActions(null);
    return session;
  }

  @Override
  public void init() throws PluginLifecycleException
  {
    logger.debug("Init for CDE");
  }

  @Override
  public void loaded() throws PluginLifecycleException
  {
    //Check if folder and subfolders exist? If not create them.
    IPentahoSession adminSession = getAdminSession();
    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, adminSession);
    ISolutionRepositoryService repService = PentahoSystem.get(ISolutionRepositoryService.class, adminSession);
    if (!solutionRepository.resourceExists("cde"))
    {
      try
      {
        repService.createFolder(adminSession, "", "", "cde", "CDE");
      }
      catch (IOException ioe)
      {
        logger.error("Error while creating folder cde for cde plugin. CDE may not work as expected", ioe);
      }
    }

    if (!solutionRepository.resourceExists("cde/styles"))
    {
      try
      {
        repService.createFolder(adminSession, "", "cde", "styles", "styles");
      }
      catch (IOException ioe)
      {
        logger.error("Error while creating folder cde/styles for cde plugin. CDE may not work as expected", ioe);
      }
    }
    
    
    
    if (!solutionRepository.resourceExists("cde/components"))
    {
      try
      {
        repService.createFolder(adminSession, "", "cde", "components", "components");
      }
      catch (IOException ioe)
      {
        logger.error("Error while creating folder cde/components for cde plugin. CDE may not work as expected", ioe);
      }
    }


    if (!solutionRepository.resourceExists("cde/templates"))
    {
      try
      {
        repService.createFolder(adminSession, "", "cde", "templates", "templates");
      }
      catch (IOException ioe)
      {
        logger.error("Error while creating folder cde/templates for cde plugin. CDE may not work as expected", ioe);
      }
    }

    if (!solutionRepository.resourceExists("cde/widgets"))
    {
      try
      {
        repService.createFolder(adminSession, "", "cde", "widgets", "widgets");
        RepositoryAccess repo = RepositoryAccess.getRepository();
        repo.copySolutionFile("system/pentaho-cdf-dd/resources/samples/widget.cdfde", "cde/widgets/sample.cdfde");
        repo.copySolutionFile("system/pentaho-cdf-dd/resources/samples/widget.wcdf", "cde/widgets/sample.wcdf");
        repo.copySolutionFile("system/pentaho-cdf-dd/resources/samples/widget.cda", "cde/widgets/sample.cda");
        repo.copySolutionFile("system/pentaho-cdf-dd/resources/samples/widget.xml", "cdv/sample.component.xml");

      }
      catch (IOException ioe)
      {
        logger.error("Error while creating folder cde/widgets for cde plugin. CDE may not work as expected", ioe);
      }
    }
  }

  @Override
  public void unLoaded() throws PluginLifecycleException
  {
    logger.debug("Unload for CDE");
  }
}
