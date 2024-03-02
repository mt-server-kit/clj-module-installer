
(ns module-installer.utils
    (:require [fruits.string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn installer-f->installer-name
  ; @ignore
  ;
  ; @param (function) n
  ;
  ; @return (string)
  [n]
  ; The module installer uses the 'installer-f' function's name as installer name to differentiate installer functions with the same module ID.
  ; In stringized function names the part after the '@' character is not constant.
  (-> n (str)
        (string/replace-part          "$" "/" {:recur?  true})
        (string/before-last-occurence "@"     {:return? true})))
