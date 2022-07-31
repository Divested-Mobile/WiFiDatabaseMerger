WiFi Database Merger
====================

Quick Start
-----------
- Compile the program
- cd WPSDB
- sh download.sh
- java -jar WiFiDatabaseMerger.jar WPSDB-USA48.cfg

Prebuilts
---------
- via CI: https://gitlab.com/divested-mobile/wifidatabasemerger/-/jobs/artifacts/master/browse?job=build

Config File Format
------------------
- First line must be the bounding box: top left lat, top left lon, bottom right lat, bottom right lon
- All following lines must be databases: file, delimeter, bssid col #, lat col #, lon col #, quoted

Donate
-------
- https://divested.dev/donate
