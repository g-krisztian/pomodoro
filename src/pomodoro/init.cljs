(ns pomodoro.init
  (:require [pomodoro.action :as action]
            [ajax.core :refer [GET]]
            [pomodoro.cookie-storage :as storage]))

(defn browser-lang []
  (or (.-language js/navigator) (.-userLanguage js/navigator)))

(defn handler [state response]
  (let [dict (cljs.reader/read-string response)]
    (swap! state merge {:dictionary dict :task-name (:default-task-name dict)})))

(defn error-handler [{:keys [status status-text]} l]
  (.log js/console (str "something bad happened: " status " " status-text " for language: " l)))

(defn update-dictionary [state language]
  (GET (str "/dictionary_" language ".edn")
       {:handler       #(handler state %1)
        :error-handler #(error-handler %1 language)}))

(defn change-width [state]
  (swap! state assoc :width (.-innerWidth js/window)))

(defn init
  ([state storage]
   (init state storage (browser-lang)))
  ([state storage language]
   (.addEventListener js/window "resize" #(change-width state))
   (change-width state)
   (when-not (= "en-US") language) (update-dictionary state language)
   (storage/init storage)
   (action/reset-task state)))

