
(ns module-installer.core.utils
    (:require [fruits.string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn installer-f->installer-name
  ; @ignore
  ;
  ; @param (function) n
  ;
  ; @usage
  ; (installer-f->installer-name my-namespace$my_installer_f@xx00000)
  ; =>
  ; "my-namespace$my_installer_f"
  ;
  ; @return (string)
  [n]
  ; Using the name of installer functions as installer names differentiate them within the same module.
  ; In stringized function names the part after the '@' character is not constant!
  (-> n (str)
        (string/replace-part          "$" "/" {:recur?  true})
        (string/before-last-occurence "@"     {:return? true})))
