(ns pomodoro.init
  (:require [pomodoro.action :as action]
            [ajax.core :refer [GET]]
            [pomodoro.cookie-storage :as storage]
            [pomodoro.dictionary :as dict]
            [pomodoro.time-format :as tf]))

(defn browser-language []
  (->> (or (.-language js/navigator) (.-userLanguage js/navigator))
       (re-matches #"([a-z]{2}).*")
       second))

(defn load-dictionary [state response]
  (let [dict (cljs.reader/read-string response)]
    (swap! state merge {:dictionary dict :task-name (get-in dict [:long :default-task-name])})))

(defn get-dictionary [state language]
  (GET (str "/dictionary_" language ".edn")
       {:handler       #(load-dictionary state %1)
        :error-handler #(get-dictionary state "en")}))

(defn change-width [state]
  (swap! state assoc :width (.-innerWidth js/window)))

(defn set-title [state]
  (set! js/document.title (str "Pompdoro - "
                               (dict/get-text state (:view @state))
                               " "
                               (when
                                 (:active @state)
                                 (str "| "
                                      (:task-name @state)
                                      ": "
                                      (tf/render-time (* 1000 (:elapsed @state))))))))
(defn init
  ([state storage]
   (init state storage (browser-language)))
  ([state storage language]
   (set-title state)
   (.addEventListener js/window "resize" #(change-width state))
   (change-width state)
   (get-dictionary state language)
   (storage/init storage)
   (action/reset-task state)))

