# diffusion-stresstest
A very simple stress testing tool for Diffusion

History
=======

The `stresstest` Publisher and client were included in earlier versions of the Diffusion. 

Depends on the classic client API. Will run on Diffusion v5.9 and earlier.

Building
========

1. Clone this repository
2. set environment variable `DIFFUSION_HOME` to the root of your Diffusion installation, e.g. `export DIFFUSION_HOME=$HOME/Diffusion5.9.2/`
3. Run `mvn clean install` to build the stress client and the Publisher

Deployment
==========

1. Copy the `Stress` Publisher definition from `./etc/Publishers.xml` into `$DIFFUSION_HOME/etc/Publishers.xml`
2. Copy `stress-publisher/target/stress-publisher-1.0.0-SNAPSHOT.jar` into `$DIFFUSION_HOME/ext/` and restart your Diffusion server.