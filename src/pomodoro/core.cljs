(ns pomodoro.core
  (:require [reagent.dom :as rd]
            [pomodoro.action :as action]
            [pomodoro.init :as init]
            [pomodoro.cookie-storage :as storage]
            [pomodoro.main :as main]
            [reagent.core :as r]
            [pomodoro.dictionary :as dict]))

(defonce app-state (r/atom {:get-key   storage/get-key
                            :length    25
                            :ready     false
                            :elapsed   0
                            :task-name "Default"
                            :now       (.getTime (js/Date.))
                            :view      :single-run
                            :unit      (or (storage/get-unit) :min)}))

(init/init app-state :pomodoro)

(defn main-loop [state]
  (swap! state merge [:now (.getTime (js/Date.))])
  (when-not (:paused @state)
    (swap! state update-in [:elapsed] inc)
    (when (> (:elapsed @state) (:length-in-seconds @state)) (action/finish state))))

(defonce ticker
         (js/setInterval #(main-loop app-state) 1000))

(defn applet []
  (when (:dictionary @app-state)
    (main/main app-state
               (when (:ready @app-state)
                 (main/set-title app-state)
                 [:div
                  (when (:dictionaries @app-state) (main/lang-switcher app-state))
                  (main/choose-view app-state)
                  (main/show-view app-state)])
      (dict/span app-state :hidden))))


(rd/render [applet] (. js/document (getElementById "app")))
