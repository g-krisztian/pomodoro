(ns pomodoro.test.action-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [pomodoro.action :refer [get-task-in-seconds]]))

(deftest get-task-in-seconds-test
  (testing "1 min is 60 seconds"
        (is (= 60 (get-task-in-seconds {:unit :min :length 1})))))

