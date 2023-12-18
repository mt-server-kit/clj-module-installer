
(ns module-installer.side-effects
    (:require [edn-log.api                 :as edn-log]
              [fruits.vector.api           :as vector]
              [io.api                      :as io]
              [module-installer.config     :as config]
              [module-installer.env        :as env]
              [module-installer.prototypes :as prototypes]
              [module-installer.state      :as state]
              [module-installer.tests      :as tests]
              [time.api                    :as time]
              [validator.api               :as v]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reg-installer!
  ; @description
  ; Registers an installer function that will be applied when the 'check-installation!'
  ; function next called for the module with the given module ID and only if it hasn't
  ; successfully installed yet.
  ;
  ; When the 'check-installation!' function applies the registered installers, ...
  ; 1. ... it applies the ':installer-f' function ...
  ; 2. ... it applies the ':test-f' function with the ':installer-f' function's return value
  ;        as its only argument ...
  ; 3. ... it evaluates the ':test-f' function's return value as boolean ...
  ; 4. ... if TRUE, it declares the installation as successful, otherwise it
  ;        will try to apply the installer again when the 'check-installation!' function
  ;        next called.
  ;
  ; You can control the installation order by passing the {:priority ...} property.
  ; As higher is the priority value, the installer function will be applied as sooner.
  ;
  ; @param (keyword) module-id
  ; @param (map) installer-props
  ; {:installer-f (function)
  ;   Do not use anonymous function as installer function!
  ;   (Installers are identified and differentiated by their names, it must be constant!)
  ;  :priority (integer)(opt)
  ;   Default: 0
  ;  :test-f (function)(opt)
  ;   Default: boolean}
  ;
  ; @usage
  ; (reg-installer! :my-module {...})
  ;
  ; @usage
  ; (defn my-installer-f [] ...)
  ; (reg-installer! :my-module {:installer-f my-installer-f})
  [module-id installer-props]
  (and (v/valid? module-id       tests/MODULE-ID-TEST       {:prefix "module-id"})
       (v/valid? installer-props tests/INSTALLER-PROPS-TEST {:prefix "installer-props"})
       (let [installer-props (prototypes/installer-props-prototype installer-props)]
            (swap! state/INSTALLERS update module-id vector/conj-item installer-props))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- installation-failed
  ; @ignore
  ;
  ; @param (keyword) module-id
  ; @param (map) installer-props
  ; {:installer-name (string)}
  ; @param (*) installer-f-output
  ; @param (*) test-f-output
  ;
  ; @usage
  ; (installation-failed :my-module {:installer-name "my-namespace$my-installer-f@63c66980"} "..." "...")
  [module-id {:keys [installer-name]} installer-f-output test-f-output]
  (edn-log/write! config/INSTALLATION-ERRORS-FILEPATH (str "\nmodule-id:"          module-id
                                                           "\ninstaller-name:"     installer-name
                                                           "\ninstaller-f-output:" installer-f-output
                                                           "\ntest-f-output:"      test-f-output
                                                           "\n"))
  ; ***
  (println "module-installer failed to apply an installer!")
  (println "\nmodule-id:"          module-id)
  (println "\ninstaller-name:"     installer-name)
  (println "\ninstaller-f-output:" installer-f-output)
  (println "\ntest-f-output:"      test-f-output)
  (println "\nmodule-installer exiting ...")
  (System/exit 0))

(defn- installation-error-catched
  ; @ignore
  ;
  ; @param (keyword) module-id
  ; @param (map) installer-props
  ; {:installer-name (string)}
  ; @param (*) error-message
  ;
  ; @usage
  ; (installation-error-catched :my-module {:installer-name "my-namespace$my-installer-f@63c66980"} "...")
  [module-id {:keys [installer-name]} error-message]
  (edn-log/write! config/INSTALLATION-ERRORS-FILEPATH (str "\nmodule-id:"      module-id
                                                           "\ninstaller-name:" installer-name
                                                           "\nerror-message:"  error-message
                                                           "\n"))
  ; ***
  (println "module-installer catched an error while applying an installer!")
  (println "\nmodule-id:"      module-id)
  (println "\ninstaller-name:" installer-name)
  (println "\nerror-message:"  error-message)
  (println "\nmodule-installer exiting ...")
  (System/exit 0))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- apply-installer!
  ; @ignore
  ;
  ; @param (keyword) module-id
  ; @param (map) installer-props
  ; {:installer-f (function)
  ;  :installer-name (string)
  ;  :test-f (function)}
  [module-id {:keys [installer-f installer-name test-f] :as installer-props}]
  (try ; Applying the installer:
       ; - applying the installer function
       ; - applying the test function on the installer function's output
       ; - evaluting the test function's output as boolean
       (let [installer-f-output (installer-f)
             test-f-output      (test-f  installer-f-output)
             test-result        (boolean test-f-output)]
            ; ...
            (if-not test-result (installation-failed module-id installer-props installer-f-output test-f-output))
            ; ...
            (when   test-result (println "module-installer successfully applied installer:" installer-name)
                                (swap! state/INSTALLATION-STATE update-in [module-id :applied-installer-count] inc)
                                (io/update-edn-file! config/INSTALLATION-LOG-FILEPATH assoc-in
                                                     [module-id installer-name]
                                                     {:installed-at (time/timestamp-string)})))
       ; ...
       (catch Exception e (installation-error-catched module-id installer-props e))))

(defn- apply-installers!
  ; @ignore
  ;
  ; @param (keyword) module-id
  [module-id]
  ; ...
  (println "module-installer applying installers for module:" module-id "...")
  ; Reseting the installation state
  (swap! state/INSTALLATION-STATE assoc module-id {:applied-installer-count 0})
  ; Reading the installation log file.
  (let [applied-installers (io/read-edn-file config/INSTALLATION-LOG-FILEPATH {:warn? false})]
       (letfn [(f0 [module-id {:keys [installer-name] :as installer-props}]
                   ; If the installer is not applied yet, installing it ...
                   (let [installed-at (get-in applied-installers [module-id installer-name :installed-at])]
                        (when-not installed-at (println "module-installer applying installer:" installer-name "...")
                                               (apply-installer! module-id installer-props))))]
              ; Iterating over the registered installer functions ...
              (let [module-installers  (get @state/INSTALLERS module-id)
                    ordered-installers (vector/sort-items-by module-installers > :priority)]
                   (doseq [installer-props ordered-installers]
                          (f0 module-id installer-props)))
              ; ...
              (let [applied-installer-count (get-in @state/INSTALLATION-STATE [module-id :applied-installer-count])]
                   (println "module-installer successfully applied:" applied-installer-count "installer(s) for module:" module-id)
                   (println "module-installer exiting ...")
                   (System/exit 0)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-installation!
  ; @description
  ; Checks whether all registered installers (of the module with the given ID) are successfully applied.
  ; If not, it applies the installers of the module that are not successfully installed, then exits.
  ;
  ; @param (keyword) module-id
  ;
  ; @usage
  ; (check-installation! :my-module)
  [module-id]
  (if (env/require-installation? module-id)
      (apply-installers!         module-id)))
