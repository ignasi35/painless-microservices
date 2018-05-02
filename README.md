# holiday-listing (for "Painless Microservices" talk)

This repository is material for the "Painless Microservices" talk.

The content is an Airbnb rip-off implemented in [Lagom](https://www.lagomframework.com/) (reservation service and serach service) and [Play](https://www.playframework.com/) (web gateway).

### Running 

It is recommended that all operations are run interactively inside an `sbt` terminal. 

This code is built using [`sbt`](https://www.scala-sbt.org/). To start the `sbt` terminal:
 
  * open a terminal
  * `cd` into the folder where you cloned this repo
  * run the command `sbt`
  
#### Running the application locally 

Once in the `sbt` terminal (see above) use the `runAll` command (see also the [Getting Started](https://www.lagomframework.com/documentation/1.4.x/java/IntroGetStarted.html) guide). After a few seconds you'll see the green message `(Services started, press enter to stop and go back to the console...)`. Open the site in a browser at http://localhost:9000. 

### Running the tests

Once in the `sbt` terminal (see above) use the `test` command. 

### Deploying on minukube

Deploying on minikube requires some extra setup. Follow the [instructions](https://developer.lightbend.com/docs/lightbend-orchestration-kubernetes/latest/kubernetes-development.html) to make sure you have all the following:

 * An sbt project with sbt-reactive-app enabled (this repo)
 * reactive-cli installed
 * helm
 * minikube v0.25.0 or later
 * kubectl
 
With all the previous requisites installed, start minikube:

```
minikube start --memory 6000
minikube addons enable ingress
```

Then, return to the `sbt` terminal and use the `deploy minikube` command.  

When the deployment completes open the application in your browser:

```
open "http://$(minikube ip)"
```


## Copyright

This repository is based on James Roper's [crud-to-es](https://github.com/jroper/crud-to-es/tree/complete) and [holiday-listing-java](https://github.com/jroper/holiday-listing-java). You may use it yourself under the terms of the [Creative Commons Attribution](https://creativecommons.org/licenses/by/3.0/au/deed.en) license.
