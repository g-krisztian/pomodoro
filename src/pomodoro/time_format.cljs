(ns pomodoro.time-format
  (:require [reagent.format :as format]))

(defonce time-offset (* 60000 (.getTimezoneOffset (js/Date.))))

(defn sec [time]
  (mod (quot time 1000) 60))

(defn minute [time]
  (-> time
      (quot 60000)
      (mod 60))) 

(defn hour [time]
  (-> time
      (quot 3600000)
      (mod 24)))

(defn render-time [time]
  (format/format "%02d:%02d:%02d" (hour time) (minute time) (sec time)))

(defn correct-time [time]
  (- time time-offset))
