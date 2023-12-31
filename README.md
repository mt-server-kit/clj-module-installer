
# clj-module-installer

### Overview

The <strong>clj-module-installer</strong> is a simple in-project module installation
manager for Clojure projects. It provides functions for registering module installer
functions and functions for checking module installation status.

### deps.edn

```
{:deps {bithandshake/clj-module-installer {:git/url "https://github.com/bithandshake/clj-module-installer"
                                           :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}}
```

### Current version

Check out the latest commit on the [release branch](https://github.com/bithandshake/clj-module-installer/tree/release).

### Documentation

The <strong>clj-module-installer</strong> functional documentation is [available here](https://bithandshake.github.io/clj-module-installer).

### Changelog

You can track the changes of the <strong>clj-module-installer</strong> library [here](CHANGES.md).

# Usage

> Some parameters of the following functions and some further functions are not discussed in this file.
  To learn more about the available functionality, check out the [functional documentation](documentation/COVER.md)!

### Index

- [How to register an installer function?](#how-to-register-an-installer-function)

- [How to apply the registered installers?](#how-to-apply-the-registered-installers)

- [Examples](#examples)

### How to register an installer function?

The [`module-installer.api/reg-installer!`](documentation/clj/module-installer/API.md#reg-installer)
function registers an installer function that will be applied when the `check-installation!`
function next called for the module with the given module ID and only if it hasn't successfully
installed yet.

```
(defn my-installer-f [] (println "Installing ..."))
(reg-installer! :my-module {:installer-f my-installer-f})
```

By specifying the `:priority` property, you can control the installation order.
As higher is the `:priority` value, the installer function will be applied as sooner.

```
(defn my-installer-a-f [] (println "Installing A ..."))
(defn my-installer-b-f [] (println "Installing B ..."))
(defn my-installer-c-f [] (println "Installing C ..."))

(reg-installer! :my-module {:installer-f my-installer-b-f :priority 2})
(reg-installer! :my-module {:installer-f my-installer-a-f :priority 3})
(reg-installer! :my-module {:installer-f my-installer-c-f :priority 1})

; "Installing A ..."
; "Installing B ..."
; "Installing C ..."
```

When the `check-installation!` function applies the registered installers, ...
1. ... it applies the ':installer-f' function ...
2. ... it applies the ':test-f' function with the ':installer-f' function's return value
       as its only argument ...
3. ... it evaluates the ':test-f' function's return value as boolean ...
4. ... if TRUE, it declares the installation as successful.
       Otherwise, it will try to apply the installer again when the 'check-installation!'
       function is called the next time.

### How to apply the registered installers?

The [`module-installer.api/check-installation!`](documentation/clj/module-installer/API.md#check-installation)
function checks whether all registered installers are successfully applied for
the module with the given ID.
If not, it applies the ones that are not successfully installed, then exits.

```
(check-installation! :my-module)
```

### Examples

In the following example there are two independent namespaces with their
own installer functions and a boot loader which calls the `check-installer!`
function.

In the first namespace we register an installer function in order to do some
necessary side-effect before the module get used.

```
(ns my-namespace-a
    (:require [module-installer.api :as module-installer]))

(defn my-installer-f []
  ; Create files, set default settings, etc.
  "Now, I'm installed!")    

(module-installer/reg-installer! :my-module {:installer-f my-installer-f})  
```

In the second namespace we register one another installer.

```
(ns my-namespace-b
    (:require [module-installer.api :as module-installer]))

(defn my-installer-f []
  ; Create files, set default settings, etc.
  "Now, I'm installed too!")    

(module-installer/reg-installer! :my-module {:installer-f my-installer-f})  
```

In the server boot loader we place the `check-installation!` function to run the registered
installers before the HTTP server starts.

> It might be the best time to call the `check-installation!` function is when the
  server already connected to the database and the installers can reach the db.

When we first call the `run-server!` function it connects to the database,
calls the `check-installation!` function which calls the previously registered
installer functions, then it exits the server.
When we next call the `run-server!` function it connects to the database,
calls the `check-installation!` function which does nothing because the installer
functions are already applied, then the HTTP server starts.

```
(ns my-boot-loader
    (:require [module-installer.api :as module-installer]))

(defn run-server!
  []
  (connect-to-database!)
  (module-installer/check-installation! :my-module)
  (start-http-server!)
  ...)    
```
