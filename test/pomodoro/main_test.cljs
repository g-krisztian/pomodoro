(ns pomodoro.main-test
  (:require [cljs.test :refer-macros [deftest is run-tests testing]]
            [pomodoro.main :as main]
            [reagent.core :as r]))

(deftest swap-view
  (let [view "single-run"]
    (is (= view ((main/swap-view (r/atom {}) view) :view)))))

(deftest show-view
  (is :div#single-run (first (main/show-view (r/atom {:view  :single-run
                                                      :width 360}))))
  (is :div#planning (first (main/show-view (r/atom {:view  :planning
                                                    :width 360}))))
  (is :div#history (first (main/show-view (r/atom {:view  :history
                                                   :width 360}))))
  (is :div#summary (first (main/show-view (r/atom {:view  :summary
                                                   :width 360}))))
  (is :div#single-run (first (main/show-view (r/atom {:width 360})))))
