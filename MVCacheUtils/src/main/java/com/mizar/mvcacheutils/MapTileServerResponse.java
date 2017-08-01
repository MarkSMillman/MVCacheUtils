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


import com.mizar.mvcacheutils.CacheInstance;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class MapTileServerResponse {
  private String requestStatus;
  private int requestId;
  private long estimatesTileRemaining;
  private long estimatesTimeRemaining;
  private long estimatesDiskSpaceRequired;
  private List<CacheInstance> mapCaches;

  public MapTileServerResponse() {
  }

  public MapTileServerResponse(Element instance) {
    //    this.dataSource = getElementValue(instance, "data_source");
    //    this.name = getElementValue(instance, "name");
    //    this.type = getElementValue(instance, "type");
    //    this.baseMap = getElementValue(instance, "base_map");
    //    this.zoomLevels = Integer.valueOf(getElementValue(instance, "zoom_levels")).intValue();
    //    this.cacheRoot = getElementValue(instance, "cache_root");
    //    this.status = getElementValue(instance, "status");
    //    this.online = Boolean.valueOf(getElementValue(instance, "online")).booleanValue();
  }

  public MapTileServerResponse(String mapTileServerResponse) throws JDOMException, IOException {
    List<CacheInstance> list = new ArrayList<CacheInstance>();
    if (StringUtils.isNotEmpty(mapTileServerResponse)) {
      SAXBuilder builder = new SAXBuilder();
      ByteArrayInputStream stream = new ByteArrayInputStream(mapTileServerResponse.getBytes());
      Document dom = builder.build(stream);
      Element rootElem = dom.getRootElement();
      List<Element> children = null;
      if (rootElem.getName().equals("map_tile_server_response")) {
        children = rootElem.getChildren();
        if (children != null && children.size() > 0) {
          for (Element child : children) {
            if (child.getName().equals("request_status")) {
              this.requestStatus = child.getValue().trim();
            } else if (child.getName().equals("request_id")) {
              this.requestId = Integer.valueOf(child.getValue()).intValue();
            } else if (child.getName().equals("estimates")) {
              List<Attribute> attrs = child.getAttributes();
              for (Attribute attr : attrs) {
                if (attr.getName().equals("tile_remaining")) {
                  this.estimatesTileRemaining = Long.valueOf(attr.getValue().trim()).longValue();
                } else if (attr.getName().equals("time_remaining")) {
                  this.estimatesTimeRemaining = Long.valueOf(attr.getValue().trim()).longValue();
                } else if (attr.getName().equals("disk_space_required")) {
                  this.estimatesDiskSpaceRequired = Long.valueOf(attr.getValue().trim()).longValue();
                }
              }
            } else if (child.getName().equals("tile_server_status")) {
              List<Element> instances = child.getChildren();
              if (instances != null && instances.size() > 0) {
                mapCaches = new ArrayList<CacheInstance>(instances.size());
                for (Element instance : instances) {
                  mapCaches.add(new CacheInstance(instance));
                }
              }
            }
          }
        }
      }
    }
  }

  public String getRequestStatus() {
    return requestStatus;
  }

  public int getRequestId() {
    return requestId;
  }


  public void setEstimatesDiskSpaceRequired(int estimatesDiskSpaceRemining) {
    this.estimatesDiskSpaceRequired = estimatesDiskSpaceRemining;
  }


  public List<CacheInstance> getMapCaches() {
    return mapCaches;
  }

  public void setEstimatesTileRemaining(long estimatesTileRemaining) {
    this.estimatesTileRemaining = estimatesTileRemaining;
  }

  public long getEstimatesTileRemaining() {
    return estimatesTileRemaining;
  }

  public void setEstimatesTimeRemaining(long estimatesTimeRemaining) {
    this.estimatesTimeRemaining = estimatesTimeRemaining;
  }

  public long getEstimatesTimeRemaining() {
    return estimatesTimeRemaining;
  }

  public long getEstimatesDiskSpaceRequired() {
    return estimatesDiskSpaceRequired;
  }
}
