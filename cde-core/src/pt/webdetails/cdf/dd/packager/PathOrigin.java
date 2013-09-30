package pt.webdetails.cdf.dd.packager;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * To keep track of file origin <u>within the same plugin</u>.<br>
 * Everything needed to reinstantiate a Plugin's IReadAccess, and translate its location to a url.<br>
 * TODO: separate urlPrepend? it's mostly a reminder now
 */
public abstract class PathOrigin {

    public PathOrigin(String basePath) {
      this.basePath = basePath;
    }
    protected String basePath;

    public abstract IReadAccess getReader(IContentAccessFactory factory);

    /**
     * TODO: change name!
     * @return What you prepend to a relative path with this origin to make is accessible from the plugin's base url
     */
    public abstract String getUrlPrepend(String localPath);

    public String getUrlPrepend() {
      return getUrlPrepend("");//TODO: which one?
    }

    public String toString() {
      return getClass().getSimpleName() + ":" + basePath;
    }

    @Override
    public boolean equals(Object other) {
      return
          other instanceof PathOrigin &&
          getClass().equals(other.getClass()) &&
          StringUtils.equals( basePath, ((PathOrigin)other).basePath );
    }
    @Override
    public int hashCode() {
      int hash = getClass().hashCode();
      hash *= 73;
      if (!StringUtils.isEmpty( basePath )) {
        hash += basePath.hashCode();
      }
      return hash;
    }
}

