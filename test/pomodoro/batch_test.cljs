(ns pomodoro.batch-test
  (:require [cljs.test :refer-macros [deftest is are run-tests testing]]
            [pomodoro.batch :as batch]
            [reagent.core :as r]))

(def task {:task-name         "task-name"
           :length            61
           :unit              :sec
           :key               "plan_3"
           :length-in-seconds 61})


(def state
  (r/atom {:task-name  "task-name"
           :dictionary {:long {:long-break  "Long break"
                               :short-break "Short break"}}
           :length     10
           :unit       :min}))

(deftest add-to-plan
  ;GIVEN
  (pomodoro.cookie-storage/delete-plan)
  ;WHEN
  (batch/add-to-plan task)
  ;THEN
  (let [plan (pomodoro.cookie-storage/get-plan)]
    (is (vector? plan))
    (is (= task (first plan)))))

(deftest add-new-task-to-plan
  ;GIVEN
  (pomodoro.cookie-storage/delete-plan)
  (pomodoro.cookie-storage/set-next-key 0)
  ;WHEN
  (batch/add-new-task-to-plan state)
  ;THEN
  (let [plan (pomodoro.cookie-storage/get-plan)]
    (is (vector? plan))
    (is (= {:task-name         "task-name"
            :length            10
            :unit              :min
            :key               "plan_0"
            :length-in-seconds 600} (first plan))))
  (pomodoro.cookie-storage/delete-plan))

(deftest get-task-in-milisec
  (is (= 61000 (batch/get-task-in-milisec task))))

(deftest add-short-break
  (pomodoro.cookie-storage/delete-plan)
  (pomodoro.cookie-storage/set-next-key 4)
  (batch/short-break state)
  (let [plan (pomodoro.cookie-storage/get-plan)
        break {:task-name         "Short break"
               :length-in-seconds 300
               :length            5
               :unit              :min
               :key               "plan_4"}]
    (is (vector? plan))
    (is (= break (first plan)))))

(deftest add-long-break
  (pomodoro.cookie-storage/delete-plan)
  (batch/long-break state)
  (let [plan (pomodoro.cookie-storage/get-plan)
        break {:task-name         "Long break"
               :length-in-seconds 900
               :length            15
               :unit              :min
               :key               "plan_5"}]
    (is (vector? plan))
    (is (= break (first plan)))))
