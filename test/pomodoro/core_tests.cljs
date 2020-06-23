(ns pomodoro.core-tests
  (:require [cljs.test :refer-macros [deftest is run-tests testing]]
            [pomodoro.core :as pomodoro]
            [reagent.core :as r]))

(pomodoro.cookie-storage/init :pomodoro-test)
