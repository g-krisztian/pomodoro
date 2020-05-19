(ns pomodoro.batch
  (:require [pomodoro.ui-common :as ui]
            [reagent.cookies :as rc]
            [pomodoro.time-format :as tf]
            [pomodoro.action :as action]))

(defn add-to-plan [task]
  (rc/set! :plan (conj (rc/get :plan []) task)))

(defn add-new-task-to-plan [state]
  (add-to-plan (action/new-task state)))

(defn add-to-plan-on-enter [event state]
  (when (= 13 (.-charCode event)) (add-new-task-to-plan state)))

(defn get-task-in-milisec [task]
  (* 1000 (action/get-task-in-seconds task)))

(defn run-next-item [state]
  (let [plan (:remain-plan @state)]
    (action/add-to-history state)
    (if (empty? plan)
      (action/reset-task state)
      (action/start-plan-on-click state plan))))

(defn plan-runner [state]
  (when (rc/contains-key? :plan)
    [:div
     [:div {:class "btn-group" :style {:margin-top "1%"}}
      (ui/hideable-button-element (@state :active) "Start batch" #(action/start-plan-on-click state (rc/get :plan [])))
      (ui/hideable-button-element (@state :paused) "Pause timer" #(action/pause-button-on-click state))
      (ui/hideable-button-element (@state :resume) "Resume timer" #(action/pause-button-on-click state))
      (when-not (empty? (:remain-plan @state)) (ui/hideable-button-element (@state :stop) "Run next" #(run-next-item state)))
      (ui/hideable-button-element (@state :stop) "Stop batch" #(action/stop-button-on-click state))]

     [:div {:style {:margin-top "1%"}}
      (ui/progress-bar @state)]]))

(defn plan-table [state]
  [:div#plan
   (when (rc/contains-key? :plan)
     [:table {:class "table table-striped table-bordered" :id "summary"}
      [:thead {:class "thead-dark"}
       [:tr
        [:th "Task name"]
        [:th (str "Planned time: " (tf/render-time (* 1000 (reduce + (map long (map :length-in-seconds (rc/get :plan)))))))]
        [:th (ui/button-element (@state :active) "Clear plan" #(rc/remove! :plan))]]]
      (into [:tbody]
            (for [task (rc/get :plan)]
              [:tr {:key (:key task)}
               [:td (:task-name task)]
               [:td (tf/render-time (get-task-in-milisec task))]
               [:td (ui/button-element (@state :active) "Remove" #(rc/set! :plan (remove (fn [t] (= t task)) (rc/get :plan))))]]))])])

(defn short-break [state]
  (add-to-plan {:task-name "Short break"
                :length-in-seconds 300
                :length 5
                :unit :min
                :key (str "plan_" ((@state :get-key)))}))

(defn long-break [state]
  (add-to-plan {:task-name "Long break"
                :length-in-seconds 900
                :length 15
                :unit :min
                :key (str "plan_" ((@state :get-key)))}))

(defn planning [state]
  [:div
   [:h3 "Planning a batch run"]
   (ui/text-input state :task-name #(add-to-plan-on-enter % state))
   (ui/input-length state :length #(add-to-plan-on-enter % state))
   [:div {:class "btn-group"}
    (ui/button-element (@state :active) "Add task" #(add-new-task-to-plan state))
    (ui/button-element (@state :active) "Add short break" #(short-break state))
    (ui/button-element (@state :active) "Add long break" #(long-break state))]
   [:p]
   (plan-table state)
   (plan-runner state)])