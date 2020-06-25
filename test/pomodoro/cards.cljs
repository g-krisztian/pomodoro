(ns ^:figwheel-hooks pomodoro.cards
  (:require
    [devcards.core :as dc]
    [sablono.core :as sab :include-macros true]
    [pomodoro.core :as pc]
    [pomodoro.single-run :as psr]
    [pomodoro.batch :as bs]
    [pomodoro.history :as ph]
    [pomodoro.summary :as ps]
    [pomodoro.ui-common :as pui]
    [pomodoro.cookie-storage :as pcs]
    [pomodoro.init :as init]
    [reagent.core :as r]
    [ajax.core :refer [GET]]
    [pomodoro.main :as main]
    [pomodoro.dictionary :as dict])
  (:require-macros
    [devcards.core :refer [defcard]]))


(defonce state-atom (r/atom {:get-key           pcs/get-key
                             :width             (.-innerWidth js/window)
                             :length            25
                             :length-in-seconds 25
                             :elapsed           10
                             :paused            true
                             :active            true
                             :task-name         "Default"
                             :now               (.getTime (js/Date.))
                             :start-time        (.getTime (js/Date.))
                             :view              :history
                             :ready             false
                             :unit              :sec}))

(init/init state-atom :pomodoro-cards "it")

(defonce ticker (js/setInterval #(pc/main-loop state-atom) 1000))

(defcard measure
         (fn [data _]
           (sab/html (dict/span data :visible (get-in @data [:dictionary :long :default-task-name]))))
         state-atom)
(defcard dictionary
         (fn [data _]
           (when (:ready @data)
             (sab/html [:div
                        [:p (dict/get-text data :task-name 160)]
                        [:p (dict/get-text data :task-name 80)]])))
         state-atom)


(defcard app-state state-atom)

(defcard text-input
         (fn [data _]
           (when (:ready @data)
             (sab/html (pui/text-input data :task-name #()))))
         state-atom)

(defcard number-input
         (fn [data _]
           (when (:ready @data)
             (sab/html (pui/input-length data :length #()))))
         state-atom)

(defcard progress-bar
         (fn [data _]
           (sab/html (pui/progress-bar data)))
         state-atom)

(defcard choose-view
         (fn [data _]
           (when (:ready @data) (sab/html (main/choose-view data))))
         state-atom)

(defcard single-run
         (fn [data _]
           (when (:ready @data) (sab/html (psr/single-run data))))
         state-atom)

(defcard batch-view
         (fn [data _]
           (when (:ready @data) (sab/html (bs/planning data))))
         state-atom)

(defcard card-history
         (fn [data _]
           (when (:ready @data) (sab/html (ph/history-table data))))
         state-atom)

(defcard summary-view
         (fn [data _]
           (when (:ready @data) (sab/html (ps/summary data))))
         state-atom)

(devcards.core/start-devcard-ui!)
