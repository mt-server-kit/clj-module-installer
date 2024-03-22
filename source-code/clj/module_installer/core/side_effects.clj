
(ns module-installer.core.side-effects
    (:require [common-state.api                      :as common-state]
              [fruits.vector.api                     :as vector]
              [module-installer.core.env             :as core.env]
              [module-installer.core.prototypes      :as core.prototypes]
              [module-installer.core.tests           :as core.tests]
              [module-installer.install.env          :as install.env]
              [module-installer.install.side-effects :as install.side-effects]
              [validator.api                         :as v]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reg-installer!
  ; @description
  ; - Registers an installer function for a specific module.
  ; - Ignores registering installer functions duplicated.
  ; - Returns the registered installer functions of the module.
  ;
  ; @param (keyword) module-id
  ; @param (map) installer-props
  ; {:installer-f (function)
  ;   Do not use anonymous functions as installer function (installer functions are identified and differentiated by their names).
  ;  :priority (integer)(opt)
  ;   The higher the priority value, the sooner the installer function will be applied.
  ;   Default: 0
  ;  :test-f (function)(opt)
  ;   Default: boolean}
  ;
  ; @usage
  ; (defn my-installer-f [] ...)
  ; (reg-installer! :my-module {:installer-f my-installer-f})
  ; =>
  ; [{:installer-name "my-namespace$my-installer-f" :installer-f my-namespace$my-installer-f@xx00000 :priority 0 :test-f clojure.core$boolean}]
  ;
  ; @return (maps in vector)
  [module-id installer-props]
  (and (v/valid? module-id       core.tests/MODULE-ID-TEST       {:prefix "module-id"})
       (v/valid? installer-props core.tests/INSTALLER-PROPS-TEST {:prefix "installer-props"})
       (let [installer-props (core.prototypes/installer-props-prototype installer-props)]
            (if-not (core.env/installer-function-registered? module-id installer-props)
                    (common-state/update-state! :module-installer :installers update module-id vector/conj-item installer-props))
            (common-state/get-state :module-installer :installers module-id))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-installation!
  ; @description
  ; Checks whether all registered installer functions of a specific module are already applied successfully.
  ; If not, it applies the installer functions of the module that are not successfully installed, then optionally exits.
  ;
  ; @param (keyword) module-id
  ; @param (map)(opt) options
  ; {:exit-when-failed? (boolean)(opt)
  ;  :exit-when-succeeded? (boolean)(opt)}
  ;
  ; @usage
  ; (check-installation! :my-module)
  ; =>
  ; :successful-installation
  ;
  ; @return (keyword)
  ; :already-installed, :successful-installation
  ([module-id]
   (check-installation! module-id {}))

  ([module-id options]
   (if (-> module-id (install.env/require-installation?))
       (-> module-id (install.side-effects/apply-installers! options))
       (-> :already-installed))))
