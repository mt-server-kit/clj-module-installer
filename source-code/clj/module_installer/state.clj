
(ns module-installer.state)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @atom (map)
; {:my-module [{:installer-name "..."}]}
(def INSTALLERS (atom {}))

; @ignore
;
; @atom (map)
; {:my-module (map)}
(def INSTALLATION-STATE (atom {}))
