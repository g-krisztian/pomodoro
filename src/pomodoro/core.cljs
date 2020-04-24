(ns pomodoro.core
  (:require [reagent.dom :as rd]
            [reagent.cookies :as rc]
            [reagent-modals.modals :as rm]
            [pomodoro.action :as action]
            [pomodoro.ui-common :as ui]
            [pomodoro.common :as common]
            [pomodoro.summary :as summary]
            [pomodoro.history :as history]
            [pomodoro.batch :as batch]
            [pomodoro.single-run :as single]
            [pomodoro.time-format :as tf]))

(enable-console-print!)

(when-not (or (rc/get :plan) (rc/get :history)) (rc/set! :next-key 0))

(defn swap-view [view]
  (swap! common/app-state merge {:view view}))

(defn choose-view [label]
  (let [views [:single-run :planning :history :summary]]
    [:div
     (into [:div {:class "btn-group"}] (for [view views] (ui/button-element :active (common/dictionary view) #(swap-view view))))
     [:p]
     (condp = label
       :summary (summary/summary)
       :history (history/history-table)
       :planning (batch/planning)
       (single/single-run))]))

(action/reset-task)

(defn main-loop []
  (swap! common/app-state merge [:now (.getTime (js/Date.))])
  (when-not (:paused @common/app-state)
    (swap! common/app-state update-in [:elapsed] inc)
    (when (> (:elapsed @common/app-state) (:length-in-seconds @common/app-state)) (action/finish))))

(defonce ticker
         (js/setInterval main-loop 1000))

(defn set-title []
  (set! js/document.title (str "Pompdoro - "
                               ((:view @common/app-state) common/dictionary)
                               " "
                               (when
                                 (:active @common/app-state)
                                 (str "| "
                                      (:task-name @common/app-state)
                                      ": "
                                      (tf/render-time (* 1000 (:elapsed @common/app-state))))))))

(defn applet []
  (set-title)
  [:div#app {:style {:margin "auto"
                     :width  "max-content"}}
   [:h1 "Pomodoro app"]
   ;   [button-element :modal "modal" #(rm/modal! [:p "semmi"])]
   [:h3 (str "Time: " (tf/render-time (tf/correct-time (:now @common/app-state))))]
   ;[:p (str @common/app-state)]
   ;[:p (str (rc/get :plan))]
   (choose-view (:view @common/app-state))
   [rm/modal-window]])

(rd/render [applet] (. js/document (getElementById "app")))

