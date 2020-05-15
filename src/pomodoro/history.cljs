(ns pomodoro.history
  (:require [reagent.cookies :as rc]
            [pomodoro.ui-common :as ui]
            [pomodoro.time-format :as tf]
            [pomodoro.action :as action]
            [pomodoro.common :as common]))

(defn history-table []
  [:div#history
   [:h3 "History"]
   [:p (str (rc/get :history))]
   (when (:active @common/app-state) [:div
                                      (ui/control-buttons)
                                      [:p]])

   (when (rc/contains-key? :history)
     [:table {:class "table table-striped table-bordered" :id "history"}
      [:thead {:class "thead-dark"}
       [:tr
        [:th "Task name"]
        [:th "Start time"]
        [:th "Planned duration"]
        [:th "Real duration"]
        [:th (ui/button-element :active "Delete history" action/delete-history-on-click)]]]
      (into [:tbody]
            (for [task (rc/get :history)]
              [:tr {:key (:key task)}
               [:td (:task-name task)]
               [:td (.toLocaleString (js/Date. (:start task)))]
               [:td (tf/render-time (* 1000 (:length task)))]
               [:td (tf/render-time (:duration task))]
               [:td (ui/button-element :active "Restart" #(action/start-button-on-click task))]]))])])