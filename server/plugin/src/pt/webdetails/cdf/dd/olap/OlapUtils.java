/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd.olap;

import java.util.List;
import javax.sql.DataSource;
import mondrian.mdx.MemberExpr;
import mondrian.olap.Connection;
import mondrian.olap.Dimension;
import mondrian.olap.DriverManager;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;
import mondrian.olap.Member;
import mondrian.olap.Position;
import mondrian.olap.Query;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoResultSet;
import mondrian.olap.Util;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.rolap.RolapMember;
import mondrian.rolap.RolapMemberBase;
import mondrian.rolap.RolapResult;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;

/**
 *
 * @author pedro
 */
@SuppressWarnings("deprecation")
public class OlapUtils
{

  private static Log logger = LogFactory.getLog(OlapUtils.class);
  private IPentahoSession userSession;
  ICacheManager cacheManager;
  boolean cachingAvailable;
  private static final String MONDRIAN_CATALOGS = "CDFDD_DATASOURCES_REPOSITORY_DOCUMENT";
  private final IMondrianCatalogService mondrianCatalogService = MondrianCatalogHelper.getInstance();
  Connection nativeConnection = null;
  String lastQuery = null;
  IPentahoResultSet resultSet = null;
  private static final String DIRECTION_DOWN = "down";

  public OlapUtils(IPentahoSession userSession)
  {

    this.userSession = userSession;
    cacheManager = PentahoSystem.getCacheManager(userSession);
    cachingAvailable = cacheManager != null && cacheManager.cacheEnabled();

  }

  public Object executeOperation(IParameterProvider pathParams)
  {

    String operation = pathParams.getStringParameter("operation", "-");

    if (operation.equals("GetOlapCubes"))
    {

      return getOlapCubes();

    }
    else if (operation.equals("GetCubeStructure"))
    {

      String catalog = pathParams.getStringParameter("catalog", null);
      String cube = pathParams.getStringParameter("cube", null);
      String jndi = pathParams.getStringParameter("jndi", null);

      return getCubeStructure(catalog, cube, jndi);

    }
    else if (operation.equals("GetLevelMembersStructure"))
    {

      String catalog = pathParams.getStringParameter("catalog", null);
      String cube = pathParams.getStringParameter("cube", null);
      String member = pathParams.getStringParameter("member", null);
      String[] members = pathParams.getStringArrayParameter("member", null);
      String direction = pathParams.getStringParameter("direction", null);

      if (members.length > 1)
      {
        return getLevelMembersStructure(catalog, cube, members, direction);
      }
      else
      {
        return getLevelMembersStructure(catalog, cube, member, direction);
      }


    }
    else if (operation.equals("GetLevelMembers"))
    {

      String catalog = pathParams.getStringParameter("catalog", null);
      String cube = pathParams.getStringParameter("cube", null);
      String member = pathParams.getStringParameter("member", null);

      return getLevelMembers(catalog, cube, member);

    }
    else if (operation.equals("GetPaginatedLevelMembers"))
    {

      String catalog = pathParams.getStringParameter("catalog", null);
      String cube = pathParams.getStringParameter("cube", null);
      String level = pathParams.getStringParameter("level", null);
      String startMember = pathParams.getStringParameter("startMember", "");
      String searchTerm = pathParams.getStringParameter("searchTerm", "");
      String context = pathParams.getStringParameter("context", null);
      long pageSize = pathParams.getLongParameter("pageSize", 100);
      long pageStart = pathParams.getLongParameter("pageStart", 0);

      return getPaginatedLevelMembers(catalog, cube, level, startMember, context, searchTerm, pageSize, pageStart);

    }
    else if (operation.equals("test"))
    {

      // Test method
      makeTest();
    }

    return "ok";

  }

  private JSONObject getOlapCubes()
  {

    logger.debug("Returning Olap cubes");

    JSONObject result = new JSONObject();
    JSONArray catalogsArray = new JSONArray();

    List<MondrianCatalog> catalogList = getMondrianCatalogs();
    for (MondrianCatalog catalog : catalogList)
    {
      JSONObject catalogJson = new JSONObject();
      catalogJson.put("name", catalog.getName());
      catalogJson.put("schema", catalog.getDefinition());
      catalogJson.put("jndi", catalog.getEffectiveDataSource().getJndi());
      catalogJson.put("cubes", JSONArray.fromObject(catalog.getSchema().getCubes()));
      catalogsArray.add(catalogJson);
    }

    logger.debug("Cubes found: " + catalogsArray.toString(2));

    result.element("catalogs", catalogsArray);
    return result;

  }

