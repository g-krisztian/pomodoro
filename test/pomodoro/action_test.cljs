(ns pomodoro.action-test
  (:require [cljs.test :refer-macros [deftest is are run-tests testing]]
            [pomodoro.action]
            [pomodoro.action :as action]
            [reagent.core :as r]))

(deftest get-seconds-by-task
  (testing "It get seconds by task"
    (is (= 60 (pomodoro.action/get-task-in-seconds {:unit :min :length 1})) "One minute should be 60 seconds")
    (is (= 31 (pomodoro.action/get-task-in-seconds {:unit :sec :length 31})) "31 secs are 31 seconds")
    (is (= 11 (pomodoro.action/get-task-in-seconds {:length 11})) "If the unit is not :min then it is :sec")
    (is (nil? (pomodoro.action/get-task-in-seconds {})))) "If no :length the the result is nil")

(deftest plan-creation
  (testing "Creating a task for a plan"
    (let [keygen #(int 3)]
      (is (= {:task-name         "task-name"
              :length            61
              :unit              :sec
              :key               "plan_3"
              :length-in-seconds 61}
             (pomodoro.action/new-plan (r/atom {:task-name "task-name"
                                                :length    61
                                                :unit      :sec
                                                :get-key   keygen})))
          "Creates a task"))))

(deftest swap-unit
  (testing "swapping unit"
    (is (= {:unit :min} (pomodoro.action/swap-unit (r/atom {}) :min)) "set unit to minute")
    (is (= {:unit :sec} (pomodoro.action/swap-unit (r/atom {}) :sec))) "set unit to second"))

(deftest start-button-on-click
  (let [next-key 1
        keygen #(int next-key)
        state (r/atom {:task-name "task-name"
                       :length    10
                       :unit      :min
                       :get-key   keygen})
        result (action/start-button-on-click state)]
    (are [x y] (= x y)
               (result :active) true
               (result :paused) false
               (result :stop) false
               (int? (result :start-time)) true
               (result :key) next-key
               (result :length) 10
               (result :unit) :min
               (result :length-in-seconds) 600
               (result :task-name) "task-name"
               (fn? (result :get-key)) true
               (result :elapsed) 0)))

(deftest run-plan
  (let [keygen #(int 1)
        task {:task-name         "task-name"
              :length            61
              :unit              :sec
              :key               "plan_3"
              :length-in-seconds 61}
        state (r/atom {:remain-plan [task]
                       :get-key     keygen})]
    (action/run-plan state)
    (are [x y] (= (get @state x) y)
               :paused false
               :key 1
               :length-in-seconds 61
               :unit :sec
               :task-name "task-name"
               :stop false
               :active true
               :remain-plan []
               :length 61
               :elapsed 0)
    (is (int? (:start-time @state)))))

(deftest start-plan
  (let [keygen #(int 1)
        task {:task-name         "task-name"
              :length            61
              :unit              :sec
              :key               "plan_3"
              :length-in-seconds 61}
        state (r/atom {:get-key keygen})]
    (pomodoro.cookie-storage/set-plan [task])
    (action/start-plan state)
    (are [x y] (= (get @state x) y)
               :paused false
               :key 1
               :length-in-seconds 61
               :unit :sec
               :task-name "task-name"
               :stop false
               :active true
               :remain-plan []
               :length 61
               :elapsed 0))
  (pomodoro.cookie-storage/delete-plan))

(deftest restart
  (let [keygen #(int 1)
        task {:task-name         "task-name"
              :length            61
              :unit              :sec
              :key               "history_4"
              :length-in-seconds 61}
        state (r/atom {:get-key keygen})]
    (action/restart state task)
    (are [x y] (= (get @state x) y)
               :paused false
               :key 1
               :length-in-seconds 61
               :unit :sec
               :task-name "task-name"
               :stop false
               :active true
               :length 61
               :elapsed 0)
    (is (int? (:start-time @state)))))

(deftest reset-task
  (is (= {:paused  true
          :active  false
          :stop    true
          :resume  true
          :elapsed 0}
         (action/reset-task (atom {}))))
  (is (= {:paused  true
          :active  false
          :stop    true
          :resume  true
          :elapsed 0
          :length  1
          :unit    :sec}
         (action/reset-task (atom {:length 1 :unit :sec})))))

(deftest get-real-duration
  (is (= 200 (action/get-real-duration (r/atom {:start-time 0}) 200)))
  (is (= 200 (action/get-real-duration (r/atom {:start-time  0
                                                :paused-time 100
                                                :paused      false}) 200)))
  (is (= 100 (action/get-real-duration (r/atom {:start-time  0
                                                :paused-time 100
                                                :paused      true}) 200))))

(deftest add-to-history
  (pomodoro.cookie-storage/delete-history)
  (action/add-to-history (r/atom {:task-name         "task-name"
                                  :length-in-seconds 61
                                  :start-time        0
                                  :key               0
                                  :duration          200}) 200)
  (let [history (pomodoro.cookie-storage/get-history)]
    (are [k v] (= (get (first history) k) v)
               :task-name "task-name"
               :length 61
               :start 0
               :key "history_0"
               :duration 200))
  (pomodoro.cookie-storage/delete-history))

(deftest pause-button-on-click
  (let [state (atom {})]
    (action/pause-button-on-click state)
    (is (:paused @state))
    (is (:resume @state))
    (is (int? (:paused-time @state))))

  (let [state (atom {:paused true
                     :resume true})]
    (action/pause-button-on-click state)
    (is (not (:paused @state)))
    (is (not (:resume @state)))
    (is (int? (:paused-time @state)))))
