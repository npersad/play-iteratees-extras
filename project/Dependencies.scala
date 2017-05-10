import sbt._

object  Version  {

  val scalatestplus   = "1.4.0"
  val scalaVersion = "2.5.0"
}


object Library {
  val akkaActor             = "com.typesafe.akka"         %% "akka-actor"                               % Version.scalaVersion
  val akkaAgent             = "com.typesafe.akka"         %% "akka-agent"                               % Version.scalaVersion
  val akkaCamel             = "com.typesafe.akka"         %% "akka-camel"                               % Version.scalaVersion
  val akkaHttpCore          = "com.typesafe.akka"         %% "akka-http-core"                           % "10.0.5"
  val akakHttp              = "com.typesafe.akka"         %% "akka-http"                                % "10.0.5"
  val akkaHttpTestKit       = "com.typesafe.akka"         %% "akka-http-testkit"                        % "10.0.5"
  val akkaHttpSprayJson     = "com.typesafe.akka"         %% "akka-http-spray-json"                     % "10.0.5"
  val akkaHttpJackson       = "com.typesafe.akka"         %% "akka-http-jackson"                        % "10.0.5"
  val akkaHttpXml           = "com.typesafe.akka"         %% "akka-http-xml"                            % "10.0.5"
  val akkaCluster           = "com.typesafe.akka"         %% "akka-cluster"                             % Version.scalaVersion
  val akkaClusterMetrics    = "com.typesafe.akka"         %% "akka-cluster-metrics"                     % Version.scalaVersion
  val akkaClusterSharding   = "com.typesafe.akka"         %% "akka-cluster-sharding"                    % Version.scalaVersion
  val akkaClusterTools      = "com.typesafe.akka"         %% "akka-cluster-tools"                       % Version.scalaVersion
  val akkaDistributedData   = "com.typesafe.akka"         %% "akka-distributed-data"                    % Version.scalaVersion
  val akkaMultiNodeTestKit  = "com.typesafe.akka"         %% "akka-multi-node-testkit"                  % Version.scalaVersion
  val akkaOsgi              = "com.typesafe.akka"         %% "akka-osgi"                                % Version.scalaVersion
  val akkaPersistence       = "com.typesafe.akka"         %% "akka-persistence"                         % Version.scalaVersion
  val akkaPersistenceQuery  = "com.typesafe.akka"         %% "akka-persistence-query"                   % Version.scalaVersion
  val akkaPersistenceTck    = "com.typesafe.akka"         %% "akka-persistence-tck"                     % Version.scalaVersion
  val akkaRemote            = "com.typesafe.akka"         %% "akka-remote"                              % Version.scalaVersion
  val akkaSl4j              = "com.typesafe.akka"         %% "akka-slf4j"                               % Version.scalaVersion
  val akkaStream            = "com.typesafe.akka"         %% "akka-stream"                              % Version.scalaVersion
  val akkaStreamTestkit     = "com.typesafe.akka"         %% "akka-stream-testkit"                      % Version.scalaVersion
  val akkaTestKit           = "com.typesafe.akka"         %% "akka-testkit"                             % Version.scalaVersion
  val akkaTyped             = "com.typesafe.akka"         %% "akka-typed"                               % Version.scalaVersion
  val akkaContirbuted       = "com.typesafe.akka"         %% "akka-contrib"                             % Version.scalaVersion
  val akkaStreamJson        = "de.knutwalker"             %% "akka-stream-json"                         % "3.3.0"
  val akkaHttpJson          = "de.knutwalker"             %% "akka-http-json"                           % "3.3.0"
//  val anorm                 = "com.typesafe.play"         %% "anorm"                                    % "2.4.0"
  val jodaDateTime          = "joda-time"                 %  "joda-time"                                % "2.9.9"
  val akkaHttp              = "com.typesafe.akka"         %% "akka-http"                                % "10.0.5"

  val sl4jOrg = "org.slf4j" % "slf4j-nop" % "1.6.4"
}


object Dependencies {

  import Library._

  val list = List(
    akkaHttpCore,
    akkaHttp,
    akkaHttpTestKit,
    akkaHttpSprayJson,
    akkaHttpJackson,
    akkaHttpXml,
    akkaActor,
    akkaAgent,
    akkaCamel,
    akkaCluster,
    akkaClusterMetrics,
    akkaClusterSharding,
    akkaClusterTools,
    akkaDistributedData,
    akkaMultiNodeTestKit,
    akkaOsgi,
    akkaPersistence,
    akkaPersistenceQuery,
    akkaPersistenceTck,
    akkaRemote,
    akkaSl4j,
    akkaStream,
    akkaStreamTestkit,
    akkaTestKit,
    akkaTyped,
    akkaContirbuted,
    akkaStreamJson,
    akkaHttpJson,
    jodaDateTime,
    akkaHttp
  )
}
