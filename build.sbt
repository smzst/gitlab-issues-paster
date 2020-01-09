name := """gitlab-issues-paster"""

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "com.google.api-client"   % "google-api-client"          % "1.23.0",
  "com.google.oauth-client" % "google-oauth-client-jetty"  % "1.23.0",
  "com.google.apis"         % "google-api-services-sheets" % "v4-rev516-1.23.0",
  "com.lihaoyi"             %% "requests"                  % "0.2.0",
  "com.lihaoyi"             %% "upickle"                   % "0.8.0",
  "com.typesafe"            % "config"                     % "1.4.0"
)
