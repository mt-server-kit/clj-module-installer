
(ns module-installer.core.env
    (:require [common-state.api  :as common-state]
              [fruits.vector.api :as vector]))

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
