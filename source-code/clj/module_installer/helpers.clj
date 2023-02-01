
(ns x.core.install-handler.helpers
    (:require [io.api                             :as io]
              [string.api                         :as string]
              [time.api                           :as time]
              [x.app-details                      :as x.app-details]
              [x.core.install-handler.config      :as install-handler.config]
              [x.core.install-handler.state       :as install-handler.state]
              [x.core.server-handler.side-effects :as server-handler.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-first-package-installed-at
  ; @ignore
  ;
  ; @return (string)
  []
  (let [installed-packages (io/read-edn-file install-handler.config/INSTALLED-PACKAGES-FILEPATH {:warn? false})]
       (letfn [(f [result _ {:keys [installed-at]}]
                  (string/abc result installed-at))]
              (reduce-kv f "9999-99-99Z99:99:99.999Z" installed-packages))))

(defn get-installed-package-count
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @return (integer)
  []
  (let [installed-packages (io/read-edn-file install-handler.config/INSTALLED-PACKAGES-FILEPATH {:warn? false})]
       (-> installed-packages keys count)))

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
  (let [installed-packages (io/read-edn-file install-handler.config/INSTALLED-PACKAGES-FILEPATH {:warn? false})]
       (get-in installed-packages [package-id :result])))

(defn require-preinstallation?
  ; @usage
  ; (require-preinstallation?)
  ;
  ; @return (boolean)
  []
  (letfn [(f [[package-id _]] (package-installed? package-id))]
         (not-every? f @install-handler.state/PREINSTALLERS)))

(defn require-installation?
  ; @usage
  ; (require-installation?)
  ;
  ; @return (boolean)
  []
  (letfn [(f [[package-id _]] (package-installed? package-id))]
         (not-every? f @install-handler.state/INSTALLERS)))
