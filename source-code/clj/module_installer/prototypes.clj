
(ns module-installer.prototypes
    (:require [noop.api :refer [param]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn package-props-prototype
  ; @ignore
  ;
  ; @param (map) package-props
  ;
  ; @return (map)
  ; {:priority (integer)
  ;  :test-f (function)}
  [package-props]
  (merge {:priority 0
          :test-f boolean}
         (param package-props)))
