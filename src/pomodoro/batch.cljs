(ns pomodoro.batch
  (:require [pomodoro.ui-common :as ui]
            [pomodoro.time-format :as tf]
            [pomodoro.action :as action]
            [pomodoro.cookie-storage :as storage]))

(defn add-to-plan [task]
  (storage/set-plan (conj (or (storage/get-plan) []) task)))

(defn add-new-task-to-plan [state]
  (add-to-plan (action/new-plan state)))

(defn add-to-plan-on-enter [event state]
  (when (= 13 (.-charCode event)) (add-new-task-to-plan state)))

(defn get-task-in-milisec [task]
  (* 1000 (action/get-task-in-seconds task)))

(defn run-next-item [state]
  (let [plan (:remain-plan @state)]
    (action/add-to-history state)
    (if (empty? plan)
      (action/reset-task state)
      (action/run-plan state))))

(defn plan-runner [state]
  (when (storage/contains-plan?)
    (let [full-width (min 600 ((:width @state) 0.94))
          skip-visible (not-empty (:remain-plan @state))
          width (cond
                  (and (:active @state) skip-visible) (* full-width 0.33)
                  (:active @state) (* full-width 0.5)
                  :else full-width)]
      [:div
       [:div {:class "btn-group" :style {:margin-top "1%"}}
        (ui/hideable-button-element (@state :active) width  "Start batch" #(action/start-plan state))
        (ui/hideable-button-element (@state :paused) width "Pause timer" #(action/pause-button-on-click state))
        (ui/hideable-button-element (@state :resume) width "Resume timer" #(action/pause-button-on-click state))
        (when skip-visible (ui/hideable-button-element (@state :stop) width "Run next" #(run-next-item state)))
        (ui/hideable-button-element (@state :stop) width "Stop batch" #(action/stop-button-on-click state))]
       [:div {:style {:margin-top "1%"}}
        (ui/progress-bar state)]])))

(defn plan-table [state]
  (when-let [plan (storage/get-plan)]
   (let [full-width ((:width @state) 0.94)
         width (min 120 (* full-width 0.33))]
     [:div#plan
      [:table {
               :class "table table-striped table-bordered" :id "plan"
               :style {:width (min 600 full-width)}}
       [:thead {:class "thead-dark"}
        [:tr
         [:th "Task name"]
         [:th (str "Planned time: " (tf/render-time (* 1000 (reduce + (map long (map :length-in-seconds plan))))))]
         [:th (ui/button-element (@state :active) width  "Clear plan" #(storage/delete-plan))]]]
       (into [:tbody]
             (for [task plan]
               [:tr {:key (:key task)}
                [:td (:task-name task)]
                [:td (tf/render-time (get-task-in-milisec task))]
                [:td (ui/button-element (@state :active) width "Remove" #(storage/set-plan (remove (fn [t] (= t task)) plan)))]]))]])))

(defn short-break [state]
  (add-to-plan {:task-name         "Short break"
                :length-in-seconds 300
                :length            5
                :unit              :min
                :key               (str "plan_" ((@state :get-key)))}))

(defn long-break [state]
  (add-to-plan {:task-name         "Long break"
                :length-in-seconds 900
                :length            15
                :unit              :min
                :key               (str "plan_" ((@state :get-key)))}))

(defn planning [state]
  (let [width (min 200 ((:width @state) 0.315))]
    [:div#planning
     [:h3 "Planning a batch run"]
     (ui/text-input state :task-name #(add-to-plan-on-enter % state))
     (ui/input-length state :length #(add-to-plan-on-enter % state))
     [:div {:class "btn-group" :style {:margin-top "1%"}}
      (ui/button-element (@state :active) width  "Add task" #(add-new-task-to-plan state))
      (ui/button-element (@state :active) width "Short break" #(short-break state))
      (ui/button-element (@state :active) width "Long break" #(long-break state))]
     [:p]
     (plan-table state)
     (plan-runner state)]))