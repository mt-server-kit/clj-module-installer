
# clj-module-installer

### Overview

The <strong>clj-module-installer</strong> is a simple in-project package handler
that you can use to manage your modules' initializations in Clojure projects.

### deps.edn

```
{:deps {bithandshake/clj-module-installer {:git/url "https://github.com/bithandshake/clj-module-installer"
                                           :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}
```

### Current version

Check out the latest commit on the [release branch](https://github.com/bithandshake/clj-module-installer/tree/release).

### Documentation

The <strong>clj-module-installer</strong> functional documentation is [available here](documentation/COVER.md).

### Changelog

You can track the changes of the <strong>clj-module-installer</strong> library [here](CHANGES.md).

# Usage

### Index

- [How to register a package installer?](#how-to-register-a-package-installer)

- [How to run the registered installers?](#how-to-run-the-registered-installers)

- [Examples](#examples)

### How to register a package installer?

The [`module-installer.api/reg-installer!`](documentation/clj/module-installer/API.md#reg-installer)
function registers a function that will be applied when the `check-installation!`
function called.

```
(defn my-installer-f [] (println "Installing ..."))
(reg-installer! ::my-installer {:installer-f my-installer-f})
```

By specifying the `:priority` property, you can control the installation order.
As higher is the `:priority` value, the installer function will be applied as sooner.

```
(defn my-installer-f [] (println "Installing ..."))
(reg-installer! ::my-installer {:installer-f my-installer-f :priority 1})
```

The `:installer-f` function's return value will be passed to the `:test-f` function,
and the `:test-f` function's return value will be evaluated as a boolean.
If false the installation will be qualified as an installation failure,
and the package will be reinstalled when the 'check-installation!' function next called.

### How to run the registered installers?

The [`module-installer.api/check-installation!`](documentation/clj/module-installer/API.md#check-installation)
function checks whether all the registered installers are successfully installed.
If not, it runs the registered but not (successfully) installed functions, then exits
the server.

```
(check-installation!)
```

### Examples

In the following example there are two independent namespaces with their
own installer functions and a boot loader which calls the `check-installer!`
function.

In the first namespace we register an installer function in order to do some
necessary stuff before the module get used.

```
(ns my-namespace-a
    (:require [module-installer.api :as module-installer]))

(defn my-installer-f []
  ; Create files, set default settings, etc.
  "Now, I'm installed!")    

(module-installer/reg-installer! ::my-installer {:installer-f my-installer-f})  
```

In the second namespace we register one another installer.

```
(ns my-namespace-b
    (:require [module-installer.api :as module-installer]))

(defn my-installer-f []
  ; Create files, set default settings, etc.
  "Now, I'm installed too!")    

(module-installer/reg-installer! ::my-installer {:installer-f my-installer-f})  
```

In the server boot loader we place the `check-installation!` function to run
the registered installers before the server starts.

> It might be the best time to call the `check-installation!` function is when the
  server already connected to the database and the installers can reach the db.

When we first call the `run-server!` function it connects to the database,
calls the `check-installation!` function which calls the previously registered
installer functions then it exits the server.
When we next call the `run-server!` function it connects to the database,
calls the `check-installation!` function which does nothing because the installer
functions are already called and then the HTTP server starts.

```
(ns my-boot-loader
    (:require [module-installer.api :as module-installer]))

(defn run-server!
  []
  (connect-to-database!)
  (module-installer/check-installation!)
  (start-http-server!)
  ...)    
```
