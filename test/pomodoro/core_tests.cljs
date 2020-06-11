(ns pomodoro.core-tests
  (:require [cljs.test :refer-macros [deftest is run-tests testing]]
            [pomodoro.core :as pomodoro]
            [reagent.core :as r]))

(pomodoro.cookie-storage/init :pomodoro-test)

(defn width [] (int 360))

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
    (is (= view ((pomodoro/swap-view (r/atom {}) view) :view)))))

(deftest show-view
  (is :div#single-run (first (pomodoro/show-view (r/atom {:view :single-run
                                                          :width width}))))
  (is :div#planning (first (pomodoro/show-view (r/atom {:view :planning
                                                        :width width}))))
  (is :div#history (first (pomodoro/show-view (r/atom {:view :history
                                                       :width width}))))
  (is :div#summary (first (pomodoro/show-view (r/atom {:view :summary
                                                       :width width}))))
  (is :div#single-run (first (pomodoro/show-view (r/atom {:width width})))))
