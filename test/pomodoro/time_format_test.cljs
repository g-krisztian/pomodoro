(ns pomodoro.time-format-test
  (:require [cljs.test :refer-macros [deftest is are run-tests testing]]
            [pomodoro.time-format :as format]))

(deftest seconds-from-long-milliseconds
  (are [x y] (= x (format/sec y))
             0 60
             3 3000
             3 3600
             45 45999))

(deftest minutes-from-long-milliseconds
  (are [x y] (= x (format/minute y))
             0 0
             5 300000
             9 560000
             16 960000
             16 1019999))

(deftest hours-from-long-milliseconds
  (are [x y] (= x (format/hour y))
             0 0
             1 (* 1 60 60 1000)
             1 (-> (* 1 60 60 1000) (* 2) (- 1))
             5 (* 5 60 60 1000)
             9 (* 9 60 60 1000)
             16 (* 16 60 60 1000)))

(deftest render-time-from-long-milliseconds
  (are [x y] (= x (format/render-time y))
             "00:00:00" 0
             "00:00:01" 1000
             "00:00:10" 10000
             "00:01:00" 60000
             "00:10:00" 600000
             "01:00:00" 3600000
             "10:00:00" 36000000
             "12:00:00" 43200000
             "23:59:59" 86399000
             "00:00:00" 86400000))
