(ns pomodoro.single-run
  (:require [pomodoro.ui-common :as ui]
            [pomodoro.action :as action]))

(defn single-run [state]
  [:div#single-run
   [:div
    [:h3 "Single run"]
    (ui/text-input state :task-name #(action/start-on-enter % state))
    (ui/input-length state :length #(action/start-on-enter % state))]
   (ui/control-buttons state)])
