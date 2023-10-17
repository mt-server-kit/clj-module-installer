
(ns module-installer.env
    (:require [io.api                  :as io]
              [module-installer.config :as config]
              [module-installer.state  :as state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn require-installation?
  ; @ignore
  ;
  ; @param (keyword) module-id
  ;
  ; @usage
  ; (require-installation? :my-module)
  ;
  ; @return (boolean)
  [module-id]
  (let [applied-installers (io/read-edn-file config/INSTALLATION-LOG-FILEPATH {:warn? false})]
       (letfn [(f [{:keys [installer-name]}]
                  (get-in applied-installers [module-id installer-name :installed-at]))]
              (not-every? f (module-id @state/INSTALLERS)))))
