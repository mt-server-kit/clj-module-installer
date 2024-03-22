
(ns module-installer.install.env
    (:require [io.api :as io]
              [module-installer.install.config :as install.config]
              [common-state.api  :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn require-installation?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if a specific module has installer functions to apply.
  ;
  ; @param (keyword) module-id
  ;
  ; @usage
  ; (require-installation? :my-module)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [module-id]
  (let [applied-installers (io/read-edn-file install.config/INSTALLATION-LOG-FILEPATH {:warn? false})]
       (letfn [(f0 [{:keys [installer-name]}]
                   (get-in applied-installers [module-id installer-name :installed-at]))]
              (not-every? f0 (common-state/get-state :module-installer :installers module-id)))))
