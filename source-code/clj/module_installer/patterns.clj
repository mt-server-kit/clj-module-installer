
(ns module-installer.patterns)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @description
; https://github.com/bithandshake/cljc-validator
;
; @constant (map)
(def INSTALLER-PROPS-PATTERN
     {:installer-f {:f*   fn?
                    :e*   ":installer-f must be a function!"}
      :priority    {:opt* true
                    :f*   integer?
                    :e*   ":priority must be an integer!"}
      :test-f      {:opt* true
                    :f*   fn?
                    :e*   ":test-f must be a function!"}})
