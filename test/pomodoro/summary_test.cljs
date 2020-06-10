(ns pomodoro.summary-test
  (:require [cljs.test :refer-macros [deftest is are run-tests testing]]
            [pomodoro.summary :as summary]))

(def history '({:task-name "Default_1"
                :length    25
                :start     1591698324666
                :key       "history_1"
                :duration  16123}
               {:task-name "Default_1"
                :length    25
                :start     1591698324666
                :key       "history_2"
                :duration  16123}
               {:task-name "Default_2"
                :length    25
                :start     1591698324666
                :key       "history_3"
                :duration  16123}
               {:task-name "Default_2"
                :length    25
                :start     1591698324666
                :key       "history_4"
                :duration  26123}
               {:task-name "Default_3"
                :length    25
                :start     1591698324666
                :key       "history_5"
                :duration  16123}
               {:task-name "Default_3"
                :length    25
                :start     1591693606227
                :key       "history_6"
                :duration  26237}))

(deftest history-aggregate-to-summary
  (let [summary (summary/sum-usage history)]
    (is (= '({:task-name "Default_1" :length 32246}
             {:task-name "Default_2" :length 42246}
             {:task-name "Default_3" :length 42360})
           summary))))
