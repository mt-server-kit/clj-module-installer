
(ns module-installer.install.side-effects
    (:require [fruits.vector.api           :as vector]
              [io.api                      :as io]
              [module-installer.install.config :as install.config]
              [module-installer.install.messages :as install.messages]
              [system-log.api              :as system-log]
              [time.api                    :as time]
              [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn installation-failed
  ; @ignore
  ;
  ; @description
  ; - Registers a log entry into the installation errors file.
  ; - Prints an error message to the console.
  ; - Throws an error or optionally exits the applicaiton.
  ;
  ; @param (keyword) module-id
  ; @param (map) options
  ; {:exit-when-failed? (boolean)(opt)}
  ; @param (map) installer-props
  ; {:installer-name (string)}
  ; @param (*) installer-f-output
  ; @param (*) test-f-output
  ;
  ; @usage
  ; (installation-failed :my-module {:installer-name "my-namespace$my-installer-f"} "..." "...")
  [module-id {:keys [exit-when-failed?]} {:keys [installer-name]} installer-f-output test-f-output]
  (system-log/prepend-entry! (str "\n"
                                  "\nmodule-id: "          module-id
                                  "\ninstaller-name: "     installer-name
                                  "\ninstaller-f output: " installer-f-output
                                  "\ntest-f output: "      test-f-output)
                             {:filepath install.config/INSTALLATION-ERRORS-FILEPATH})
  ; ***
  (println "module-installer failed to apply an installer!")
  (println "module-id:"          module-id)
  (println "installer-name:"     installer-name)
  (println "installer-f output:" installer-f-output)
  (println "test-f output:"      test-f-output)
  (when exit-when-failed? (println "\nmodule-installer exiting ...")
                          (System/exit 0))
  (throw (Exception. install.messages/INSTALLATION-FAILED-ERROR)))

(defn installation-error-catched
  ; @ignore
  ;
  ; @description
  ; - Registers a log entry into the installation errors file.
  ; - Prints an error message to the console.
  ; - Throws an error or optionally exits the applicaiton.
  ;
  ; @param (keyword) module-id
  ; @param (map) options
  ; {:exit-when-failed? (boolean)(opt)}
  ; @param (map) installer-props
  ; {:installer-name (string)}
  ; @param (*) error-message
  ;
  ; @usage
  ; (installation-error-catched :my-module {:installer-name "my-namespace$my-installer-f"} "...")
  [module-id {:keys [exit-when-failed?]} {:keys [installer-name]} error-message]
  (system-log/prepend-entry! (str "\n"
                                  "\nmodule-id: "      module-id
                                  "\ninstaller-name: " installer-name
                                  "\nerror-message: "  error-message)
                             {:filepath install.config/INSTALLATION-ERRORS-FILEPATH})
  ; ***
  (println "module-installer catched an error while applying an installer!")
  (println "module-id:"      module-id)
  (println "installer-name:" installer-name)
  (println "error-message:"  error-message)
  (when exit-when-failed? (println "\nmodule-installer exiting ...")
                          (System/exit 0))
  (throw (Exception. install.messages/INSTALLATION-ERROR-CATCHED)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-installer!
  ; @ignore
  ;
  ; @description
  ; Applies a specific installer function of a specific module.
  ;
  ; @param (keyword) module-id
  ; @param (map) options
  ; @param (map) installer-props
  ; {:installer-f (function)
  ;  :installer-name (string)
  ;  :test-f (function)}
  ;
  ; @usage
  ; (apply-installer! :my-module {...} {:installer-f my-namespace$my-installer-f@xx00000 :installer-name "my-namespace$my-installer-f" :test-f clojure.core$boolean})
  [module-id options {:keys [installer-f installer-name test-f] :as installer-props}]
  (try ; - Applyies the installer function.
       ; - Applyies the test function on the output of the installer function.
       ; - Evaluates the output of the test function as a boolean.
       (let [installer-f-output (installer-f)
             test-f-output      (test-f  installer-f-output)
             test-result        (boolean test-f-output)]
            ; ...
            (if-not test-result (installation-failed module-id options installer-props installer-f-output test-f-output))
            ; ...
            (when   test-result (println "module-installer successfully applied installer:" installer-name)
                                (common-state/update-state! :module-installer :installation-state update-in [module-id :applied-installer-count] inc)
                                (io/update-edn-file! install.config/INSTALLATION-LOG-FILEPATH assoc-in
                                                     [module-id installer-name]
                                                     {:installed-at (time/timestamp-string)})))
       ; ...
       (catch Exception e (installation-error-catched module-id options installer-props e))))

(defn apply-installers!
  ; @ignore
  ;
  ; @description
  ; Applies the installer functions of a specific module.
  ;
  ; @param (keyword) module-id
  ; @param (map) options
  ; {:exit-when-succeeded? (boolean)(opt)}
  ;
  ; @usage
  ; (apply-installers! :my-module {...})
  ;
  ; @return (keyword)
  ; :failed-installation, :successful-installation
  [module-id {:keys [exit-when-succeeded?] :as options}]
  ; ...
  (println "module-installer applying installers for module:" module-id "...")
  ; Reseting the installation state.
  (common-state/assoc-state! :module-installer :installation-state module-id :applied-installer-count 0)
  ; Reading the installation log file.
  (let [applied-installers (io/read-edn-file install.config/INSTALLATION-LOG-FILEPATH {:warn? false})]
       (letfn [(f0 [module-id {:keys [installer-name] :as installer-props}]
                   ; If the installer is not applied yet, installing it ...
                   (let [installed-at (get-in applied-installers [module-id installer-name :installed-at])]
                        (when-not installed-at (println "module-installer applying installer:" installer-name "...")
                                               (apply-installer! module-id options installer-props))))]
              ; Iterating over the registered installer functions ...
              (let [module-installers  (common-state/get-state :module-installer :installers module-id)
                    ordered-installers (vector/sort-items-by module-installers > :priority)]
                   (doseq [installer-props ordered-installers]
                          (f0 module-id installer-props)))
              ; ...
              (let [applied-installer-count (common-state/get-state :module-installer :installation-state module-id :applied-installer-count)]
                   (println "module-installer successfully applied:" applied-installer-count "installer(s) for module:" module-id)
                   (when exit-when-succeeded? (println "module-installer exiting ...")
                                              (System/exit 0))
                   (-> :successful-installation)))))
