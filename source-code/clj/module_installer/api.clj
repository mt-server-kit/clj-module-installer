
(ns module-installer.api
    (:require [module-installer.env          :as env]
              [module-installer.side-effects :as side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; module-installer.side-effects
(def installer-applied?    env/installer-applied?)
(def require-installation? env/require-installation?)

; module-installer.side-effects
(def reg-installer!      side-effects/reg-installer!)
(def check-installation! side-effects/check-installation!)
