(ns pomodoro.core
  (:require [reagent.dom :as rd]
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

(storage/init :pomodoro)

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


(defn swap-view [state view]
  (swap! state merge {:view view}))

(defn show-view [state]
  (condp = (:view @state)
    :summary (summary/summary state)
    :history (history/history-table state)
    :planning (batch/planning state)
    (single/single-run state)))

(defn choose-view [state]
  (let [views [:single-run :planning :history :summary]]
    [:div
     (into [:div {:class "btn-group"}] (for [view views] (ui/button-element (@state :active) (dictionary view) #(swap-view state view))))
     [:p]]))


(action/reset-task app-state)

(defn main-loop [state]
  (swap! state merge [:now (.getTime (js/Date.))])
  (when-not (:paused @state)
    (swap! state update-in [:elapsed] inc)
    (when (> (:elapsed @state) (:length-in-seconds @state)) (action/finish state))))

(defonce ticker
         (js/setInterval #(main-loop app-state) 1000))

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
   [:p @storage/source]
   ;[:p (str (storage/get-plan))]
   (choose-view app-state)
   (show-view app-state)
   [rm/modal-window]])

(rd/render [applet] (. js/document (getElementById "app")))
