
(ns module-installer.state)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @atom (vectors in vector)
; [[(keyword) installer-id
;   (map) installer-props]]
(def INSTALLERS (atom []))

; @ignore
;
; @atom (map)
(def INSTALLATION-STATE (atom {}))
