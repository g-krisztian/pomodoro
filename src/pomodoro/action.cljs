(ns pomodoro.action
  (:require [reagent.cookies :as rc]
            [pomodoro.audio :as audio]))

(defn get-task-in-seconds [task]
  (if (= (:unit task) :min)
    (* 60 (:length task))
    (:length task)))

(defn new-task [state]
  (let [task (select-keys @state [:task-name :length :unit])]
    (merge task {:key (str "plan_" ((@state :get-key))) :length-in-seconds (get-task-in-seconds task)})))

(defn start-button-on-click [state task]
  (swap! state merge
         {:start-time        (.getTime (js/Date.))
          :elapsed           0
          :paused            false
          :active            true
          :stop              false
          :length-in-seconds (get-task-in-seconds (select-keys @state [:length :unit]))}
         task
         {:key ((@state :get-key))}))

(defn reset-task [state]
  (swap! state merge {:paused  true
                      :active  false
                      :stop    true
                      :resume  true
                      :elapsed 0}))

(defn get-real-duration [state]
  (let [now (.getTime (js/Date.))
        start (:start-time @state)
        real-duration (- now start)
        paused-duration (- (:paused-time @state) start)]
    (if (:paused @state false) paused-duration real-duration)))

(defn add-to-history [state]
  (rc/set! :history
           (conj (rc/get :history)
                 {:task-name (:task-name @state)
                  :length    (:length-in-seconds @state)
                  :start     (:start-time @state)
                  :key       (str "history_" (:key @state))
                  :duration  (get-real-duration state)})))

(defn stop-button-on-click [state]
  (add-to-history state)
  (reset-task state))

(defn run-new-task [state]
  (start-button-on-click state (new-task state)))

(defn start-on-enter [event state]
  (when (= 13 (.-charCode event)) (run-new-task state)))

(defn delete-history-on-click []
  (rc/remove! :history))

(defn start-plan-on-click [state batch]
  (when-not (empty? batch)
    (do (swap! state merge {:remain-plan (rest batch)})
        (start-button-on-click state (select-keys (first batch) [:length :task-name :unit :length-in-seconds])))))

(defn pause-button-on-click [state]
  (swap! state update-in [:paused] not)
  (swap! state update-in [:resume] not)
  (swap! state merge {:paused-time (.getTime (js/Date.))}))

(defn finish [state]
  (stop-button-on-click state)
  (audio/playback-mp3)
  (when-not (empty? (:remain-plan @state)) (start-plan-on-click state (:remain-plan @state))))
