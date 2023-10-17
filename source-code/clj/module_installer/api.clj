
(ns module-installer.api
    (:require [module-installer.side-effects :as side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; module-installer.side-effects
(def reg-installer!      side-effects/reg-installer!)
(def check-installation! side-effects/check-installation!)
