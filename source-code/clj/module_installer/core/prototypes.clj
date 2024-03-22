
(ns module-installer.core.prototypes
    (:require [module-installer.core.utils :as core.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn installer-props-prototype
  ; @ignore
  ;
  ; @param (map) installer-props
  ; {:installer-f (function)}
  ;
  ; @return (map)
  ; {:installer-name (string)
  ;  :priority (integer)
  ;  :test-f (function)}
  [{:keys [installer-f] :as installer-props}]
  (merge {:priority 0
          :test-f   boolean}
         (-> installer-props)
         {:installer-name (core.utils/installer-f->installer-name installer-f)}))
