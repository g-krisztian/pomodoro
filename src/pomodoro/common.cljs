(ns pomodoro.common
  (:require [reagent.core :as r]
            [reagent.cookies :as rc]))

(defn- get-key []
  (let [actual (rc/get :next-key 0)]
    (rc/set! :next-key (inc actual))
    actual))

(defonce app-state (r/atom {:get-key get-key
                            :length    25
                            :elapsed   0
                            :task-name "Default"
                            :now       (.getTime (js/Date.))
                            :view      :single-run
                            :unit      (rc/get :unit :min)}))

(def dictionary {:summary    "Summary"
                 :history    "History"
                 :planning   "Batch run"
                 :single-run "Single run"
                 :sec        "Second"
                 :min        "Minute"})

