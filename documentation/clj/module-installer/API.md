
# module-installer.api Clojure namespace

##### [README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > module-installer.api

### Index

- [check-installation!](#check-installation)

- [reg-installer!](#reg-installer)

### check-installation!

```
@description
Checks whether all registered installers are successfully applied for the module
with the given ID.
If not, it applies the ones that are not successfully installed, then exits.
```

```
@param (keyword) module-id
```

```
@usage
(check-installation! :my-module)
```

<details>
<summary>Source code</summary>

```
(defn check-installation!
  [module-id]
  (if (env/require-installation? module-id)
      (apply-installers!         module-id)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [module-installer.api :refer [check-installation!]]))

(module-installer.api/check-installation! ...)
(check-installation!                      ...)
```

</details>

---

### reg-installer!

```
@description
Registers an installer function that will be applied when the 'check-installation!'
function next called for the module with the given module ID and only if it hasn't
successfully installed yet.
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
@param (keyword) module-id
@param (map) installer-props
{:installer-f (function)
  Do not use anonymous function as installer function!
  (Installers are identified and differentiated by their names, it has to be constant
   over different compiles!)
 :priority (integer)(opt)
  Default: 0
 :test-f (function)(opt)
  Default: boolean}
```

```
@usage
(reg-installer! :my-module {...})
```

```
@usage
(defn my-installer-f [] ...)
(reg-installer! :my-module {:installer-f my-installer-f})
```

<details>
<summary>Source code</summary>

```
(defn reg-installer!
  [module-id installer-props]
  (and (v/valid? module-id       {:test* {:f* keyword? :e* "module-id must be a keyword!"}})
       (v/valid? installer-props {:pattern* patterns/INSTALLER-PROPS-PATTERN :prefix* "installer-props"})
       (let [installer-props (prototypes/installer-props-prototype installer-props)]
            (swap! state/INSTALLERS update module-id vector/conj-item installer-props))))
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

This documentation is generated with the [clj-docs-generator](https://github.com/bithandshake/clj-docs-generator) engine.

