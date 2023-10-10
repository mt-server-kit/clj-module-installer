
# module-installer.api Clojure namespace

##### [README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > module-installer.api

### Index

- [check-installation!](#check-installation)

- [package-installed?](#package-installed)

- [reg-installer!](#reg-installer)

- [require-installation?](#require-installation)

### check-installation!

```
@usage
(check-installation!)
```

<details>
<summary>Source code</summary>

```
(defn check-installation!
  []
  (if (env/require-installation?)
      (install-packages!)
      (print-installation-state!)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [module-installer.api :refer [check-installation!]]))

(module-installer.api/check-installation!)
(check-installation!)
```

</details>

---

### package-installed?

```
@param (keyword) package-id
```

```
@usage
(package-installed? :my-package)
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn package-installed?
  [package-id]
  (let [installed-packages (io/read-edn-file config/INSTALLED-PACKAGES-FILEPATH {:warn? false})]
       (get-in installed-packages [package-id :result])))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [module-installer.api :refer [package-installed?]]))

(module-installer.api/package-installed? ...)
(package-installed?                      ...)
```

</details>

---

### reg-installer!

```
@description
Registers a module installer function that will be applied when the
'check-installation!' function next called and only if it doesn't successfully
installed yet.
The :installer-f function's return value will be passed to the :test-f function,
and the :test-f function's return value will be evaluted as a boolean.
If false the installation will be declared as an installation failure,
and the package will be reinstalled when the 'check-installation!' next called.
If you don't pass the :test-f function, the :installer-f function's return
value will be evaluted as a boolean.
By passing the {:priority ...} property, you can control the package installation
order. As higher is the priority value, the installer function will be applied as sooner.
```

```
@param (keyword) package-id
@param (map) package-props
{:installer-f (function)
 :priority (integer)(opt)
  Default: 0
 :test-f (function)(opt)
  Default: boolean}
```

```
@usage
(reg-installer! :my-package {...})
```

```
@usage
(defn my-package-f [] ...)
(reg-installer! :my-package {:installer-f my-package-f})
```

<details>
<summary>Source code</summary>

```
(defn reg-installer!
  [package-id {:keys [preinstaller?] :as package-props}]
  (and (v/valid? package-id    {:test* {:f* keyword? :e* "package-id must be a keyword!"}})
       (v/valid? package-props {:pattern* patterns/PACKAGE-PROPS-PATTERN :prefix* "package-props"})
       (let [package-props (prototypes/package-props-prototype package-props)]
            (swap! state/INSTALLERS assoc package-id package-props))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [module-installer.api :refer [reg-installer!]]))

(module-installer.api/reg-installer! ...)
(reg-installer!                      ...)
```

</details>

---

### require-installation?

```
@usage
(require-installation?)
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn require-installation?
  []
  (letfn [(f [[package-id _]] (package-installed? package-id))]
         (not-every? f @state/INSTALLERS)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [module-installer.api :refer [require-installation?]]))

(module-installer.api/require-installation?)
(require-installation?)
```

</details>

---

This documentation is generated with the [clj-docs-generator](https://github.com/bithandshake/clj-docs-generator) engine.

