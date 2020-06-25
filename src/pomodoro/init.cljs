(ns pomodoro.init
  (:require [pomodoro.action :as action]
            [pomodoro.cookie-storage :as storage]
            [pomodoro.dictionary :as dict]
            [pomodoro.time-format :as tf]))

(defn browser-language []
  (->> (or (.-language js/navigator) (.-userLanguage js/navigator))
       (re-matches #"([a-z]{2}).*")
       second))

(defn change-width [state]
  (swap! state assoc :width (.-innerWidth js/window)))

(defn init
  ([state storage]
   (init state storage (browser-language)))
  ([state storage language]
   (.addEventListener js/window "resize" #(change-width state))
   (change-width state)
   (dict/get-dictionaries state)
   (dict/get-dictionary state language)
   (storage/init storage)
   (action/reset-task state)))

