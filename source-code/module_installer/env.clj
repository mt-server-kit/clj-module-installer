
(ns module-installer.env
    (:require [io.api                  :as io]
              [module-installer.config :as config]
              [module-installer.state  :as state]
              [noop.api                :refer [return]]
              [string.api              :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn package-installed?
  ; @param (keyword) package-id
  ;
  ; @usage
  ; (package-installed? :my-package)
  ;
  ; @return (boolean)
  [package-id]
  (let [installed-packages (io/read-edn-file config/INSTALLED-PACKAGES-FILEPATH {:warn? false})]
       (get-in installed-packages [package-id :result])))

(defn require-installation?
  ; @usage
  ; (require-installation?)
  ;
  ; @return (boolean)
  []
  (letfn [(f [[package-id _]] (package-installed? package-id))]
         (not-every? f @state/INSTALLERS)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-first-package-installed-at
  ; @ignore
  ;
  ; @return (string)
  []
  (let [installed-packages (io/read-edn-file config/INSTALLED-PACKAGES-FILEPATH {:warn? false})]
       (letfn [(f [result _ {:keys [installed-at]}]
                  (string/abc result installed-at))]
              (reduce-kv f "9999-99-99Z99:99:99.999Z" installed-packages))))

(defn get-installed-package-count
  ; @ignore
  ;
  ; @return (integer)
  []
  (let [installed-packages (io/read-edn-file config/INSTALLED-PACKAGES-FILEPATH {:warn? false})]
       (-> installed-packages keys count)))

(defn get-non-installed-package-count
  ; @ignore
  ;
  ; @return (integer)
  []
  (let [installed-packages (io/read-edn-file config/INSTALLED-PACKAGES-FILEPATH {:warn? false})]
       (letfn [(f [result installer-id _] (if (package-installed? installer-id)
                                              (return result)
                                              (inc    result)))]
              (reduce-kv f 0 @state/INSTALLERS))))
