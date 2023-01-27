
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

### Index

- [How register a package installer?](#how-to-register-a-package-installer)

- [How install an EDN file?](#how-to-install-an-edn-file)

- [How to run the registered installers?](#how-to-run-the-registered-installers)

# Usage

### How register a package installer?

The [`module-installer.api/reg-installer!`](documentation/clj/module-installer/API.md#reg-installer)
function registers a function that will be applied when the `check-installation!`
function will be called.

```
(defn my-installer-f [] (println "Installing a module ..."))
(reg-installer! ::my-installer {:installer-f my-installer-f})
```

By specifying the `:priority` property, you can control the installation order.
As higher is the `:priority` value, the installer function will be applied as sooner.

```
(defn my-installer-f [] (println "Installing a module ..."))
(reg-installer! ::my-installer {:installer-f my-installer-f :priority 1})
```

The `:installer-f` function's return value will be passed to the `:test-f` function,
and the `:test-f` function's return value will be evaluted as a boolean.
If false the installation will be qualified as an installation failure,
and the package will be reinstalled when the 'check-installation!' next called.

### How to install an EDN file?

The [`module-installer.api/install-edn-file!`](documentation/clj/module-installer/API.md#install-edn-file)
function creates and EDN file onto the given filepath (only if it does not exist),
and when creating, writes the body and/or the header into the created file.
In case of the file exists it doesn't change it anymore.

```
(install-edn-file! "my-file.edn" {:my-item "My value"})
```

```
(install-edn-file! "my-file.edn" {:my-item "My value"} "My header\n...")
```

```
(install-edn-file! "my-file.edn" nil "My header\n...")
```

### How to run the registered installers?

The [`module-installer.api/check-installation!`](documentation/clj/module-installer/API.md#check-installation)
function checks whether all the registered installers are successfully installed.
If not, it runs the registered but not (successfully) installed functions, then quits
the server.

```
(check-installation!)
```

### What does it look like in practice?

In the following example there will be two independent namespaces with their
own installer functions and a boot loader which calls the `check-installer!`
function.

In the first namespace we register an installer function for creating necessary
things before the module get used.

```
(ns my-namespace-a
    (:require [module-installer.api :as module-installer]))

(defn my-installer-f []
  ; Create files, set default settings, etc.
  "Now, I'm installed!")    

(module-installer/reg-installer! ::my-installer {:installer-f my-installer-f})  
```

In the second namespace we register one another installer for preparing things
for the second module.

```
(ns my-namespace-b
    (:require [module-installer.api :as module-installer]))

(defn my-installer-f []
  ; Create files, set default settings, etc.
  "Now, I'm installed too!")    

(module-installer/reg-installer! ::my-installer {:installer-f my-installer-f})  
```

In the server boot loader we place the `check-installation!` function to run
the installers before the server starts.
Maybe the best time to call is when the server already connected to its database,
and the installers can reach the db.

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
