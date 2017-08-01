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
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * You may NOT remove this copyright notice; it must be retained in any modified
  * version of the software.
 */
package com.mizar.mvcacheutils;

import org.apache.commons.lang3.StringUtils;

public class ClientConfig {
  private String mapTileLayer;
  private String mapSource;
  private String dataSource;
  private String format;
  private boolean transparent;
  private CoordSys coordSys;

  public ClientConfig() {
  }

  /**
   * <p><strong>Example input:</strong></p>
   * <p>The input has been formatted for readability here but does not contain new lines as delivered by mcserver and mcsadmin</p>
   * <code><pre>
   [ {
    "mapTileLayer":"LYR_CONGRESSIONAL_DISTRICT",
    "mapSource":"LYR_CONGRESSIONAL_DISTRICT",
    "dataSource":"PRECINCT",
    "format":"PNG",
    "transparent":true,
    "coordSys": {
        "srid":8307,
        "type":"GEODETIC",
        "distConvFactor":0.0,
        "minX":-85.36377,
        "minY":38.00886,
        "maxX":-80.112305,
        "maxY":42.548764},
        "zoomLevels": [
            {"zoomLevel":0,
             "name":"",
             "scale":"3887449.0",
             "tileWidth":2.3714207413954855,
             "tileHeight":2.3714207413954855,
             "tileImageWidth":256,
             "tileImageHeight":256},
            {"zoomLevel":1,
             "name":"",
             "scale":"1700976.0",
             "tileWidth":1.0376289867766566,
             "tileHeight":1.0376289867766566,
             "tileImageWidth":256,
             "tileImageHeight":256},
            {"zoomLevel":10,
             "name":"",
             "scale":"1000.0",
             "tileWidth":6.100197691070637E-4,
             "tileHeight":6.100197691070637E-4,
             "tileImageWidth":256,
             "tileImageHeight":256
            }
          ]
      } ]
   * </code></pre>
   * @param clientConfigResponse a response from a <code>get_client_config</code> request to mcserver
   * @see MapCacheUtils#getClientConfig(String)
   */
  public ClientConfig(String clientConfigResponse) {
    this.mapTileLayer = ClientConfig.getValue(clientConfigResponse, "mapTileLayer", ",");
    this.mapSource = ClientConfig.getValue(clientConfigResponse, "mapSource", ",");
    this.dataSource = ClientConfig.getValue(clientConfigResponse, "dataSource", ",");
    this.format = ClientConfig.getValue(clientConfigResponse, "format", ",");
    this.transparent = Boolean.valueOf(ClientConfig.getValue(clientConfigResponse, "transparent", ",")).booleanValue();
    String fragment = ClientConfig.getValue(clientConfigResponse, "coordSys", null);
    if ( StringUtils.isNotEmpty(fragment) ){
      this.coordSys = new CoordSys(fragment);
    }
  }

  protected static String getValue(String response, String key, String terminator) {
    String value = "";
    int s = response.indexOf("\"" + key + "\":") + key.length() + 2;
    int e = response.length();
    if (terminator != null && terminator.length() > 0) {
      e = response.indexOf(terminator, s);
    }
    if (s >= 0 && e >= 0 && e >= s) {
      value = response.substring(s, e);
      if (value.startsWith(":")) {
        value = value.substring(1);
      }
      if (value.startsWith("\"")) {
        value = value.substring(1);
      }
      if (value.endsWith("\"")) {
        value = value.substring(0, value.length() - 1);
      }
      if (value.endsWith("}")) {
        value = value.substring(0, value.length() - 1);
      }
    }
    return value;
  }

  public void setMapTileLayer(String mapTileLayer) {
    this.mapTileLayer = mapTileLayer;
  }

  public String getMapTileLayer() {
    return mapTileLayer;
  }

  public void setMapSource(String mapSource) {
    this.mapSource = mapSource;
  }

  public String getMapSource() {
    return mapSource;
  }

  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  public String getDataSource() {
    return dataSource;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getFormat() {
    return format;
  }

  public void setTransparent(boolean transparent) {
    this.transparent = transparent;
  }

  public boolean isTransparent() {
    return transparent;
  }

  public void setCoordSys(CoordSys coordsys) {
    this.coordSys = coordsys;
  }

  public CoordSys getCoordSys() {
    return coordSys;
  }

  public int getLevelCount() {
    int levelCount = 0;
    if (getCoordSys() != null) {
      if (getCoordSys().getZoomLevels() != null) {
        levelCount = getCoordSys().getZoomLevels().size();
      }
    }
    return levelCount;
  }
}
