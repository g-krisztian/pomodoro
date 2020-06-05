(ns pomodoro.core
  (:require [reagent.dom :as rd]
            [reagent.cookies :as rc]
            [reagent-modals.modals :as rm]
            [pomodoro.action :as action]
            [pomodoro.ui-common :as ui]
            [pomodoro.summary :as summary]
            [pomodoro.history :as history]
            [pomodoro.batch :as batch]
            [pomodoro.single-run :as single]
            [pomodoro.time-format :as tf]
            [pomodoro.cookie-storage :as storage]
            [reagent.core :as r]))

(enable-console-print!)

(def dictionary {:summary    "Summary"
                 :history    "History"
                 :planning   "Batch run"
                 :single-run "Single run"
                 :sec        "Second"
                 :min        "Minute"})

(defonce app-state (r/atom {:get-key   storage/get-key
                            :dictionary dictionary
                            :length    25
                            :elapsed   0
                            :task-name "Default"
                            :now       (.getTime (js/Date.))
                            :view      :single-run
                            :unit      (or (storage/get-unit) :min)}))

(when-not (or (storage/get-plan) (storage/get-history)) (storage/set-next-key 0))

(defn swap-view [view]
  (swap! app-state merge {:view view}))

(defn choose-view [label]
  (let [views [:single-run :planning :history :summary]]
    [:div
     (into [:div {:class "btn-group"}] (for [view views] (ui/button-element (@app-state :active) (dictionary view) #(swap-view view))))
     [:p]
     (condp = label
       :summary (summary/summary app-state)
       :history (history/history-table app-state)
       :planning (batch/planning app-state)
       (single/single-run app-state))]))

(action/reset-task app-state)

(defn main-loop []
  (swap! app-state merge [:now (.getTime (js/Date.))])
  (when-not (:paused @app-state)
    (swap! app-state update-in [:elapsed] inc)
    (when (> (:elapsed @app-state) (:length-in-seconds @app-state)) (action/finish app-state))))

(defonce ticker
         (js/setInterval main-loop 1000))

(defn set-title []
  (set! js/document.title (str "Pompdoro - "
                               ((:view @app-state) dictionary)
                               " "
                               (when
                                 (:active @app-state)
                                 (str "| "
                                      (:task-name @app-state)
                                      ": "
                                      (tf/render-time (* 1000 (:elapsed @app-state))))))))

(defn applet []
  (set-title)
  [:div#app {:style {:margin "auto"
                     :width  "max-content"}}
   [:h1 "Pomodoro app"]
   ;   [button-element :modal "modal" #(rm/modal! [:p "semmi"])]
   [:h3 (str "Time: " (tf/render-time (tf/correct-time (:now @app-state))))]
   ;[:p (str @app-state)]
   ;[:p (str (storage/get-plan))]
   (choose-view (:view @app-state))
   [rm/modal-window]])

(rd/render [applet] (. js/document (getElementById "app")))

