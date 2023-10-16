
(ns module-installer.side-effects
    (:require [git-handler.api             :as git-handler]
              [io.api                      :as io]
              [edn-log.api                 :as edn-log]
              [module-installer.config     :as config]
              [module-installer.env        :as env]
              [module-installer.patterns   :as patterns]
              [module-installer.prototypes :as prototypes]
              [module-installer.state      :as state]
              [noop.api                    :refer [return]]
              [time.api                    :as time]
              [validator.api               :as v]
              [vector.api                  :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reg-installer!
  ; @description
  ; Registers an installer function that will be applied when the 'check-installation!'
  ; function next called and only if it hasn't successfully installed yet.
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
  ; @param (keyword) installer-id
  ; @param (map) installer-props
  ; {:installer-f (function)
  ;  :priority (integer)(opt)
  ;   Default: 0
  ;  :test-f (function)(opt)
  ;   Default: boolean}
  ;
  ; @usage
  ; (reg-installer! :my-installer {...})
  ;
  ; @usage
  ; (defn my-installer-f [] ...)
  ; (reg-installer! :my-installer {:installer-f my-installer-f})
  [installer-id installer-props]
  (and (v/valid? installer-id    {:test* {:f* keyword? :e* "installer-id must be a keyword!"}})
       (v/valid? installer-props {:pattern* patterns/INSTALLER-PROPS-PATTERN :prefix* "installer-props"})
       (let [installer-props (prototypes/installer-props-prototype installer-props)]
            (swap! state/INSTALLERS vector/conj-item [installer-id installer-props]))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- installation-failed
  ; @ignore
  ;
  ; @param (keyword) installer-id
  ; @param (*) installer-f-output
  ; @param (*) test-f-output
  ;
  ; @usage
  ; (installation-error-catched :my-installation "...")
  [installer-id installer-f-output test-f-output]
  (edn-log/write! config/INSTALLATION-ERRORS-FILEPATH (str "\ninstaller-id: "       installer-id
                                                           "\ninstaller-f output: " installer-f-output
                                                           "\ntest-f output: "      test-f-output
                                                           "\n"))
  ; ***
  (println "module-installer failed to apply installer:" installer-id)
  (println "installer-f output:"                         installer-f-output)
  (println "test-f output:"                              test-f-output)
  (println "module-installer exiting ...")
  (System/exit 0))

(defn- installation-error-catched
  ; @ignore
  ;
  ; @param (keyword) installer-id
  ; @param (*) error-message
  ;
  ; @usage
  ; (installation-error-catched :my-installer "...")
  [installer-id error-message]
  (edn-log/write! config/INSTALLATION-ERRORS-FILEPATH (str "\ninstaller-id: "  installer-id
                                                           "\nerror-message: " error-message
                                                           "\n"))
  ; ***
  (println "module-installer catched an error while applying the installer:" installer-id)
  (println "error-message:" error-message)
  (println "module-installer exiting ...")
  (System/exit 0))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- print-installation-state!
  ; @ignore
  ;
  ; @usage
  ; (print-installation-state!)
  []
  (let [first-installer-applied-at (env/get-first-installer-applied-at)
        applied-installer-count    (env/get-applied-installer-count)
        first-installer-applied-at (time/timestamp-string->date-time first-installer-applied-at)]
       (println "module-installer applied" applied-installer-count "installer(s) since" first-installer-applied-at)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- apply-installer!
  ; @ignore
  ;
  ; @param (keyword) installer-id
  ; @param (map) installer-props
  [installer-id {:keys [installer-f test-f]}]
  (try ; Applying the installer:
       ; - applying the installer function
       ; - applying the test function on the installer function's output
       ; - evaluting the test function's output as boolean
       (let [installer-f-output (installer-f)
             test-f-output      (test-f  installer-f-output)
             test-result        (boolean test-f-output)]
            ; ...
            (if-not test-result (installation-failed installer-id installer-f-output test-f-output))
            ; ...
            (when   test-result (println "module-installer successfully applied installer:" installer-id)
                                (swap! state/INSTALLATION-STATE update :applied-installer-count inc)
                                (io/swap-edn-file! config/INSTALLATION-LOG-FILEPATH assoc installer-id
                                                   {:installed-at (time/timestamp-string)})))
       ; ...
       (catch Exception e (installation-error-catched installer-id e))))

(defn- apply-installers!
  ; @ignore
  []
  ; ...
  (println "module-installer applying installers ...")
  ; Initializing the module installer:
  ; - reseting the installation state
  ; - updating the .gitignore file
  (reset!              state/INSTALLATION-STATE            {:applied-installer-count 0})
  (git-handler/ignore! config/INSTALLATION-LOG-FILEPATH    {:group "module-installer"})
  (git-handler/ignore! config/INSTALLATION-ERRORS-FILEPATH {:group "module-installer"})
  ; Reading the installation log file.
  (let [applied-installers (io/read-edn-file config/INSTALLATION-LOG-FILEPATH {:warn? false})]
       (letfn [(f [installer-id installer-props]
                  ; If the installer is not applied yet, installing it
                  (when-let [install? (-> applied-installers installer-id :installed-at nil?)]
                            (println "module-installer applying installer:" installer-id "...")
                            (apply-installer! installer-id installer-props)))]
              ; Iterating over the registered installer functions ...
              (let [ordered-installers (vector/sort-items-by @state/INSTALLERS > #(-> % second :priority))]
                   (doseq [[installer-id installer-props] ordered-installers]
                          (f installer-id installer-props)))
              ; ...
              (let [applied-installer-count (get @state/INSTALLATION-STATE :applied-installer-count)]
                   (println "module-installer successfully applied:" applied-installer-count "installer(s)")
                   (println "module-installer exiting ...")
                   (System/exit 0)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-installation!
  ; @description
  ; Checks whether all registered installers are successfully applied.
  ; If not, it applies the ones that are not successfully installed, then exits.
  ;
  ; @usage
  ; (check-installation!)
  []
  (if (env/require-installation?)
      (apply-installers!)
      (print-installation-state!)))
