(ns pomodoro.core-tests
  (:require [cljs.test :refer-macros [deftest is run-tests testing]]
            [pomodoro.core :as pomodoro]))

(pomodoro.cookie-storage/init :pomodoro-test)

(deftest dictionary
  "## Here are some example tests"
  (testing "dictionary"
           (is (= {:summary    "Summary"
                   :history    "History"
                   :planning   "Batch run"
                   :single-run "Single run"
                   :sec        "Second"
                   :min        "Minute"}
                  pomodoro/dictionary))) "Dictionary")




(run-tests)

#_(deftest
    first-testers
    "## This is documentation\n   It should work well"
    (testing
      "good stuff"
      (is (= (+ 3 4 55555) 5) "Testing the adding")
      (is (= (+ 1 0 0 0) 1) "This should work")
      (is (= 1 3))
      (is false)
      (is (throw "heck"))
      (is (js/asdf)))
    "## And here is more documentation"
    (testing
      "bad stuff"
      (is (= (+ 1 0 0 0) 1))
      (is (= (+ 3 4 55555) 4))
      (is false)
      (testing
        "mad stuff"
        (is (= (+ 1 0 0 0) 1))
        (is (= (+ 3 4 55555) 4))
        (is false))))