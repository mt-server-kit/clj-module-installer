
(ns module-installer.patterns)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (map)
; https://github.com/bithandshake/cljc-validator
(def PACKAGE-PROPS-PATTERN
     {:installer-f {:f*   fn?
                    :e*   ":installer-f must be a function!"}
      :priority    {:opt* true
                    :f*   integer?
                    :e*   ":priority must be an integer!"}
      :test-f      {:opt* true
                    :f*   fn?
                    :e*   ":test-f must be a function!"}})
