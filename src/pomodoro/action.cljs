(ns pomodoro.action
  (:require [pomodoro.audio :as audio]
            [pomodoro.cookie-storage :as storage]
            [pomodoro.time-format :as tf]))

(defn get-task-in-seconds [task]
  (if (= (:unit task) :min)
    (* 60 (:length task))
    (:length task)))

(defn new-plan [state]
  (let [task (select-keys @state [:task-name :length :unit])]
    (merge task {:key               (str "plan_" ((@state :get-key)))
                 :length-in-seconds (get-task-in-seconds task)})))

(defn swap-value [state key e]
  (swap! state merge {key (-> e .-target .-value)}))

(defn swap-unit [state m]
  (storage/set-unit m)
  (swap! state merge {:unit m}))

(defn start-button-on-click [state]
  (swap! state merge
         {:start-time        (.getTime (js/Date.))
          :elapsed           0
          :paused            false
          :resume            true
          :active            true
          :stop              false
          :length-in-seconds (get-task-in-seconds @state)
          :key               ((@state :get-key))}))

(defn run-plan [state]
  (let [batch (:remain-plan @state)]
    (when-not (empty? batch)
      (let [task (select-keys (first batch) [:length :task-name :unit :length-in-seconds])]
        (swap! state merge {:remain-plan (vec (rest batch))} task)
        (start-button-on-click state)))))

(defn start-plan [state]
  (swap! state merge {:remain-plan (or (storage/get-plan) [])})
  (run-plan state))

(defn restart [state task]
  (swap! state merge (select-keys task [:length :task-name :unit :length-in-seconds]))
  (start-button-on-click state))

(defn reset-task [state]
  (swap! state merge {:paused  true
                      :active  false
                      :stop    true
                      :resume  true
                      :elapsed 0}))

(defn get-real-duration [state]
  (let [start (:start-time @state)
        now (:now @state)
        real-duration (- now start)
        paused-duration (- (:paused-time @state) start)]
    (if (:paused @state false) paused-duration real-duration)))

(defn add-to-history [state]
  (storage/set-history
    (conj (storage/get-history)
          {:task-name (:task-name @state)
           :length    (:length-in-seconds @state)
           :start     (:start-time @state)
           :key       (str "history_" (:key @state))
           :duration  (get-real-duration state)})))

(defn stop-button-on-click [state]
  (add-to-history state)
  (reset-task state))

(defn start-on-enter [event state]
  (when (= 13 (.-charCode event)) (start-button-on-click state)))

(defn pause-button-on-click [state]
  (swap! state update-in [:paused] not)
  (swap! state update-in [:resume] not)
  (swap! state merge {:paused-time (.getTime (js/Date.))}))

(defn finish [state]
  (stop-button-on-click state)
  (audio/playback-mp3)
  (when-not (empty? (:remain-plan @state)) (run-plan state)))

(defn dict
  ([state key]
   (get-in @state [:dictionary :long key]))
  ([state key width]
   (let [short-value (get-in @state [:dictionary :short key])
         long-value (get-in @state [:dictionary :long key])]
     (or short-value long-value))))

(defn set-title [state]
  (set! js/document.title (str "Pompdoro - "
                               (dict state (:view @state))
                               " "
                               (when
                                 (:active @state)
                                 (str "| "
                                      (:task-name @state)
                                      ": "
                                      (tf/render-time (* 1000 (:elapsed @state))))))))