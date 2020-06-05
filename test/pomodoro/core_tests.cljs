(ns pomodoro.core-tests
  (:require [cljs.test :refer-macros [deftest is run-tests]]
            [pomodoro.core :as pomodoro]))

#_(deftest should-not-pass
           (is (= 1 20)))

(deftest dictionary
  "Is dictionary works"
  (is (= {:summary    "Summary"
          :history    "History"
          :planning   "Batch run"
          :single-run "Single run"
          :sec        "Second"
          :min        "Minute"}
         pomodoro/dictionary)))


(run-tests)
