(ns pomodoro.history
  (:require [reagent.cookies :as rc]
            [pomodoro.ui-common :as ui]
            [pomodoro.time-format :as tf]
            [pomodoro.action :as action]
            [pomodoro.cookie-storage :as storage]))



(defn history-table [state]
  [:div#history
   [:h3 "History"]
   (when (:active @state) [:div
                           (ui/control-buttons state)
                           [:p]])
   (when (storage/contains-history?)
     [:table {:class "table table-striped table-bordered" :id "history" :style {:max-width "597px"}}
      [:thead {:class "thead-dark"}
       [:tr
        [:th "Task name"]
        [:th "Start time"]
        [:th "Planned duration"]
        [:th "Real duration"]
        [:th (ui/button-element (@state :active) "Delete history" action/delete-history-on-click)]]]
      (into [:tbody]
            (for [task (storage/get-history)]
              [:tr {:key (:key task)}
               [:td (:task-name task)]
               [:td (.toLocaleString (js/Date. (:start task)))]
               [:td (tf/render-time (* 1000 (:length task)))]
               [:td (tf/render-time (:duration task))]
               [:td (ui/button-element (@state :active) "Restart" #(action/restart state task))]]))])])