package pt.webdetails.cdf.dd;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.messages.MessageUtil;

/**
 * Utility class for internationalization
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 *
 */
public class Messages {
  private static final Map<Locale,ResourceBundle> locales = Collections.synchronizedMap(new HashMap<Locale,ResourceBundle>());

  private static ResourceBundle getBundle() {
    Locale locale = LocaleHelper.getLocale();
    ResourceBundle bundle = Messages.locales.get(locale);
    if (bundle == null) {
      IPentahoSession session = new StandaloneSession( "dashboards messages" ); //$NON-NLS-1$
        try {
          String messagesPath = "system/"+DashboardDesignerContentGenerator.PLUGIN_NAME+"/lang/messages.properties";//$NON-NLS-1$ //$NON-NLS-2$
          InputStream in = PentahoSystem.get(ISolutionRepository.class, session).getResourceInputStream(messagesPath, true, ISolutionRepository.ACTION_EXECUTE); 
          bundle = new PropertyResourceBundle( in );
          Messages.locales.put(locale, bundle);
        } catch (Exception e) {
          Logger.error( Messages.class.getName(), "Could not get localization bundle", e ); //$NON-NLS-1$
        }
    }
    return bundle;
  }

  public static String getEncodedString(final String rawValue) {
    if (rawValue == null) {
      return (""); //$NON-NLS-1$
    }

    StringBuffer value = new StringBuffer();
    for (int n = 0; n < rawValue.length(); n++) {
      int charValue = rawValue.charAt(n);
      if (charValue >= 0x80) {
        value.append("&#x"); //$NON-NLS-1$
        value.append(Integer.toString(charValue, 0x10));
        value.append(";"); //$NON-NLS-1$
      } else {
        value.append((char) charValue);
      }
    }
    return value.toString();

  }

  public static String getXslString(final String key) {
    String rawValue = Messages.getString(key);
    return Messages.getEncodedString(rawValue);
  }

  public static String getString(final String key) {
    try {
      return Messages.getBundle().getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

  public static String getString(final String key, final String param1) {
    return MessageUtil.getString(Messages.getBundle(), key, param1);
  }

  public static String getString(final String key, final String param1, final String param2) {
    return MessageUtil.getString(Messages.getBundle(), key, param1, param2);
  }

  public static String getString(final String key, final String param1, final String param2, final String param3) {
    return MessageUtil.getString(Messages.getBundle(), key, param1, param2, param3);
  }

  public static String getString(final String key, final String param1, final String param2, final String param3,
      final String param4) {
    return MessageUtil.getString(Messages.getBundle(), key, param1, param2, param3, param4);
  }

  public static String getErrorString(final String key) {
    return MessageUtil.formatErrorMessage(key, Messages.getString(key));
  }

  public static String getErrorString(final String key, final String param1) {
    return MessageUtil.getErrorString(Messages.getBundle(), key, param1);
  }

  public static String getErrorString(final String key, final String param1, final String param2) {
    return MessageUtil.getErrorString(Messages.getBundle(), key, param1, param2);
  }

  public static String getErrorString(final String key, final String param1, final String param2, final String param3) {
    return MessageUtil.getErrorString(Messages.getBundle(), key, param1, param2, param3);
  }
}
