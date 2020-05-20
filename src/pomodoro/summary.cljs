(ns pomodoro.summary
  (:require [pomodoro.time-format :as tf]
            [pomodoro.ui-common :as ui]
            [pomodoro.action :as action]
            [pomodoro.cookie-storage :as storage]))

(defn sum-usage []
  (->> (storage/get-history)
       (group-by :task-name)
       (map (fn [[task-name v]] (let [length (reduce + (for [d v] (:duration d)))]
                                  {:task-name task-name
                                   :length    length})))))
(defn summary [state]
  [:div#summary
   [:h3 "Summary"]
   (when (:active @state) [:div
                           (ui/control-buttons state)
                           [:p]])
   (when (storage/contains-history?)
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
               [:td (ui/button-element (@state :active) "Restart" #(action/restart state (update-in task [:length] quot 1000)))]]))])])
