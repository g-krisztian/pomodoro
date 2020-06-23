(ns pomodoro.core
  (:require [reagent.dom :as rd]
            [pomodoro.action :as action]
            [pomodoro.init :as init]
            [pomodoro.time-format :as tf]
            [pomodoro.cookie-storage :as storage]
            [pomodoro.main :as main]
            [reagent.core :as r]))

(defonce app-state (r/atom {:get-key                 storage/get-key
                            :length                  25
                            :elapsed                 0
                            :task-name               "Default"
                            :now                     (.getTime (js/Date.))
                            :view                    :single-run
                            :unit                    (or (storage/get-unit) :min)}))

(init/init app-state :pomodoro)

(defn main-loop [state]
  (swap! state merge [:now (.getTime (js/Date.))])
  (when-not (:paused @state)
    (swap! state update-in [:elapsed] inc)
    (when (> (:elapsed @state) (:length-in-seconds @state)) (action/finish state))))

(defonce ticker
         (js/setInterval #(main-loop app-state) 1000))

(defn applet []
  (action/set-title app-state)
  (when (:dictionary @app-state)
    (main/main app-state
               (main/choose-view app-state)
               (main/show-view app-state))))

(rd/render [applet] (. js/document (getElementById "app")))
