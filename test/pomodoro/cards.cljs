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
                             :dictionary        pc/dictionary
                             :length            25
                             :length-in-seconds 25
                             :elapsed           10
                             :task-name         "Default"
                             :now               (.getTime (js/Date.))
                             :start-time        (.getTime (js/Date.))
                             :view              :history
                             :unit              :sec}))

(pcs/init :pomodoro-cards)

(defcard app-state state-atom)

(defonce ticker (js/setInterval #(pc/main-loop state-atom) 1000))

(defcard text-input
         (sab/html (pui/text-input state-atom :task-name #())))

(defcard number-input
         (sab/html (pui/input-length state-atom :length #())))

(defcard progress-bar
         (sab/html (pui/progress-bar state-atom)))

(defcard choose-view
         (sab/html (pc/choose-view state-atom)))

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

