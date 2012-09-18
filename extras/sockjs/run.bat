call mvn compile
xcopy c:\workspace\Atmosphere-master\extras\sockjs\target\classes\*.* C:\dev\jetty-distribution-8.1.7.v20120910\webapps\atmosphere-sockjs-chat\WEB-INF\classes /S /Y /Q
