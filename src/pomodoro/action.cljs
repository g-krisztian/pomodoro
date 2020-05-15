(ns pomodoro.action
  (:require [pomodoro.common :as common]
            [reagent.cookies :as rc]
            [pomodoro.audio :as audio]))

(defn get-task-in-seconds [task]
  (if (= (:unit task) :min)
    (* 60 (:length task))
    (:length task)))

(defn new-task []
  (let [task (select-keys @common/app-state [:task-name :length :unit])]
    (merge task {:key (str "plan_" ((@common/app-state :get-key))) :length-in-seconds (get-task-in-seconds task)})))

(defn start-button-on-click [task]
  (swap! common/app-state merge
         {:start-time        (.getTime (js/Date.))
          :elapsed           0
          :paused            false
          :active            true
          :stop              false
          :length-in-seconds (get-task-in-seconds (select-keys @common/app-state [:length :unit]))}
         task
         {:key ((@common/app-state :get-key))}))

(defn reset-task []
  (swap! common/app-state merge {:paused  true
                                 :active  false
                                 :stop    true
                                 :resume  true
                                 :elapsed 0}))
(defn get-real-duration []
  (let [now (.getTime (js/Date.))
        start (:start-time @common/app-state)
        real-duration (- now start)
        paused-duration (- (:paused-time @common/app-state) start)]
    (if (:paused @common/app-state false) paused-duration real-duration)))

(defn add-to-history []
  (rc/set! :history
           (conj (rc/get :history)
                 {:task-name (:task-name @common/app-state)
                  :length    (:length-in-seconds @common/app-state)
                  :start     (:start-time @common/app-state)
                  :key       (str "history_" (:key @common/app-state))
                  :duration  (get-real-duration)})))

(defn stop-button-on-click []
  (add-to-history)
  (reset-task))

(defn run-new-task []
  (start-button-on-click (new-task)))

(defn start-on-enter [event]
  (when (= 13 (.-charCode event)) (run-new-task)))

(defn delete-history-on-click []
  (rc/remove! :history))

(defn start-plan-on-click [batch]
  (when-not (empty? batch)
    (do (swap! common/app-state merge {:remain-plan (rest batch)})
        (start-button-on-click (select-keys (first batch) [:length :task-name :unit :length-in-seconds])))))

(defn pause-button-on-click []
  (swap! common/app-state update-in [:paused] not)
  (swap! common/app-state update-in [:resume] not)
  (swap! common/app-state merge {:paused-time (.getTime (js/Date.))}))

(defn finish []
  (stop-button-on-click)
  (audio/playback-mp3)
  (when-not (empty? (:remain-plan @common/app-state)) (start-plan-on-click (:remain-plan @common/app-state))))
