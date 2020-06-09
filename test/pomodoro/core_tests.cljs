(ns pomodoro.core-tests
  (:require [cljs.test :refer-macros [deftest is run-tests testing]]
            [pomodoro.core :as pomodoro]
            [reagent.core :as r]))

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

(deftest swap-view
  (let [view "single-run"]
    (is (= view ((pomodoro/swap-view (atom {}) view) :view)))))

(deftest show-view
  (is :div#single-run (first (pomodoro/show-view (r/atom {:view :single-run}))))
  (is :div#planning (first (pomodoro/show-view (r/atom {:view :planning}))))
  (is :div#history (first (pomodoro/show-view (r/atom {:view :history}))))
  (is :div#summary (first (pomodoro/show-view (r/atom {:view :summary}))))
  (is :div#single-run (first (pomodoro/show-view (r/atom {})))))


(run-tests)