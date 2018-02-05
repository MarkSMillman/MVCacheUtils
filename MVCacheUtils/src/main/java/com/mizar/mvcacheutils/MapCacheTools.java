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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.JDOMException;

public class MapCacheTools {

    private static Log getLogger() {
        return LogFactory.getLog(MapCacheTools.class.getName());
    }

    private String mcsAdminUrl;
    private String adminUrl;
    private String dataSource;
    private String user;
    private String password;
    private List<CacheInstance> cacheInstances = null;
    private MapTileServerResponse mapTileServerResponse;
    private Map<String, ClientConfig> clientConfigs = new HashMap<String, ClientConfig>();
    private int requestId;
    private int estimatedTileRemaining;
    private int estimatedTimeRemaining;
    private int estimatedDiskSpaceRequired;
    private boolean debug = false;

    private String createCacheInstance
            = "<create_cache_instance data_source='mvdemo'>" + "<cache_instancename='demo_map' image_format='PNG'><internal_map_source base_map='demo_map'/>"
            + "<cache_storage root_path='/scratch/mapcache'/><coordinate_systemsrid='8307' " + "minX='-180' maxX='180' minY='-90' maxY='90'/><tile_image width='250' height='250'/>"
            + "<zoom_levels levels='10' min_scale='5000' max_scale='10000000'></zoom_levels></cache_instance></create_cache_instance>";

    private String redefineCacheInstance
            = "<redefine_cache_instance data_source='mvdemo'>" + "<cache_instancename='demo_map' image_format='PNG'><internal_map_source base_map='demo_map'/>"
            + "<cache_storage root_path='/scratch/mapcache'/><coordinate_systemsrid='8307' " + "minX='-180' maxX='180' minY='-90' maxY='90'/><tile_image width='250' height='250'/>"
            + "<zoom_levels levels='10' min_scale='5000' max_scale='10000000'></zoom_levels></cache_instance></redefine_cache_instance>";

    /*
  http://hc.apache.org/httpcomponents-client/
  http://localhost:7101/mapviewer/tilecache/mcsadmin.html
     */
    public MapCacheTools(String user, String password, String serverUrl, String mcsAdminUrl, String adminUrl, String dataSource) {

        this.user = user;
        this.password = password;
        this.mcsAdminUrl = mcsAdminUrl;
        this.adminUrl = adminUrl;
        this.dataSource = dataSource;
        getLogger();
    }

    /**
     * The method will work with either the mcserver or mcsadmin urls
     *
     * @return the response to the <code>get_cache_status</code> request
     */
    public String getCacheStatus() {
        String request = wrap("<get_cache_status />");
        String response = sendRequest(getMcsAdminUrl(), request);
        setTileServerStatus(response);
        return response;
    }

    /**
     * The method will work with either the mcserver or mcsadmin urls
     *
     * @param cacheName
     * @return
     */
    public String getClientConfig(String cacheName) {
        String request = wrap("<get_client_config map_cache_names='" + getMapCache(cacheName) + "' format='JSON'/>");
        String response = sendRequest(getMcsAdminUrl(), request);
        if (StringUtils.isNotEmpty(response)) {
            this.clientConfigs.clear();
            ClientConfig clientConfig = new ClientConfig(response);
            if (clientConfig != null && StringUtils.isNotEmpty(clientConfig.getMapTileLayer())) {
                this.clientConfigs.put(clientConfig.getDataSource() + "." + clientConfig.getMapTileLayer(), clientConfig);
            }
        }
        return response;
    }

