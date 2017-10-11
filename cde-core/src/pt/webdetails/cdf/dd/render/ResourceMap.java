/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

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