  private JSONObject getCubeStructure(String catalog, String cube, String jndi)
  {

    logger.debug("Returning Olap structure for cube " + cube);
    JSONObject result = new JSONObject();

    Connection connection = jndi != null ? getMdxConnection(catalog, jndi) : getMdxConnection(catalog);

    if (connection == null)
    {
      logger.error("Failed to get valid connection");
      return null;
    }

    JSONArray dimensionsArray = getDimensions(connection, cube);
    System.out.println(dimensionsArray.toString(2));
    result.put("dimensions", dimensionsArray);

    JSONArray measuresArray = getMeasures(connection, cube);
    System.out.println(measuresArray.toString(2));
    result.put("measures", measuresArray);

    return result;
  }

  private JSONArray getDimensions(Connection connection, String cube)
  {

    String query = "select {} ON Rows,  {} ON Columns from [" + cube + "]";
    Query mdxQuery = connection.parseQuery(query);

    Dimension[] dimensions = mdxQuery.getCube().getDimensions();

    JSONArray dimensionsArray = new JSONArray();

    for (Dimension dimension : dimensions)
    {
      if (dimension.isMeasures())
      {
        continue;
      }

      JSONObject jsonDimension = new JSONObject();
      jsonDimension.put("name", dimension.getName());
      jsonDimension.put("type", dimension.getDimensionType().name());

      // Hierarchies
      JSONArray hierarchiesArray = new JSONArray();
      Hierarchy[] hierarchies = dimension.getHierarchies();
      for (Hierarchy hierarchy : hierarchies)
      {
        JSONObject jsonHierarchy = new JSONObject();
        jsonHierarchy.put("type", "hierarchy");
        jsonHierarchy.put("name", hierarchy.getName());
        jsonHierarchy.put("hasAll", hierarchy.hasAll());
        jsonHierarchy.put("qualifiedName", hierarchy.getQualifiedName().substring(11, hierarchy.getQualifiedName().length() - 1));
        jsonHierarchy.put("defaultMember", hierarchy.getAllMember().getName());
        jsonHierarchy.put("defaultMemberQualifiedName", hierarchy.getAllMember().getQualifiedName().substring(8, hierarchy.getAllMember().getQualifiedName().length() - 1));
        ;
        // Levels
        JSONArray levelsArray = new JSONArray();
        Level[] levels = hierarchy.getLevels();
        for (Level level : levels)
        {
          JSONObject jsonLevel = new JSONObject();
          if (!level.isAll())
          {
            jsonLevel.put("type", "level");
            jsonLevel.put("depth", level.getDepth());
            jsonLevel.put("name", level.getName());
            jsonLevel.put("qualifiedName", level.getQualifiedName().substring(7, level.getQualifiedName().length() - 1));
            levelsArray.add(jsonLevel);
          }
        }
        jsonHierarchy.put("levels", levelsArray);

        hierarchiesArray.add(jsonHierarchy);
      }
      jsonDimension.put("hierarchies", hierarchiesArray);

      dimensionsArray.add(jsonDimension);
    }

    return dimensionsArray;

  }

  private JSONArray getMeasures(Connection connection, String cube)
  {

    String query = "select {Measures.Children} ON Rows,  {} ON Columns from [" + cube + "]";
    Query mdxQuery = connection.parseQuery(query);
    RolapResult result = (RolapResult) connection.execute(mdxQuery);
    List<RolapMember> rolapMembers = result.getCube().getMeasuresMembers();

    JSONArray measuresArray = new JSONArray();

    for (RolapMember measure : rolapMembers)
    {

      JSONObject jsonMeasure = new JSONObject();
      jsonMeasure.put("type", "measure");
      jsonMeasure.put("name", ((RolapMemberBase) measure).getName());
      jsonMeasure.put("qualifiedName", measure.getQualifiedName().substring(8, measure.getQualifiedName().length() - 1));
      jsonMeasure.put("memberType", measure.getMemberType().toString());

      measuresArray.add(jsonMeasure);

    }

    return measuresArray;

  }

  private Connection getMdxConnection(String catalog)
  {

    if (catalog != null && catalog.startsWith("/"))
    {
      catalog = StringUtils.substring(catalog, 1);
    }

    MondrianCatalog selectedCatalog = mondrianCatalogService.getCatalog(catalog, userSession);
    if (selectedCatalog == null)
    {
      logger.error("Received catalog '" + catalog + "' doesn't appear to be valid");
      return null;
    }
    selectedCatalog.getDataSourceInfo();
    logger.info("Found catalog " + selectedCatalog.toString());

    String connectStr = "provider=mondrian;dataSource=" + selectedCatalog.getEffectiveDataSource().getJndi()
            + "; Catalog=" + selectedCatalog.getDefinition();

    return getMdxConnectionFromConnectionString(connectStr);
  }

