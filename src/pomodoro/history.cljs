(ns pomodoro.history
  (:require [pomodoro.ui-common :as ui]
            [pomodoro.time-format :as tf]
            [pomodoro.action :as action]
            [pomodoro.cookie-storage :as storage]
            [pomodoro.dictionary :as dict]))



(defn history-table [state]
  [:div#:history
   [:h3 (dict/get-text state :history)]
   (when (:active @state) [:div
                           (ui/control-buttons state)
                           [:p]])
   (let [history (storage/get-history)
         full-width (min 600 (* (:width @state) 0.94))
         width (* full-width 0.2)]
     (when (seq? history)
       [:table {:class "table table-striped table-bordered"
                :id "history"
                :style {:width full-width}}
        [:thead {:class "thead-dark"}
         [:tr
          [:th (dict/get-text state :task-name width)]
          (when (< 550 full-width) [:th (dict/get-text state :start-time width)])
          [:th (dict/get-text state :planed-duration width)]
          [:th (dict/get-text state :real-duration width)]
          [:th (ui/button-element (@state :active) width (dict/get-text state :remove width) storage/delete-history)]]]
        (into [:tbody]
              (for [task history]
                [:tr {:key (:key task)}
                 [:td (:task-name task)]
                 (when (< 550 full-width)[:td (.toLocaleString (js/Date. (:start task)))])
                 [:td (tf/render-time (* 1000 (:length task)))]
                 [:td (tf/render-time (:duration task))]
                 [:td (ui/button-element
                        (@state :active)
                        width
                        (dict/get-text state :restart width)
                        #(action/restart state task))]]))]))])