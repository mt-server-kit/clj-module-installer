
(ns module-installer.api
    (:require [module-installer.core.side-effects :as core.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Demo
;
; @usage
; ;; Declaring an installer function:
; (defn my-installer-f
;   []
;   (println "Setting up environment for module ...")
;   (my-config-handler/import-config-file! "my-config.edn")
;   (my-database-handler/connect-to-db!    "my-database")
;   (-> :successfully-installed))
;
; ;; Declaring a test function:
; (defn my-test-f
;   [installer-f-output]
;   (= installer-f-output :successfully-installed))
;
; ;; Registering the installer function associated with a module:
; (reg-installer! :my-module {:installer-f my-installer-f :test-f :my-test-f})
;
; ;; Applying registered installer functions of the module:
; (check-installation! :my-module)
; ;; 1. Applies the registered 'my-installer-f' function.
; ;; 2. Applies the registered 'my-test-f' function on the output of the 'my-installer-f' function.
; ;; 3. Evaluates the output of the 'my-test-f' function as a boolean,
; ;;    ... if TRUE, declares the installation as successful.
; ;;    ... if FALSE, tries to apply the 'my-installer-f' function the next time the 'check-installation!' function is called.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (module-installer.core.side-effects/*)
(def reg-installer!      core.side-effects/reg-installer!)
(def check-installation! core.side-effects/check-installation!)
