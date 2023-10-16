
(ns module-installer.prototypes
    (:require [noop.api :refer [param]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn installer-props-prototype
  ; @ignore
  ;
  ; @param (map) installer-props
  ;
  ; @return (map)
  ; {:priority (integer)
  ;  :test-f (function)}
  [installer-props]
  (merge {:priority 0
          :test-f boolean}
         (param installer-props)))
