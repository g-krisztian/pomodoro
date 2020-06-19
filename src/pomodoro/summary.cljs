(ns pomodoro.summary
  (:require [pomodoro.time-format :as tf]
            [pomodoro.ui-common :as ui]
            [pomodoro.action :as action]
            [pomodoro.cookie-storage :as storage]))

(defn sum-usage [history]
  (->> history
       (group-by :task-name)
       (map (fn [[task-name v]] (let [length (reduce + (for [d v] (:duration d)))]
                                  {:task-name task-name
                                   :length    length})))))
(defn summary [state]
  [:div#summary
   [:h3 (get-in @state [:dictionary :summary])]
   (when (:active @state) [:div
                           (ui/control-buttons state)
                           [:p]])
   (let [history (storage/get-history)
         full-width ((:width @state) 0.94)
         width (min 120 (* full-width 0.33))]
     (when (seq? history)
       [:table {:class "table table-striped table-bordered" :id "summary" :style {:width (min 600 full-width)}}
        [:thead {:class "thead-dark"}
         [:tr
          [:th (get-in @state [:dictionary :task-name])]
          [:th (get-in @state [:dictionary :spent-time])]
          [:th ""]]]
        (into [:tbody]
              (for [task (sum-usage history)]
                [:tr {:key (:task-name task)}
                 [:td (:task-name task)]
                 [:td (tf/render-time (:length task))]
                 [:td {:style {:width width}}
                  (ui/button-element (@state :active) width (get-in @state [:dictionary :restart]) #(action/restart state (update-in task [:length] quot 1000)))]]))]))])