  private Connection getMdxConnection(String catalog, String jndi)
  {

    String connectStr = "provider=mondrian;dataSource=" + jndi + "; Catalog=" + catalog;

    return getMdxConnectionFromConnectionString(connectStr);
  }

  private Connection getMdxConnectionFromConnectionString(String connectStr)
  {

    Util.PropertyList properties = Util.parseConnectString(connectStr);
    try
    {
      String dataSourceName = properties.get(RolapConnectionProperties.DataSource.name());

      if (dataSourceName != null)
      {
        IDatasourceService datasourceService = PentahoSystem.getObjectFactory().get(IDatasourceService.class, null);
        DataSource dataSourceImpl = datasourceService.getDataSource(dataSourceName);
        if (dataSourceImpl != null)
        {
          properties.remove(RolapConnectionProperties.DataSource.name());
          nativeConnection = DriverManager.getConnection(properties, null, dataSourceImpl);
        }
        else
        {
          nativeConnection = DriverManager.getConnection(properties, null);
        }
      }
      else
      {
        nativeConnection = DriverManager.getConnection(properties, null);
      }

      if (nativeConnection == null)
      {
        logger.error("Invalid connection: " + connectStr);
      }
    }
    catch (Throwable t)
    {
      logger.error("Unable to get connection: " + connectStr, t);
    }



    return nativeConnection;
  }

  private List<MondrianCatalog> getMondrianCatalogs()
  {

    List<MondrianCatalog> catalogs = null;

    if (cachingAvailable
            && (catalogs = (List<MondrianCatalog>) cacheManager.getFromSessionCache(
            userSession, MONDRIAN_CATALOGS)) != null)
    {
      logger.debug("Datasource document found in cache");
      return catalogs;
    }
    else
    {

      catalogs = mondrianCatalogService.listCatalogs(userSession, true);
      cacheManager.putInSessionCache(userSession, MONDRIAN_CATALOGS, catalogs);

    }

    return catalogs;
  }

  private JSONObject getLevelMembersStructure(String catalog, String cube, String memberString, String direction)
  {

    Connection connection = getMdxConnection(catalog);

    String query = "";
    if (direction.equals(DIRECTION_DOWN))
    {
      query = "select " + memberString + ".children on Rows, {} ON Columns from [" + cube + "]";
    }
    else
    {
      query = "select " + memberString + ".parent.parent.children on Rows, {} ON Columns from [" + cube + "]";
    }

    Query mdxQuery = connection.parseQuery(query);
    RolapResult result = (RolapResult) connection.execute(mdxQuery);
    List<Position> positions = result.getAxes()[1].getPositions();

    JSONArray membersArray = new JSONArray();

    for (Position position : positions)
    {

      Member member = position.get(0);

      JSONObject jsonMeasure = new JSONObject();
      jsonMeasure.put("type", "member");
      jsonMeasure.put("name", member.getName());
      jsonMeasure.put("qualifiedName", member.getQualifiedName().substring(8, member.getQualifiedName().length() - 1));
      jsonMeasure.put("memberType", member.getMemberType().toString());

      membersArray.add(jsonMeasure);

    }

    JSONObject output = new JSONObject();
    output.put("members", membersArray);
    return output;


  }

  private JSONObject getLevelMembersStructure(String catalog, String cube, String[] memberString, String direction)
  {

    Connection connection = getMdxConnection(catalog);

    String query = "";
    query += "select {";
    for (int i = 0; i < memberString.length; i++)
    {
      if (direction.equals(DIRECTION_DOWN))
      {
        query += memberString[i] + ".children";

      }
      else
      {
        query += memberString[i] + ".parent.parent.children";
      }

      if (i < memberString.length - 1)
      {
        query += ",";
      }
    }

    query += "} on Rows, {} ON Columns from [" + cube + "]";


    Query mdxQuery = connection.parseQuery(query);
    RolapResult result = (RolapResult) connection.execute(mdxQuery);
    List<Position> positions = result.getAxes()[1].getPositions();

    JSONArray membersArray = new JSONArray();

    for (Position position : positions)
    {

      Member member = position.get(0);

      JSONObject jsonMeasure = new JSONObject();
      jsonMeasure.put("type", "member");
      jsonMeasure.put("name", member.getName());
      jsonMeasure.put("qualifiedName", member.getQualifiedName().substring(8, member.getQualifiedName().length() - 1));
      jsonMeasure.put("memberType", member.getMemberType().toString());

      membersArray.add(jsonMeasure);

    }

    JSONObject output = new JSONObject();
    output.put("members", membersArray);
    return output;


  }

