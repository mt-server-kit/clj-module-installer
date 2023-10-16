
(ns module-installer.env
    (:require [io.api                  :as io]
              [module-installer.config :as config]
              [module-installer.state  :as state]
              [noop.api                :refer [return]]
              [string.api              :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn installer-applied?
  ; @param (keyword) installer-id
  ;
  ; @usage
  ; (installer-applied? :my-installer)
  ;
  ; @return (boolean)
  [installer-id]
  (let [applied-installers (io/read-edn-file config/INSTALLATION-LOG-FILEPATH {:warn? false})]
       (get-in applied-installers [installer-id :installed-at])))

(defn require-installation?
  ; @usage
  ; (require-installation?)
  ;
  ; @return (boolean)
  []
  (letfn [(f [[installer-id _]] (installer-applied? installer-id))]
         (not-every? f @state/INSTALLERS)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-first-installer-applied-at
  ; @ignore
  ;
  ; @return (string)
  []
  (let [applied-installers (io/read-edn-file config/INSTALLATION-LOG-FILEPATH {:warn? false})]
       (letfn [(f [result _ {:keys [installed-at]}]
                  (string/abc result installed-at))]
              (reduce-kv f "9999-99-99Z99:99:99.999Z" applied-installers))))

(defn get-applied-installer-count
  ; @ignore
  ;
  ; @return (integer)
  []
  (let [applied-installers (io/read-edn-file config/INSTALLATION-LOG-FILEPATH {:warn? false})]
       (-> applied-installers keys count)))
