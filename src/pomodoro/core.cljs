(ns pomodoro.core
  (:require [reagent.dom :as rd]
            [pomodoro.action :as action]
            [pomodoro.ui-common :as ui]
            [pomodoro.summary :as summary]
            [pomodoro.history :as history]
            [pomodoro.batch :as batch]
            [pomodoro.single-run :as single]
            [pomodoro.time-format :as tf]
            [pomodoro.cookie-storage :as storage]
            [reagent.core :as r]
            [ajax.core :refer [GET]]))

(storage/init :pomodoro)

(def dictionary {:add-task          "Add task"
                 :clear-plan        "Clear plan"
                 :default-task-name "Task"
                 :history           "History"
                 :length            "Task length"
                 :long-break        "Long break"
                 :min               "Minutes"
                 :pause-timer       "Pause"
                 :planed-duration   "Planed duration"
                 :planed-time       "Planed time: "
                 :planning          "Batch run"
                 :real-duration     "Real duration"
                 :remove            "Remove"
                 :restart           "Restart"
                 :resume-timer      "Resume"
                 :run-next          "Run next"
                 :sec               "Seconds"
                 :short-break       "Short break"
                 :single-run        "Single run"
                 :spent-time        "Spent time"
                 :start-batch       "Start batch"
                 :start-time        "Start time"
                 :start-timer       "Start timer"
                 :stop-batch        "Stop batch"
                 :stop-timer        "Stop timer"
                 :summary           "Summary"
                 :task-name         "Task name"})

(defn width
  ([] (width 1))
  ([r] (* r (.-innerWidth js/window))))

(defonce app-state (r/atom {:get-key    storage/get-key
                            :width      width
                            :dictionary dictionary
                            :length     25
                            :elapsed    0
                            :task-name  "Default"
                            :now        (.getTime (js/Date.))
                            :view       :single-run
                            :unit       (or (storage/get-unit) :min)}))

(defn browser-lang []
  (or (.-language js/navigator) (.-userLanguage js/navigator)))

(defn handler [response]
  (let [dict (cljs.reader/read-string response)]
    (swap! app-state merge {:dictionary dict :task-name (:default-task-name dict)})))

(defn error-handler [{:keys [status status-text]} l]
  (.log js/console (str "something bad happened: " status " " status-text " for language: " l)))

(defn update-dictionary [l]
  (GET (str "/dictionary_" l ".edn")
       {:handler handler
        :error-handler #(error-handler %1 l)}))

(let [lang (browser-lang)]
  (when-not (= "en-US") lang) (update-dictionary lang))

(defn swap-view [state view]
  (swap! state merge {:view view}))

(defn show-view [state]
  (condp = (:view @state)
    :summary (summary/summary state)
    :history (history/history-table state)
    :planning (batch/planning state)
    (single/single-run state)))

(defn choose-view [state]
  (let [views [:single-run :planning :history :summary]
        width (:width @state)]
    [:div {:style {:width "10%"}}
     (into [:div {:class "btn-group"}] (for [view views] (ui/button-element (@state :active) (min 150 (width 0.238)) (action/dict app-state view) #(swap-view state view))))
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
                               (action/dict app-state (:view @app-state))
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
   (show-view app-state)])

(rd/render [applet] (. js/document (getElementById "app")))
