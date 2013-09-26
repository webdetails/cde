package pt.webdetails.cdf.dd.packager;

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
     * TODO: is this needed only for css? if so, maybe another approach
     * @return What you prepend to a relative path with this origin to make is accessible from the plugin's base url
     */
    public abstract String getUrlPrepend();
}

