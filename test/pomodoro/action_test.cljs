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
  (is (= 200 (action/get-real-duration (r/atom {:start-time 0
                                                :now        200}))))
  (is (= 200 (action/get-real-duration (r/atom {:start-time  0
                                                :paused-time 100
                                                :paused      false
                                                :now         200}))))
  (is (= 100 (action/get-real-duration (r/atom {:start-time  0
                                                :paused-time 100
                                                :paused      true
                                                :now         200})))))

(deftest add-to-history
  (pomodoro.cookie-storage/delete-history)
  (action/add-to-history (r/atom {:task-name         "task-name"
                                  :length-in-seconds 61
                                  :start-time        0
                                  :key               0
                                  :duration          200
                                  :now               200}))
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

(deftest run-next-item-with-no-more-task
  (pomodoro.cookie-storage/delete-history)
  (let [app-state (r/atom {:remain-plan       []
                           :length-in-seconds 5
                           :key               3
                           :start-time        12345
                           :now               234567
                           :task-name         "task-name"
                           :dictionary        {:long-break  "Long break"
                                               :short-break "Short break"}
                           :length            10
                           :unit              :min
                           :get-key           #(int 4)})]

    (action/run-next-item app-state)
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

(deftest run-next-item-with-two-tasks
  (pomodoro.cookie-storage/delete-history)
  (let [task {:task-name         "task-name"
              :length            61
              :unit              :sec
              :key               "plan_3"
              :length-in-seconds 61}
        second-task {:task-name         "task-name"
                     :length            61
                     :unit              :sec
                     :key               "plan_4"
                     :length-in-seconds 61}
        tasks [task second-task]
        app-state (r/atom {:task-name  "task-name"
                           :dictionary {:long-break  "Long break"
                                        :short-break "Short break"}
                           :length     10
                           :unit       :min
                           :get-key    #(int 3)
                           :remain-plan       tasks
                           :length-in-seconds 5
                           :key               2
                           :start-time        12345
                           :now               123456})]


    (action/run-next-item app-state)
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