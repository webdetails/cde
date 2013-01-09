/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

import java.io.IOException;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import pt.webdetails.cpf.repository.RepositoryAccess;

public class CdeLifeCycleListener implements IPluginLifecycleListener
{
  static final String SESSION_PRINCIPAL = "SECURITY_PRINCIPAL";
  static Log logger = LogFactory.getLog(CdeLifeCycleListener.class);

  private static IPentahoSession getAdminSession() {
      UserSession session = new UserSession("admin", null, false, null);
      IUserRoleListService service = PentahoSystem.get(IUserRoleListService.class);
      List<String> authorities = service.getAllRoles();

      GrantedAuthority[] grantedAuthorities = new GrantedAuthority[authorities.size()];
      if (!authorities.isEmpty()) {
          for (int i = 0; i < authorities.size(); i++) {
              grantedAuthorities[i] = new GrantedAuthorityImpl(authorities.get(i));
          }
      }

      Authentication auth = new AnonymousAuthenticationToken("admin", SESSION_PRINCIPAL, grantedAuthorities);
      session.setAttribute(SESSION_PRINCIPAL, auth);
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
