
# module-installer.api Clojure namespace

##### [README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > module-installer.api

### Index

- [check-installation!](#check-installation)

- [installer-applied?](#installer-applied)

- [reg-installer!](#reg-installer)

- [require-installation?](#require-installation)

### check-installation!

```
@description
Checks whether all registered installers are successfully applied.
If not, it applies the ones that are not successfully installed, then exits.
```

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
      (apply-installers!)
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

### installer-applied?

```
@param (keyword) installer-id
```

```
@usage
(installer-applied? :my-installer)
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn installer-applied?
  [installer-id]
  (let [applied-installers (io/read-edn-file config/INSTALLATION-LOG-FILEPATH {:warn? false})]
       (get-in applied-installers [installer-id :installed-at])))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [module-installer.api :refer [installer-applied?]]))

(module-installer.api/installer-applied? ...)
(installer-applied?                      ...)
```

</details>

---

### reg-installer!

```
@description
Registers an installer function that will be applied when the 'check-installation!'
function next called and only if it hasn't successfully installed yet.
When the 'check-installation!' function applies the registered installers, ...
1. ... it applies the ':installer-f' function ...
2. ... it applies the ':test-f' function with the ':installer-f' function's return value
       as its only argument ...
3. ... it evaluates the ':test-f' function's return value as boolean ...
4. ... if TRUE, it declares the installation as successful, otherwise it
       will try to apply the installer again when the 'check-installation!' function
       next called.
You can control the installation order by passing the {:priority ...} property.
As higher is the priority value, the installer function will be applied as sooner.
```

```
@param (keyword) installer-id
@param (map) installer-props
{:installer-f (function)
 :priority (integer)(opt)
  Default: 0
 :test-f (function)(opt)
  Default: boolean}
```

```
@usage
(reg-installer! :my-installer {...})
```

```
@usage
(defn my-installer-f [] ...)
(reg-installer! :my-installer {:installer-f my-installer-f})
```

<details>
<summary>Source code</summary>

```
(defn reg-installer!
  [installer-id installer-props]
  (and (v/valid? installer-id    {:test* {:f* keyword? :e* "installer-id must be a keyword!"}})
       (v/valid? installer-props {:pattern* patterns/INSTALLER-PROPS-PATTERN :prefix* "installer-props"})
       (let [installer-props (prototypes/installer-props-prototype installer-props)]
            (swap! state/INSTALLERS vector/conj-item [installer-id installer-props]))))
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
  (letfn [(f [[installer-id _]] (installer-applied? installer-id))]
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

