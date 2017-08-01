MVCacheUtils is a command line tool that an be included in scripts to perform 
operations documented in Section 5 "MapViewer XML Requests: Administrative and other".
of the Oracle(r) Fusion Middleware: User's Guide to Oracle MapViewer 12c Release 1 (12.1)
E29624-03, which is included in this project.

The command line may be as follows

 > mvcacheutils parameters
 
 parameters include
 
  mode=[refresh|clear|prefetch] 
  user=<username> 
  password=<password> 
  datasource=<datasource> 
  cacheName=<layer-name>
  level=<levels, may include commas and dashes>
  serverUrl=<mapviewer url>
  mcsAdminUrl=<mapviewer map cache server url> DEFAULTS to <serverUrl>/mcsadmin
  range=<minX,minY,maxX,maxY>
  parameters=<parameter JSON file>
  
 e.g. 
 
 > mvcacheutils para=MyParams.json pass=welcome1
 
 The parameter JSON file may look as follows.
 
 {
    "user": "admin",
    "password": "welcome1",
    "serverUrl": "http://localhost:8080/mapviewer",
    "dataSource": "MVDEMO",
    "cacheName" : "DEMO_MAP",
    "level" : "0,2-4"
}

If no parameter file is provided the application looks for a default parameters file MVCacheUtils.json
in the current folder.  Command line arguments override any parameter file arguments.

DEVELOPER NOTES:

The messages sent to the Map Cache Server changed between 11c and 12g and only the CLEAR messages have been updated
as of August 1, 2017.  More problematically the response to any request appears to be "not enough parameters for the REST request".
I expect that the solution is to modify MapCacheTools.sendRequests to package the request in a REST wrapper.  Life used to be simpler.

Mark Millman
mark.millman@mizar.com
 
 