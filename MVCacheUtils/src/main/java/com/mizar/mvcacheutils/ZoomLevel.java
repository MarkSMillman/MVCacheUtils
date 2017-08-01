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

import com.mizar.mvcacheutils.ClientConfig;
import java.util.ArrayList;
import java.util.List;

public class ZoomLevel {
  private int level;
  private String name;
  private double scale;
  private double tileWidth;
  private double tileHeight;
  private int tileImageWidth;
  private int tileImageHeight;

  protected ZoomLevel() {
  }

  protected ZoomLevel(String frag) {
    this.level = Integer.valueOf(ClientConfig.getValue(frag, "zoomLevel", ","));
    this.name = ClientConfig.getValue(frag, "name", ",");
    this.scale = Double.valueOf(ClientConfig.getValue(frag, "scale", ","));
    this.tileWidth = Double.valueOf(ClientConfig.getValue(frag, "tileWidth", ","));
    this.tileHeight = Double.valueOf(ClientConfig.getValue(frag, "tileHeight", ","));
    this.tileImageWidth = Integer.valueOf(ClientConfig.getValue(frag, "tileImageWidth", ","));
    this.tileImageHeight = Integer.valueOf(ClientConfig.getValue(frag, "tileImageHeight", null));

  }

  protected static List<ZoomLevel> getZoomLevels(String frag) {
    List<ZoomLevel> levels = new ArrayList<ZoomLevel>();
    int start = frag.indexOf("{", 0);
    int end = frag.indexOf("}", start);
    while (start >= 0 && end < frag.length() - 1) {
      levels.add(new ZoomLevel(frag.substring(start, end)));
      start = frag.indexOf("{", end);
      end = frag.indexOf("}", start);
    }
    return levels;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getLevel() {
    return level;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setScale(double scale) {
    this.scale = scale;
  }

  public double getScale() {
    return scale;
  }

  public void setTileWidth(double tileWidth) {
    this.tileWidth = tileWidth;
  }

  public double getTileWidth() {
    return tileWidth;
  }

  public void setTileHeight(double tileHeight) {
    this.tileHeight = tileHeight;
  }

  public double getTileHeight() {
    return tileHeight;
  }

  public void setTileImageWidth(int tileImageWidth) {
    this.tileImageWidth = tileImageWidth;
  }

  public int getTileImageWidth() {
    return tileImageWidth;
  }

  public void setTileImageHeight(int tileImageHeight) {
    this.tileImageHeight = tileImageHeight;
  }

  public int getTileImageHeight() {
    return tileImageHeight;
  }
}
