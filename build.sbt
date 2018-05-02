organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val lombok = "org.projectlombok" % "lombok" % "1.16.12"

lazy val `holiday-listing` = (project in file("."))
  .aggregate(`reservation-api`, `reservation-impl`, `search-api`, `search-impl`, `web-gateway`)

lazy val `reservation-api` = (project in file("reservation-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson,
      lombok
    )
  )

lazy val `reservation-impl` = (project in file("reservation-impl"))
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaBroker,
      lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`reservation-api`)


lazy val `search-api` = (project in file("search-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `search-impl` = (project in file("search-impl"))
  .enablePlugins(LagomScala, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslKafkaClient,
      macwire
    )
  )
  .dependsOn(`search-api`)

lazy val `web-gateway` = (project in file("web-gateway"))
  .enablePlugins(PlayScala && LagomPlay && SbtReactiveAppPlugin)
  .disablePlugins(PlayLayoutPlugin, PlayFilters)
  .dependsOn(`search-api`)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomScaladslServer,
      filters,
      macwire,
      scalaTest,
      "org.webjars" % "foundation" % "6.2.3",
      "org.webjars" % "foundation-icon-fonts" % "d596a3cfb3"
    ),
    lagomWatchDirectories ++= (sourceDirectories in(Compile, TwirlKeys.compileTemplates)).value,
    httpIngressPaths := Seq("/")

  )
