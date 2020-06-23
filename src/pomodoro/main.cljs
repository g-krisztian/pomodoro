(ns pomodoro.main
  (:require [pomodoro.ui-common :as ui]
            [pomodoro.action :as action]
            [pomodoro.single-run :as single]
            [pomodoro.batch :as batch]
            [pomodoro.history :as history]
            [pomodoro.summary :as summary]
            [pomodoro.time-format :as tf]
            [pomodoro.dictionary :as dict]))

(defn show-view [state]
  (condp = (:view @state)
    :summary (summary/summary state)
    :history (history/history-table state)
    :planning (batch/planning state)
    (single/single-run state)))

(defn swap-view [state view]
  (swap! state merge {:view view}))

(defn choose-view [state]
  (let [views [:single-run :planning :history :summary]
        width (:width @state)]
    [:div {:style {:width "10%"}}
     (into [:div {:class "btn-group"}]
           (for [view views]
             (ui/button-element
               (@state :active)
               (min 150 (* width 0.238))
               (dict/get state view)
               #(swap-view state view))))
     [:p]]))

(defn main [state & context]
  (into [:div#:app {:style {:margin "auto"
                            :width  "max-content"}}
         [:h1 "Pomodoro app"]
         [:h3 (str "Time: " (tf/render-time (tf/correct-time (:now @state))))]]
        context))

