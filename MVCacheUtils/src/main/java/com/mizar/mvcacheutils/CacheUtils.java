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

import com.mizar.mvcacheutils.MapCacheTools;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author mark.millman@mizar.com
 */
public class CacheUtils {

    private static List<Integer> parseLevel(String sLevelRanges) {
        List<Integer> ilevels = new ArrayList<Integer>();
        String[] sLevelList = sLevelRanges.split(",");
        for (String sLevelRange : sLevelList) {
            String[] sLevels = sLevelRange.split("-");
            if ( sLevels.length == 1 ) {
                ilevels.add(Integer.valueOf(sLevels[0]));
            } else {
                int start = Integer.valueOf(sLevels[0]).intValue();
                int end = Integer.valueOf(sLevels[1]).intValue();
                for ( int i=start; i<= end; i++) {
                    ilevels.add(i);//new Integer(i));
                }
            }
        }
        return ilevels;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String mode = "refresh";
        String parameterFile = null;
        String user = null;
        String password = null;
        String serverUrl = null;
        String mcsAdminUrl = null;
        String adminUrl = null;
        String dataSource = null;
        String cacheName = null;
        List<Integer> levels = null;
        double[] corners = null;

        for (String arg : args) {
            String[] kv = arg.split("=");
            if (kv[0].toLowerCase().startsWith("par")) {
                parameterFile = kv[1];
            }
        }

        BufferedReader br = null;

        if (parameterFile == null) {
            try {
                String currentDir = System.getProperty("user.dir");
                String defaultJson = currentDir + "\\" + "MVCacheUtils.json";
                FileReader fr = new FileReader(defaultJson);
                br = new BufferedReader(fr);
                if (br != null) {
                    parameterFile = defaultJson;
                }
            } catch (IOException ignore) {
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ignore2) {
                    }
                }
            }
        }
        String jsonData = null;
        if (parameterFile != null) {
            JSONObject oJson = null;
            try {
                FileReader fr = new FileReader(parameterFile);
                br = new BufferedReader(fr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (jsonData == null) {
                        // this looks stupid but if I didn't do this jsonData began with a null
                        // and the JSON parser chocked on it.
                        jsonData = line;
                    } else {
                        jsonData += line;
                    }
                }
                jsonData = jsonData.trim();
                oJson = new JSONObject(jsonData);
                if (oJson != null) {
                    if (oJson.has("user")) {
                        user = oJson.getString("user");
                    }
                    if (oJson.has("password")) {
                        password = oJson.getString("password");
                    }

                    if (oJson.has("serverUrl")) {
                        serverUrl = oJson.getString("serverUrl");
                    }
                    if (oJson.has("adminUrl")) {
                        adminUrl = oJson.getString("adminUrl");
                    }
                    if (oJson.has("mcsAdminUrl")) {
                        mcsAdminUrl = oJson.getString("mcsAdminUrl");
                    }
                    if (oJson.has("dataSource")) {
                        dataSource = oJson.getString("dataSource");
                    }
                    if (oJson.has("cacheName")) {
                        dataSource = oJson.getString("cacheName");
                    }
                    if (oJson.has("level")) {
                        levels = parseLevel(oJson.getString("level"));
                    }
                }
            } catch (IOException e) {
                String problem = e.getLocalizedMessage();
                System.out.println(problem);
                e.printStackTrace();
                System.exit(1);
            } catch (Exception ee) {
                String problem = ee.getLocalizedMessage();
                System.out.println(problem);
                System.exit(1);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();;
                    }
                }
            }
        }

        for (String arg : args) {
            String[] kv = arg.split("=");
            if (kv[0].toLowerCase().startsWith("mode")) {
                mode = kv[1];
            } else if (kv[0].toLowerCase().startsWith("u")) {
                user = kv[1];
            } else if (kv[0].toLowerCase().startsWith("pas")) {
                password = kv[1];
            } else if (kv[0].toLowerCase().startsWith("s")) {
                serverUrl = kv[1];
            } else if (kv[0].toLowerCase().startsWith("a")) {
                adminUrl = kv[1];
            } else if (kv[0].toLowerCase().startsWith("m")) {
                mcsAdminUrl = kv[1];
            } else if (kv[0].toLowerCase().startsWith("d")) {
                dataSource = kv[1];
            } else if (kv[0].toLowerCase().startsWith("c")) {
                cacheName = kv[1];
            } else if (kv[0].toLowerCase().startsWith("l")) {
                levels = parseLevel(kv[1]);
            } else if (kv[0].toLowerCase().startsWith("r")) {
                String[] range = kv[1].split(",");
                if (range.length == 4) {
                    corners = new double[4];
                    corners[0] = Double.valueOf(range[0]).doubleValue();
                    corners[1] = Double.valueOf(range[1]).doubleValue();
                    corners[2] = Double.valueOf(range[2]).doubleValue();
                    corners[3] = Double.valueOf(range[3]).doubleValue();
                }
            }
        }

        int[] ilevels = null;
        if (levels != null && levels.size() > 0) {
            ilevels = new int[levels.size()];
            for (int i = 0; i < levels.size(); i++) {
                ilevels[i] = levels.get(i).intValue();
            }
        }
        MapCacheTools tool = new MapCacheTools(user, password, serverUrl, mcsAdminUrl, adminUrl, dataSource);
        String response = null;
        if (mode.startsWith("clear")) {
            if (corners == null) {
                response = tool.clearCache(cacheName, ilevels);
            } else {
                response = tool.clearCache(cacheName, ilevels, corners);
            }
        } else if (mode.startsWith("refresh")) {
            if (corners == null) {
                response = tool.refreshCache(cacheName, ilevels);
            } else {
                response = tool.refreshCache(cacheName, ilevels, corners);
            }
        } else if (mode.startsWith("prefetch")) {
            if (corners == null) {
                response = tool.prefetchCache(cacheName, ilevels);
            } else {
                response = tool.prefetchCache(cacheName, ilevels, corners);
            }
        }
        if ( response != null ) {
            System.out.println(response);
        }
    }
}