  private JSONObject getLevelMembers(String catalog, String cube, String levelString)
  {

    Connection connection = getMdxConnection(catalog);

    String query = "select distinct(" + levelString + ".members) on Rows, {} ON Columns from [" + cube + "]";

    Query mdxQuery = connection.parseQuery(query);
    RolapResult result = (RolapResult) connection.execute(mdxQuery);
    List<Position> positions = result.getAxes()[1].getPositions();

    System.out.println();

    JSONArray membersArray = new JSONArray();

    for (Position position : positions)
    {

      Member member = position.get(0);

      JSONObject jsonMeasure = new JSONObject();
      jsonMeasure.put("type", "member");
      jsonMeasure.put("name", member.getName());
      jsonMeasure.put("qualifiedName", member.getQualifiedName().substring(8, member.getQualifiedName().length() - 1));
      jsonMeasure.put("memberType", member.getMemberType().toString());

      membersArray.add(jsonMeasure);

    }

    JSONObject output = new JSONObject();
    output.put("members", membersArray);
    return output;


  }

  private JSONObject getPaginatedLevelMembers(String catalog, String cube, String level, String startMember, String context, String searchTerm, long pageSize, long pageStart)
  {

    Connection connection = getMdxConnection(catalog);

    boolean hasStartMember = true;
    boolean hasFilter = !(searchTerm.equals(""));

    if (startMember == null || startMember.equals(""))
    {

      hasStartMember = false;
      startMember = level + ".Hierarchy.defaultMember";

    }

    String query = "with "
            + "set descendantsSet as Descendants(" + startMember + " , " + level + ") "
            + "set membersSet as " + level + ".Members "
            + "set resultSet as " + (hasStartMember ? "descendantsSet" : "membersSet") + " "
            + "set filteredSet as filter(resultSet, " + level + ".hierarchy.currentMember.name MATCHES '(?i).*" + searchTerm + ".*' ) "
            + "select {} ON COLUMNS,  "
            + "Subset(Order( "
            + (hasFilter ? "filteredSet " : "resultSet ")
            /* Try to fetch pageSize + 1 results -- the extra element allows us 
             * to know whether there are any more members for the next page
             */
            + ", " + level + ".hierarchy.currentMember.Name,BASC), " + pageStart + ", " + (pageSize + 1) + ") ON ROWS "
            + "from [" + cube + "] where {" + context + "}";

    Query mdxQuery = connection.parseQuery(query);
    RolapResult result = (RolapResult) connection.execute(mdxQuery);
    List<Position> positions = result.getAxes()[1].getPositions();

    /* check whether there is data for the next page, and remove
     * excess elements resulting from querying for extra results
     */
    boolean nextPage = positions.size() == pageSize + 1;

    JSONArray membersArray = new JSONArray();
    int i = 0;
    for (Position position : positions)
    {
      if (i++ == pageSize)
      {
        break;
      }
      Member member = position.get(0);

      JSONObject jsonMeasure = new JSONObject();
      jsonMeasure.put("type", "member");
      jsonMeasure.put("name", member.getName());
      jsonMeasure.put("qualifiedName", member.getQualifiedName().substring(8, member.getQualifiedName().length() - 1));
      jsonMeasure.put("memberType", member.getMemberType().toString());

      membersArray.add(jsonMeasure);

    }

    JSONObject output = new JSONObject();
    output.put("members", membersArray);
    output.put("more", nextPage);
    return output;


  }

  private void makeTest()
  {


    String catalog = "SteelWheels";
    String cube = "SteelWheelsSales";
    Connection connection = getMdxConnection(catalog);

    String query = "select NON EMPTY {[Measures].[Quantity]} ON COLUMNS,  NON EMPTY  [Product].Children ON ROWS from [SteelWheelsSales] where [Markets].[All Markets].[EMEA]";
    Query mdxQuery = connection.parseQuery(query);
    MemberExpr member = (MemberExpr) mdxQuery.getSlicerAxis().getChildren()[0];
    member.getMember();



    RolapResult result = (RolapResult) connection.execute(mdxQuery);
    List<RolapMember> rolapMembers = result.getCube().getMeasuresMembers();
    System.out.println("Hello World");


  }
}
