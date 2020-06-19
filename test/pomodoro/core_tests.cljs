(ns pomodoro.core-tests
  (:require [cljs.test :refer-macros [deftest is run-tests testing]]
            [pomodoro.core :as pomodoro]
            [reagent.core :as r]))

(pomodoro.cookie-storage/init :pomodoro-test)

(defn width [] (int 360))

(deftest dictionary
  "## Here are some example tests"
  (testing "dictionary"
           (is (= {:add-task          "Add task"
                   :clear-plan        "Clear plan"
                   :default-task-name "Task"
                   :history           "History"
                   :length            "Task length"
                   :long-break        "Long break"
                   :min               "Minutes"
                   :pause-timer       "Pause"
                   :planed-duration   "Planed duration"
                   :planed-time       "Planed time: "
                   :planning          "Batch run"
                   :real-duration     "Real duration"
                   :remove            "Remove"
                   :restart           "Restart"
                   :resume-timer      "Resume"
                   :run-next          "Run next"
                   :sec               "Seconds"
                   :short-break       "Short break"
                   :single-run        "Single run"
                   :spent-time        "Spent time"
                   :start-batch       "Start batch"
                   :start-time        "Start time"
                   :start-timer       "Start timer"
                   :stop-batch        "Stop batch"
                   :stop-timer        "Stop timer"
                   :summary           "Summary"
                   :task-name         "Task name"}
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
