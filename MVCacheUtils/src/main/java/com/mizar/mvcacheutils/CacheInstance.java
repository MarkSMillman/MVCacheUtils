/*
  * Copyright 2008, 2009, 2010, 2014, 2017 Mizar, LLC
  * 9908 Alegria Drive,
  * Las Vegas, NV, 98281, U.S.A.
  * All Rights Reserved.
  *
  * This file is part of the Mizar Framework
  *
  * The Mizar Framework is the exclusive property of MIZAR, LLC and may not 
  * be redistributed without the express written permission of MIZAR, LLC.
  * License is granted to use this source code to maintain or extend software
  * originally developed by MIZAR, LLC or an associated company.
  *
  * The Mizar Framework is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *
  * You may NOT remove this copyright notice; it must be retained in any modified 
  * version of the software.
  */
package com.mizar.mvcacheutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class CacheInstance {
  private String dataSource;
  private String name;
  private String type;
  private String baseMap;
  private int zoomLevels;
  private String cacheRoot;
  private String status;
  private boolean online;

  public CacheInstance() {
  }

  private String getElementValue(Element element, String attribute) {
    String attr = "";
    Attribute a = element.getAttribute(attribute);
    if (a != null) {
      attr = a.getValue().trim();
    }
    return attr;
  }

  public CacheInstance(Element instance) {
    this.dataSource = getElementValue(instance, "data_source");
    this.name = getElementValue(instance, "name");
    this.type = getElementValue(instance, "type");
    this.baseMap = getElementValue(instance, "base_map");
    this.zoomLevels = Integer.valueOf(getElementValue(instance, "zoom_levels")).intValue();
    this.cacheRoot = getElementValue(instance, "cache_root");
    this.status = getElementValue(instance, "status");
    this.online = Boolean.valueOf(getElementValue(instance, "online")).booleanValue();
  }

  /**
   * <p><strong>Example input parameter returned by mcserver:</strong></p>
   * <p>The input has been formatted for readability here but does not contain new lines as delivered by mcserver and mcsadmin</p>
   * <p>The &lt;map_tile_server_response> is delivered by mcsadmin but is not delivered by mcserver.  The method handles both cases.</p>
   * <pre><code>
   &lt;map_tile_server_response>
     &lt;tile_server_status>
       &lt;cache_instance data_source="MVDEMO" name="CUSTOMER_MAP" type="internal" base_map="CUSTOMER_MAP" zoom_levels="10" cache_root="C:\oracle\Middleware\mapviewer\mapviewer.ear\web.war\tilecache\MVDEMO.CUSTOMER_MAP\" status="ready" online="true"/>
       &lt;cache_instance data_source="MVDEMO" name="DEMO_MAP" type="internal" base_map="DEMO_MAP" zoom_levels="10" cache_root="C:\oracle\Middleware\mapviewer\mapviewer.ear\web.war\tilecache\MVDEMO.DEMO_MAP\" status="ready" online="true"/>
       &lt;cache_instance data_source="PRECINCT" name="BASE_MAP" type="internal" base_map="OHIO_MAP" zoom_levels="11" cache_root="/temp\PRECINCT.BASE_MAP\" status="ready" online="true"/>
       &lt;cache_instance data_source="PRECINCT" name="LYR_BLOCK_GROUP" type="internal" base_map="LYR_BLOCK_GROUP" zoom_levels="11" cache_root="C:\oracle\Middleware\mapviewer\mapviewer.ear\web.war\tilecache\PRECINCT.LYR_BLOCK_GROUP\" status="ready" online="true"/>
       &lt;cache_instance data_source="PRECINCT" name="LYR_CENSUS_BLOCK" type="internal" base_map="LYR_CENSUS_BLOCK" zoom_levels="11" cache_root="C:\oracle\Middleware\mapviewer\mapviewer.ear\web.war\tilecache\PRECINCT.LYR_CENSUS_BLOCK\" status="ready" online="true"/>
       &lt;cache_instance data_source="PRECINCT" name="LYR_STREET" type="internal" base_map="LYR_STREET" zoom_levels="11" cache_root="C:\oracle\Middleware\mapviewer\mapviewer.ear\web.war\tilecache\PRECINCT.LYR_STREET\" status="ready" online="true"/>
       &lt;cache_instance data_source="PRECINCT" name="LYR_TRACT" type="internal" base_map="LYR_TRACT" zoom_levels="11" cache_root="C:\oracle\Middleware\mapviewer\mapviewer.ear\web.war\tilecache\PRECINCT.LYR_TRACT\" status="ready" online="true"/>
       &lt;cache_instance data_source="PRECINCT" name="OHIO_82249_MAP" type="internal" base_map="OHIO_MAP" zoom_levels="11" cache_root="C:\oracle\Middleware\mapviewer\mapviewer.ear\web.war\tilecache\PRECINCT.OHIO_82249_MAP\" status="ready" online="true"/>
       &lt;cache_instance data_source="PRECINCT" name="OHIO_MAP" type="internal" base_map="OHIO_MAP" zoom_levels="11" cache_root="C:\oracle\Middleware\mapviewer\mapviewer.ear\web.war\tilecache\PRECINCT.OHIO_MAP\" status="ready" online="true"/>
     &lt;/tile_server_status>
   &lt;/map_tile_server_response>
   * </code></pre>
   * @param tileServerStatusXML
   * @return
   * @throws JDOMException
   * @throws IOException
   * @see MapCacheUtils#getClientConfig(String)
   */
  public static List<CacheInstance> getFromTileServerStatus(String tileServerStatusXML) throws JDOMException, IOException {
    List<CacheInstance> list = new ArrayList<CacheInstance>();
    if (StringUtils.isNotEmpty(tileServerStatusXML)) {
      SAXBuilder builder = new SAXBuilder();
      ByteArrayInputStream stream = new ByteArrayInputStream(tileServerStatusXML.getBytes());
      Document dom = builder.build(stream);
      Element rootElem = dom.getRootElement();
      List<Element> cacheInstance = null;
      if (rootElem.getName().equals("map_tile_server_response")) {
        cacheInstance = rootElem.getChild("tile_server_status").getChildren();
      } else if (rootElem.getName().equals("tile_server_status")) {
        cacheInstance = rootElem.getChildren();
      }

      if (cacheInstance != null && cacheInstance.size() > 0) {
        Element instance1 = cacheInstance.get(0);
        if (instance1.getName().equals("tile_server_status")) {
          // An outer wrapper is added when received from mcsadmin
          cacheInstance = instance1.getChildren();
        }

        for (Element instance : cacheInstance) {
          list.add(new CacheInstance(instance));
        }
      }
    }
    return list;
  }

  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  public String getDataSource() {
    return dataSource;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setBaseMap(String baseMap) {
    this.baseMap = baseMap;
  }

  public String getBaseMap() {
    return baseMap;
  }

  public void setZoomLevels(int zoomLevels) {
    this.zoomLevels = zoomLevels;
  }

  public int getZoomLevels() {
    return zoomLevels;
  }

  public void setCacheRoot(String cacheRoot) {
    this.cacheRoot = cacheRoot;
  }

  public String getCacheRoot() {
    return cacheRoot;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  public void setOnline(boolean online) {
    this.online = online;
  }

  public boolean isOnline() {
    return online;
  }
}
