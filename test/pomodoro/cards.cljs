(ns ^:figwheel-hooks pomodoro.cards
  (:require
    [devcards.core :as dc]                                  ; <-- here
    [sablono.core :as sab :include-macros true]
    [pomodoro.core :as pc]
    [pomodoro.single-run :as psr]
    [pomodoro.batch :as bs]
    [pomodoro.history :as ph]
    [pomodoro.summary :as ps]
    [pomodoro.ui-common :as pui]
    [pomodoro.cookie-storage :as pcs]
    [reagent.core :as r]
    [reagent.cookies :as rc])
  (:require-macros
    [devcards.core :refer [defcard]]))

(defonce state-atom (r/atom {:get-key           pcs/get-key
                             :width             pc/width
                             :dictionary        pc/dictionary
                             :length            25
                             :length-in-seconds 25
                             :elapsed           10
                             :paused            true
                             :active            true
                             :task-name         "Default"
                             :now               (.getTime (js/Date.))
                             :start-time        (.getTime (js/Date.))
                             :view              :history
                             :unit              :sec}))

(pcs/init :pomodoro-cards)

(defcard app-state state-atom)

(defonce ticker (js/setInterval #(pc/main-loop state-atom) 1000))

(defcard text-input
         (fn [data _]
           (sab/html (pui/text-input data :task-name #())))
         state-atom)

(defcard number-input
         (fn [data _]
           (sab/html (pui/input-length data :length #())))
         state-atom)

(defcard progress-bar
         (fn [data _]
           (sab/html (pui/progress-bar data)))
         state-atom)

(defcard choose-view
         (fn [data _]
           (sab/html (pc/choose-view data)))
         state-atom)

(defcard single-run
         (fn [data _]
           (sab/html (psr/single-run data)))
         state-atom)

(defcard batch-view
         (fn [data _]
           (sab/html (bs/planning data)))
         state-atom)

(defcard card-history
         (fn [data _]
           (sab/html (ph/history-table data)))
         state-atom)

(defcard summary-view
         (fn [data _]
           (sab/html (ps/summary data)))
         state-atom)

(devcards.core/start-devcard-ui!)

