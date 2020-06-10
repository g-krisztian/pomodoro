(ns pomodoro.batch-test
  (:require [cljs.test :refer-macros [deftest is are run-tests testing]]
            [pomodoro.batch :as batch]
            [reagent.core :as r]))

(def task {:task-name         "task-name"
           :length            61
           :unit              :sec
           :key               "plan_3"
           :length-in-seconds 61})


(defn state [key]
  (r/atom {:task-name "task-name"
           :length    10
           :unit      :min
           :get-key   #(int key)}))

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
  ;WHEN
  (batch/add-new-task-to-plan (state 0))
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

(deftest run-next-item-with-to-tasks
  (pomodoro.cookie-storage/delete-history)
  (let [second-task (merge task {:key "plan_4"})
        tasks [task second-task]
        app-state (r/atom (merge @(state 3) {:remain-plan       tasks
                                             :length-in-seconds 5
                                             :key               2
                                             :start-time        12345}))

        now 123456]
    (batch/run-next-item app-state now)
    (let [history (pomodoro.cookie-storage/get-history)
          remain-plan (:remain-plan @app-state)]
      (is (seq? history))
      (is (not-empty history))
      (is (= {:task-name "task-name"
              :length    5
              :start     12345
              :key       "history_2"
              :duration  111111}
             (first history)))
      (is (vector? remain-plan))
      (is (not-empty remain-plan))
      (is (= second-task (first remain-plan)))
      (are [k v] (= (get @app-state k) v)
                 :paused false
                 :key 3
                 :length-in-seconds 61
                 :unit :sec
                 :task-name "task-name"
                 :stop false
                 :active true
                 :remain-plan [{:task-name         "task-name"
                                :length            61
                                :unit              :sec
                                :key               "plan_4"
                                :length-in-seconds 61}]
                 :length 61)))
  (pomodoro.cookie-storage/delete-history))

(deftest run-next-item-with-no-more-task
  (pomodoro.cookie-storage/delete-history)
  (let [app-state (r/atom (merge @(state 4) {:remain-plan       []
                                             :length-in-seconds 5
                                             :key               3
                                             :start-time        12345}))
        now 234567]
    (batch/run-next-item app-state now)
    (let [history (pomodoro.cookie-storage/get-history)
          remain-plan (:remain-plan @app-state)]
      ;check history
      (is (seq? history))
      (is (not-empty history))
      (is (= {:task-name "task-name"
              :length    5
              :start     12345
              :key       "history_3"
              :duration  222222}
             (first history)))
      ;check remain plan
      (is (vector? remain-plan))
      (is (empty remain-plan))
      ;check app-state is not running
      (are [k v] (= (get @app-state k) v)
                 :paused true
                 :key 3
                 :length-in-seconds 5
                 :start-time 12345
                 :task-name "task-name"
                 :stop true
                 :active false
                 :remain-plan []
                 :resume true
                 :elapsed 0)))
  (pomodoro.cookie-storage/delete-history))

(deftest add-short-break
  (pomodoro.cookie-storage/delete-plan)
  (batch/short-break (state 4))
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
  (batch/long-break (state 5))
  (let [plan (pomodoro.cookie-storage/get-plan)
        break {:task-name         "Long break"
               :length-in-seconds 900
               :length            15
               :unit              :min
               :key               "plan_5"}]
    (is (vector? plan))
    (is (= break (first plan)))))

(run-tests)