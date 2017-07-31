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

import com.mizar.mvcacheutils.ClientConfig;
import java.util.List;

public class CoordSys {
  private int srid;
  private String type;
  private double distConvFactor;
  private double minX;
  private double minY;
  private double maxX;
  private double maxY;
  private List<ZoomLevel> zoomLevels;

  protected CoordSys() {
  }

  protected CoordSys(String frag) {
    this.srid = Integer.valueOf(ClientConfig.getValue(frag, "srid", ","));
    this.type = ClientConfig.getValue(frag, "type", ",");
    this.distConvFactor = Double.valueOf(ClientConfig.getValue(frag, "distConvFactor", ","));
    this.minX = Double.valueOf(ClientConfig.getValue(frag, "minX", ","));
    this.minY = Double.valueOf(ClientConfig.getValue(frag, "minY", ","));
    this.maxX = Double.valueOf(ClientConfig.getValue(frag, "maxX", ","));
    this.maxY = Double.valueOf(ClientConfig.getValue(frag, "maxY", ","));
    this.zoomLevels = ZoomLevel.getZoomLevels(ClientConfig.getValue(frag, "zoomLevels", null));
  }

  public void setSrid(int srid) {
    this.srid = srid;
  }

  public int getSrid() {
    return srid;
  }

  public void setDistConvFactor(double distConvFactor) {
    this.distConvFactor = distConvFactor;
  }

  public double getDistConvFactor() {
    return distConvFactor;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setMinX(double minX) {
    this.minX = minX;
  }

  public double getMinX() {
    return minX;
  }

  public void setMinY(double minY) {
    this.minY = minY;
  }

  public double getMinY() {
    return minY;
  }

  public void setMaxX(double maxX) {
    this.maxX = maxX;
  }

  public double getMaxX() {
    return maxX;
  }

  public void setMaxY(double maxY) {
    this.maxY = maxY;
  }

  public double getMaxY() {
    return maxY;
  }

  public void setZoomLevels(List<ZoomLevel> zoomLevels) {
    this.zoomLevels = zoomLevels;
  }

  public List<ZoomLevel> getZoomLevels() {
    return zoomLevels;
  }

  public int getLevelCount() {
    int levelCount = 0;
    if ( getZoomLevels()!= null){
      levelCount = getZoomLevels().size();
    }
    return levelCount;
  }
}
