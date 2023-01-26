
(ns module-installer.patterns)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (map)
; https://github.com/bithandshake/pattern-api
(def PACKAGE-PROPS-PATTERN
     {:installer-f {:f*   fn?
                    :e*   "key :installer-f must be a function!"}
      :priority    {:opt* true
                    :f*   integer?
                    :e*   "key :priority must be an integer!"}
      :test-f      {:opt* true
                    :f*   fn?
                    :e*   "key :test-f must be a function!"}})
