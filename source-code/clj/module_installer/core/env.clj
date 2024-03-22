
(ns module-installer.core.env
    (:require [fruits.vector.api :as vector]
              [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn installer-function-registered?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given installer function is already registered for the module.
  ;
  ; @usage
  ; (installer-function-registered? :my-module {...})
  ; =>
  ; true
  ;
  ; @return (boolean)
  [module-id {:keys [installer-name]}]
  (letfn [(f0 [%] (-> % :installer-name (= installer-name)))]
         (let [module-installers (common-state/get-state :module-installer :installers module-id)]
              (vector/any-item-matches? module-installers f0))))
