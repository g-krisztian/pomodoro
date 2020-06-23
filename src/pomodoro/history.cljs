(ns pomodoro.history
  (:require [pomodoro.ui-common :as ui]
            [pomodoro.time-format :as tf]
            [pomodoro.action :as action]
            [pomodoro.cookie-storage :as storage]
            [pomodoro.dictionary :as dict]))



(defn history-table [state]
  [:div#:history
   [:h3 (dict/get state :history)]
   (when (:active @state) [:div
                           (ui/control-buttons state)
                           [:p]])
   (let [history (storage/get-history)
         full-width (min 600 (* (:width @state) 0.94))
         width (* full-width 0.2)
         str-delete (if (< 550 full-width) (dict/get state :remove) "Delete")]
     (when (seq? history)
       [:table {:class "table table-striped table-bordered"
                :id "history"
                :style {:width full-width}}
        [:thead {:class "thead-dark"}
         [:tr
          [:th (dict/get state :task-name)]
          (when (< 550 full-width) [:th (dict/get state :start-time)])
          [:th (dict/get state :planed-duration)]
          [:th (dict/get state :real-duration)]
          [:th (ui/button-element (@state :active) width str-delete storage/delete-history)]]]
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
                        (dict/get state :restart)
                        #(action/restart state task))]]))]))])