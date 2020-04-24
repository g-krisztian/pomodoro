(ns pomodoro.single-run
  (:require [pomodoro.ui-common :as ui]
            [pomodoro.action :as action]))

(defn single-run []
  [:div#single-run
   [:div
    [:h3 "Single run"]
    (ui/text-input :task-name action/start-on-enter)
    (ui/input-length :length action/start-on-enter)]
   (ui/control-buttons)])