    /**
     * The method requires the mcsadmin serverUrl, otherwise it will do nothing
     *
     * @param cacheName
     * @param cleanDisk
     * @param removePermanently
     * @return
     */
    public String removeCacheInstance(String cacheName, boolean cleanDisk, boolean removePermanently) {
        String request
                = wrap("<remove_cache_instance map_cache_name='" + getMapCache(cacheName) + "' clean_disk='" + Boolean.valueOf(cleanDisk).toString() + "' remove_permanently='"
                        + Boolean.valueOf(removePermanently).toString() + "'/>");
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * @param requestNumber
     * @return
     */
    public String getAdminRequestStatus(int requestNumber) {
        String request = wrap("<get_admin_request_status requests_num='" + Integer.valueOf(requestNumber).toString() + "'/>");
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * The method requires the mcsadmin serverUrl, otherwise it will do nothing
     *
     * @param cacheName
     * @return
     */
    public String takeCacheOffline(String cacheName) {
        String request = wrap("<take_cache_offline map_cache_name='" + getMapCache(cacheName) + "'/>");
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * The method requires the mcsadmin serverUrl, otherwise it will do nothing
     *
     * @param cacheName
     * @return
     */
    public String bringCacheOnline(String cacheName) {
        String request = wrap("<bring_cache_online map_cache_name='" + getMapCache(cacheName) + "'/>");
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * The method requires the mcsadmin serverUrl, otherwise it will do nothing
     *
     * @return the response to the <code>restart_cache_server</code> request
     */
    public String restartCacheServer() {
        String request = wrap("<restart_cache_server />");
        return sendRequest(getMcsAdminUrl(), request);
    }

    private String tileAdminTask(String operation, String mapTileLayer, int[] levels, double[] ordinates) {
        String request = "<tile_admin_task operation=\"" + operation + "\" ";
        request += "map_tile_layer=\"" + dataSource + "." + mapTileLayer + "\" ";
        if (levels != null && levels.length > 0) {

            request += "zoom_levels=\"" + getLevels(levels) + "\" ";
        }
        if (ordinates != null && ordinates.length == 4) {
            request += "bounding_box=\"" + getRange(ordinates) + "\" ";
        }
        request += ">";
        request += "</tile_admin_task>";
        return request;
    }

    /**
     * The method requires the mcsadmin serverUrl, otherwise it will do nothing
     *
     * @param cacheName a name of a cache map in the current data source
     * @param level a valid zoomLevel for the cached map
     * @return the response to the <code>clear_cache map_cache_name</code>
     * request
     */
    public String clearCache(String cacheName, int level) {
        int[] levels = new int[1];
        levels[0] = level;
        String request = wrap(tileAdminTask("clear_tiles", cacheName, levels, null));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * The method interprets <code>geometry</code> as a point if it has a length
     * of 2. Otherwise as a polyline unless the first and last coordinates are
     * the same and then it is assumed to be a polygon.
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     *
     * @param cacheName a name of a cache map in the current data source
     * @param level a valid zoomLevel for the cached map
     * @param ordinates a 2D geometry
     * @return the response to the <code>clear_cache map_cache_name</code>
     * request
     */
    public String clearCache(String cacheName, int level, double[] ordinates) {
        int[] levels = new int[1];
        levels[0] = level;
        String request = wrap(tileAdminTask("clear_tiles", cacheName, levels, ordinates));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     *
     * @param cacheName a name of a cache map in the current data source
     * @param levels an array of valid zoomLevels for the cached map
     * @return the response to the <code>clear_cache map_cache_name</code>
     * request
     */
    public String clearCache(String cacheName, int[] levels) {
        String request = wrap(tileAdminTask("clear_tiles", cacheName, levels, null));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     * The method interprets <code>geometry</code> as a point if it has a length
     * of 2. Otherwise as a polyline unless the first and last coordinates are
     * the same and then it is assumed to be a polygon.
     *
     * @param cacheName a name of a cache map in the current data source
     * @param levels an array of valid zoomLevels for the cached map
     * @param ordinates a 2D geometry
     * @return the response to the <code>clear_cache map_cache_name</code>
     * request
     */
    public String clearCache(String cacheName, int[] levels, double[] ordinates) {
        String request = wrap(tileAdminTask("clear_tiles", cacheName, levels, ordinates));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     *
     * @param cacheName a name of a cache map in the current data source
     * @param level a valid zoomLevel for the cached map
     * @return the response to the <code>prefetch_cache map_cache_name</code>
     * request
     */
    public String prefetchCache(String cacheName, int level) {
        int[] levels = new int[1];
        levels[0] = level;
        //String request = wrap("<prefetch_cache map_cache_name='" + getMapCache(cacheName) + "' zoom_levels='" + getLevels(level) + "'/>");
        String request = wrap(tileAdminTask("fetch_tiles", cacheName, levels, null));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     * The method interprets <code>geometry</code> as a point if it has a length
     * of 2. Otherwise as a polyline unless the first and last coordinates are
     * the same and then it is assumed to be a polygon.
     *
     * @param cacheName a name of a cache map in the current data source
     * @param level a valid zoomLevel for the cached map
     * @param ordinates a 2D geometry
     * @return the response to the <code>prefetch_cache map_cache_name</code>
     * request
     */
    public String prefetchCache(String cacheName, int level, double[] ordinates) {
        int[] levels = new int[1];
        levels[0] = level;
        //String request = wrap("<prefetch_cache map_cache_name='" + getMapCache(cacheName) + "' zoom_levels='" + getLevels(level) + "' bounding_box='" + getRange(ordinates) + "'/>");
        String request = wrap(tileAdminTask("fetch_tiles", cacheName, levels, ordinates));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     *
     * @param cacheName a name of a cache map in the current data source
     * @param levels an array of valid zoomLevels for the cached map
     * @return the response to the <code>prefetch_cache map_cache_name</code>
     * request
     */
    public String prefetchCache(String cacheName, int[] levels) {
        //String request = wrap("<prefetch_cache map_cache_name='" + getMapCache(cacheName) + "' zoom_levels='" + getLevels(levels) + "'/>");
        String request = wrap(tileAdminTask("fetch_tiles", cacheName, levels, null));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     * The method interprets <code>geometry</code> as a point if it has a length
     * of 2. Otherwise as a polyline unless the first and last coordinates are
     * the same and then it is assumed to be a polygon.
     *
     * @param cacheName a name of a cache map in the current data source
     * @param levels an array of valid zoomLevels for the cached map
     * @param ordinates a 2D geometry
     * @return the response to the <code>prefetch_cache map_cache_name</code>
     * request
     */
    public String prefetchCache(String cacheName, int[] levels, double[] ordinates) {
        //String request = wrap("<prefetch_cache map_cache_name='" + getMapCache(cacheName) + "' zoom_levels='" + getLevels(levels) + "' bounding_box='" + getRange(ordinates) + "'/>");
        String request = wrap(tileAdminTask("fetch_tiles", cacheName, levels, ordinates));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     *
     * @param cacheName a name of a cache map in the current data source
     * @param level a valid zoomLevel for the cached map
     * @return the response to the <code>refresh_cache map_cache_name</code>
     * request
     */
    public String refreshCache(String cacheName, int level) {
        //String request = wrap("<refresh_cache map_cache_name='" + getMapCache(cacheName) + "' zoom_levels='" + getLevels(level) + "'/>");
        int[] levels = new int[1];
        levels[0] = level;
        String request = wrap(tileAdminTask("refresh_tiles", cacheName, levels, null));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     * The method interprets <code>geometry</code> as a point if it has a length
     * of 2. Otherwise as a polyline unless the first and last coordinates are
     * the same and then it is assumed to be a polygon.
     *
     * @param cacheName a name of a cache map in the current data source
     * @param level a valid zoomLevel for the cached map
     * @param ordinates a 2D geometry
     * @return the response to the <code>refresh_cache map_cache_name</code>
     * request
     */
    public String refreshCache(String cacheName, int level, double[] ordinates) {
        //String request = wrap("<refresh_cache map_cache_name='" + getMapCache(cacheName) + "' zoom_levels='" + getLevels(level) + "' bounding_box='" + getRange(ordinates) + "'/>");
        int[] levels = new int[1];
        levels[0] = level;
        String request = wrap(tileAdminTask("refresh_tiles", cacheName, levels, ordinates));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     *
     * @param cacheName a name of a cache map in the current data source
     * @param levels an array of valid zoomLevels for the cached map
     * @return the response to the <code>refresh_cache map_cache_name</code>
     * request
     */
    public String refreshCache(String cacheName, int[] levels) {
        // String request = wrap("<refresh_cache map_cache_name='" + getMapCache(cacheName) + "' zoom_levels='" + getLevels(levels) + "'/>");
        String request = wrap(tileAdminTask("refresh_tiles", cacheName, levels, null));
        return sendRequest(getMcsAdminUrl(), request);
    }

    /**
     * <p>
     * The method requires the mcsadmin serverUrl, otherwise it will do
     * nothing</p>
     * The method interprets <code>geometry</code> as a point if it has a length
     * of 2. Otherwise as a polyline unless the first and last coordinates are
     * the same and then it is assumed to be a polygon.
     *
     * @param cacheName a name of a cache map in the current data source
     * @param levels an array of valid zoomLevels for the cached map
     * @param ordinates a 2D geometry
     * @return the response to the <code>refresh_cache map_cache_name</code>
     * request
     */
    public String refreshCache(String cacheName, int[] levels, double[] ordinates) {
        //String request = wrap("<refresh_cache map_cache_name='" + getMapCache(cacheName) + "' zoom_levels='" + getLevels(levels) + "' bounding_box='" + getRange(ordinates) + "'/>");
        String request = wrap(tileAdminTask("refresh_tiles", cacheName, levels, ordinates));
        return sendRequest(getMcsAdminUrl(), request);
    }

    private String wrap(String request) {
        return "<?xml version='1.0' standalone='yes'?><map_cache_admin_request>" + request + "</map_cache_admin_request>";
    }

    private String getLevels(int level) {
        return Integer.valueOf(level).toString();
    }

    private String getLevels(int[] levels) {
        StringBuffer levelString = new StringBuffer();
        for (int i = 0; i < levels.length; i++) {
            levelString.append(Integer.valueOf(levels[i]).toString());
            if (i != levels.length - 1) {
                levelString.append(",");
            }
        }
        return levelString.toString();
    }

    private String getRange(double[] range) {
        StringBuffer boundingBox = new StringBuffer();
        boundingBox.append(Double.valueOf(range[0]).toString() + ",");
        boundingBox.append(Double.valueOf(range[1]).toString() + ",");
        boundingBox.append(Double.valueOf(range[2]).toString() + ",");
        boundingBox.append(Double.valueOf(range[3]).toString());
        return boundingBox.toString();
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getMapCache(String cacheName) {
        return dataSource + "." + cacheName;
    }

    /**
     * @param adminUrl must be the full URL specification of the administration
     * (<code>mcsadmin</code>) servlet as specified in <code>web.xml</code>
     */
    public void setMcsAdminUrl(String adminUrl) {
        this.mcsAdminUrl = adminUrl;
    }

    public String getMcsAdminUrl() {
        return mcsAdminUrl;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private void setMapTileServerResponse(String response) {
        try {
            this.mapTileServerResponse = new MapTileServerResponse(response);
            this.cacheInstances = mapTileServerResponse.getMapCaches();
        } catch (JDOMException e) {
            getLogger().error(e.getLocalizedMessage());
        } catch (IOException e) {
            getLogger().error(e.getLocalizedMessage());
        }
    }

    private void setTileServerStatus(String tileServerStatusXML) {
        try {
            this.cacheInstances = CacheInstance.getFromTileServerStatus(tileServerStatusXML);
        } catch (JDOMException e) {
            getLogger().error(e.getLocalizedMessage());
        } catch (IOException e) {
            getLogger().error(e.getLocalizedMessage());
        }
    }

    public List<CacheInstance> getCacheInstances() {
        return cacheInstances;
    }

    public Map<String, ClientConfig> getClientConfigs() {
        return clientConfigs;
    }

    public MapTileServerResponse getMapTileServerResponse() {
        return mapTileServerResponse;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getEstimatedTileRemaining() {
        return estimatedTileRemaining;
    }

    public int getEstimatedTimeRemaining() {
        return estimatedTimeRemaining;
    }

    public int getEstimatedDiskSpaceRequired() {
        return estimatedDiskSpaceRequired;
    }

    public String sendRequests(String url, List<String> requests) throws IOException {
        String response = " ";
        HttpState initialState = new HttpState();
        HttpClient httpclient = new HttpClient();
        PostMethod reqPost = null;
        PostMethod postMethod = null;
        if (debug) {
            System.out.println(url);
            for (String request : requests) {
                System.out.println(request);
            }
        }
        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.setState(initialState);
            // RFC 2101 cookie management spec is used per default
            // to parse, validate, format & match cookies
            httpclient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
            GetMethod httpget = new GetMethod(url);
            int httpResponseCode = 0;
            try {
                httpResponseCode = httpclient.executeMethod(httpget);
                if (httpResponseCode == 200) {
                    getLogger().info("## Response status code: " + httpResponseCode);

                    Cookie[] cookies = httpclient.getState().getCookies();
                    httpget.releaseConnection();
                    getLogger().info("## Authenticating with the server...");
                    int ix = url.indexOf("/mapviewer");
                    if (url.length() - ix < 11) {
                        url += "/";
                    }
                    ix += 11;
                    String s1 = url.substring(0, ix);
                    String s2 = s1 + "j_security_check";
                    postMethod = new PostMethod(s2);

                    NameValuePair[] postData = new NameValuePair[2];
                    postData[0] = new NameValuePair("j_username", user);
                    postData[1] = new NameValuePair("j_password", password);
                    postMethod.addParameters(postData);

                    for (int i = 0; i < cookies.length; i++) {
                        initialState.addCookie(cookies[i]);
                    }
                    httpclient.setState(initialState);

                    httpResponseCode = httpclient.executeMethod(postMethod);
                    postMethod.releaseConnection();
                    postMethod = null;
                    getLogger().info("... done.");

                    for (String requestBody : requests) {
                        getLogger().info("## Sending xml request from file: " + requestBody);
                        NameValuePair[] data = new NameValuePair[1];
                        data[0] = new NameValuePair("xml_request", requestBody);
                        reqPost = new PostMethod(url);
                        reqPost.addParameters(data);

                        httpResponseCode = httpclient.executeMethod(reqPost);
                        getLogger().info("## Server response:");

                        InputStream responseStream = null;
                        try {
                            responseStream = reqPost.getResponseBodyAsStream();
                            response += readInputStream(responseStream);
                        } catch (IOException e) {
                            getLogger().error(e.getLocalizedMessage());
                        } finally {
                            try {
                                if (responseStream != null) {
                                    responseStream.close();
                                }
                            } catch (IOException e) {
                                getLogger().error(e.getLocalizedMessage());
                            }
                        }
                    }
                } else {
                    if (httpResponseCode == 404) {
                        response = "HTTP response code " + Integer.toString(httpResponseCode) + " " + url + " was not found.";
                    } else {
                        response = "HTTP response code " + Integer.toString(httpResponseCode) + " " + url + " was not found.";
                    }
                }
            } catch (HttpException httpe) {
                getLogger().error(httpe.getLocalizedMessage());
            } catch (ConnectException ce) {
                response = ce.getLocalizedMessage() + " to " + mcsAdminUrl + "failed";
            } catch (Exception e) {
                response = e.getLocalizedMessage();
            }
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
            if (reqPost != null) {
                reqPost.releaseConnection();
            }
        }

        setMapTileServerResponse(response);

        return response;
    }

    /**
     * @param is an open InputStream
     * @return the entire InputStream as a String
     * @throws IOException
     */
    public String readInputStream(InputStream is) throws IOException {
        String streamInAString = null;
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        int c;

        try {
            while ((c = is.read()) != -1) {
                content.write(c);
            }
            is.close();
            content.flush();
            streamInAString = content.toString();
            content.close();
        } catch (Exception e) {
            ;
        }
        return streamInAString;
    }

    /**
     * @param requestBody
     * @return the response to the request, might include an error message or
     * single space if no response found.
     */
    public String sendRequest(String url, String requestBody) {
        List<String> requests = new ArrayList<String>();
        requests.add(requestBody);

        String response = "";

        try {
            System.out.println(requests.get(0));
            response = sendRequests(url, requests);
        } catch (IOException ex) {
            Logger.getLogger(MapCacheTools.class.getName()).log(Level.SEVERE, null, ex);
            response = ex.getLocalizedMessage();
        }

        return response;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
