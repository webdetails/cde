/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cdf.dd.render;


import java.util.LinkedList;
import java.util.List;

public class ResourceMap {
  private List<Resource> javascriptResources;
  private List<Resource> cssResources;

  /**
   * *
   */
  public ResourceMap() {
    javascriptResources = new LinkedList<Resource>();
    cssResources = new LinkedList<Resource>();
  }

  /**
   * *
   *
   * @return
   */
  public List<Resource> getJavascriptResources() {
    return this.javascriptResources;
  }

  /**
   * *
   *
   * @return
   */
  public List<Resource> getCssResources() {
    return this.cssResources;
  }

  public void add( ResourceKind resourceKind, ResourceType resourceType, String resourceName, String resourcePath,
                   String processedResource ) {
    Resource res = new Resource( resourceType, resourceName, resourcePath, processedResource );
    if ( resourceKind.equals( ResourceKind.JAVASCRIPT ) ) {
      javascriptResources.add( res );
    } else if ( resourceKind.equals( ResourceKind.CSS ) ) {
      cssResources.add( res );
    }
  }

  public enum ResourceKind {
    CSS( "Css" ), JAVASCRIPT( "Javascript" );

    private String value;

    private ResourceKind( String value ) {
      this.value = value;
    }
  }

  public enum ResourceType {
    FILE( "LayoutResourceFile" ), CODE( "LayoutResourceCode" );

    private String value;

    private ResourceType( String value ) {
      this.value = value;
    }
  }

  /**
   * *
   */
  public class Resource {
    private ResourceType resourceType;
    private String resourceName;
    private String resourcePath;

    private String processedResource;

    /**
     * *
     *
     * @param resourceType
     * @param resourceName
     * @param resourcePath
     * @param processedResource
     */
    public Resource( ResourceType resourceType, String resourceName, String resourcePath, String processedResource ) {
      this.setResourceType( resourceType );
      this.setResourceName( resourceName );
      this.setResourcePath( resourcePath );

      this.setProcessedResource( processedResource );
    }

    /**
     * *
     *
     * @return
     */
    public ResourceType getResourceType() {
      return resourceType;
    }

    /**
     * *
     *
     * @param resourceType
     */
    public void setResourceType( ResourceType resourceType ) {
      this.resourceType = resourceType;
    }

    /**
     * *
     *
     * @return
     */
    public String getResourceName() {
      return resourceName;
    }

    /**
     * *
     *
     * @param resourceName
     */
    public void setResourceName( String resourceName ) {
      this.resourceName = resourceName;
    }

    /**
     * *
     *
     * @return
     */
    public String getResourcePath() {
      return this.resourcePath;
    }

    /**
     * *
     *
     * @param resourcePath
     */
    public void setResourcePath( String resourcePath ) {
      this.resourcePath = resourcePath;
    }

    /**
     * *
     *
     * @return
     */
    public String getProcessedResource() {
      return processedResource;
    }

    /**
     * *
     *
     * @param processedResource
     */
    public void setProcessedResource( String processedResource ) {
      this.processedResource = processedResource;
    }
  }

}
