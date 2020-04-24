(ns pomodoro.summary
  (:require [pomodoro.time-format :as tf]
            [pomodoro.ui-common :as ui]
            [reagent.cookies :as rc]
            [pomodoro.action :as action]
            [pomodoro.common :as common]))

(defn sum-usage []
  (->> (rc/get :history)
       (group-by :task-name)
       (map (fn [[task-name v]] (let [length (reduce + (for [d v] (:duration d)))]
                                  {:task-name task-name
                                   :length    length})))))
(defn summary []
  [:div#summary
   [:h3 "Summary"]
   (when (:active @common/app-state) [:div
                                      (ui/control-buttons)
                                      [:p]])
   (when (rc/contains-key? :history)
     [:table {:class "table table-striped table-bordered" :id "summary"}
      [:thead {:class "thead-dark"}
       [:tr
        [:th "Task name"]
        [:th "Spent time"]
        [:th ""]]]
      (into [:tbody]
            (for [task (sum-usage)]
              [:tr {:key (:task-name task)}
               [:td (:task-name task)]
               [:td (tf/render-time (:length task))]
               [:td (ui/button-element :active "Restart" #(action/start-button-on-click (update-in task [:length] quot 1000)))]]))])])
